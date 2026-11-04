package steam_boiler.sensor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.SteamBoilerConstants;
import steam_boiler.event.OutputEvent;

/**
 * The program's knowledge of the plant, derived once per cycle from the incoming
 * transmission: the current measures q, v and p, the lower and upper bound on the
 * water level, the status of every physical unit (sound or out of order, failure
 * announcement acknowledged or not), failure detection, and the count of
 * successive transmissions carrying STOP.
 *
 * <p>{@link #analyse()} runs before the controller's step in every cycle;
 * {@link #endCycle(List)} runs after the outgoing transmission is complete.
 */
public final class BoilerSensor {

    private static final int PUMPS = SteamBoilerConstants.PUMP_COUNT;
    @RoboChartType("real") private static final double CYCLE = SteamBoilerConstants.CYCLE_SECONDS;
    @RoboChartType("real") private static final double PUMP_VOLUME_PER_CYCLE =
            SteamBoilerConstants.PUMP_CAPACITY_P * SteamBoilerConstants.CYCLE_SECONDS;

    private final IncomingTransmission transmission;

    // q: water level most recently reported by the level measuring device (litre).
    @RoboChartType("real") private double waterLevel = 0.0;
    // v: steam rate most recently reported by the steam measuring device (litre/sec).
    @RoboChartType("real") private double steamRate = 0.0;
    // p: current throughput of the pumps taken together (litre/sec).
    @RoboChartType("real") private double pumpThroughput = 0.0;
    // Lower and upper bound on the true water level; equal to q while the level device is sound.
    @RoboChartType("real") private double levelLowerBound = 0.0;
    @RoboChartType("real") private double levelUpperBound = 0.0;

    // Unit status: out of order, failure announcement not yet acknowledged, repair to acknowledge.
    private final boolean[] pumpOutOfOrder = new boolean[PUMPS];
    private final boolean[] pumpControlOutOfOrder = new boolean[PUMPS];
    private boolean levelOutOfOrder = false;
    private boolean steamOutOfOrder = false;
    private final boolean[] pumpFailureUnacknowledged = new boolean[PUMPS];
    private final boolean[] pumpControlFailureUnacknowledged = new boolean[PUMPS];
    private boolean levelFailureUnacknowledged = false;
    private boolean steamFailureUnacknowledged = false;
    private final boolean[] pumpRepairToAcknowledge = new boolean[PUMPS];
    private final boolean[] pumpControlRepairToAcknowledge = new boolean[PUMPS];
    private boolean levelRepairToAcknowledge = false;
    private boolean steamRepairToAcknowledge = false;

    // Number of successive transmissions that carried STOP.
    @RoboChartType("nat") private int consecutiveStops = 0;

    private boolean transmissionFailed = false;

    // Failure detection starts once STEAM_BOILER_WAITING has arrived.
    private boolean monitoring = false;
    // The previous cycle's readings are available for the dynamic checks.
    private boolean historyKnown = false;
    private final boolean[] previousPumpOn = new boolean[PUMPS];
    private final boolean[] previousFlow = new boolean[PUMPS];
    @RoboChartType("real") private double previousSteam = 0.0;
    private boolean previousSteamTrusted = false;
    // Orders sent in the previous cycle, and the evacuation valve as last requested.
    private final boolean[] orderedOn = new boolean[PUMPS];
    private final boolean[] orderedOff = new boolean[PUMPS];
    private boolean valveOpen = false;

    /** Creates the sensor service over the transmission system's incoming messages. */
    public BoilerSensor(IncomingTransmission transmission) {
        this.transmission = transmission;
    }

    /**
     * Analyses the current transmission: counts STOP, checks the transmission,
     * handles acknowledgements and repairs, detects unit failures and advances the
     * water-level bounds.
     */
    public void analyse() {
        countStopMessages();
        clearRepairAcknowledgements();
        transmissionFailed = !transmission.dataMessagesComplete()
                || transmission.malformedMessagePresent()
                || unexpectedUnitMessagePresent();
        if (transmissionFailed) {
            return;
        }
        acceptFailureAcknowledgements();
        acceptRepairs();
        waterLevel = transmission.levelReading();
        steamRate = transmission.steamReading();
        if (transmission.steamBoilerWaitingPresent()) {
            monitoring = true;
        }
        if (monitoring) {
            detectSteamFailure();
            detectPumpFailures();
            detectPumpControlFailures();
            updateLevelKnowledge();
        } else {
            levelLowerBound = waterLevel;
            levelUpperBound = waterLevel;
        }
        pumpThroughput = currentThroughput();
        rememberCycle();
    }

    /**
     * Records the pump orders and valve requests of the outgoing transmission, so
     * that the next cycle can judge the units' responses.
     */
    public void endCycle(List<OutputEvent> sent) {
        for (int i = 0; i < PUMPS; i++) {
            orderedOn[i] = false;
            orderedOff[i] = false;
        }
        for (OutputEvent message : sent) {
            if (message instanceof OutputEvent.OpenPump) {
                OutputEvent.OpenPump order = (OutputEvent.OpenPump) message;
                markOrder(orderedOn, order.pump());
            } else if (message instanceof OutputEvent.ClosePump) {
                OutputEvent.ClosePump order = (OutputEvent.ClosePump) message;
                markOrder(orderedOff, order.pump());
            } else if (message instanceof OutputEvent.Valve) {
                valveOpen = !valveOpen;
            }
        }
    }

    /**
     * The failure announcements still awaiting acknowledgement and the repair
     * acknowledgements due in the current cycle.
     */
    public List<OutputEvent> protocolMessages() {
        List<OutputEvent> messages = new ArrayList<>();
        for (int i = 0; i < PUMPS; i++) {
            if (pumpFailureUnacknowledged[i]) {
                messages.add(new OutputEvent.PumpFailureDetection(i + 1));
            }
            if (pumpControlFailureUnacknowledged[i]) {
                messages.add(new OutputEvent.PumpControlFailureDetection(i + 1));
            }
            if (pumpRepairToAcknowledge[i]) {
                messages.add(new OutputEvent.PumpRepairedAcknowledgement(i + 1));
            }
            if (pumpControlRepairToAcknowledge[i]) {
                messages.add(new OutputEvent.PumpControlRepairedAcknowledgement(i + 1));
            }
        }
        if (levelFailureUnacknowledged) {
            messages.add(new OutputEvent.LevelFailureDetection());
        }
        if (steamFailureUnacknowledged) {
            messages.add(new OutputEvent.SteamFailureDetection());
        }
        if (levelRepairToAcknowledge) {
            messages.add(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (steamRepairToAcknowledge) {
            messages.add(new OutputEvent.SteamRepairedAcknowledgement());
        }
        return messages;
    }

    /** STEAM_BOILER_WAITING is present in the current transmission. */
    public boolean steamBoilerWaitingPresent() {
        return transmission.steamBoilerWaitingPresent();
    }

    /** PHYSICAL_UNITS_READY is present in the current transmission. */
    public boolean physicalUnitsReadyPresent() {
        return transmission.physicalUnitsReadyPresent();
    }

    /** q: the water level most recently reported (litre). */
    @RoboChartType("real")
    public double levelReading() {
        return waterLevel;
    }

    /** v: the steam rate most recently reported (litre/sec). */
    @RoboChartType("real")
    public double steamReading() {
        return steamRate;
    }

    /** p: the current throughput of the pumps taken together (litre/sec). */
    @RoboChartType("real")
    public double pumpThroughput() {
        return pumpThroughput;
    }

    /** Number of successive transmissions, up to the current one, that carried STOP. */
    @RoboChartType("nat")
    public int consecutiveStopCount() {
        return consecutiveStops;
    }

    /** An indispensable message is missing or an aberrant message is present. */
    public boolean transmissionFailure() {
        return transmissionFailed;
    }

    /** The water-level measuring device is out of order. */
    public boolean levelDeviceOutOfOrder() {
        return levelOutOfOrder;
    }

    /** The steam measuring device is out of order. */
    public boolean steamDeviceOutOfOrder() {
        return steamOutOfOrder;
    }

    /** At least one pump controller is out of order. */
    public boolean pumpControllerOutOfOrder() {
        boolean failed = false;
        for (int i = 0; i < PUMPS; i++) {
            failed = failed || pumpControlOutOfOrder[i];
        }
        return failed;
    }

    /** At least one unit other than the water-level measuring device is out of order. */
    public boolean nonLevelUnitOutOfOrder() {
        boolean failed = steamOutOfOrder;
        for (int i = 0; i < PUMPS; i++) {
            failed = failed || pumpOutOfOrder[i] || pumpControlOutOfOrder[i];
        }
        return failed;
    }

    /**
     * The program's value for the current water level: q while the level device
     * is sound, otherwise the middle of the estimated bounds (litre).
     */
    @RoboChartType("real")
    public double levelEstimate() {
        return (levelLowerBound + levelUpperBound) / 2.0;
    }

    /**
     * Lowest level reachable by the end of the next cycle if the pumps are
     * closed now: maximal steam increase and no water from the pumps (litre).
     */
    @RoboChartType("real")
    public double worstCaseLevelLowPumpsClosed() {
        return levelLowerBound - steamOutflowMax(steamRate, !steamOutOfOrder);
    }

    /**
     * Lowest level reachable by the end of the next cycle if the pumps are left
     * on: maximal steam increase, and water only from the sound pumps that will
     * surely pour for the whole cycle; a pump ordered on now pours nothing during
     * its five seconds of pressure balancing (litre).
     */
    @RoboChartType("real")
    public double worstCaseLevelLowPumpsKept() {
        return worstCaseLevelLowPumpsClosed() + pumpsSurelyPouring() * PUMP_VOLUME_PER_CYCLE;
    }

    /**
     * Highest level reachable by the end of the next cycle if the pumps are
     * closed now, closing taking effect at once (litre).
     */
    @RoboChartType("real")
    public double worstCaseLevelHighPumpsClosed() {
        return levelUpperBound - steamOutflowMin(steamRate, !steamOutOfOrder);
    }

    /**
     * Highest level reachable by the end of the next cycle if every pump now on
     * keeps pouring at full capacity (litre).
     */
    @RoboChartType("real")
    public double worstCaseLevelHighPumpsKept() {
        return worstCaseLevelHighPumpsClosed() + pumpsPossiblyPouring() * PUMP_VOLUME_PER_CYCLE;
    }

    private void countStopMessages() {
        if (transmission.stopPresent()) {
            consecutiveStops = consecutiveStops + 1;
        } else {
            consecutiveStops = 0;
        }
    }

    private void clearRepairAcknowledgements() {
        for (int i = 0; i < PUMPS; i++) {
            pumpRepairToAcknowledge[i] = false;
            pumpControlRepairToAcknowledge[i] = false;
        }
        levelRepairToAcknowledge = false;
        steamRepairToAcknowledge = false;
    }

    // A repair message for a sound unit, or an acknowledgement of an announcement not pending.
    private boolean unexpectedUnitMessagePresent() {
        boolean unexpected = (transmission.levelRepairedPresent() && !levelOutOfOrder)
                || (transmission.steamRepairedPresent() && !steamOutOfOrder)
                || (transmission.levelFailureAcknowledgedPresent() && !levelFailureUnacknowledged)
                || (transmission.steamFailureAcknowledgedPresent() && !steamFailureUnacknowledged);
        for (int i = 0; i < PUMPS; i++) {
            unexpected = unexpected
                    || (transmission.pumpRepairedPresent(i) && !pumpOutOfOrder[i])
                    || (transmission.pumpControlRepairedPresent(i) && !pumpControlOutOfOrder[i])
                    || (transmission.pumpFailureAcknowledgedPresent(i) && !pumpFailureUnacknowledged[i])
                    || (transmission.pumpControlFailureAcknowledgedPresent(i)
                            && !pumpControlFailureUnacknowledged[i]);
        }
        return unexpected;
    }

    private void acceptFailureAcknowledgements() {
        for (int i = 0; i < PUMPS; i++) {
            if (transmission.pumpFailureAcknowledgedPresent(i)) {
                pumpFailureUnacknowledged[i] = false;
            }
            if (transmission.pumpControlFailureAcknowledgedPresent(i)) {
                pumpControlFailureUnacknowledged[i] = false;
            }
        }
        if (transmission.levelFailureAcknowledgedPresent()) {
            levelFailureUnacknowledged = false;
        }
        if (transmission.steamFailureAcknowledgedPresent()) {
            steamFailureUnacknowledged = false;
        }
    }

    private void acceptRepairs() {
        for (int i = 0; i < PUMPS; i++) {
            if (transmission.pumpRepairedPresent(i)) {
                pumpOutOfOrder[i] = false;
                pumpFailureUnacknowledged[i] = false;
                pumpRepairToAcknowledge[i] = true;
            }
            if (transmission.pumpControlRepairedPresent(i)) {
                pumpControlOutOfOrder[i] = false;
                pumpControlFailureUnacknowledged[i] = false;
                pumpControlRepairToAcknowledge[i] = true;
            }
        }
        if (transmission.levelRepairedPresent()) {
            levelOutOfOrder = false;
            levelFailureUnacknowledged = false;
            levelRepairToAcknowledge = true;
        }
        if (transmission.steamRepairedPresent()) {
            steamOutOfOrder = false;
            steamFailureUnacknowledged = false;
            steamRepairToAcknowledge = true;
        }
    }

    // Steam device: reading outside [0, W], or a change faster than U1 (up) or U2 (down) allow.
    private void detectSteamFailure() {
        if (steamOutOfOrder) {
            return;
        }
        boolean outOfRange = steamRate < 0.0 || steamRate > SteamBoilerConstants.STEAM_MAX_W;
        boolean dynamicsKnown = historyKnown && previousSteamTrusted && !steamRepairToAcknowledge;
        boolean risesTooFast = dynamicsKnown
                && steamRate > previousSteam + SteamBoilerConstants.STEAM_INCREASE_U1 * CYCLE;
        boolean fallsTooFast = dynamicsKnown
                && steamRate < previousSteam - SteamBoilerConstants.STEAM_DECREASE_U2 * CYCLE;
        if (outOfRange || risesTooFast || fallsTooFast) {
            steamOutOfOrder = true;
            steamFailureUnacknowledged = true;
        }
    }

    // Pump: in the transmission after an order it does not show the ordered state,
    // or it changes state without an order.
    private void detectPumpFailures() {
        for (int i = 0; i < PUMPS; i++) {
            boolean judged = historyKnown && !pumpOutOfOrder[i] && !pumpRepairToAcknowledge[i];
            boolean expectedOn = previousPumpOn[i];
            if (orderedOn[i]) {
                expectedOn = true;
            }
            if (orderedOff[i]) {
                expectedOn = false;
            }
            if (judged && transmission.pumpOn(i) != expectedOn) {
                pumpOutOfOrder[i] = true;
                pumpFailureUnacknowledged[i] = true;
            }
        }
    }

    // Pump controller of a sound pump: once the pump has held its state over two
    // transmissions (the second transmission after an order), the controller must
    // report flow exactly when the pump is on; a spontaneous change breaks this too.
    private void detectPumpControlFailures() {
        for (int i = 0; i < PUMPS; i++) {
            boolean judged = historyKnown && !pumpControlOutOfOrder[i] && !pumpOutOfOrder[i]
                    && !pumpControlRepairToAcknowledge[i] && !pumpRepairToAcknowledge[i];
            boolean pumpSteady = transmission.pumpOn(i) == previousPumpOn[i];
            if (judged && pumpSteady && transmission.waterFlowing(i) != transmission.pumpOn(i)) {
                pumpControlOutOfOrder[i] = true;
                pumpControlFailureUnacknowledged[i] = true;
            }
        }
    }

    // Level device: reading outside [0, C], or outside the interval the level could
    // reach from the previous cycle under extreme steam and pump behaviour. While the
    // device is out of order the bounds are advanced from that interval instead.
    private void updateLevelKnowledge() {
        double predictedLow = 0.0;
        double predictedHigh = SteamBoilerConstants.CAPACITY_C;
        if (historyKnown) {
            predictedHigh = Math.min(SteamBoilerConstants.CAPACITY_C,
                    levelUpperBound - steamOutflowMin(previousSteam, previousSteamTrusted) + pumpInflowMax());
            if (!valveOpen) {
                predictedLow = Math.max(0.0,
                        levelLowerBound - steamOutflowMax(previousSteam, previousSteamTrusted) + pumpInflowMin());
            }
        }
        boolean outOfRange = waterLevel < 0.0 || waterLevel > SteamBoilerConstants.CAPACITY_C;
        boolean tooHigh = historyKnown && waterLevel > predictedHigh;
        boolean tooLow = historyKnown && !valveOpen && waterLevel < predictedLow;
        if (!levelOutOfOrder && (outOfRange || tooHigh || tooLow)) {
            levelOutOfOrder = true;
            levelFailureUnacknowledged = true;
        }
        if (levelOutOfOrder) {
            levelLowerBound = predictedLow;
            levelUpperBound = predictedHigh;
        } else {
            levelLowerBound = waterLevel;
            levelUpperBound = waterLevel;
        }
    }

    // Most steam that can leave in one cycle starting from the given rate.
    @RoboChartType("real")
    private static double steamOutflowMax(@RoboChartType("real") double rate, boolean trusted) {
        double fullRate = SteamBoilerConstants.STEAM_MAX_W * CYCLE;
        if (!trusted) {
            return fullRate;
        }
        return Math.min(fullRate,
                rate * CYCLE + SteamBoilerConstants.STEAM_INCREASE_U1 * CYCLE * CYCLE / 2.0);
    }

    // Least steam that can leave in one cycle starting from the given rate.
    @RoboChartType("real")
    private static double steamOutflowMin(@RoboChartType("real") double rate, boolean trusted) {
        if (!trusted) {
            return 0.0;
        }
        return Math.max(0.0,
                rate * CYCLE - SteamBoilerConstants.STEAM_DECREASE_U2 * CYCLE * CYCLE / 2.0);
    }

    // A pump poured nothing during the cycle just ended: it was closed at its start
    // (closing acts at once) or switched on at its start (five seconds of balancing).
    private boolean pouredNothing(@RoboChartType("nat") int index) {
        boolean stoppedAtStart = orderedOff[index] && !transmission.pumpOn(index);
        boolean startedAtStart = orderedOn[index] && !previousPumpOn[index];
        return stoppedAtStart || startedAtStart;
    }

    // Water certainly supplied during the cycle just ended: pumps whose (sound)
    // controller reported flow at both of its ends.
    @RoboChartType("real")
    private double pumpInflowMin() {
        double volume = 0.0;
        for (int i = 0; i < PUMPS; i++) {
            boolean flowedThroughout = previousFlow[i] && transmission.waterFlowing(i);
            if (!pouredNothing(i) && !pumpControlOutOfOrder[i] && flowedThroughout) {
                volume = volume + PUMP_VOLUME_PER_CYCLE;
            }
        }
        return volume;
    }

    // Water possibly supplied during the cycle just ended.
    @RoboChartType("real")
    private double pumpInflowMax() {
        double volume = 0.0;
        for (int i = 0; i < PUMPS; i++) {
            boolean flowSeen = previousFlow[i] || transmission.waterFlowing(i);
            boolean pumpSeenOn = previousPumpOn[i] || transmission.pumpOn(i);
            boolean controllerSound = !pumpControlOutOfOrder[i];
            boolean mayHavePoured = (controllerSound && flowSeen) || (!controllerSound && pumpSeenOn);
            if (!pouredNothing(i) && mayHavePoured) {
                volume = volume + PUMP_VOLUME_PER_CYCLE;
            }
        }
        return volume;
    }

    // Pumps that will surely pour during the next cycle: sound, with a sound controller,
    // on now, and either seen flowing or at the end of the pressure balancing that
    // followed the previous cycle's order.
    @RoboChartType("nat")
    private int pumpsSurelyPouring() {
        int count = 0;
        for (int i = 0; i < PUMPS; i++) {
            boolean unitsSound = !pumpOutOfOrder[i] && !pumpControlOutOfOrder[i];
            boolean started = transmission.waterFlowing(i) || orderedOn[i];
            if (unitsSound && transmission.pumpOn(i) && started) {
                count = count + 1;
            }
        }
        return count;
    }

    // Pumps that may pour during the next cycle: on now, or reported flowing.
    @RoboChartType("nat")
    private int pumpsPossiblyPouring() {
        int count = 0;
        for (int i = 0; i < PUMPS; i++) {
            if (transmission.pumpOn(i) || transmission.waterFlowing(i)) {
                count = count + 1;
            }
        }
        return count;
    }

    // p: pumps that are on and whose controller sees water flowing (or, with the
    // controller out of order, pumps that are on), each at capacity P.
    @RoboChartType("real")
    private double currentThroughput() {
        double throughput = 0.0;
        for (int i = 0; i < PUMPS; i++) {
            boolean pouring = transmission.pumpOn(i)
                    && (transmission.waterFlowing(i) || pumpControlOutOfOrder[i]);
            if (pouring) {
                throughput = throughput + SteamBoilerConstants.PUMP_CAPACITY_P;
            }
        }
        return throughput;
    }

    private void rememberCycle() {
        for (int i = 0; i < PUMPS; i++) {
            previousPumpOn[i] = transmission.pumpOn(i);
            previousFlow[i] = transmission.waterFlowing(i);
        }
        previousSteam = steamRate;
        previousSteamTrusted = !steamOutOfOrder;
        historyKnown = monitoring;
    }

    private static void markOrder(boolean[] orders, @RoboChartType("nat") int pump) {
        if (pump >= 1 && pump <= PUMPS) {
            orders[pump - 1] = true;
        }
    }
}
