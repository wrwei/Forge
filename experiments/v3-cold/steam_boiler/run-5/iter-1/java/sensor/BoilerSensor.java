package steam_boiler.sensor;

import java.util.List;
import steam_boiler.BoilerConstants;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.FlowObservation;
import steam_boiler.domain.ModeIndicator;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;

/**
 * The program's view of the physical units. In the reception stage of a cycle it buffers the
 * incoming transmission; in the analysis stage ({@link #analyse()}) it judges unit and
 * transmission failures, keeps the unit status, the STOP count, q, v, p and the level
 * estimate, and forecasts the worst-case level at the end of the coming cycle. It also
 * observes the outgoing transmission ({@link #recordSent(OutputEvent)}), which it needs to
 * judge the pumps and pump controllers. Pumps are numbered 1 to 4.
 */
public final class BoilerSensor {

    private static final int PUMPS = BoilerConstants.PUMP_COUNT;
    @RoboChartType("real") private static final double DT = BoilerConstants.CYCLE_SECONDS;

    // ---- contents of the current incoming transmission ----
    private boolean stopPresent;
    private boolean waitingPresent;
    private boolean unitsReadyPresent;
    @RoboChartType("nat") private int levelMessages;
    @RoboChartType("nat") private int steamMessages;
    @RoboChartType("real") private double receivedLevel;
    @RoboChartType("real") private double receivedSteam;
    private final int[] pumpStateMessages = new int[PUMPS];
    private final boolean[] receivedPumpOn = new boolean[PUMPS];
    private final int[] pumpControlMessages = new int[PUMPS];
    private final boolean[] receivedFlow = new boolean[PUMPS];
    private final boolean[] pumpRepairedPresent = new boolean[PUMPS];
    private final boolean[] pumpControlRepairedPresent = new boolean[PUMPS];
    private final boolean[] pumpFailureAckPresent = new boolean[PUMPS];
    private final boolean[] pumpControlFailureAckPresent = new boolean[PUMPS];
    private boolean levelRepairedPresent;
    private boolean steamRepairedPresent;
    private boolean levelFailureAckPresent;
    private boolean steamFailureAckPresent;
    private boolean unknownPumpReferenced;

    // ---- knowledge kept across cycles ----
    private final UnitStatusBoard units = new UnitStatusBoard();
    @RoboChartType("nat") private int stopCount;
    private boolean boilerWaitingLatched;
    private boolean initializationLeft;
    private boolean programReadySent;
    private boolean valveIsOpen;
    private boolean previousCycleAnalysed;
    private boolean previousSteamKnown;
    @RoboChartType("real") private double previousSteam;
    private final boolean[] previousPumpOn = new boolean[PUMPS];
    private final boolean[] previousFlow = new boolean[PUMPS];
    private final boolean[] orderedThisCycle = new boolean[PUMPS];
    private final boolean[] orderedOnThisCycle = new boolean[PUMPS];
    private final boolean[] orderedLastCycle = new boolean[PUMPS];
    private final boolean[] orderedOnLastCycle = new boolean[PUMPS];
    private final boolean[] orderedTwoCyclesAgo = new boolean[PUMPS];
    private final boolean[] orderedOnTwoCyclesAgo = new boolean[PUMPS];
    private final boolean[] pouringSurely = new boolean[PUMPS];
    private final boolean[] pouringPossibly = new boolean[PUMPS];
    @RoboChartType("real") private double steamOutMin;
    @RoboChartType("real") private double steamOutMax;
    private boolean levelKnown;

    // ---- results of the analysis stage ----
    private boolean transmissionFailed;
    @RoboChartType("real") private double waterLevelQ;
    @RoboChartType("real") private double steamRateV;
    @RoboChartType("real") private double pumpThroughputP;
    @RoboChartType("real") private double estimatedLow;
    @RoboChartType("real") private double estimatedHigh = BoilerConstants.CAPACITY_C;
    @RoboChartType("real") private double forecastMin;
    @RoboChartType("real") private double forecastMax = BoilerConstants.CAPACITY_C;

    // =====================================================================================
    // Reception stage
    // =====================================================================================

    /** Starts the reception stage of a new cycle: the previous transmission is forgotten. */
    public void beginCycle() {
        stopPresent = false;
        waitingPresent = false;
        unitsReadyPresent = false;
        levelMessages = 0;
        steamMessages = 0;
        levelRepairedPresent = false;
        steamRepairedPresent = false;
        levelFailureAckPresent = false;
        steamFailureAckPresent = false;
        unknownPumpReferenced = false;
        for (int i = 0; i < PUMPS; i++) {
            pumpStateMessages[i] = 0;
            pumpControlMessages[i] = 0;
            pumpRepairedPresent[i] = false;
            pumpControlRepairedPresent[i] = false;
            pumpFailureAckPresent[i] = false;
            pumpControlFailureAckPresent[i] = false;
            orderedTwoCyclesAgo[i] = orderedLastCycle[i];
            orderedOnTwoCyclesAgo[i] = orderedOnLastCycle[i];
            orderedLastCycle[i] = orderedThisCycle[i];
            orderedOnLastCycle[i] = orderedOnThisCycle[i];
            orderedThisCycle[i] = false;
            orderedOnThisCycle[i] = false;
        }
        units.beginCycle();
    }

    /** Records one incoming message of the current transmission. */
    public void receive(InputEvent message) {
        if (message instanceof InputEvent.StopSignal) {
            stopPresent = true;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            waitingPresent = true;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            unitsReadyPresent = true;
        } else if (message instanceof InputEvent.PumpState) {
            InputEvent.PumpState ps = (InputEvent.PumpState) message;
            int n = ps.report().reportedPump();
            if (validPump(n)) {
                pumpStateMessages[n - 1] = pumpStateMessages[n - 1] + 1;
                receivedPumpOn[n - 1] = ps.report().pumpOn();
            }
        } else if (message instanceof InputEvent.PumpControlState) {
            InputEvent.PumpControlState pcs = (InputEvent.PumpControlState) message;
            int n = pcs.report().controlledPump();
            if (validPump(n)) {
                pumpControlMessages[n - 1] = pumpControlMessages[n - 1] + 1;
                receivedFlow[n - 1] = pcs.report().observation() == FlowObservation.FLOW;
            }
        } else if (message instanceof InputEvent.Level) {
            InputEvent.Level lv = (InputEvent.Level) message;
            levelMessages = levelMessages + 1;
            receivedLevel = lv.litres();
        } else if (message instanceof InputEvent.Steam) {
            InputEvent.Steam st = (InputEvent.Steam) message;
            steamMessages = steamMessages + 1;
            receivedSteam = st.litresPerSecond();
        } else if (message instanceof InputEvent.PumpRepaired) {
            InputEvent.PumpRepaired pr = (InputEvent.PumpRepaired) message;
            if (validPump(pr.pumpNumber())) {
                pumpRepairedPresent[pr.pumpNumber() - 1] = true;
            }
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired pcr = (InputEvent.PumpControlRepaired) message;
            if (validPump(pcr.pumpNumber())) {
                pumpControlRepairedPresent[pcr.pumpNumber() - 1] = true;
            }
        } else if (message instanceof InputEvent.LevelRepaired) {
            levelRepairedPresent = true;
        } else if (message instanceof InputEvent.SteamRepaired) {
            steamRepairedPresent = true;
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement pa = (InputEvent.PumpFailureAcknowledgement) message;
            if (validPump(pa.pumpNumber())) {
                pumpFailureAckPresent[pa.pumpNumber() - 1] = true;
            }
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement pca =
                    (InputEvent.PumpControlFailureAcknowledgement) message;
            if (validPump(pca.pumpNumber())) {
                pumpControlFailureAckPresent[pca.pumpNumber() - 1] = true;
            }
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            levelFailureAckPresent = true;
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            steamFailureAckPresent = true;
        }
    }

    private boolean validPump(int n) {
        boolean valid = n >= 1 && n <= PUMPS;
        if (!valid) {
            unknownPumpReferenced = true;
        }
        return valid;
    }

    // =====================================================================================
    // Analysis stage
    // =====================================================================================

    /** Analysis stage: judges failures and updates every derived quantity. */
    public void analyse() {
        transmissionFailed = transmissionMissing() || transmissionAberrant();
        if (stopPresent) {
            if (stopCount < BoilerConstants.STOP_LIMIT) {
                stopCount = stopCount + 1;
            }
        } else {
            stopCount = 0;
        }
        if (waitingPresent && !initializationLeft) {
            boilerWaitingLatched = true;
        }
        if (transmissionFailed) {
            // The controller goes to emergency stop; the readings of this transmission are
            // not trusted and not used.
            return;
        }
        processAcknowledgementsAndRepairs();
        double expectedLow = expectedLevelLow();
        double expectedHigh = expectedLevelHigh();
        detectSteamFailure();
        detectPumpFailures();
        detectPumpControlFailures();
        detectLevelFailure(expectedLow, expectedHigh);
        updateLevelKnowledge(expectedLow, expectedHigh);
        updateSteamOutflow();
        updatePumpInflow();
        forecastMin = estimatedLow - steamOutMax + countOf(pouringSurely) * BoilerConstants.PUMP_CAPACITY_P * DT;
        forecastMax = estimatedHigh - steamOutMin + countOf(pouringPossibly) * BoilerConstants.PUMP_CAPACITY_P * DT;
        previousSteam = receivedSteam;
        previousSteamKnown = !units.outOfOrder(UnitStatusBoard.STEAM_UNIT);
        for (int i = 0; i < PUMPS; i++) {
            previousPumpOn[i] = receivedPumpOn[i];
            previousFlow[i] = receivedFlow[i];
        }
        previousCycleAnalysed = true;
    }

    /** An indispensable message (a data message of some unit) is missing. */
    private boolean transmissionMissing() {
        boolean missing = levelMessages == 0 || steamMessages == 0;
        for (int i = 0; i < PUMPS; i++) {
            if (pumpStateMessages[i] == 0 || pumpControlMessages[i] == 0) {
                missing = true;
            }
        }
        return missing;
    }

    /** A message is present whose presence is aberrant. */
    private boolean transmissionAberrant() {
        boolean aberrant = levelMessages > 1 || steamMessages > 1 || unknownPumpReferenced;
        if (waitingPresent && initializationLeft) {
            aberrant = true;
        }
        if (unitsReadyPresent && (initializationLeft || !programReadySent)) {
            aberrant = true;
        }
        for (int i = 0; i < PUMPS; i++) {
            int pump = UnitStatusBoard.pumpUnit(i);
            int control = UnitStatusBoard.pumpControlUnit(i);
            if (pumpStateMessages[i] > 1 || pumpControlMessages[i] > 1) {
                aberrant = true;
            }
            if (pumpRepairedPresent[i] && !units.outOfOrder(pump)) {
                aberrant = true;
            }
            if (pumpControlRepairedPresent[i] && !units.outOfOrder(control)) {
                aberrant = true;
            }
            if (pumpFailureAckPresent[i] && !units.awaitingAcknowledgement(pump)) {
                aberrant = true;
            }
            if (pumpControlFailureAckPresent[i] && !units.awaitingAcknowledgement(control)) {
                aberrant = true;
            }
        }
        if (levelRepairedPresent && !units.outOfOrder(UnitStatusBoard.LEVEL_UNIT)) {
            aberrant = true;
        }
        if (steamRepairedPresent && !units.outOfOrder(UnitStatusBoard.STEAM_UNIT)) {
            aberrant = true;
        }
        if (levelFailureAckPresent && !units.awaitingAcknowledgement(UnitStatusBoard.LEVEL_UNIT)) {
            aberrant = true;
        }
        if (steamFailureAckPresent && !units.awaitingAcknowledgement(UnitStatusBoard.STEAM_UNIT)) {
            aberrant = true;
        }
        return aberrant;
    }

    private void processAcknowledgementsAndRepairs() {
        for (int i = 0; i < PUMPS; i++) {
            if (pumpFailureAckPresent[i]) {
                units.acknowledgeFailure(UnitStatusBoard.pumpUnit(i));
            }
            if (pumpControlFailureAckPresent[i]) {
                units.acknowledgeFailure(UnitStatusBoard.pumpControlUnit(i));
            }
            if (pumpRepairedPresent[i]) {
                units.repair(UnitStatusBoard.pumpUnit(i));
            }
            if (pumpControlRepairedPresent[i]) {
                units.repair(UnitStatusBoard.pumpControlUnit(i));
            }
        }
        if (levelFailureAckPresent) {
            units.acknowledgeFailure(UnitStatusBoard.LEVEL_UNIT);
        }
        if (steamFailureAckPresent) {
            units.acknowledgeFailure(UnitStatusBoard.STEAM_UNIT);
        }
        if (levelRepairedPresent) {
            units.repair(UnitStatusBoard.LEVEL_UNIT);
        }
        if (steamRepairedPresent) {
            units.repair(UnitStatusBoard.STEAM_UNIT);
        }
    }

    /**
     * Lowest level reachable now from what was known one cycle ago, under worst-case steam
     * (rising at U1) and the pumps surely pouring during the last cycle.
     */
    private double expectedLevelLow() {
        if (!levelKnown || valveIsOpen) {
            // No earlier knowledge, or the evacuation valve drained an unknown quantity.
            return 0.0;
        }
        return clamp(estimatedLow - steamOutMax + countOf(pouringSurely) * BoilerConstants.PUMP_CAPACITY_P * DT);
    }

    /**
     * Highest level reachable now from what was known one cycle ago, under worst-case steam
     * (falling at U2) and every pump possibly pouring during the last cycle.
     */
    private double expectedLevelHigh() {
        if (!levelKnown) {
            return BoilerConstants.CAPACITY_C;
        }
        return clamp(estimatedHigh - steamOutMin + countOf(pouringPossibly) * BoilerConstants.PUMP_CAPACITY_P * DT);
    }

    /** SB-FR4 (and SB-Beh3: in initialization the steam rate must be zero). */
    private void detectSteamFailure() {
        int unit = UnitStatusBoard.STEAM_UNIT;
        if (units.outOfOrder(unit)) {
            return;
        }
        boolean failed = receivedSteam < 0.0 || receivedSteam > BoilerConstants.MAX_STEAM_W;
        if (previousSteamKnown && previousCycleAnalysed && !units.repairedThisCycle(unit)) {
            if (receivedSteam > previousSteam + BoilerConstants.STEAM_INCREASE_U1 * DT
                    || receivedSteam < previousSteam - BoilerConstants.STEAM_DECREASE_U2 * DT) {
                failed = true;
            }
        }
        if (!initializationLeft && boilerWaitingLatched && receivedSteam != 0.0) {
            failed = true;
        }
        if (failed) {
            units.detectFailure(unit);
        }
    }

    /**
     * SB-FR1: after an order sent in the previous cycle the pump must show the ordered state;
     * without an order it must not change state.
     */
    private void detectPumpFailures() {
        for (int i = 0; i < PUMPS; i++) {
            int unit = UnitStatusBoard.pumpUnit(i);
            if (previousCycleAnalysed && !units.outOfOrder(unit) && !units.repairedThisCycle(unit)) {
                boolean expectedOn = previousPumpOn[i];
                if (orderedLastCycle[i]) {
                    expectedOn = orderedOnLastCycle[i];
                }
                if (receivedPumpOn[i] != expectedOn) {
                    units.detectFailure(unit);
                }
            }
        }
    }

    /**
     * SB-FR2: judged only while the pump itself is known to work correctly. In the second
     * transmission after an order the controller must show the coming or ceasing of flow;
     * the first transmission after an order is a transition and is not judged; without a
     * recent order the observation must not change.
     */
    private void detectPumpControlFailures() {
        for (int i = 0; i < PUMPS; i++) {
            int unit = UnitStatusBoard.pumpControlUnit(i);
            int pump = UnitStatusBoard.pumpUnit(i);
            boolean judged = previousCycleAnalysed && !units.outOfOrder(unit) && !units.repairedThisCycle(unit)
                    && !units.outOfOrder(pump) && !units.repairedThisCycle(pump) && !orderedLastCycle[i];
            if (judged) {
                boolean expectedFlow = previousFlow[i];
                if (orderedTwoCyclesAgo[i]) {
                    expectedFlow = orderedOnTwoCyclesAgo[i];
                }
                if (receivedFlow[i] != expectedFlow) {
                    units.detectFailure(unit);
                }
            }
        }
    }

    /** SB-FR3: outside [0, C], or outside the range reachable from the previous cycle. */
    private void detectLevelFailure(double expectedLow, double expectedHigh) {
        int unit = UnitStatusBoard.LEVEL_UNIT;
        if (units.outOfOrder(unit)) {
            return;
        }
        boolean failed = receivedLevel < 0.0 || receivedLevel > BoilerConstants.CAPACITY_C;
        if (levelKnown && !units.repairedThisCycle(unit)
                && (receivedLevel < expectedLow || receivedLevel > expectedHigh)) {
            failed = true;
        }
        if (failed) {
            units.detectFailure(unit);
        }
    }

    /** q, and the level bounds: the reading itself, or the estimate while the device is out of order. */
    private void updateLevelKnowledge(double expectedLow, double expectedHigh) {
        waterLevelQ = receivedLevel;
        if (units.outOfOrder(UnitStatusBoard.LEVEL_UNIT)) {
            estimatedLow = expectedLow;
            estimatedHigh = expectedHigh;
        } else {
            estimatedLow = receivedLevel;
            estimatedHigh = receivedLevel;
        }
        levelKnown = true;
    }

    /** v, and the steam volume that can leave during the coming cycle (bounded by W, U1, U2). */
    private void updateSteamOutflow() {
        steamRateV = receivedSteam;
        if (units.outOfOrder(UnitStatusBoard.STEAM_UNIT)) {
            steamOutMax = BoilerConstants.MAX_STEAM_W * DT;
            steamOutMin = 0.0;
        } else {
            steamOutMax = receivedSteam * DT + 0.5 * BoilerConstants.STEAM_INCREASE_U1 * DT * DT;
            if (steamOutMax > BoilerConstants.MAX_STEAM_W * DT) {
                steamOutMax = BoilerConstants.MAX_STEAM_W * DT;
            }
            steamOutMin = receivedSteam * DT - 0.5 * BoilerConstants.STEAM_DECREASE_U2 * DT * DT;
            if (steamOutMin < 0.0) {
                steamOutMin = 0.0;
            }
        }
    }

    /**
     * p, and which pumps surely / possibly pour during the coming cycle. A pump ordered on in
     * this cycle pours nothing during the coming cycle (start-up latency, SB-Beh25), so only
     * pumps already reported on count, and a pump counts as surely pouring only once its flow
     * is confirmed; a pump closed in this cycle stops at once (see
     * {@link #recordSent(OutputEvent)}).
     */
    private void updatePumpInflow() {
        int confirmed = 0;
        for (int i = 0; i < PUMPS; i++) {
            boolean pumpSound = !units.outOfOrder(UnitStatusBoard.pumpUnit(i));
            boolean controlSound = !units.outOfOrder(UnitStatusBoard.pumpControlUnit(i));
            boolean knownOn = pumpSound && receivedPumpOn[i];
            // Surely pouring: the controller confirms the flow, or (controller out of order) the
            // pump has been on for longer than its start-up latency.
            pouringSurely[i] = knownOn && ((controlSound && receivedFlow[i]) || (!controlSound && !orderedLastCycle[i]));
            pouringPossibly[i] = knownOn || !pumpSound;
            if (receivedPumpOn[i] && receivedFlow[i]) {
                confirmed = confirmed + 1;
            }
        }
        pumpThroughputP = confirmed * BoilerConstants.PUMP_CAPACITY_P;
    }

    private static int countOf(boolean[] flags) {
        int count = 0;
        for (int i = 0; i < flags.length; i++) {
            if (flags[i]) {
                count = count + 1;
            }
        }
        return count;
    }

    private static double clamp(double litres) {
        double result = litres;
        if (result < 0.0) {
            result = 0.0;
        }
        if (result > BoilerConstants.CAPACITY_C) {
            result = BoilerConstants.CAPACITY_C;
        }
        return result;
    }

    // =====================================================================================
    // Transmission stage
    // =====================================================================================

    /** Records one message transmitted at the end of the current cycle. */
    public void recordSent(OutputEvent message) {
        if (message instanceof OutputEvent.ModeMessage) {
            OutputEvent.ModeMessage mm = (OutputEvent.ModeMessage) message;
            if (mm.operatingMode() != ModeIndicator.INITIALIZATION_MODE) {
                initializationLeft = true;
            }
        } else if (message instanceof OutputEvent.ProgramReady) {
            programReadySent = true;
        } else if (message instanceof OutputEvent.Valve) {
            valveIsOpen = !valveIsOpen;
        } else if (message instanceof OutputEvent.OpenPump) {
            OutputEvent.OpenPump op = (OutputEvent.OpenPump) message;
            int n = op.pumpNumber();
            if (n >= 1 && n <= PUMPS) {
                orderedThisCycle[n - 1] = true;
                orderedOnThisCycle[n - 1] = true;
            }
        } else if (message instanceof OutputEvent.ClosePump) {
            OutputEvent.ClosePump cp = (OutputEvent.ClosePump) message;
            int n = cp.pumpNumber();
            if (n >= 1 && n <= PUMPS) {
                orderedThisCycle[n - 1] = true;
                orderedOnThisCycle[n - 1] = false;
                pouringSurely[n - 1] = false;
                if (!units.outOfOrder(UnitStatusBoard.pumpUnit(n - 1))) {
                    pouringPossibly[n - 1] = false;
                }
            }
        }
    }

    /** Failure announcements still awaiting acknowledgement, and this cycle's repair acknowledgements. */
    public List<OutputEvent> unitProtocolMessages() {
        return units.protocolMessages();
    }

    // =====================================================================================
    // Quantities the controller reads
    // =====================================================================================

    /** Number of successive transmissions that carried STOP (capped at the stop limit). */
    @RoboChartType("nat")
    public int successiveStops() {
        return stopCount;
    }

    /** True when the current transmission is judged failed (message missing or aberrant). */
    public boolean transmissionFailure() {
        return transmissionFailed;
    }

    /** True while the water-level measuring device is out of order. */
    public boolean levelDeviceFailed() {
        return units.outOfOrder(UnitStatusBoard.LEVEL_UNIT);
    }

    /** True while the steam measuring device is out of order. */
    public boolean steamDeviceFailed() {
        return units.outOfOrder(UnitStatusBoard.STEAM_UNIT);
    }

    /** True while some pump is out of order. */
    public boolean pumpFailurePresent() {
        return units.anyPumpOutOfOrder();
    }

    /** True while some pump controller is out of order. */
    public boolean pumpControlFailurePresent() {
        return units.anyPumpControlOutOfOrder();
    }

    /** True once STEAM_BOILER_WAITING has arrived during initialization. */
    public boolean boilerWaitingReceived() {
        return boilerWaitingLatched;
    }

    /** True when the current transmission carries PHYSICAL_UNITS_READY. */
    public boolean unitsReadyReceived() {
        return unitsReadyPresent;
    }

    /** q: the water level most recently reported, litres. */
    @RoboChartType("real")
    public double waterLevel() {
        return waterLevelQ;
    }

    /** v: the steam rate most recently reported, litres per second. */
    @RoboChartType("real")
    public double steamRate() {
        return steamRateV;
    }

    /** p: throughput of the pumps reported on whose controller sees water flowing, litres per second. */
    @RoboChartType("real")
    public double pumpThroughput() {
        return pumpThroughputP;
    }

    /** Lower bound of the current water level (equal to q while the level device is sound), litres. */
    @RoboChartType("real")
    public double estimatedLevelLow() {
        return estimatedLow;
    }

    /** Upper bound of the current water level (equal to q while the level device is sound), litres. */
    @RoboChartType("real")
    public double estimatedLevelHigh() {
        return estimatedHigh;
    }

    /** Worst-case lowest level at the end of the coming cycle with the current pumps, litres. */
    @RoboChartType("real")
    public double levelForecastMin() {
        return forecastMin;
    }

    /** Worst-case highest level at the end of the coming cycle with the current pumps, litres. */
    @RoboChartType("real")
    public double levelForecastMax() {
        return forecastMax;
    }

    /** True while the evacuation valve is open. */
    public boolean valveOpen() {
        return valveIsOpen;
    }

    /** Number of sound pumps reported off, which could be opened. */
    @RoboChartType("nat")
    public int openablePumpCount() {
        int count = 0;
        for (int i = 0; i < PUMPS; i++) {
            if (!units.outOfOrder(UnitStatusBoard.pumpUnit(i)) && !receivedPumpOn[i]) {
                count = count + 1;
            }
        }
        return count;
    }

    /** Number of sound pumps reported on, which could be closed. */
    @RoboChartType("nat")
    public int closablePumpCount() {
        int count = 0;
        for (int i = 0; i < PUMPS; i++) {
            if (!units.outOfOrder(UnitStatusBoard.pumpUnit(i)) && receivedPumpOn[i]) {
                count = count + 1;
            }
        }
        return count;
    }

    /** Number of the lowest-numbered openable pump; 1 when none is openable. */
    @RoboChartType("nat")
    public int pumpToOpen() {
        int chosen = 1;
        boolean found = false;
        for (int i = 0; i < PUMPS; i++) {
            if (!found && !units.outOfOrder(UnitStatusBoard.pumpUnit(i)) && !receivedPumpOn[i]) {
                chosen = i + 1;
                found = true;
            }
        }
        return chosen;
    }

    /** Number of the highest-numbered closable pump; 4 when none is closable. */
    @RoboChartType("nat")
    public int pumpToClose() {
        int chosen = PUMPS;
        boolean found = false;
        for (int i = PUMPS - 1; i >= 0; i--) {
            if (!found && !units.outOfOrder(UnitStatusBoard.pumpUnit(i)) && receivedPumpOn[i]) {
                chosen = i + 1;
                found = true;
            }
        }
        return chosen;
    }
}
