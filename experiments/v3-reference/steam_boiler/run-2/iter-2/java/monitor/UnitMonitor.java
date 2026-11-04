package steam_boiler.monitor;

import java.util.List;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.SteamBoilerConstants;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.ModeValue;
import steam_boiler.mode.WaterFlow;
import steam_boiler.sensor.BoilerSensor;

/**
 * Receives the incoming transmission of each cycle, keeps the program's
 * knowledge about the physical units (readings, unit status, STOP count, level
 * estimate), detects failures, runs the failure/repair message protocol, and
 * publishes the per-cycle quantities the controller reads from
 * {@link BoilerSensor}.
 *
 * <p>Use per cycle: the receive methods for every incoming message, then
 * {@link #analyse()}, then the controller step, then
 * {@link #sendProtocolMessages(BoilerActuator)} and {@link #endCycle(List)}.
 */
public final class UnitMonitor {

    /** Transmissions after an order from which a report must no longer change. */
    @RoboChartType("nat") private static final int SETTLED = 3;

    /** Number of successive STOP transmissions that demands emergency stop. */
    @RoboChartType("nat") private static final int STOPS_FOR_EMERGENCY = 3;

    @RoboChartType("real") private static final double T = SteamBoilerConstants.CYCLE_SECONDS;

    private final BoilerSensor sensor;

    // Messages of the transmission being received.
    private boolean stopReceived;
    private boolean waitingReceived;
    private boolean unitsReadyReceived;
    private boolean malformedMessage;
    @RoboChartType("nat") private int levelMessages;
    @RoboChartType("nat") private int steamMessages;
    @RoboChartType("real") private double levelValue;
    @RoboChartType("real") private double steamValue;
    @RoboChartType("nat") private final int[] pumpStateMessages;
    @RoboChartType("nat") private final int[] pumpControlMessages;
    private final boolean[] pumpOpenReported;
    private final boolean[] pumpFlowReported;
    private final boolean[] pumpRepairedReceived;
    private final boolean[] pumpControlRepairedReceived;
    private final boolean[] pumpFailureAckReceived;
    private final boolean[] pumpControlFailureAckReceived;
    private boolean levelRepairedReceived;
    private boolean steamRepairedReceived;
    private boolean levelFailureAckReceived;
    private boolean steamFailureAckReceived;

    // SB-V10: successive transmissions that carried STOP.
    @RoboChartType("nat") private int consecutiveStops;

    // SB-V9: unit status and pending failure announcements.
    private final boolean[] pumpOutOfOrder;
    private final boolean[] pumpFailureUnacknowledged;
    private final boolean[] pumpControlOutOfOrder;
    private final boolean[] pumpControlFailureUnacknowledged;
    private boolean levelOutOfOrder;
    private boolean levelFailureUnacknowledged;
    private boolean steamOutOfOrder;
    private boolean steamFailureUnacknowledged;

    // SB-FR7: repair acknowledgements owed in the current cycle.
    private final boolean[] pumpRepairAckDue;
    private final boolean[] pumpControlRepairAckDue;
    private boolean levelRepairAckDue;
    private boolean steamRepairAckDue;

    // SB-V5, SB-V6, SB-V7: current measures.
    @RoboChartType("real") private double q;
    @RoboChartType("real") private double v;
    @RoboChartType("real") private double p;

    // SB-V8: lower and upper bound on the true level.
    @RoboChartType("real") private double levelLowerBound;
    @RoboChartType("real") private double levelUpperBound = SteamBoilerConstants.CAPACITY_C;

    private boolean transmissionFailure;
    private boolean plantStarted;

    // What the program itself sent, learnt at the end of each cycle.
    private ModeValue lastMode = ModeValue.INITIALIZATION_MODE;
    private boolean programReadySent;
    private final boolean[] pumpOrderedOpen;
    @RoboChartType("nat") private final int[] transmissionsSinceOrder;
    private final boolean[] previousFlow;
    private boolean previousSteamKnown;
    @RoboChartType("real") private double previousSteam;

    // Where the level can be at the next transmission, prepared at the end of a cycle.
    private boolean envelopeKnown;
    @RoboChartType("real") private double envelopeLow;
    @RoboChartType("real") private double envelopeHigh = SteamBoilerConstants.CAPACITY_C;
    @RoboChartType("real") private double estimateLow;
    @RoboChartType("real") private double estimateHigh = SteamBoilerConstants.CAPACITY_C;

    /** Creates a monitor that publishes into the given sensor service. */
    public UnitMonitor(BoilerSensor sensor) {
        this.sensor = sensor;
        int n = SteamBoilerConstants.PUMP_COUNT;
        pumpStateMessages = new int[n];
        pumpControlMessages = new int[n];
        pumpOpenReported = new boolean[n];
        pumpFlowReported = new boolean[n];
        pumpRepairedReceived = new boolean[n];
        pumpControlRepairedReceived = new boolean[n];
        pumpFailureAckReceived = new boolean[n];
        pumpControlFailureAckReceived = new boolean[n];
        pumpOutOfOrder = new boolean[n];
        pumpFailureUnacknowledged = new boolean[n];
        pumpControlOutOfOrder = new boolean[n];
        pumpControlFailureUnacknowledged = new boolean[n];
        pumpRepairAckDue = new boolean[n];
        pumpControlRepairAckDue = new boolean[n];
        pumpOrderedOpen = new boolean[n];
        transmissionsSinceOrder = new int[n];
        previousFlow = new boolean[n];
        for (int i = 0; i < n; i++) {
            transmissionsSinceOrder[i] = SETTLED;
        }
    }

    // ------------------------------------------------------------------
    // Reception (SB-DM9)
    // ------------------------------------------------------------------

    /** STOP. */
    public void receiveStop() {
        stopReceived = true;
    }

    /** STEAM_BOILER_WAITING. */
    public void receiveSteamBoilerWaiting() {
        waitingReceived = true;
    }

    /** PHYSICAL_UNITS_READY. */
    public void receivePhysicalUnitsReady() {
        unitsReadyReceived = true;
    }

    /** PUMP_STATE(n, b): whether pump n is open. */
    public void receivePumpState(@RoboChartType("nat") int pump, boolean open) {
        if (validPump(pump)) {
            pumpStateMessages[pump - 1] = pumpStateMessages[pump - 1] + 1;
            pumpOpenReported[pump - 1] = open;
        }
    }

    /** PUMP_CONTROL_STATE(n, b): whether the controller of pump n sees water flowing. */
    public void receivePumpControlState(@RoboChartType("nat") int pump, WaterFlow flow) {
        if (validPump(pump)) {
            pumpControlMessages[pump - 1] = pumpControlMessages[pump - 1] + 1;
            pumpFlowReported[pump - 1] = flow == WaterFlow.FLOWING;
        }
    }

    /** LEVEL(v): the water level reported by the level measuring device (SB-DM5). */
    public void receiveLevel(@RoboChartType("real") double litres) {
        levelMessages = levelMessages + 1;
        levelValue = litres;
    }

    /** STEAM(v): the steam rate reported by the steam measuring device (SB-DM6). */
    public void receiveSteam(@RoboChartType("real") double litresPerSecond) {
        steamMessages = steamMessages + 1;
        steamValue = litresPerSecond;
    }

    /** PUMP_REPAIRED(n). */
    public void receivePumpRepaired(@RoboChartType("nat") int pump) {
        if (validPump(pump)) {
            pumpRepairedReceived[pump - 1] = true;
        }
    }

    /** PUMP_CONTROL_REPAIRED(n). */
    public void receivePumpControlRepaired(@RoboChartType("nat") int pump) {
        if (validPump(pump)) {
            pumpControlRepairedReceived[pump - 1] = true;
        }
    }

    /** LEVEL_REPAIRED. */
    public void receiveLevelRepaired() {
        levelRepairedReceived = true;
    }

    /** STEAM_REPAIRED. */
    public void receiveSteamRepaired() {
        steamRepairedReceived = true;
    }

    /** PUMP_FAILURE_ACKNOWLEDGEMENT(n). */
    public void receivePumpFailureAcknowledgement(@RoboChartType("nat") int pump) {
        if (validPump(pump)) {
            pumpFailureAckReceived[pump - 1] = true;
        }
    }

    /** PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT(n). */
    public void receivePumpControlFailureAcknowledgement(@RoboChartType("nat") int pump) {
        if (validPump(pump)) {
            pumpControlFailureAckReceived[pump - 1] = true;
        }
    }

    /** LEVEL_FAILURE_ACKNOWLEDGEMENT. */
    public void receiveLevelFailureAcknowledgement() {
        levelFailureAckReceived = true;
    }

    /** STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT. */
    public void receiveSteamOutcomeFailureAcknowledgement() {
        steamFailureAckReceived = true;
    }

    private boolean validPump(@RoboChartType("nat") int pump) {
        if (pump < 1 || pump > SteamBoilerConstants.PUMP_COUNT) {
            malformedMessage = true;
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Analysis
    // ------------------------------------------------------------------

    /** Analyses the received transmission and publishes the results to the sensor service. */
    public void analyse() {
        for (int i = 0; i < SteamBoilerConstants.PUMP_COUNT; i++) {
            if (transmissionsSinceOrder[i] < SETTLED) {
                transmissionsSinceOrder[i] = transmissionsSinceOrder[i] + 1;
            }
        }
        countStops();
        transmissionFailure = transmissionFaulty();
        if (!transmissionFailure) {
            q = levelValue;
            v = steamValue;
            acceptAcknowledgements();
            if (waitingReceived) {
                plantStarted = true;
            }
            if (plantStarted) {
                acceptRepairs();
                detectSteamFailure();
                detectPumpFailures();
                detectPumpControlFailures();
                computeThroughput();
                updateLevelKnowledge();
            }
        }
        publish();
    }

    /** SB-V10: count successive transmissions carrying STOP. */
    private void countStops() {
        if (stopReceived) {
            if (consecutiveStops < STOPS_FOR_EMERGENCY) {
                consecutiveStops = consecutiveStops + 1;
            }
        } else {
            consecutiveStops = 0;
        }
    }

    /** SB-FR5: an indispensable message is missing, or a message's presence is aberrant. */
    private boolean transmissionFaulty() {
        boolean faulty = malformedMessage || levelMessages != 1 || steamMessages != 1;
        for (int i = 0; i < SteamBoilerConstants.PUMP_COUNT; i++) {
            faulty = faulty || pumpStateMessages[i] != 1 || pumpControlMessages[i] != 1;
            faulty = faulty || (pumpRepairedReceived[i] && !pumpOutOfOrder[i]);
            faulty = faulty || (pumpControlRepairedReceived[i] && !pumpControlOutOfOrder[i]);
            faulty = faulty || (pumpFailureAckReceived[i] && !pumpFailureUnacknowledged[i]);
            faulty = faulty || (pumpControlFailureAckReceived[i] && !pumpControlFailureUnacknowledged[i]);
        }
        boolean initialising = lastMode == ModeValue.INITIALIZATION_MODE;
        faulty = faulty || (waitingReceived && !initialising);
        faulty = faulty || (unitsReadyReceived && !(initialising && programReadySent));
        faulty = faulty || (levelRepairedReceived && !levelOutOfOrder);
        faulty = faulty || (steamRepairedReceived && !steamOutOfOrder);
        faulty = faulty || (levelFailureAckReceived && !levelFailureUnacknowledged);
        faulty = faulty || (steamFailureAckReceived && !steamFailureUnacknowledged);
        return faulty;
    }

    /** SB-FR6: an acknowledgement ends the repetition of a failure announcement. */
    private void acceptAcknowledgements() {
        for (int i = 0; i < SteamBoilerConstants.PUMP_COUNT; i++) {
            if (pumpFailureAckReceived[i]) {
                pumpFailureUnacknowledged[i] = false;
            }
            if (pumpControlFailureAckReceived[i]) {
                pumpControlFailureUnacknowledged[i] = false;
            }
        }
        if (levelFailureAckReceived) {
            levelFailureUnacknowledged = false;
        }
        if (steamFailureAckReceived) {
            steamFailureUnacknowledged = false;
        }
    }

    /**
     * SB-FR7: a repaired unit is sound again and its repair is acknowledged. A
     * repaired pump is expected to keep its reported state, and its controller
     * gets the same settling time as after an order (SB-FR2, SB-Beh25).
     */
    private void acceptRepairs() {
        for (int i = 0; i < SteamBoilerConstants.PUMP_COUNT; i++) {
            if (pumpRepairedReceived[i]) {
                pumpOutOfOrder[i] = false;
                pumpFailureUnacknowledged[i] = false;
                pumpRepairAckDue[i] = true;
                pumpOrderedOpen[i] = pumpOpenReported[i];
                transmissionsSinceOrder[i] = 0;
            }
            if (pumpControlRepairedReceived[i]) {
                pumpControlOutOfOrder[i] = false;
                pumpControlFailureUnacknowledged[i] = false;
                pumpControlRepairAckDue[i] = true;
                previousFlow[i] = pumpFlowReported[i];
            }
        }
        if (levelRepairedReceived) {
            levelOutOfOrder = false;
            levelFailureUnacknowledged = false;
            levelRepairAckDue = true;
        }
        if (steamRepairedReceived) {
            steamOutOfOrder = false;
            steamFailureUnacknowledged = false;
            steamRepairAckDue = true;
            previousSteamKnown = false;
        }
    }

    /**
     * SB-FR4: steam reading outside [0, W], or changing faster than U1 / U2
     * allow. SB-Beh3: during initialisation, once STEAM_BOILER_WAITING has
     * arrived, a steam rate other than zero means the device is defective.
     */
    private void detectSteamFailure() {
        if (steamOutOfOrder) {
            return;
        }
        boolean outOfRange = v < 0.0 || v > SteamBoilerConstants.MAXIMAL_STEAM_W;
        boolean tooFast = previousSteamKnown
                && (v > previousSteam + SteamBoilerConstants.STEAM_INCREASE_U1 * T
                    || v < previousSteam - SteamBoilerConstants.STEAM_DECREASE_U2 * T);
        boolean notZeroAtStart = lastMode == ModeValue.INITIALIZATION_MODE && v != 0.0;
        if (outOfRange || tooFast || notZeroAtStart) {
            steamOutOfOrder = true;
            steamFailureUnacknowledged = true;
        }
    }

    /**
     * SB-FR1: a pump whose reported state differs from the program's last order
     * either did not obey the order (transmission following it) or changed
     * spontaneously. Until ordered otherwise, pumps are expected closed.
     */
    private void detectPumpFailures() {
        for (int i = 0; i < SteamBoilerConstants.PUMP_COUNT; i++) {
            boolean checkable = !pumpOutOfOrder[i] && !pumpRepairedReceived[i];
            if (checkable && pumpOpenReported[i] != pumpOrderedOpen[i]) {
                pumpOutOfOrder[i] = true;
                pumpFailureUnacknowledged[i] = true;
            }
        }
    }

    /**
     * SB-FR2: with its pump believed sound, a pump controller must show the
     * flow matching the order on the second transmission after it (the pump's
     * five-second start-up latency, SB-Beh25), and must not change its report
     * spontaneously once settled.
     */
    private void detectPumpControlFailures() {
        for (int i = 0; i < SteamBoilerConstants.PUMP_COUNT; i++) {
            boolean checkable = !pumpControlOutOfOrder[i] && !pumpControlRepairedReceived[i]
                    && !pumpOutOfOrder[i] && !pumpRepairedReceived[i];
            if (checkable) {
                int age = transmissionsSinceOrder[i];
                boolean missedOrder = age == 2 && pumpFlowReported[i] != pumpOrderedOpen[i];
                boolean spontaneous = age >= SETTLED && pumpFlowReported[i] != previousFlow[i];
                if (missedOrder || spontaneous) {
                    pumpControlOutOfOrder[i] = true;
                    pumpControlFailureUnacknowledged[i] = true;
                }
            }
        }
    }

    /**
     * SB-V7, SB-Beh25: throughput the pumps supply over the coming cycle. A
     * pump reported open has completed its five seconds of pressure balancing
     * (it was ordered open at least one transmission ago) and pours at
     * capacity P; only pumps whose pump and pump controller are believed sound
     * are counted.
     */
    private void computeThroughput() {
        p = soundOpenPumps() * SteamBoilerConstants.PUMP_CAPACITY_P;
    }

    private int soundOpenPumps() {
        int pouring = 0;
        for (int i = 0; i < SteamBoilerConstants.PUMP_COUNT; i++) {
            if (pumpOpenReported[i] && !pumpOutOfOrder[i] && !pumpControlOutOfOrder[i]) {
                pouring = pouring + 1;
            }
        }
        return pouring;
    }

    /**
     * SB-FR3, SB-V5, SB-V8: check the level reading against [0, C] and against
     * the envelope reachable since the previous transmission (not during
     * initialisation, where the valve's outflow is unknown); while the device
     * is out of order, the level is known only as the estimate's bounds.
     */
    private void updateLevelKnowledge() {
        if (!levelOutOfOrder) {
            boolean dynamicCheck = envelopeKnown && !levelRepairedReceived
                    && lastMode != ModeValue.INITIALIZATION_MODE;
            boolean outOfRange = q < 0.0 || q > SteamBoilerConstants.CAPACITY_C;
            boolean unreachable = dynamicCheck && (q < envelopeLow || q > envelopeHigh);
            if (outOfRange || unreachable) {
                levelOutOfOrder = true;
                levelFailureUnacknowledged = true;
            }
        }
        if (levelOutOfOrder) {
            levelLowerBound = estimateLow;
            levelUpperBound = estimateHigh;
        } else {
            levelLowerBound = q;
            levelUpperBound = q;
        }
    }

    /** Maximal steam volume leaving the boiler during one cycle (litre). */
    @RoboChartType("real")
    private double steamOutflowMax() {
        double ceiling = SteamBoilerConstants.MAXIMAL_STEAM_W * T;
        if (steamOutOfOrder) {
            return ceiling;
        }
        return Math.min(ceiling, v * T + SteamBoilerConstants.STEAM_INCREASE_U1 * T * T / 2.0);
    }

    /** Minimal steam volume leaving the boiler during one cycle (litre). */
    @RoboChartType("real")
    private double steamOutflowMin() {
        if (steamOutOfOrder) {
            return 0.0;
        }
        return Math.max(0.0, v * T - SteamBoilerConstants.STEAM_DECREASE_U2 * T * T / 2.0);
    }

    /**
     * SB-V4, SB-Beh13, SB-Beh25: worst-case level until the next transmission,
     * from the level bounds (q, or the estimate), worst-case steam (W, U1, U2)
     * and the pumps. Pumps ordered open in this cycle pour nothing before then;
     * pumps closed now stop at once, except those out of order, which may not
     * obey. The controller closes the pumps exactly when the level could rise
     * above N2 (and cannot fall below N1), so the limit risk is judged with
     * the pumps closed in that case and left as they are otherwise.
     */
    private void publish() {
        int open = 0;
        int stuckOpen = 0;
        boolean anyPumpOut = false;
        boolean anyControllerOut = false;
        for (int i = 0; i < SteamBoilerConstants.PUMP_COUNT; i++) {
            if (pumpOpenReported[i]) {
                open = open + 1;
                if (pumpOutOfOrder[i]) {
                    stuckOpen = stuckOpen + 1;
                }
            }
            anyPumpOut = anyPumpOut || pumpOutOfOrder[i];
            anyControllerOut = anyControllerOut || pumpControlOutOfOrder[i];
        }
        double pumpVolume = SteamBoilerConstants.PUMP_CAPACITY_P * T;
        double outMax = steamOutflowMax();
        double outMin = steamOutflowMin();
        double minKept = Math.min(levelLowerBound, levelLowerBound + p * T - outMax);
        double maxKept = Math.max(levelUpperBound, levelUpperBound + open * pumpVolume - outMin);
        double minClosed = Math.min(levelLowerBound, levelLowerBound - outMax);
        double maxClosed = Math.max(levelUpperBound, levelUpperBound + stuckOpen * pumpVolume - outMin);
        boolean mayFall = minKept < SteamBoilerConstants.MINIMAL_NORMAL_N1;
        boolean mayRise = !mayFall && maxKept > SteamBoilerConstants.MAXIMAL_NORMAL_N2;
        boolean risk = minKept <= SteamBoilerConstants.MINIMAL_LIMIT_M1
                || maxKept >= SteamBoilerConstants.MAXIMAL_LIMIT_M2;
        if (mayRise) {
            risk = minClosed <= SteamBoilerConstants.MINIMAL_LIMIT_M1
                    || maxClosed >= SteamBoilerConstants.MAXIMAL_LIMIT_M2;
        }
        boolean below = q < SteamBoilerConstants.MINIMAL_NORMAL_N1;
        boolean above = !below && q > SteamBoilerConstants.MAXIMAL_NORMAL_N2;
        sensor.updateMessages(consecutiveStops >= STOPS_FOR_EMERGENCY, transmissionFailure,
                waitingReceived, unitsReadyReceived);
        sensor.updateUnits(levelOutOfOrder, steamOutOfOrder, anyControllerOut,
                anyPumpOut || anyControllerOut || steamOutOfOrder);
        sensor.updateLevels(below, above, mayFall, mayRise, risk);
    }

    // ------------------------------------------------------------------
    // Transmission
    // ------------------------------------------------------------------

    /**
     * SB-FR6 and SB-FR7: announce every unacknowledged failure, and acknowledge
     * every repair notified in this cycle.
     */
    public void sendProtocolMessages(BoilerActuator actuator) {
        for (int i = 0; i < SteamBoilerConstants.PUMP_COUNT; i++) {
            int pump = i + 1;
            if (pumpFailureUnacknowledged[i]) {
                actuator.apply(new OutputEvent.PumpFailureDetection(pump));
            }
            if (pumpControlFailureUnacknowledged[i]) {
                actuator.apply(new OutputEvent.PumpControlFailureDetection(pump));
            }
            if (pumpRepairAckDue[i]) {
                actuator.apply(new OutputEvent.PumpRepairedAcknowledgement(pump));
            }
            if (pumpControlRepairAckDue[i]) {
                actuator.apply(new OutputEvent.PumpControlRepairedAcknowledgement(pump));
            }
        }
        if (levelFailureUnacknowledged) {
            actuator.apply(new OutputEvent.LevelFailureDetection());
        }
        if (steamFailureUnacknowledged) {
            actuator.apply(new OutputEvent.SteamFailureDetection());
        }
        if (levelRepairAckDue) {
            actuator.apply(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (steamRepairAckDue) {
            actuator.apply(new OutputEvent.SteamRepairedAcknowledgement());
        }
    }

    /**
     * Ends the cycle: learns the mode, handshake and pump orders the program
     * sent, prepares the level envelope for the next transmission, and clears
     * the reception buffer.
     */
    public void endCycle(List<OutputEvent> sent) {
        for (OutputEvent message : sent) {
            if (message instanceof OutputEvent.ModeMessage) {
                OutputEvent.ModeMessage modeMessage = (OutputEvent.ModeMessage) message;
                lastMode = modeMessage.value();
            } else if (message instanceof OutputEvent.ProgramReady) {
                programReadySent = true;
            } else if (message instanceof OutputEvent.OpenPump) {
                OutputEvent.OpenPump openPump = (OutputEvent.OpenPump) message;
                noteOrder(openPump.pump(), true);
            } else if (message instanceof OutputEvent.ClosePump) {
                OutputEvent.ClosePump closePump = (OutputEvent.ClosePump) message;
                noteOrder(closePump.pump(), false);
            }
        }
        if (plantStarted && !transmissionFailure) {
            prepareNextEnvelope();
        }
        clearReception();
    }

    private void noteOrder(@RoboChartType("nat") int pump, boolean open) {
        int i = pump - 1;
        if (i >= 0 && i < SteamBoilerConstants.PUMP_COUNT && pumpOrderedOpen[i] != open) {
            pumpOrderedOpen[i] = open;
            transmissionsSinceOrder[i] = 0;
        }
    }

    /**
     * SB-FR3 envelope (extreme behaviour: no pump water at worst) and SB-V8 /
     * SB-Beh19 estimate (water known to be supplied by sound pumps that stay
     * open counts, one litre pumped being one litre of boiler content), both
     * over the next cycle, after this cycle's pump orders.
     */
    private void prepareNextEnvelope() {
        int keepPouring = 0;
        int mayPour = 0;
        for (int i = 0; i < SteamBoilerConstants.PUMP_COUNT; i++) {
            boolean staysOpen = pumpOpenReported[i] && (pumpOrderedOpen[i] || pumpOutOfOrder[i]);
            if (staysOpen) {
                mayPour = mayPour + 1;
                if (!pumpOutOfOrder[i] && !pumpControlOutOfOrder[i]) {
                    keepPouring = keepPouring + 1;
                }
            }
            previousFlow[i] = pumpFlowReported[i];
        }
        double pumpVolume = SteamBoilerConstants.PUMP_CAPACITY_P * T;
        double outMax = steamOutflowMax();
        double outMin = steamOutflowMin();
        envelopeLow = clamp(levelLowerBound - outMax);
        envelopeHigh = clamp(levelUpperBound + mayPour * pumpVolume - outMin);
        estimateLow = clamp(levelLowerBound + keepPouring * pumpVolume - outMax);
        estimateHigh = envelopeHigh;
        envelopeKnown = true;
        previousSteamKnown = !steamOutOfOrder;
        previousSteam = v;
    }

    @RoboChartType("real")
    private double clamp(@RoboChartType("real") double litres) {
        return Math.max(0.0, Math.min(SteamBoilerConstants.CAPACITY_C, litres));
    }

    private void clearReception() {
        stopReceived = false;
        waitingReceived = false;
        unitsReadyReceived = false;
        malformedMessage = false;
        levelMessages = 0;
        steamMessages = 0;
        levelRepairedReceived = false;
        steamRepairedReceived = false;
        levelFailureAckReceived = false;
        steamFailureAckReceived = false;
        levelRepairAckDue = false;
        steamRepairAckDue = false;
        for (int i = 0; i < SteamBoilerConstants.PUMP_COUNT; i++) {
            pumpStateMessages[i] = 0;
            pumpControlMessages[i] = 0;
            pumpRepairedReceived[i] = false;
            pumpControlRepairedReceived[i] = false;
            pumpFailureAckReceived[i] = false;
            pumpControlFailureAckReceived[i] = false;
            pumpRepairAckDue[i] = false;
            pumpControlRepairAckDue[i] = false;
        }
    }
}
