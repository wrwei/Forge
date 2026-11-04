package steam_boiler.sensor;

import java.util.List;
import steam_boiler.BoilerConstants;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.OperatingMode;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.event.PumpPosition;
import steam_boiler.event.WaterFlow;

/**
 * Sensor service of the steam-boiler program. In the receive stage of a cycle
 * it collects the incoming messages; {@link #analyse()} then judges the units
 * and the transmission and derives the quantities the controller's guards
 * read; {@link #endCycle(List)} records what was transmitted, for the next
 * cycle's judgements.
 */
public final class BoilerSensor {
    // Receive stage: the messages of the current transmission.
    private boolean stopPresent;
    private boolean waitingPresent;
    private boolean unitsReadyPresent;
    private boolean levelPresent;
    private boolean steamPresent;
    @RoboChartType("real") private double receivedLevel;
    @RoboChartType("real") private double receivedSteam;
    private final boolean[] pumpStatePresent = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] reportedOn = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] pumpControlPresent = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] reportedFlow = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] pumpRepairedPresent = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] pumpControlRepairedPresent = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] pumpAckPresent = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] pumpControlAckPresent = new boolean[BoilerConstants.PUMP_COUNT];
    private boolean levelRepairedPresent;
    private boolean steamRepairedPresent;
    private boolean levelAckPresent;
    private boolean steamAckPresent;
    private boolean aberrantMessage;

    // Kept across cycles.
    @RoboChartType("nat") private int consecutiveStops;
    @RoboChartType("real") private double lastLevelReading;
    @RoboChartType("real") private double lastSteamReading;
    private boolean monitoring;
    private boolean programReadyTransmitted;
    private boolean valveOpened;
    private OperatingMode transmittedMode = OperatingMode.INITIALIZATION;
    private final UnitHealth[] pumps = new UnitHealth[BoilerConstants.PUMP_COUNT];
    private final UnitHealth[] pumpControls = new UnitHealth[BoilerConstants.PUMP_COUNT];
    private final UnitHealth levelDevice = new UnitHealth();
    private final UnitHealth steamDevice = new UnitHealth();
    private final boolean[] pumpBaselineKnown = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] expectedOn = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] pumpVerified = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] orderedLastCycle = new boolean[BoilerConstants.PUMP_COUNT];
    private boolean levelHistoryValid;
    @RoboChartType("real") private double checkLevelLow;
    @RoboChartType("real") private double checkLevelHigh;
    @RoboChartType("real") private double estimatedLow;
    @RoboChartType("real") private double estimatedHigh;
    private boolean steamHistoryValid;
    @RoboChartType("real") private double previousSteamLow;
    @RoboChartType("real") private double previousSteamHigh;

    // Derived in the analysis stage of the current cycle.
    private boolean transmissionFailure;
    @RoboChartType("real") private double levelLow;
    @RoboChartType("real") private double levelHigh;
    @RoboChartType("real") private double steamLow;
    @RoboChartType("real") private double steamHigh;
    @RoboChartType("real") private double throughput;
    @RoboChartType("real") private double riskLow;
    @RoboChartType("real") private double riskHigh;
    @RoboChartType("nat") private int runningPumps;
    @RoboChartType("nat") private int wantedPumps;
    @RoboChartType("nat") private int nextPumpToOpen = 1;
    @RoboChartType("nat") private int nextPumpToClose = 1;
    private boolean startableAvailable;
    private boolean stoppableAvailable;

    /** Creates a sensor service with every unit believed sound. */
    public BoilerSensor() {
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            pumps[i] = new UnitHealth();
            pumpControls[i] = new UnitHealth();
        }
    }

    /** Starts the receive stage of a new cycle. */
    public void beginCycle() {
        stopPresent = false;
        waitingPresent = false;
        unitsReadyPresent = false;
        levelPresent = false;
        steamPresent = false;
        levelRepairedPresent = false;
        steamRepairedPresent = false;
        levelAckPresent = false;
        steamAckPresent = false;
        aberrantMessage = false;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            pumpStatePresent[i] = false;
            reportedOn[i] = false;
            pumpControlPresent[i] = false;
            reportedFlow[i] = false;
            pumpRepairedPresent[i] = false;
            pumpControlRepairedPresent[i] = false;
            pumpAckPresent[i] = false;
            pumpControlAckPresent[i] = false;
            pumpVerified[i] = false;
            pumps[i].startCycle();
            pumpControls[i].startCycle();
        }
        levelDevice.startCycle();
        steamDevice.startCycle();
    }

    /** Records one incoming message of the current transmission. */
    public void receive(InputEvent message) {
        if (message instanceof InputEvent.StopRequest) {
            stopPresent = true;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            waitingPresent = true;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            unitsReadyPresent = true;
        } else if (message instanceof InputEvent.PumpStateReport) {
            InputEvent.PumpStateReport report = (InputEvent.PumpStateReport) message;
            receivePumpState(report);
        } else if (message instanceof InputEvent.PumpControlStateReport) {
            InputEvent.PumpControlStateReport report = (InputEvent.PumpControlStateReport) message;
            receivePumpControlState(report);
        } else if (message instanceof InputEvent.LevelReport) {
            InputEvent.LevelReport report = (InputEvent.LevelReport) message;
            if (levelPresent) {
                aberrantMessage = true;
            }
            levelPresent = true;
            receivedLevel = report.levelValue();
            lastLevelReading = receivedLevel;
        } else if (message instanceof InputEvent.SteamReport) {
            InputEvent.SteamReport report = (InputEvent.SteamReport) message;
            if (steamPresent) {
                aberrantMessage = true;
            }
            steamPresent = true;
            receivedSteam = report.steamValue();
            lastSteamReading = receivedSteam;
        } else if (message instanceof InputEvent.PumpRepaired) {
            InputEvent.PumpRepaired repaired = (InputEvent.PumpRepaired) message;
            markIndexed(pumpRepairedPresent, repaired.repairedPump());
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired repaired = (InputEvent.PumpControlRepaired) message;
            markIndexed(pumpControlRepairedPresent, repaired.repairedController());
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement ack = (InputEvent.PumpFailureAcknowledgement) message;
            markIndexed(pumpAckPresent, ack.acknowledgedPump());
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement ack =
                    (InputEvent.PumpControlFailureAcknowledgement) message;
            markIndexed(pumpControlAckPresent, ack.acknowledgedController());
        } else if (message instanceof InputEvent.LevelRepaired) {
            levelRepairedPresent = true;
        } else if (message instanceof InputEvent.SteamRepaired) {
            steamRepairedPresent = true;
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            levelAckPresent = true;
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            steamAckPresent = true;
        }
    }

    private void receivePumpState(InputEvent.PumpStateReport report) {
        if (report.pumpObservation() == null || !validPump(report.pumpObservation().pumpNumber())) {
            aberrantMessage = true;
            return;
        }
        int i = report.pumpObservation().pumpNumber() - 1;
        if (pumpStatePresent[i]) {
            aberrantMessage = true;
        }
        pumpStatePresent[i] = true;
        reportedOn[i] = report.pumpObservation().position() == PumpPosition.PUMP_ON;
    }

    private void receivePumpControlState(InputEvent.PumpControlStateReport report) {
        if (report.flowObservation() == null || !validPump(report.flowObservation().controlledPump())) {
            aberrantMessage = true;
            return;
        }
        int i = report.flowObservation().controlledPump() - 1;
        if (pumpControlPresent[i]) {
            aberrantMessage = true;
        }
        pumpControlPresent[i] = true;
        reportedFlow[i] = report.flowObservation().flow() == WaterFlow.WATER_FLOWING;
    }

    private void markIndexed(boolean[] present, int n) {
        if (!validPump(n) || present[n - 1]) {
            aberrantMessage = true;
            return;
        }
        present[n - 1] = true;
    }

    private static boolean validPump(int n) {
        return n >= 1 && n <= BoilerConstants.PUMP_COUNT;
    }

    /**
     * Analysis stage: counts successive STOPs, applies repairs and
     * acknowledgements, judges the transmission and every unit, and derives
     * the level, steam and pump quantities for the current cycle.
     */
    public void analyse() {
        if (stopPresent) {
            consecutiveStops = consecutiveStops + 1;
        } else {
            consecutiveStops = 0;
        }
        boolean initializing = transmittedMode == OperatingMode.INITIALIZATION;
        if (waitingPresent && !initializing) {
            aberrantMessage = true;
        }
        if (unitsReadyPresent && (!initializing || !programReadyTransmitted)) {
            aberrantMessage = true;
        }
        if (waitingPresent && initializing) {
            monitoring = true;
        }
        applyRepairsAndAcknowledgements();
        transmissionFailure = aberrantMessage || !levelPresent || !steamPresent || !allPumpDataPresent();
        if (monitoring) {
            judgePumps();
            judgePumpControls();
            judgeSteamDevice();
            judgeLevelDevice();
        }
        deriveQuantities();
    }

    private boolean allPumpDataPresent() {
        boolean complete = true;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            if (!pumpStatePresent[i] || !pumpControlPresent[i]) {
                complete = false;
            }
        }
        return complete;
    }

    private void applyRepairsAndAcknowledgements() {
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            applyAcknowledgement(pumps[i], pumpAckPresent[i]);
            applyAcknowledgement(pumpControls[i], pumpControlAckPresent[i]);
            applyRepair(pumps[i], pumpRepairedPresent[i]);
            applyRepair(pumpControls[i], pumpControlRepairedPresent[i]);
        }
        applyAcknowledgement(levelDevice, levelAckPresent);
        applyAcknowledgement(steamDevice, steamAckPresent);
        applyRepair(levelDevice, levelRepairedPresent);
        applyRepair(steamDevice, steamRepairedPresent);
    }

    private void applyAcknowledgement(UnitHealth unit, boolean received) {
        if (received) {
            if (unit.detectionPending()) {
                unit.acknowledgeDetection();
            } else {
                aberrantMessage = true;
            }
        }
    }

    private void applyRepair(UnitHealth unit, boolean reported) {
        if (reported) {
            if (unit.outOfOrder()) {
                unit.markRepaired();
            } else {
                aberrantMessage = true;
            }
        }
    }

    // SB-FR1: a pump fails if its state differs from the one ordered in the
    // previous transmission, or changes without an order.
    private void judgePumps() {
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            if (!pumps[i].outOfOrder() && pumpStatePresent[i]) {
                if (pumps[i].repairedThisCycle() || !pumpBaselineKnown[i]) {
                    pumpBaselineKnown[i] = true;
                } else if (reportedOn[i] != expectedOn[i]) {
                    pumps[i].judgeFailed();
                } else {
                    pumpVerified[i] = true;
                }
                expectedOn[i] = reportedOn[i];
            }
        }
    }

    // SB-FR2: from the second transmission after an order on, and whenever no
    // order is in progress, a controller whose pump is known to work must
    // report flow exactly when the pump is on.
    private void judgePumpControls() {
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            UnitHealth control = pumpControls[i];
            boolean checkable = !control.outOfOrder() && pumpControlPresent[i]
                    && !control.repairedThisCycle() && pumpVerified[i] && !orderedLastCycle[i];
            if (checkable && reportedFlow[i] != reportedOn[i]) {
                control.judgeFailed();
            }
        }
    }

    // SB-FR4: static range [0, W] and the gradients U1 and U2.
    private void judgeSteamDevice() {
        if (steamDevice.outOfOrder() || !steamPresent) {
            return;
        }
        boolean outOfRange = receivedSteam < 0.0 || receivedSteam > BoilerConstants.MAX_STEAM_RATE;
        boolean tooFast = false;
        if (steamHistoryValid && !steamDevice.repairedThisCycle()) {
            double rise = BoilerConstants.MAX_STEAM_INCREASE * BoilerConstants.CYCLE_SECONDS;
            double fall = BoilerConstants.MAX_STEAM_DECREASE * BoilerConstants.CYCLE_SECONDS;
            tooFast = receivedSteam > previousSteamHigh + rise || receivedSteam < previousSteamLow - fall;
        }
        if (outOfRange || tooFast) {
            steamDevice.judgeFailed();
        }
    }

    // SB-FR3: static range [0, C] and the extreme-dynamics bounds predicted in
    // the previous cycle.
    private void judgeLevelDevice() {
        if (levelDevice.outOfOrder() || !levelPresent) {
            return;
        }
        boolean outOfRange = receivedLevel < 0.0 || receivedLevel > BoilerConstants.CAPACITY;
        boolean unreachable = false;
        if (levelHistoryValid && !levelDevice.repairedThisCycle()) {
            unreachable = receivedLevel < checkLevelLow || receivedLevel > checkLevelHigh;
        }
        if (outOfRange || unreachable) {
            levelDevice.judgeFailed();
        }
    }

    private void deriveQuantities() {
        // Level: the reading while the device is trusted, else the estimate (SB-V8).
        if (!levelDevice.outOfOrder() && levelPresent) {
            levelLow = receivedLevel;
            levelHigh = receivedLevel;
        } else if (levelHistoryValid) {
            levelLow = estimatedLow;
            levelHigh = estimatedHigh;
        } else {
            levelLow = 0.0;
            levelHigh = BoilerConstants.CAPACITY;
        }
        // Steam: the reading while trusted, else advanced by the gradients (SB-Beh19).
        if (!steamDevice.outOfOrder() && steamPresent) {
            steamLow = receivedSteam;
            steamHigh = receivedSteam;
        } else if (steamHistoryValid) {
            steamLow = Math.max(0.0, previousSteamLow
                    - BoilerConstants.MAX_STEAM_DECREASE * BoilerConstants.CYCLE_SECONDS);
            steamHigh = Math.min(BoilerConstants.MAX_STEAM_RATE, previousSteamHigh
                    + BoilerConstants.MAX_STEAM_INCREASE * BoilerConstants.CYCLE_SECONDS);
        } else {
            steamLow = 0.0;
            steamHigh = BoilerConstants.MAX_STEAM_RATE;
        }
        // Pumps (SB-V7).
        double supplyKnown = 0.0;
        double supplyHigh = 0.0;
        throughput = 0.0;
        runningPumps = 0;
        startableAvailable = false;
        stoppableAvailable = false;
        nextPumpToOpen = 1;
        nextPumpToClose = 1;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            supplyKnown = supplyKnown + pumpSupplyKnown(i);
            supplyHigh = supplyHigh + pumpSupplyHigh(i);
            if (pumpStatePresent[i] && reportedOn[i]) {
                runningPumps = runningPumps + 1;
                if (pumpControlPresent[i] && reportedFlow[i]) {
                    throughput = throughput + BoilerConstants.PUMP_CAPACITY;
                }
            }
            if (pumpTrusted(i) && reportedOn[i]) {
                stoppableAvailable = true;
                nextPumpToClose = i + 1;
            }
            if (pumpTrusted(i) && !reportedOn[i] && !startableAvailable) {
                startableAvailable = true;
                nextPumpToOpen = i + 1;
            }
        }
        // Worst case over the next cycle (SB-V4, SB-Beh13): only the water known
        // to be supplied, a pump that is off pours nothing (SB-Beh25), steam
        // moves at U1 or U2.
        riskLow = Math.min(levelLow,
                levelLow + supplyKnown * BoilerConstants.CYCLE_SECONDS - steamOutflowMax(steamHigh));
        riskHigh = Math.max(levelHigh,
                levelHigh + supplyHigh * BoilerConstants.CYCLE_SECONDS - steamOutflowMin(steamLow));
        wantedPumps = pumpsForTarget();
    }

    private boolean pumpTrusted(int i) {
        return !pumps[i].outOfOrder() && pumpStatePresent[i];
    }

    @RoboChartType("real")
    private double pumpSupplyHigh(int i) {
        if (!pumpTrusted(i) || reportedOn[i]) {
            return BoilerConstants.PUMP_CAPACITY;
        }
        return 0.0;
    }

    @RoboChartType("real")
    private double pumpSupplyKnown(int i) {
        boolean controlTrusted = !pumpControls[i].outOfOrder() && pumpControlPresent[i];
        if (pumpTrusted(i) && reportedOn[i] && controlTrusted && reportedFlow[i]) {
            return BoilerConstants.PUMP_CAPACITY;
        }
        return 0.0;
    }

    // Most steam that can leave in one cycle starting from rate vHigh.
    @RoboChartType("real")
    private static double steamOutflowMax(@RoboChartType("real") double vHigh) {
        double t = BoilerConstants.CYCLE_SECONDS;
        return Math.min(BoilerConstants.MAX_STEAM_RATE * t,
                vHigh * t + BoilerConstants.MAX_STEAM_INCREASE * t * t / 2.0);
    }

    // Least steam that can leave in one cycle starting from rate vLow.
    @RoboChartType("real")
    private static double steamOutflowMin(@RoboChartType("real") double vLow) {
        double t = BoilerConstants.CYCLE_SECONDS;
        double u2 = BoilerConstants.MAX_STEAM_DECREASE;
        if (vLow >= u2 * t) {
            return vLow * t - u2 * t * t / 2.0;
        }
        return vLow * vLow / (2.0 * u2);
    }

    // Number of pumps that balances the steam and steers the level towards the
    // middle of [N1, N2] over two cycles (SB-Beh10).
    private int pumpsForTarget() {
        double target = (BoilerConstants.N1 + BoilerConstants.N2) / 2.0;
        double levelMiddle = (levelLow + levelHigh) / 2.0;
        double steamMiddle = (steamLow + steamHigh) / 2.0;
        double rate = steamMiddle + (target - levelMiddle) / (2.0 * BoilerConstants.CYCLE_SECONDS);
        long pumpsNeeded = Math.round(rate / BoilerConstants.PUMP_CAPACITY);
        if (pumpsNeeded < 0) {
            return 0;
        }
        if (pumpsNeeded > BoilerConstants.PUMP_COUNT) {
            return BoilerConstants.PUMP_COUNT;
        }
        return (int) pumpsNeeded;
    }

    @RoboChartType("real")
    private static double clampLevel(@RoboChartType("real") double quantity) {
        return Math.max(0.0, Math.min(BoilerConstants.CAPACITY, quantity));
    }

    /**
     * Transmit stage: appends the failure detections still awaiting
     * acknowledgement (SB-FR6) and the acknowledgements of this cycle's
     * repairs (SB-FR7).
     */
    public void appendProtocolMessages(List<OutputEvent> outgoing) {
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            int n = i + 1;
            if (pumps[i].detectionPending()) {
                outgoing.add(new OutputEvent.PumpFailureDetection(n));
            }
            if (pumps[i].repairedThisCycle()) {
                outgoing.add(new OutputEvent.PumpRepairedAcknowledgement(n));
            }
            if (pumpControls[i].detectionPending()) {
                outgoing.add(new OutputEvent.PumpControlFailureDetection(n));
            }
            if (pumpControls[i].repairedThisCycle()) {
                outgoing.add(new OutputEvent.PumpControlRepairedAcknowledgement(n));
            }
        }
        if (levelDevice.detectionPending()) {
            outgoing.add(new OutputEvent.LevelFailureDetection());
        }
        if (levelDevice.repairedThisCycle()) {
            outgoing.add(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (steamDevice.detectionPending()) {
            outgoing.add(new OutputEvent.SteamFailureDetection());
        }
        if (steamDevice.repairedThisCycle()) {
            outgoing.add(new OutputEvent.SteamRepairedAcknowledgement());
        }
    }

    /**
     * End of the cycle: records the transmitted mode, PROGRAM_READY, valve and
     * pump orders, and predicts the level interval for the next cycle.
     */
    public void endCycle(List<OutputEvent> transmitted) {
        boolean[] orderedOpen = new boolean[BoilerConstants.PUMP_COUNT];
        boolean[] orderedClosed = new boolean[BoilerConstants.PUMP_COUNT];
        for (OutputEvent message : transmitted) {
            if (message instanceof OutputEvent.ModeMessage) {
                OutputEvent.ModeMessage modeMessage = (OutputEvent.ModeMessage) message;
                transmittedMode = modeMessage.broadcastMode();
            } else if (message instanceof OutputEvent.ProgramReady) {
                programReadyTransmitted = true;
            } else if (message instanceof OutputEvent.ValveCommand) {
                valveOpened = !valveOpened;
            } else if (message instanceof OutputEvent.OpenPump) {
                OutputEvent.OpenPump order = (OutputEvent.OpenPump) message;
                if (validPump(order.openedPump())) {
                    orderedOpen[order.openedPump() - 1] = true;
                }
            } else if (message instanceof OutputEvent.ClosePump) {
                OutputEvent.ClosePump order = (OutputEvent.ClosePump) message;
                if (validPump(order.closedPump())) {
                    orderedClosed[order.closedPump() - 1] = true;
                }
            }
        }
        // An opened pump pours nothing for one cycle; a closed pump stops at once (SB-Beh25).
        double knownAfterOrders = 0.0;
        double highAfterOrders = 0.0;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            boolean ordered = orderedOpen[i] || orderedClosed[i];
            orderedLastCycle[i] = ordered;
            if (orderedOpen[i]) {
                expectedOn[i] = true;
            } else if (orderedClosed[i]) {
                expectedOn[i] = false;
            }
            if (!ordered) {
                knownAfterOrders = knownAfterOrders + pumpSupplyKnown(i);
                highAfterOrders = highAfterOrders + pumpSupplyHigh(i);
            }
        }
        double t = BoilerConstants.CYCLE_SECONDS;
        double outMax = steamOutflowMax(steamHigh);
        double outMin = steamOutflowMin(steamLow);
        estimatedLow = clampLevel(levelLow + knownAfterOrders * t - outMax);
        estimatedHigh = clampLevel(levelHigh + highAfterOrders * t - outMin);
        checkLevelLow = clampLevel(levelLow - outMax);
        checkLevelHigh = estimatedHigh;
        levelHistoryValid = monitoring && !valveOpened;
        previousSteamLow = steamLow;
        previousSteamHigh = steamHigh;
        steamHistoryValid = monitoring;
    }

    // ---- Queries read by the controller's guards ----

    /** Number of successive transmissions, up to this one, that carried STOP (SB-V10). */
    @RoboChartType("nat")
    public int consecutiveStopCount() {
        return consecutiveStops;
    }

    /** Whether this transmission is judged failed: an indispensable message missing or an aberrant one present (SB-FR5). */
    public boolean transmissionFailureDetected() {
        return transmissionFailure;
    }

    /** Whether the level measuring device was judged failed in this cycle (SB-FR3). */
    public boolean levelFailureDetected() {
        return levelDevice.failedThisCycle();
    }

    /** Whether the steam measuring device was judged failed in this cycle (SB-FR4). */
    public boolean steamFailureDetected() {
        return steamDevice.failedThisCycle();
    }

    /** Whether some pump was judged failed in this cycle (SB-FR1). */
    public boolean pumpFailureDetected() {
        boolean failed = false;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            if (pumps[i].failedThisCycle()) {
                failed = true;
            }
        }
        return failed;
    }

    /** Whether some pump controller was judged failed in this cycle (SB-FR2). */
    public boolean pumpControlFailureDetected() {
        boolean failed = false;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            if (pumpControls[i].failedThisCycle()) {
                failed = true;
            }
        }
        return failed;
    }

    /** Whether LEVEL_REPAIRED arrived in this transmission for the out-of-order level device (SB-FR7). */
    public boolean levelRepairReported() {
        return levelDevice.repairedThisCycle();
    }

    /** Whether every unit is believed sound after this cycle's repairs and judgements (SB-V9). */
    public boolean allUnitsSound() {
        boolean sound = !levelDevice.outOfOrder() && !steamDevice.outOfOrder();
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            if (pumps[i].outOfOrder() || pumpControls[i].outOfOrder()) {
                sound = false;
            }
        }
        return sound;
    }

    /** Whether the steam measuring device is believed out of order (SB-V9). */
    public boolean steamDeviceOutOfOrder() {
        return steamDevice.outOfOrder();
    }

    /** Whether some pump controller is believed out of order (SB-V9). */
    public boolean anyPumpControllerOutOfOrder() {
        boolean outOfOrder = false;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            if (pumpControls[i].outOfOrder()) {
                outOfOrder = true;
            }
        }
        return outOfOrder;
    }

    /** Lowest level the boiler may reach by the end of the next cycle (SB-Beh13). */
    @RoboChartType("real")
    public double predictedLevelLow() {
        return riskLow;
    }

    /** Highest level the boiler may reach by the end of the next cycle (SB-Beh13). */
    @RoboChartType("real")
    public double predictedLevelHigh() {
        return riskHigh;
    }

    /** Middle of the current level interval: q, or the middle of its estimate. */
    @RoboChartType("real")
    public double levelMidpoint() {
        return (levelLow + levelHigh) / 2.0;
    }

    /** Whether a steam rate other than zero was reported in this transmission (SB-Beh3). */
    public boolean steamRateNonZero() {
        return steamPresent && receivedSteam != 0.0;
    }

    /** Whether STEAM_BOILER_WAITING arrived in this transmission (SB-Beh2). */
    public boolean boilerWaitingReceived() {
        return waitingPresent;
    }

    /** Whether PHYSICAL_UNITS_READY arrived in this transmission (SB-Beh8). */
    public boolean unitsReadyReceived() {
        return unitsReadyPresent;
    }

    /** Number of pumps reported on in this transmission. */
    @RoboChartType("nat")
    public int pumpsRunning() {
        return runningPumps;
    }

    /** Number of pumps that should be on to steer the level towards [N1, N2] (SB-Beh10). */
    @RoboChartType("nat")
    public int pumpsWanted() {
        return wantedPumps;
    }

    /** Whether some sound pump is off and can be ordered on. */
    public boolean startablePumpAvailable() {
        return startableAvailable;
    }

    /** Whether some sound pump is on and can be ordered off. */
    public boolean stoppablePumpAvailable() {
        return stoppableAvailable;
    }

    // ---- Quantities read by the controller's actions ----

    /** The sound pump to order on: the lowest-numbered one that is off (1 when there is none). */
    @RoboChartType("nat")
    public int pumpToOpen() {
        return nextPumpToOpen;
    }

    /** The sound pump to order off: the highest-numbered one that is on (1 when there is none). */
    @RoboChartType("nat")
    public int pumpToClose() {
        return nextPumpToClose;
    }

    // ---- Maintained quantities ----

    /** q: the water level most recently reported (SB-V5). */
    @RoboChartType("real")
    public double levelReading() {
        return lastLevelReading;
    }

    /** v: the steam rate most recently reported (SB-V6). */
    @RoboChartType("real")
    public double steamReading() {
        return lastSteamReading;
    }

    /** p: throughput of the pumps reported on whose controllers see water flowing (SB-V7). */
    @RoboChartType("real")
    public double pumpThroughput() {
        return throughput;
    }

    /** Lower bound on the true level: q while the device works, else the estimate (SB-V8). */
    @RoboChartType("real")
    public double levelLowerBound() {
        return levelLow;
    }

    /** Upper bound on the true level: q while the device works, else the estimate (SB-V8). */
    @RoboChartType("real")
    public double levelUpperBound() {
        return levelHigh;
    }
}
