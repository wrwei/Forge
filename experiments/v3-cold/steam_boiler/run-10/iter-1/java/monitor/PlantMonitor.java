package steam_boiler.monitor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.BoilerConstants;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.FlowReport;
import steam_boiler.domain.OperatingMode;
import steam_boiler.domain.PumpReport;
import steam_boiler.domain.WaterFlow;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * Receives the messages of each cycle, analyses them and publishes the result to the
 * {@link BoilerSensor} the controller reads (SB-V3). It keeps the program's variables q, v, p, the
 * level estimate, the unit status and the STOP count (SB-V5 to SB-V10), detects unit and
 * transmission failures (SB-FR1 to SB-FR5), and produces the failure announcements and repair
 * acknowledgements (SB-FR6, SB-FR7).
 */
public final class PlantMonitor {

    @RoboChartType("nat") private static final int PUMPS = BoilerConstants.PUMP_COUNT;
    @RoboChartType("real") private static final double DT = BoilerConstants.CYCLE_SECONDS;

    private final BoilerSensor sensor;

    // ---- the messages of the current cycle (receive stage) ----
    private boolean stopInCycle;
    private boolean waitingInCycle;
    private boolean unitsReadyInCycle;
    private boolean levelInCycle;
    @RoboChartType("real") private double levelValue;
    private boolean steamInCycle;
    @RoboChartType("real") private double steamValue;
    private final boolean[] pumpStateInCycle = new boolean[PUMPS];
    private final boolean[] pumpOnInCycle = new boolean[PUMPS];
    private final boolean[] controlStateInCycle = new boolean[PUMPS];
    private final boolean[] flowInCycle = new boolean[PUMPS];
    private final boolean[] pumpRepairedInCycle = new boolean[PUMPS];
    private final boolean[] controlRepairedInCycle = new boolean[PUMPS];
    private boolean levelRepairedInCycle;
    private boolean steamRepairedInCycle;
    private final boolean[] pumpAckInCycle = new boolean[PUMPS];
    private final boolean[] controlAckInCycle = new boolean[PUMPS];
    private boolean levelAckInCycle;
    private boolean steamAckInCycle;
    private boolean aberrantInCycle;

    // ---- what the program knows across cycles ----
    @RoboChartType("nat") private int successiveStops;
    private boolean waitingSignalled;
    private boolean transmissionFailure;
    private OperatingMode transmittedMode = OperatingMode.INITIALIZATION;
    private boolean programReadySent;
    private boolean valveOpen;
    @RoboChartType("real") private double levelQ;
    @RoboChartType("real") private double steamV;
    @RoboChartType("real") private double pumpThroughput;
    @RoboChartType("real") private double previousThroughput;
    @RoboChartType("real") private double estimateLow;
    @RoboChartType("real") private double estimateHigh;
    @RoboChartType("real") private double predictedLow;
    @RoboChartType("real") private double predictedHigh;
    private boolean predictionValid;
    private boolean steamBaseline;
    @RoboChartType("real") private double steamPrevious;
    private final boolean[] pumpExpectedOn = new boolean[PUMPS];
    private final boolean[] pumpOnPrevious = new boolean[PUMPS];
    private final UnitStatus[] pumpUnits = new UnitStatus[PUMPS];
    private final UnitStatus[] controlUnits = new UnitStatus[PUMPS];
    private final UnitStatus levelUnit = new UnitStatus();
    private final UnitStatus steamUnit = new UnitStatus();

    /**
     * Creates a monitor that publishes its analysis to the given sensor.
     *
     * @param sensor the sensor the controller reads
     */
    public PlantMonitor(BoilerSensor sensor) {
        this.sensor = sensor;
        for (int n = 0; n < PUMPS; n++) {
            pumpUnits[n] = new UnitStatus();
            controlUnits[n] = new UnitStatus();
        }
        this.estimateHigh = BoilerConstants.CAPACITY_C;
    }

    /** Starts the receive stage of a new cycle: no message received yet. */
    public void beginCycle() {
        stopInCycle = false;
        waitingInCycle = false;
        unitsReadyInCycle = false;
        levelInCycle = false;
        steamInCycle = false;
        levelRepairedInCycle = false;
        steamRepairedInCycle = false;
        levelAckInCycle = false;
        steamAckInCycle = false;
        aberrantInCycle = false;
        for (int n = 0; n < PUMPS; n++) {
            pumpStateInCycle[n] = false;
            pumpOnInCycle[n] = false;
            controlStateInCycle[n] = false;
            flowInCycle[n] = false;
            pumpRepairedInCycle[n] = false;
            controlRepairedInCycle[n] = false;
            pumpAckInCycle[n] = false;
            controlAckInCycle[n] = false;
        }
    }

    /**
     * Receives one incoming message of the current cycle. All messages of a cycle are considered
     * received simultaneously (SB-V3).
     *
     * @param message the message
     */
    public void receive(InputEvent message) {
        if (message instanceof InputEvent.StopRequest) {
            stopInCycle = true;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            waitingInCycle = true;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            unitsReadyInCycle = true;
        } else if (message instanceof InputEvent.PumpState) {
            InputEvent.PumpState pumpState = (InputEvent.PumpState) message;
            receivePumpState(pumpState.report());
        } else if (message instanceof InputEvent.PumpControlState) {
            InputEvent.PumpControlState controlState = (InputEvent.PumpControlState) message;
            receiveControlState(controlState.report());
        } else if (message instanceof InputEvent.Level) {
            InputEvent.Level level = (InputEvent.Level) message;
            if (levelInCycle) {
                aberrantInCycle = true;
            }
            levelInCycle = true;
            levelValue = level.litres();
        } else if (message instanceof InputEvent.Steam) {
            InputEvent.Steam steam = (InputEvent.Steam) message;
            if (steamInCycle) {
                aberrantInCycle = true;
            }
            steamInCycle = true;
            steamValue = steam.litresPerSecond();
        } else if (message instanceof InputEvent.PumpRepaired) {
            InputEvent.PumpRepaired repaired = (InputEvent.PumpRepaired) message;
            markPump(pumpRepairedInCycle, repaired.pumpNumber());
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired repaired = (InputEvent.PumpControlRepaired) message;
            markPump(controlRepairedInCycle, repaired.pumpNumber());
        } else if (message instanceof InputEvent.LevelRepaired) {
            levelRepairedInCycle = true;
        } else if (message instanceof InputEvent.SteamRepaired) {
            steamRepairedInCycle = true;
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement ack =
                    (InputEvent.PumpFailureAcknowledgement) message;
            markPump(pumpAckInCycle, ack.pumpNumber());
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement ack =
                    (InputEvent.PumpControlFailureAcknowledgement) message;
            markPump(controlAckInCycle, ack.pumpNumber());
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            levelAckInCycle = true;
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            steamAckInCycle = true;
        }
    }

    /**
     * The analysis stage: updates the STOP count, detects failures, advances the level estimate,
     * predicts the worst-case level of the next cycle, and publishes the result to the sensor.
     * Before STEAM_BOILER_WAITING has arrived only the STOP count is kept (SB-Beh2).
     */
    public void analyse() {
        if (stopInCycle) {
            successiveStops = successiveStops + 1;
        } else {
            successiveStops = 0;
        }
        if (waitingInCycle && transmittedMode == OperatingMode.INITIALIZATION) {
            waitingSignalled = true;
        }
        transmissionFailure = false;
        if (waitingSignalled) {
            transmissionFailure = detectTransmissionFailure();
            if (!transmissionFailure) {
                transmissionFailure = !processRepairsAndAcknowledgements();
            }
            if (!transmissionFailure) {
                detectSteamFailure();
                detectPumpFailures();
                detectPumpControlFailures();
                computeThroughput();
                detectLevelFailure();
                estimateLevel();
                predictLevel();
            }
        }
        publish();
    }

    /**
     * The failure announcements that still await acknowledgement and the repair acknowledgements
     * due in this cycle (SB-FR6, SB-FR7).
     *
     * @return the messages to add to this cycle's transmission
     */
    public List<OutputEvent> unitStatusMessages() {
        List<OutputEvent> messages = new ArrayList<>();
        for (int n = 0; n < PUMPS; n++) {
            if (pumpUnits[n].isAnnouncementPending()) {
                messages.add(new OutputEvent.PumpFailureDetection(n));
            }
            if (controlUnits[n].isAnnouncementPending()) {
                messages.add(new OutputEvent.PumpControlFailureDetection(n));
            }
            if (pumpUnits[n].takeRepairAcknowledgement()) {
                messages.add(new OutputEvent.PumpRepairedAcknowledgement(n));
            }
            if (controlUnits[n].takeRepairAcknowledgement()) {
                messages.add(new OutputEvent.PumpControlRepairedAcknowledgement(n));
            }
        }
        if (levelUnit.isAnnouncementPending()) {
            messages.add(new OutputEvent.LevelFailureDetection());
        }
        if (steamUnit.isAnnouncementPending()) {
            messages.add(new OutputEvent.SteamFailureDetection());
        }
        if (levelUnit.takeRepairAcknowledgement()) {
            messages.add(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (steamUnit.takeRepairAcknowledgement()) {
            messages.add(new OutputEvent.SteamRepairedAcknowledgement());
        }
        return messages;
    }

    /**
     * Learns what the program transmitted in this cycle: its mode, PROGRAM_READY, the valve and
     * the pump orders. The orders set what each pump must report in the next transmission (SB-FR1).
     *
     * @param sent the messages transmitted in this cycle
     */
    public void recordTransmission(List<OutputEvent> sent) {
        if (waitingSignalled) {
            for (int n = 0; n < PUMPS; n++) {
                pumpExpectedOn[n] = pumpOnInCycle[n];
                pumpOnPrevious[n] = pumpOnInCycle[n];
            }
        }
        for (OutputEvent message : sent) {
            if (message instanceof OutputEvent.ModeReport) {
                OutputEvent.ModeReport report = (OutputEvent.ModeReport) message;
                transmittedMode = report.operatingMode();
            } else if (message instanceof OutputEvent.ProgramReady) {
                programReadySent = true;
            } else if (message instanceof OutputEvent.Valve) {
                valveOpen = !valveOpen;
            } else if (message instanceof OutputEvent.OpenPump) {
                OutputEvent.OpenPump order = (OutputEvent.OpenPump) message;
                if (validPump(order.pumpNumber())) {
                    pumpExpectedOn[order.pumpNumber()] = true;
                }
            } else if (message instanceof OutputEvent.ClosePump) {
                OutputEvent.ClosePump order = (OutputEvent.ClosePump) message;
                if (validPump(order.pumpNumber())) {
                    pumpExpectedOn[order.pumpNumber()] = false;
                }
            }
        }
        if (valveOpen) {
            // The valve's drainage rate is not known: the level may fall arbitrarily.
            predictedLow = 0.0;
        }
    }

    private void receivePumpState(PumpReport report) {
        if (report == null || !validPump(report.pumpIndex())
                || pumpStateInCycle[report.pumpIndex()]) {
            aberrantInCycle = true;
            return;
        }
        pumpStateInCycle[report.pumpIndex()] = true;
        pumpOnInCycle[report.pumpIndex()] = report.pumpOn();
    }

    private void receiveControlState(FlowReport report) {
        if (report == null || !validPump(report.controllerIndex())
                || controlStateInCycle[report.controllerIndex()]) {
            aberrantInCycle = true;
            return;
        }
        controlStateInCycle[report.controllerIndex()] = true;
        flowInCycle[report.controllerIndex()] = report.flow() == WaterFlow.WATER_FLOWING;
    }

    private void markPump(boolean[] received, int pumpNumber) {
        if (validPump(pumpNumber)) {
            received[pumpNumber] = true;
        } else {
            aberrantInCycle = true;
        }
    }

    private static boolean validPump(int pumpNumber) {
        return pumpNumber >= 0 && pumpNumber < PUMPS;
    }

    /* SB-FR5: an aberrant message, or a missing data message. */
    private boolean detectTransmissionFailure() {
        boolean failed = aberrantInCycle;
        boolean initialising = transmittedMode == OperatingMode.INITIALIZATION;
        if (waitingInCycle && !initialising) {
            failed = true;
        }
        if (unitsReadyInCycle && (!initialising || !programReadySent)) {
            failed = true;
        }
        if (!levelInCycle || !steamInCycle) {
            failed = true;
        }
        for (int n = 0; n < PUMPS; n++) {
            if (!pumpStateInCycle[n] || !controlStateInCycle[n]) {
                failed = true;
            }
        }
        return failed;
    }

    /*
     * SB-FR6 and SB-FR7: acknowledgements end the repetition of failure announcements; repair
     * reports make the unit sound again.
     *
     * @return false if a unit reported a repair while it was not out of order (aberrant, SB-FR5)
     */
    private boolean processRepairsAndAcknowledgements() {
        boolean consistent = true;
        for (int n = 0; n < PUMPS; n++) {
            if (pumpAckInCycle[n]) {
                pumpUnits[n].acknowledgeFailure();
            }
            if (controlAckInCycle[n]) {
                controlUnits[n].acknowledgeFailure();
            }
            if (pumpRepairedInCycle[n] && !pumpUnits[n].markRepaired()) {
                consistent = false;
            }
            if (controlRepairedInCycle[n] && !controlUnits[n].markRepaired()) {
                consistent = false;
            }
        }
        if (levelAckInCycle) {
            levelUnit.acknowledgeFailure();
        }
        if (steamAckInCycle) {
            steamUnit.acknowledgeFailure();
        }
        if (levelRepairedInCycle && !levelUnit.markRepaired()) {
            consistent = false;
        }
        if (steamRepairedInCycle) {
            if (steamUnit.markRepaired()) {
                steamBaseline = false;
            } else {
                consistent = false;
            }
        }
        return consistent;
    }

    /* SB-FR4: outside [0, W], or changing faster than U1 (increase) or U2 (decrease) allow. */
    private void detectSteamFailure() {
        steamV = steamValue;
        if (steamUnit.isOutOfOrder()) {
            steamBaseline = false;
            return;
        }
        boolean outOfRange = steamValue < 0.0 || steamValue > BoilerConstants.STEAM_MAX_W;
        boolean tooFast = steamBaseline
                && (steamValue > steamPrevious + BoilerConstants.STEAM_RISE_U1 * DT
                    || steamValue < steamPrevious - BoilerConstants.STEAM_FALL_U2 * DT);
        if (outOfRange || tooFast) {
            steamUnit.detectFailure();
            steamBaseline = false;
        } else {
            steamPrevious = steamValue;
            steamBaseline = true;
        }
    }

    /*
     * SB-FR1: a pump must report the state it was last ordered to; without an order, the state it
     * reported before (a change is spontaneous).
     */
    private void detectPumpFailures() {
        for (int n = 0; n < PUMPS; n++) {
            if (!pumpUnits[n].isOutOfOrder() && !pumpRepairedInCycle[n]
                    && pumpOnInCycle[n] != pumpExpectedOn[n]) {
                pumpUnits[n].detectFailure();
            }
        }
    }

    /*
     * SB-FR2: once its sound pump has kept the same state over two transmissions (the second
     * transmission after an order, the pump needing one cycle to start, SB-Beh25), a controller
     * must report flow exactly when the pump is on; any other change is spontaneous.
     */
    private void detectPumpControlFailures() {
        for (int n = 0; n < PUMPS; n++) {
            boolean judged = !controlUnits[n].isOutOfOrder() && !controlRepairedInCycle[n]
                    && !pumpUnits[n].isOutOfOrder();
            boolean pumpSteady = pumpOnInCycle[n] == pumpOnPrevious[n];
            if (judged && pumpSteady && flowInCycle[n] != pumpOnInCycle[n]) {
                controlUnits[n].detectFailure();
            }
        }
    }

    /* SB-V7: p, from the controllers' flow reports, or the pump states where a controller is down. */
    private void computeThroughput() {
        previousThroughput = pumpThroughput;
        int pouring = 0;
        for (int n = 0; n < PUMPS; n++) {
            if (!controlUnits[n].isOutOfOrder()) {
                if (flowInCycle[n]) {
                    pouring = pouring + 1;
                }
            } else if (!pumpUnits[n].isOutOfOrder() && pumpOnInCycle[n] && pumpOnPrevious[n]) {
                pouring = pouring + 1;
            }
        }
        pumpThroughput = BoilerConstants.PUMP_CAPACITY_P * pouring;
    }

    /*
     * SB-FR3: outside [0, C], or outside the interval the level could have reached since the
     * previous cycle under extreme steam and pump behaviour.
     */
    private void detectLevelFailure() {
        levelQ = levelValue;
        if (levelUnit.isOutOfOrder()) {
            return;
        }
        boolean outOfRange = levelValue < 0.0 || levelValue > BoilerConstants.CAPACITY_C;
        boolean incompatible = predictionValid
                && (levelValue < predictedLow || levelValue > predictedHigh);
        if (outOfRange || incompatible) {
            levelUnit.detectFailure();
        }
    }

    /*
     * SB-V8, SB-Beh19: the level interval is the reading while the device is sound; otherwise the
     * previous worst-case prediction, its lower bound raised by the water the pumps are known to
     * have supplied (one litre pumped is one litre of boiler content).
     */
    private void estimateLevel() {
        if (!levelUnit.isOutOfOrder()) {
            estimateLow = levelValue;
            estimateHigh = levelValue;
        } else if (predictionValid) {
            double supplied = DT * Math.min(previousThroughput, pumpThroughput);
            estimateHigh = predictedHigh;
            estimateLow = Math.min(predictedLow + supplied, estimateHigh);
        } else {
            estimateLow = 0.0;
            estimateHigh = BoilerConstants.CAPACITY_C;
        }
    }

    /*
     * SB-Beh13, SB-Beh25: the lowest and highest level reachable by the next cycle. Steam may evolve
     * at its maximal gradients (anywhere in [0, W] if the steam device is down); every pump now on
     * may pour for the whole cycle or stop; a pump not yet on pours nothing within the next cycle,
     * because it needs one cycle to start.
     */
    private void predictLevel() {
        double steamOutMax;
        double steamOutMin;
        if (steamUnit.isOutOfOrder()) {
            steamOutMax = BoilerConstants.STEAM_MAX_W * DT;
            steamOutMin = 0.0;
        } else {
            steamOutMax = Math.min(BoilerConstants.STEAM_MAX_W * DT,
                    steamValue * DT + BoilerConstants.STEAM_RISE_U1 * DT * DT / 2.0);
            steamOutMin = Math.max(0.0,
                    steamValue * DT - BoilerConstants.STEAM_FALL_U2 * DT * DT / 2.0);
        }
        int possiblyPouring = 0;
        for (int n = 0; n < PUMPS; n++) {
            if (pumpOnInCycle[n] || pumpUnits[n].isOutOfOrder()) {
                possiblyPouring = possiblyPouring + 1;
            }
        }
        double inflowMax = BoilerConstants.PUMP_CAPACITY_P * DT * possiblyPouring;
        predictedLow = Math.max(0.0, estimateLow - steamOutMax);
        predictedHigh = Math.min(BoilerConstants.CAPACITY_C,
                estimateHigh + inflowMax - steamOutMin);
        if (predictedHigh < predictedLow) {
            predictedHigh = predictedLow;
        }
        predictionValid = true;
    }

    private void publish() {
        boolean controlDown = false;
        boolean anyDown = levelUnit.isOutOfOrder() || steamUnit.isOutOfOrder();
        for (int n = 0; n < PUMPS; n++) {
            if (controlUnits[n].isOutOfOrder()) {
                controlDown = true;
            }
            if (pumpUnits[n].isOutOfOrder() || controlUnits[n].isOutOfOrder()) {
                anyDown = true;
            }
        }
        sensor.updateMessages(successiveStops, transmissionFailure, waitingSignalled,
                unitsReadyInCycle, valveOpen);
        sensor.updateUnits(levelUnit.isOutOfOrder(), steamUnit.isOutOfOrder(), controlDown,
                !anyDown);
        sensor.updateLevels(levelQ, steamV, (estimateLow + estimateHigh) / 2.0, predictedLow,
                predictedHigh);
    }
}
