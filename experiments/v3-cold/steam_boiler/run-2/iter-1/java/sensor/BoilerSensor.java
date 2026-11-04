package steam_boiler.sensor;

import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.BoilerConstants;
import steam_boiler.controller.BoilerMode;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;

/**
 * The program's view of the physical units. Each cycle it receives the incoming
 * messages, analyses them (failure detection, repairs, acknowledgements, the STOP
 * count, the level and steam bounds) and answers the controller's queries. After the
 * outgoing messages are known it records them and predicts the next cycle.
 */
public final class BoilerSensor {
    private static final int PUMPS = BoilerConstants.PUMP_COUNT;
    private static final double DT = BoilerConstants.CYCLE_SECONDS;

    private final TransmissionBuffer buffer = new TransmissionBuffer();
    private final UnitRegistry registry = new UnitRegistry();

    @RoboChartType("nat") private int consecutiveStops = 0;
    private boolean boilerWaitingSeen = false;
    private boolean programReadySent = false;
    private boolean valveOpen = false;
    private boolean initializationOver = false;
    private boolean transmissionFailed = false;
    private boolean steamNonZero = false;

    @RoboChartType("real") private double levelValue = 0.0;
    @RoboChartType("real") private double steamValue = 0.0;
    @RoboChartType("real") private double pumpThroughput = 0.0;
    @RoboChartType("real") private double knownLow = 0.0;
    @RoboChartType("real") private double knownHigh = 0.0;
    @RoboChartType("real") private double worstLow = 0.0;
    @RoboChartType("real") private double worstHigh = 0.0;
    @RoboChartType("real") private double steamOutMin = 0.0;
    @RoboChartType("real") private double steamOutMax = 0.0;
    @RoboChartType("real") private double estimateLow = 0.0;
    @RoboChartType("real") private double estimateHigh = 0.0;
    private boolean estimateKnown = false;
    @RoboChartType("real") private double plausibleLow = 0.0;
    @RoboChartType("real") private double plausibleHigh = 0.0;
    private boolean plausibleKnown = false;
    private boolean plausibleLowKnown = false;
    @RoboChartType("real") private double previousSteam = 0.0;
    private boolean previousSteamValid = false;

    private final boolean[] hasOrder = new boolean[PUMPS];
    private final boolean[] orderedRunning = new boolean[PUMPS];
    private final int[] orderAge = new int[PUMPS];
    private final boolean[] closingNow = new boolean[PUMPS];
    private final boolean[] pumpBaseline = new boolean[PUMPS];
    private final boolean[] previousRunning = new boolean[PUMPS];
    private final boolean[] flowBaseline = new boolean[PUMPS];
    private final boolean[] previousFlowing = new boolean[PUMPS];
    private final boolean[] possiblePouring = new boolean[PUMPS];
    private final boolean[] confirmedPouring = new boolean[PUMPS];

    // ------------------------------------------------------------------ cycle stages

    /** Starts the reception stage of a cycle. */
    public void beginCycle() {
        buffer.clear();
        registry.startCycle();
    }

    /** Receives one message of the current transmission. */
    public void receive(InputEvent message) {
        buffer.receive(message);
    }

    /** Analyses the transmission received in this cycle. */
    public void analyse() {
        if (buffer.stopReceived()) {
            if (consecutiveStops < BoilerConstants.STOP_LIMIT) {
                consecutiveStops = consecutiveStops + 1;
            }
        } else {
            consecutiveStops = 0;
        }
        if (buffer.steamBoilerWaiting()) {
            boilerWaitingSeen = true;
        }
        transmissionFailed = false;
        steamNonZero = false;
        if (!boilerWaitingSeen) {
            return;
        }
        boolean unexpectedReady = buffer.physicalUnitsReady() && (!programReadySent || initializationOver);
        boolean unexpectedWaiting = buffer.steamBoilerWaiting() && initializationOver;
        transmissionFailed = !buffer.dataComplete() || buffer.aberrant() || unexpectedReady || unexpectedWaiting;

        applyAcknowledgements();
        applyRepairs();
        if (buffer.levelPresent()) {
            levelValue = buffer.levelValue();
        }
        if (buffer.steamPresent()) {
            steamValue = buffer.steamValue();
        }
        steamNonZero = buffer.steamPresent() && steamValue != 0.0;
        for (int i = 0; i < PUMPS; i++) {
            if (hasOrder[i] && orderAge[i] < 3) {
                orderAge[i] = orderAge[i] + 1;
            }
        }
        checkSteam();
        checkLevel();
        checkPumps();
        checkPumpControllers();
        computeThroughput();
        computeKnownLevel();
        computeWorstCase();
    }

    /** The failure detections and repair acknowledgements to transmit this cycle. */
    public List<OutputEvent> unitMessages() {
        return registry.announcements();
    }

    /** Records the messages transmitted this cycle and predicts the next cycle. */
    public void completeCycle(List<OutputEvent> transmitted) {
        for (int i = 0; i < PUMPS; i++) {
            closingNow[i] = false;
        }
        for (OutputEvent message : transmitted) {
            recordTransmitted(message);
        }
        if (!boilerWaitingSeen) {
            return;
        }
        predictNextCycle();
        for (int i = 0; i < PUMPS; i++) {
            if (buffer.pumpStatePresent(i)) {
                previousRunning[i] = buffer.pumpRunning(i);
                pumpBaseline[i] = true;
            }
            if (buffer.flowPresent(i)) {
                previousFlowing[i] = buffer.flowing(i);
                flowBaseline[i] = true;
            }
        }
        previousSteamValid = steamUsable();
        previousSteam = steamValue;
    }

    // ------------------------------------------------------------ controller queries

    /** Number of successive transmissions that carried STOP (at most STOP_LIMIT). */
    @RoboChartType("nat") public int consecutiveStops() { return consecutiveStops; }

    /** A message indispensable to this transmission is missing, or one is aberrant. */
    public boolean transmissionFailure() { return transmissionFailed; }

    /** STEAM_BOILER_WAITING has arrived (in this or an earlier cycle). */
    public boolean boilerWaitingReceived() { return boilerWaitingSeen; }

    /** PHYSICAL_UNITS_READY arrived in this transmission. */
    public boolean physicalUnitsReadyReceived() { return buffer.physicalUnitsReady(); }

    /** PROGRAM_READY has been transmitted in an earlier cycle. */
    public boolean programReadySent() { return programReadySent; }

    /** The evacuation valve has been requested open and not yet closed. */
    public boolean valveOpen() { return valveOpen; }

    /** The steam rate reported this cycle is not zero. */
    public boolean steamNonZero() { return steamNonZero; }

    /** The level measuring device is out of order. */
    public boolean levelDeviceFailed() { return registry.isFailed(UnitRegistry.LEVEL_UNIT); }

    /** The steam measuring device is out of order. */
    public boolean steamDeviceFailed() { return registry.isFailed(UnitRegistry.STEAM_UNIT); }

    /** At least one pump is out of order. */
    public boolean anyPumpFailed() { return registry.anyPumpFailed(); }

    /** At least one pump controller is out of order. */
    public boolean anyPumpControllerFailed() { return registry.anyControllerFailed(); }

    /** Lower bound of the current water level: the reading, or the estimate when it is unusable. */
    @RoboChartType("real") public double knownLevelLow() { return knownLow; }

    /** Upper bound of the current water level: the reading, or the estimate when it is unusable. */
    @RoboChartType("real") public double knownLevelHigh() { return knownHigh; }

    /** Lowest level reachable by the end of the next cycle under worst-case behaviour. */
    @RoboChartType("real") public double worstCaseLevelLow() { return worstLow; }

    /** Highest level reachable by the end of the next cycle under worst-case behaviour. */
    @RoboChartType("real") public double worstCaseLevelHigh() { return worstHigh; }

    /** q: the water level most recently reported by the level device (litre). */
    @RoboChartType("real") public double levelReading() { return levelValue; }

    /** v: the steam rate most recently reported by the steam device (litre/sec). */
    @RoboChartType("real") public double steamReading() { return steamValue; }

    /** p: the throughput of the pumps taken together (litre/sec). */
    @RoboChartType("real") public double pumpThroughput() { return pumpThroughput; }

    /** Lower bound of the level estimate advanced from the previous cycle (litre). */
    @RoboChartType("real") public double levelEstimateLow() {
        if (estimateKnown) {
            return estimateLow;
        }
        return knownLow;
    }

    /** Upper bound of the level estimate advanced from the previous cycle (litre). */
    @RoboChartType("real") public double levelEstimateHigh() {
        if (estimateKnown) {
            return estimateHigh;
        }
        return knownHigh;
    }

    // ------------------------------------------------------------------- analysis

    private void applyAcknowledgements() {
        for (int i = 0; i < PUMPS; i++) {
            if (buffer.pumpFailureAcknowledged(i)) {
                registry.acknowledgeFailure(UnitRegistry.pumpUnit(i));
            }
            if (buffer.pumpControlFailureAcknowledged(i)) {
                registry.acknowledgeFailure(UnitRegistry.controllerUnit(i));
            }
        }
        if (buffer.levelFailureAcknowledged()) {
            registry.acknowledgeFailure(UnitRegistry.LEVEL_UNIT);
        }
        if (buffer.steamFailureAcknowledged()) {
            registry.acknowledgeFailure(UnitRegistry.STEAM_UNIT);
        }
    }

    private void applyRepairs() {
        for (int i = 0; i < PUMPS; i++) {
            if (buffer.pumpRepaired(i)) {
                registry.repair(UnitRegistry.pumpUnit(i));
                hasOrder[i] = false;
                pumpBaseline[i] = false;
            }
            if (buffer.pumpControlRepaired(i)) {
                registry.repair(UnitRegistry.controllerUnit(i));
                flowBaseline[i] = false;
            }
        }
        if (buffer.levelRepaired()) {
            registry.repair(UnitRegistry.LEVEL_UNIT);
        }
        if (buffer.steamRepaired()) {
            registry.repair(UnitRegistry.STEAM_UNIT);
            previousSteamValid = false;
        }
    }

    private void checkSteam() {
        int unit = UnitRegistry.STEAM_UNIT;
        if (!buffer.steamPresent() || registry.isFailed(unit) || registry.repairedThisCycle(unit)) {
            return;
        }
        boolean invalid = steamValue < 0.0 || steamValue > BoilerConstants.STEAM_MAX_W;
        if (previousSteamValid) {
            boolean riseTooFast = steamValue - previousSteam > BoilerConstants.STEAM_INCREASE_U1 * DT;
            boolean fallTooFast = previousSteam - steamValue > BoilerConstants.STEAM_DECREASE_U2 * DT;
            invalid = invalid || riseTooFast || fallTooFast;
        }
        if (invalid) {
            registry.detectFailure(unit);
        }
    }

    private void checkLevel() {
        int unit = UnitRegistry.LEVEL_UNIT;
        if (!buffer.levelPresent() || registry.isFailed(unit) || registry.repairedThisCycle(unit)) {
            return;
        }
        boolean invalid = levelValue < 0.0 || levelValue > BoilerConstants.CAPACITY_C;
        if (plausibleKnown) {
            boolean tooHigh = levelValue > plausibleHigh;
            boolean tooLow = plausibleLowKnown && levelValue < plausibleLow;
            invalid = invalid || tooHigh || tooLow;
        }
        if (invalid) {
            registry.detectFailure(unit);
        }
    }

    private void checkPumps() {
        for (int i = 0; i < PUMPS; i++) {
            int unit = UnitRegistry.pumpUnit(i);
            if (!buffer.pumpStatePresent(i) || registry.isFailed(unit) || registry.repairedThisCycle(unit)) {
                continue;
            }
            boolean running = buffer.pumpRunning(i);
            if (hasOrder[i]) {
                if (running != orderedRunning[i]) {
                    registry.detectFailure(unit);
                }
            } else if (pumpBaseline[i]) {
                if (running != previousRunning[i]) {
                    registry.detectFailure(unit);
                }
            }
        }
    }

    private void checkPumpControllers() {
        for (int i = 0; i < PUMPS; i++) {
            int unit = UnitRegistry.controllerUnit(i);
            if (!buffer.flowPresent(i) || registry.isFailed(unit) || registry.repairedThisCycle(unit)) {
                continue;
            }
            if (registry.isFailed(UnitRegistry.pumpUnit(i))) {
                continue;
            }
            boolean flowing = buffer.flowing(i);
            if (hasOrder[i] && orderAge[i] == 1) {
                continue;
            }
            if (hasOrder[i] && orderAge[i] == 2) {
                if (flowing != orderedRunning[i]) {
                    registry.detectFailure(unit);
                }
            } else if (flowBaseline[i]) {
                if (flowing != previousFlowing[i]) {
                    registry.detectFailure(unit);
                }
            }
        }
    }

    private void computeThroughput() {
        int delivering = 0;
        for (int i = 0; i < PUMPS; i++) {
            boolean pumpSound = !registry.isFailed(UnitRegistry.pumpUnit(i));
            boolean controllerSound = !registry.isFailed(UnitRegistry.controllerUnit(i));
            boolean running = buffer.pumpStatePresent(i) && buffer.pumpRunning(i);
            boolean flowing = buffer.flowPresent(i) && buffer.flowing(i);
            confirmedPouring[i] = controllerSound && flowing;
            possiblePouring[i] = running || flowing || !pumpSound || !controllerSound;
            if (confirmedPouring[i] || (!controllerSound && pumpSound && running)) {
                delivering = delivering + 1;
            }
        }
        pumpThroughput = BoilerConstants.PUMP_CAPACITY_P * delivering;
    }

    private boolean levelUsable() {
        return buffer.levelPresent() && !registry.isFailed(UnitRegistry.LEVEL_UNIT);
    }

    private boolean steamUsable() {
        return buffer.steamPresent() && !registry.isFailed(UnitRegistry.STEAM_UNIT);
    }

    private void computeKnownLevel() {
        if (levelUsable()) {
            knownLow = levelValue;
            knownHigh = levelValue;
        } else if (estimateKnown) {
            knownLow = estimateLow;
            knownHigh = estimateHigh;
        } else {
            knownLow = 0.0;
            knownHigh = BoilerConstants.CAPACITY_C;
        }
    }

    /** Worst case over the next cycle; pumps now off pour nothing within it (latency). */
    private void computeWorstCase() {
        double steamLow = 0.0;
        double steamHigh = BoilerConstants.STEAM_MAX_W;
        if (steamUsable()) {
            steamLow = steamValue;
            steamHigh = steamValue;
        }
        steamOutMin = steamLow * DT - 0.5 * BoilerConstants.STEAM_DECREASE_U2 * DT * DT;
        if (steamOutMin < 0.0) {
            steamOutMin = 0.0;
        }
        steamOutMax = steamHigh * DT + 0.5 * BoilerConstants.STEAM_INCREASE_U1 * DT * DT;
        if (steamOutMax > BoilerConstants.STEAM_MAX_W * DT) {
            steamOutMax = BoilerConstants.STEAM_MAX_W * DT;
        }
        int possible = 0;
        for (int i = 0; i < PUMPS; i++) {
            if (possiblePouring[i]) {
                possible = possible + 1;
            }
        }
        worstLow = knownLow - steamOutMax;
        worstHigh = knownHigh + BoilerConstants.PUMP_CAPACITY_P * DT * possible - steamOutMin;
    }

    // ----------------------------------------------------------------- prediction

    private void recordTransmitted(OutputEvent message) {
        if (message instanceof OutputEvent.Mode) {
            OutputEvent.Mode mode = (OutputEvent.Mode) message;
            if (mode.value() != BoilerMode.INITIALIZATION) {
                initializationOver = true;
            }
        } else if (message instanceof OutputEvent.ProgramReady) {
            programReadySent = true;
        } else if (message instanceof OutputEvent.Valve) {
            valveOpen = !valveOpen;
        } else if (message instanceof OutputEvent.OpenPump) {
            OutputEvent.OpenPump open = (OutputEvent.OpenPump) message;
            recordOrder(open.value() - 1, true);
        } else if (message instanceof OutputEvent.ClosePump) {
            OutputEvent.ClosePump close = (OutputEvent.ClosePump) message;
            recordOrder(close.value() - 1, false);
            if (close.value() >= 1 && close.value() <= PUMPS) {
                closingNow[close.value() - 1] = true;
            }
        }
    }

    /** An order counts as a new order only when it changes the ordered state. */
    private void recordOrder(int i, boolean running) {
        if (i < 0 || i >= PUMPS) {
            return;
        }
        if (!hasOrder[i] || orderedRunning[i] != running) {
            hasOrder[i] = true;
            orderedRunning[i] = running;
            orderAge[i] = 0;
        }
    }

    /**
     * Advances the level bounds to the next cycle. The estimate uses the water known to
     * be supplied (closed pumps stop at once; pumps not yet pouring stay dry for one
     * cycle); the plausibility range used to judge the next level reading assumes the
     * extreme behaviour of steam and of every pump that could be pouring.
     */
    private void predictNextCycle() {
        int confirmed = 0;
        int possible = 0;
        for (int i = 0; i < PUMPS; i++) {
            if (confirmedPouring[i] && !closingNow[i]) {
                confirmed = confirmed + 1;
            }
            if (possiblePouring[i] && !closingNow[i]) {
                possible = possible + 1;
            }
        }
        double perPump = BoilerConstants.PUMP_CAPACITY_P * DT;
        estimateLow = knownLow + perPump * confirmed - steamOutMax;
        estimateHigh = knownHigh + perPump * possible - steamOutMin;
        if (valveOpen || estimateLow < 0.0) {
            estimateLow = 0.0;
        }
        if (estimateHigh > BoilerConstants.CAPACITY_C) {
            estimateHigh = BoilerConstants.CAPACITY_C;
        }
        if (estimateHigh < estimateLow) {
            estimateHigh = estimateLow;
        }
        estimateKnown = true;
        plausibleLow = worstLow;
        plausibleHigh = worstHigh;
        plausibleLowKnown = !valveOpen;
        plausibleKnown = true;
    }
}
