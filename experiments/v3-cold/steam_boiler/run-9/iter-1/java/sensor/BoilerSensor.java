package steam_boiler.sensor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.BoilerConstants;
import steam_boiler.domain.PumpControlReading;
import steam_boiler.domain.PumpReading;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.BoilerMode;

/**
 * Reception and analysis stage of the program (SB-V3). Each cycle the
 * transmission system calls {@link #startCycle()}, then {@link #receive} once
 * per incoming message, then {@link #analyse()}. The analysis judges unit
 * failures (SB-FR1..SB-FR5), keeps the per-unit status (SB-V9), the STOP count
 * (SB-V10), the quantities q, v, p (SB-V5..SB-V7) and the level bounds (SB-V8).
 * Pumps are numbered 1..PUMP_COUNT.
 */
public final class BoilerSensor {

    private static final int PUMPS = BoilerConstants.PUMP_COUNT;
    private static final int SETTLED = 3;

    /** Status of one physical unit (SB-V9). */
    private static final class UnitStatus {
        private boolean outOfOrder = false;
        private boolean failureAcknowledged = true;
        private boolean repairAcknowledgementDue = false;
        private boolean repairedReceived = false;
        private boolean failureAckReceived = false;

        private void clearCycle() {
            repairAcknowledgementDue = false;
            repairedReceived = false;
            failureAckReceived = false;
        }

        private void markFailed() {
            outOfOrder = true;
            failureAcknowledged = false;
        }

        private boolean announcementPending() {
            return outOfOrder && !failureAcknowledged;
        }
    }

    private final UnitStatus[] pumpUnits = new UnitStatus[PUMPS];
    private final UnitStatus[] controlUnits = new UnitStatus[PUMPS];
    private final UnitStatus levelUnit = new UnitStatus();
    private final UnitStatus steamUnit = new UnitStatus();

    // Messages of the current transmission.
    private boolean stopReceived = false;
    private boolean waitingReceived = false;
    private boolean unitsReadyReceived = false;
    private boolean aberrantMessage = false;
    @RoboChartType("nat") private int levelMessages = 0;
    @RoboChartType("nat") private int steamMessages = 0;
    @RoboChartType("real") private double levelValue = 0.0;
    @RoboChartType("real") private double steamValue = 0.0;
    private final int[] pumpStateMessages = new int[PUMPS];
    private final boolean[] pumpReportedOn = new boolean[PUMPS];
    private final int[] controlStateMessages = new int[PUMPS];
    private final boolean[] controlReportedFlow = new boolean[PUMPS];

    // What the program itself transmitted (recorded by the actuator).
    private BoilerMode modeSentPrevious = BoilerMode.INITIALIZATION;
    private BoilerMode modeSentCurrent = BoilerMode.INITIALIZATION;
    private boolean programReadySentPrevious = false;
    private boolean programReadySentCurrent = false;
    private boolean valveOpen = false;
    private boolean valveOpenDuringCycle = false;
    private final boolean[] pumpCommanded = new boolean[PUMPS];
    private final boolean[] pumpOrderedCurrent = new boolean[PUMPS];
    private final int[] cyclesSinceOrder = new int[PUMPS];

    // Derived quantities.
    /** q: water level most recently reported (SB-V5). */
    @RoboChartType("real") private double q = 0.0;
    /** v: steam rate most recently reported (SB-V6). */
    @RoboChartType("real") private double v = 0.0;
    /** p: current throughput of the pumps taken together (SB-V7). */
    @RoboChartType("real") private double p = 0.0;
    /** Lower bound on the true water level (SB-V8). */
    @RoboChartType("real") private double levelLow = 0.0;
    /** Upper bound on the true water level (SB-V8). */
    @RoboChartType("real") private double levelHigh = 0.0;
    @RoboChartType("real") private double steamRateLow = 0.0;
    @RoboChartType("real") private double steamRateHigh = 0.0;
    @RoboChartType("real") private double predictedLow = 0.0;
    @RoboChartType("real") private double predictedHigh = 0.0;
    /** Number of successive transmissions that carried STOP (SB-V10). */
    @RoboChartType("nat") private int stopCount = 0;
    private boolean transmissionFailed = false;
    private boolean boundsKnown = false;
    private final boolean[] certainFlowPrevious = new boolean[PUMPS];
    private final boolean[] couldPourPrevious = new boolean[PUMPS];

    /** Creates the sensor stage with every unit sound and every pump commanded off. */
    public BoilerSensor() {
        for (int i = 0; i < PUMPS; i++) {
            pumpUnits[i] = new UnitStatus();
            controlUnits[i] = new UnitStatus();
            cyclesSinceOrder[i] = SETTLED;
        }
        steamRateHigh = BoilerConstants.MAX_STEAM_W;
        levelHigh = BoilerConstants.CAPACITY_C;
    }

    /** Begins the reception stage of a new cycle. */
    public void startCycle() {
        modeSentPrevious = modeSentCurrent;
        programReadySentPrevious = programReadySentCurrent;
        programReadySentCurrent = false;
        valveOpenDuringCycle = valveOpen;
        for (int i = 0; i < PUMPS; i++) {
            if (pumpOrderedCurrent[i]) {
                cyclesSinceOrder[i] = 1;
            } else if (cyclesSinceOrder[i] < SETTLED) {
                cyclesSinceOrder[i] = cyclesSinceOrder[i] + 1;
            }
            pumpOrderedCurrent[i] = false;
            pumpStateMessages[i] = 0;
            controlStateMessages[i] = 0;
            pumpUnits[i].clearCycle();
            controlUnits[i].clearCycle();
        }
        levelUnit.clearCycle();
        steamUnit.clearCycle();
        stopReceived = false;
        waitingReceived = false;
        unitsReadyReceived = false;
        aberrantMessage = false;
        levelMessages = 0;
        steamMessages = 0;
    }

    /** Records one incoming message of the current transmission. */
    public void receive(InputEvent message) {
        if (message instanceof InputEvent.StopRequest) {
            stopReceived = true;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            waitingReceived = true;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            unitsReadyReceived = true;
        } else if (message instanceof InputEvent.PumpState) {
            InputEvent.PumpState ps = (InputEvent.PumpState) message;
            PumpReading reading = ps.reading();
            if (validPump(reading.pumpNumber())) {
                int i = reading.pumpNumber() - 1;
                pumpStateMessages[i] = pumpStateMessages[i] + 1;
                pumpReportedOn[i] = reading.switchedOn();
            } else {
                aberrantMessage = true;
            }
        } else if (message instanceof InputEvent.PumpControlState) {
            InputEvent.PumpControlState pcs = (InputEvent.PumpControlState) message;
            PumpControlReading reading = pcs.reading();
            if (validPump(reading.controlledPump())) {
                int i = reading.controlledPump() - 1;
                controlStateMessages[i] = controlStateMessages[i] + 1;
                controlReportedFlow[i] = reading.waterFlowing();
            } else {
                aberrantMessage = true;
            }
        } else if (message instanceof InputEvent.Level) {
            InputEvent.Level lv = (InputEvent.Level) message;
            levelMessages = levelMessages + 1;
            levelValue = lv.levelValue();
        } else if (message instanceof InputEvent.Steam) {
            InputEvent.Steam st = (InputEvent.Steam) message;
            steamMessages = steamMessages + 1;
            steamValue = st.steamValue();
        } else if (message instanceof InputEvent.PumpRepaired) {
            InputEvent.PumpRepaired pr = (InputEvent.PumpRepaired) message;
            noteRepaired(pumpUnits, pr.pump());
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired pcr = (InputEvent.PumpControlRepaired) message;
            noteRepaired(controlUnits, pcr.pump());
        } else if (message instanceof InputEvent.LevelRepaired) {
            levelUnit.repairedReceived = true;
        } else if (message instanceof InputEvent.SteamRepaired) {
            steamUnit.repairedReceived = true;
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement pfa = (InputEvent.PumpFailureAcknowledgement) message;
            noteFailureAck(pumpUnits, pfa.pump());
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement pcfa =
                    (InputEvent.PumpControlFailureAcknowledgement) message;
            noteFailureAck(controlUnits, pcfa.pump());
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            levelUnit.failureAckReceived = true;
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            steamUnit.failureAckReceived = true;
        }
    }

    /** Analyses the messages of the current transmission (SB-FR1..SB-FR5, SB-V5..SB-V10). */
    public void analyse() {
        // SB-V10: successive transmissions carrying STOP.
        if (stopReceived) {
            if (stopCount < BoilerConstants.STOP_REPETITIONS) {
                stopCount = stopCount + 1;
            }
        } else {
            stopCount = 0;
        }

        // SB-FR5: aberrant presence of a message.
        if (waitingReceived && modeSentPrevious != BoilerMode.INITIALIZATION) {
            aberrantMessage = true;
        }
        if (unitsReadyReceived
                && !(modeSentPrevious == BoilerMode.INITIALIZATION && programReadySentPrevious)) {
            aberrantMessage = true;
        }

        // SB-FR6 acknowledgements, then SB-FR7 repairs.
        acknowledge(levelUnit);
        acknowledge(steamUnit);
        for (int i = 0; i < PUMPS; i++) {
            acknowledge(pumpUnits[i]);
            acknowledge(controlUnits[i]);
        }
        repair(levelUnit);
        repair(steamUnit);
        for (int i = 0; i < PUMPS; i++) {
            boolean pumpBackInService = pumpUnits[i].repairedReceived && pumpUnits[i].outOfOrder;
            repair(pumpUnits[i]);
            repair(controlUnits[i]);
            if (pumpBackInService && pumpStateMessages[i] == 1) {
                // A repaired pump's reported state becomes the new commanded state.
                pumpCommanded[i] = pumpReportedOn[i];
                cyclesSinceOrder[i] = 1;
            }
        }

        // SB-FR5: indispensable messages present exactly once.
        boolean dataComplete = levelMessages == 1 && steamMessages == 1;
        for (int i = 0; i < PUMPS; i++) {
            dataComplete = dataComplete && pumpStateMessages[i] == 1 && controlStateMessages[i] == 1;
        }
        transmissionFailed = aberrantMessage || !dataComplete;
        if (dataComplete) {
            analyseReadings();
        }
    }

    private void analyseReadings() {
        double dt = BoilerConstants.CYCLE_SECONDS;
        double previousSteamLow = steamRateLow;
        double previousSteamHigh = steamRateHigh;

        // SB-FR4: steam device, static range and gradients U1, U2.
        v = steamValue;
        boolean steamStaticBad = v < 0.0 || v > BoilerConstants.MAX_STEAM_W;
        boolean steamDynamicBad = v > previousSteamHigh + BoilerConstants.STEAM_INCREASE_U1 * dt
                || v < previousSteamLow - BoilerConstants.STEAM_DECREASE_U2 * dt;
        if (!steamUnit.outOfOrder && (steamStaticBad || steamDynamicBad)) {
            steamUnit.markFailed();
        }
        if (!steamUnit.outOfOrder) {
            steamRateLow = v;
            steamRateHigh = v;
        } else {
            steamRateLow = Math.max(0.0, previousSteamLow - BoilerConstants.STEAM_DECREASE_U2 * dt);
            steamRateHigh = Math.min(BoilerConstants.MAX_STEAM_W,
                    previousSteamHigh + BoilerConstants.STEAM_INCREASE_U1 * dt);
        }

        // SB-FR1: a pump whose state differs from the last order (or changed spontaneously).
        for (int i = 0; i < PUMPS; i++) {
            if (!pumpUnits[i].outOfOrder && pumpReportedOn[i] != pumpCommanded[i]) {
                pumpUnits[i].markFailed();
            }
        }
        // SB-FR2: a controller judged from the second transmission after an order on.
        for (int i = 0; i < PUMPS; i++) {
            if (!controlUnits[i].outOfOrder && !pumpUnits[i].outOfOrder
                    && cyclesSinceOrder[i] >= 2 && controlReportedFlow[i] != pumpCommanded[i]) {
                controlUnits[i].markFailed();
            }
        }

        // SB-V7 and SB-Beh25: which pumps pour water, honouring the start-up latency.
        int certainCount = 0;
        int bothCycleCount = 0;
        int couldPourPreviousCount = 0;
        int couldPourNowCount = 0;
        for (int i = 0; i < PUMPS; i++) {
            boolean known = true;
            boolean flowing = false;
            if (!controlUnits[i].outOfOrder) {
                flowing = controlReportedFlow[i];
            } else if (!pumpUnits[i].outOfOrder) {
                flowing = pumpReportedOn[i] && cyclesSinceOrder[i] >= 2;
            } else {
                known = false;
            }
            boolean certainNow = known && flowing;
            boolean couldPourNow = !known || flowing || pumpReportedOn[i];
            if (certainNow) {
                certainCount = certainCount + 1;
            }
            if (certainNow && certainFlowPrevious[i]) {
                bothCycleCount = bothCycleCount + 1;
            }
            if (couldPourPrevious[i] || couldPourNow) {
                couldPourPreviousCount = couldPourPreviousCount + 1;
            }
            if (couldPourNow) {
                couldPourNowCount = couldPourNowCount + 1;
            }
            certainFlowPrevious[i] = certainNow;
            couldPourPrevious[i] = couldPourNow;
        }
        p = BoilerConstants.PUMP_CAPACITY_P * certainCount;
        double pumpVolumeMin = BoilerConstants.PUMP_CAPACITY_P * bothCycleCount * dt;
        double pumpVolumeMax = BoilerConstants.PUMP_CAPACITY_P * couldPourPreviousCount * dt;

        // SB-FR3: level device, static range and dynamics since the previous cycle.
        q = levelValue;
        double expectedLow = levelLow - maxSteamVolume(previousSteamHigh) + pumpVolumeMin;
        double expectedHigh = levelHigh - minSteamVolume(previousSteamLow) + pumpVolumeMax;
        boolean levelStaticBad = q < 0.0 || q > BoilerConstants.CAPACITY_C;
        boolean levelDynamicBad = boundsKnown && !valveOpenDuringCycle
                && (q < expectedLow || q > expectedHigh);
        if (!levelUnit.outOfOrder && (levelStaticBad || levelDynamicBad)) {
            levelUnit.markFailed();
        }

        // SB-V8: level bounds, from the reading or from worst-case dynamics.
        if (!levelUnit.outOfOrder) {
            levelLow = q;
            levelHigh = q;
        } else if (boundsKnown) {
            levelLow = Math.max(0.0, expectedLow);
            levelHigh = Math.min(BoilerConstants.CAPACITY_C, expectedHigh);
        } else {
            levelLow = 0.0;
            levelHigh = BoilerConstants.CAPACITY_C;
        }
        boundsKnown = true;

        // SB-V4, SB-Beh13: worst-case level at the next cycle. Pumps may stop
        // (none counted for the low bound); pumps that are on or uncertain may pour.
        predictedLow = levelLow - maxSteamVolume(steamRateHigh);
        predictedHigh = levelHigh - minSteamVolume(steamRateLow)
                + BoilerConstants.PUMP_CAPACITY_P * couldPourNowCount * dt;
    }

    private static double maxSteamVolume(double rate) {
        double dt = BoilerConstants.CYCLE_SECONDS;
        return Math.min(BoilerConstants.MAX_STEAM_W * dt,
                rate * dt + 0.5 * BoilerConstants.STEAM_INCREASE_U1 * dt * dt);
    }

    private static double minSteamVolume(double rate) {
        double dt = BoilerConstants.CYCLE_SECONDS;
        return Math.max(0.0, rate * dt - 0.5 * BoilerConstants.STEAM_DECREASE_U2 * dt * dt);
    }

    private static boolean validPump(int pump) {
        return pump >= 1 && pump <= PUMPS;
    }

    private void noteRepaired(UnitStatus[] units, int pump) {
        if (validPump(pump)) {
            units[pump - 1].repairedReceived = true;
        } else {
            aberrantMessage = true;
        }
    }

    private void noteFailureAck(UnitStatus[] units, int pump) {
        if (validPump(pump)) {
            units[pump - 1].failureAckReceived = true;
        } else {
            aberrantMessage = true;
        }
    }

    private void acknowledge(UnitStatus unit) {
        if (unit.failureAckReceived) {
            if (unit.announcementPending()) {
                unit.failureAcknowledged = true;
            } else {
                aberrantMessage = true;
            }
        }
    }

    private void repair(UnitStatus unit) {
        if (unit.repairedReceived) {
            if (unit.outOfOrder) {
                unit.outOfOrder = false;
                unit.failureAcknowledged = true;
                unit.repairAcknowledgementDue = true;
            } else {
                aberrantMessage = true;
            }
        }
    }

    /** Records that the program ordered pump {@code pump} on or off in this cycle. */
    public void notePumpOrder(@RoboChartType("nat") int pump, boolean switchOn) {
        if (validPump(pump)) {
            pumpCommanded[pump - 1] = switchOn;
            pumpOrderedCurrent[pump - 1] = true;
        }
    }

    /** Records the MODE message sent in this cycle. */
    public void noteModeSent(BoilerMode mode) {
        modeSentCurrent = mode;
    }

    /** Records that PROGRAM_READY was sent in this cycle. */
    public void noteProgramReadySent() {
        programReadySentCurrent = true;
    }

    /** Records the evacuation valve position requested in this cycle. */
    public void noteValve(boolean open) {
        valveOpen = open;
    }

    /** Whether the last order to pump {@code pump} was to switch it on. */
    public boolean pumpCommandedOn(@RoboChartType("nat") int pump) {
        boolean commanded = false;
        if (validPump(pump)) {
            commanded = pumpCommanded[pump - 1];
        }
        return commanded;
    }

    /** Failure announcements still unacknowledged (SB-FR6) and repair acknowledgements due (SB-FR7). */
    public List<OutputEvent> statusMessagesDue() {
        List<OutputEvent> due = new ArrayList<>();
        for (int i = 0; i < PUMPS; i++) {
            if (pumpUnits[i].announcementPending()) {
                due.add(new OutputEvent.PumpFailureDetection(i + 1));
            }
            if (controlUnits[i].announcementPending()) {
                due.add(new OutputEvent.PumpControlFailureDetection(i + 1));
            }
            if (pumpUnits[i].repairAcknowledgementDue) {
                due.add(new OutputEvent.PumpRepairedAcknowledgement(i + 1));
            }
            if (controlUnits[i].repairAcknowledgementDue) {
                due.add(new OutputEvent.PumpControlRepairedAcknowledgement(i + 1));
            }
        }
        if (levelUnit.announcementPending()) {
            due.add(new OutputEvent.LevelFailureDetection());
        }
        if (steamUnit.announcementPending()) {
            due.add(new OutputEvent.SteamFailureDetection());
        }
        if (levelUnit.repairAcknowledgementDue) {
            due.add(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (steamUnit.repairAcknowledgementDue) {
            due.add(new OutputEvent.SteamRepairedAcknowledgement());
        }
        return due;
    }

    /** Number of successive transmissions that carried STOP (SB-V10). */
    @RoboChartType("nat")
    public int successiveStops() {
        return stopCount;
    }

    /** Whether the current transmission failed: an aberrant or a missing message (SB-FR5). */
    public boolean transmissionFailure() {
        return transmissionFailed;
    }

    /** Whether STEAM_BOILER_WAITING arrived in the current transmission. */
    public boolean steamBoilerWaitingReceived() {
        return waitingReceived;
    }

    /** Whether PHYSICAL_UNITS_READY arrived in the current transmission. */
    public boolean physicalUnitsReadyReceived() {
        return unitsReadyReceived;
    }

    /** q: water level most recently reported (SB-V5). */
    @RoboChartType("real")
    public double levelReading() {
        return q;
    }

    /** v: steam rate most recently reported (SB-V6). */
    @RoboChartType("real")
    public double steamReading() {
        return v;
    }

    /** p: current throughput of the pumps (SB-V7). */
    @RoboChartType("real")
    public double pumpThroughput() {
        return p;
    }

    /** Lower bound on the water level (SB-V8). */
    @RoboChartType("real")
    public double levelLowerBound() {
        return levelLow;
    }

    /** Upper bound on the water level (SB-V8). */
    @RoboChartType("real")
    public double levelUpperBound() {
        return levelHigh;
    }

    /** Level used for regulation: the reading, or the middle of the estimate (SB-V8, SB-Beh19). */
    @RoboChartType("real")
    public double levelEstimate() {
        return (levelLow + levelHigh) / 2.0;
    }

    /** Worst-case lowest level at the next cycle (SB-Beh13). */
    @RoboChartType("real")
    public double predictedLevelLow() {
        return predictedLow;
    }

    /** Worst-case highest level at the next cycle (SB-Beh13). */
    @RoboChartType("real")
    public double predictedLevelHigh() {
        return predictedHigh;
    }

    /** Whether the level measuring device is out of order (SB-FR3, SB-V9). */
    public boolean levelDeviceFailed() {
        return levelUnit.outOfOrder;
    }

    /** Whether the steam measuring device is out of order (SB-FR4, SB-V9). */
    public boolean steamDeviceFailed() {
        return steamUnit.outOfOrder;
    }

    /** Whether pump {@code pump} is out of order (SB-FR1, SB-V9). */
    public boolean pumpFailed(@RoboChartType("nat") int pump) {
        boolean failed = false;
        if (validPump(pump)) {
            failed = pumpUnits[pump - 1].outOfOrder;
        }
        return failed;
    }

    /** Whether the controller of pump {@code pump} is out of order (SB-FR2, SB-V9). */
    public boolean pumpControllerFailed(@RoboChartType("nat") int pump) {
        boolean failed = false;
        if (validPump(pump)) {
            failed = controlUnits[pump - 1].outOfOrder;
        }
        return failed;
    }

    /** Whether some pump is out of order. */
    public boolean anyPumpFailed() {
        boolean failed = false;
        for (int i = 0; i < PUMPS; i++) {
            failed = failed || pumpUnits[i].outOfOrder;
        }
        return failed;
    }

    /** Whether some pump controller is out of order. */
    public boolean anyPumpControllerFailed() {
        boolean failed = false;
        for (int i = 0; i < PUMPS; i++) {
            failed = failed || controlUnits[i].outOfOrder;
        }
        return failed;
    }
}
