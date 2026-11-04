package steam_boiler.sensor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.BoilerConstants;
import steam_boiler.domain.FlowReport;
import steam_boiler.domain.PumpReport;
import steam_boiler.domain.PumpSetting;
import steam_boiler.domain.WaterFlow;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;

/**
 * The reception and analysis stages of a cycle. It buffers the incoming
 * transmission, checks it for missing or aberrant messages, detects unit
 * failures, keeps the per-unit bookkeeping, the operator-stop count and the
 * bounds on the water level, and publishes the results in the
 * {@link BoilerSensor} the controller reads. For the transmission stage it
 * composes the failure announcements and repair acknowledgements, and it
 * records the pump orders the controller issued, which the next cycles' pump
 * and pump-controller checks are judged against.
 */
public final class CycleAnalyser {

    private static final int PUMPS = BoilerConstants.PUMP_COUNT;
    /** A pump controller is judged from the second transmission after an order. */
    private static final int SETTLING_TRANSMISSIONS = 2;
    private static final double CYCLE = BoilerConstants.CYCLE_SECONDS;

    private final BoilerSensor sensor;

    // Reception buffer of the current cycle.
    private boolean stopReceived;
    private boolean waitingReceived;
    private boolean unitsReadyReceived;
    private boolean aberrantMessage;
    private int levelMessages;
    private int steamMessages;
    private final int[] pumpStateMessages = new int[PUMPS];
    private final int[] pumpControlStateMessages = new int[PUMPS];
    private double levelReading;
    private double steamReading;
    private final boolean[] pumpOn = new boolean[PUMPS];
    private final boolean[] flowing = new boolean[PUMPS];
    private final boolean[] pumpRepairReceived = new boolean[PUMPS];
    private final boolean[] pumpControlRepairReceived = new boolean[PUMPS];
    private final boolean[] pumpAcknowledgementReceived = new boolean[PUMPS];
    private final boolean[] pumpControlAcknowledgementReceived = new boolean[PUMPS];
    private boolean levelRepairReceived;
    private boolean steamRepairReceived;
    private boolean levelAcknowledgementReceived;
    private boolean steamAcknowledgementReceived;

    // History carried from cycle to cycle.
    private boolean plantStarted;
    private boolean historyAvailable;
    private boolean steamHistoryValid;
    private double previousSteam;
    private double levelLow;
    private double levelHigh = BoilerConstants.CAPACITY_C;
    private final boolean[] previousFlowMin = new boolean[PUMPS];
    private final boolean[] previousFlowMax = new boolean[PUMPS];
    private final boolean[] expectedPumpOn = new boolean[PUMPS];
    private final boolean[] closeOrdered = new boolean[PUMPS];
    private final int[] transmissionsSinceOrder = new int[PUMPS];
    private int consecutiveStops;

    // Bookkeeping of the ten physical units.
    private final UnitStatus[] pumps = new UnitStatus[PUMPS];
    private final UnitStatus[] pumpControls = new UnitStatus[PUMPS];
    private final UnitStatus levelDevice = new UnitStatus();
    private final UnitStatus steamDevice = new UnitStatus();

    /** Creates the analyser that publishes its results in {@code sensor}. */
    public CycleAnalyser(BoilerSensor sensor) {
        this.sensor = sensor;
        for (int n = 0; n < PUMPS; n++) {
            pumps[n] = new UnitStatus();
            pumpControls[n] = new UnitStatus();
            transmissionsSinceOrder[n] = SETTLING_TRANSMISSIONS;
        }
    }

    /** Empties the reception buffer at the start of a cycle. */
    public void beginCycle() {
        stopReceived = false;
        waitingReceived = false;
        unitsReadyReceived = false;
        aberrantMessage = false;
        levelMessages = 0;
        steamMessages = 0;
        levelRepairReceived = false;
        steamRepairReceived = false;
        levelAcknowledgementReceived = false;
        steamAcknowledgementReceived = false;
        levelDevice.startCycle();
        steamDevice.startCycle();
        for (int n = 0; n < PUMPS; n++) {
            pumpStateMessages[n] = 0;
            pumpControlStateMessages[n] = 0;
            pumpRepairReceived[n] = false;
            pumpControlRepairReceived[n] = false;
            pumpAcknowledgementReceived[n] = false;
            pumpControlAcknowledgementReceived[n] = false;
            pumps[n].startCycle();
            pumpControls[n].startCycle();
        }
    }

    /** Buffers one message of the incoming transmission. */
    public void receive(InputEvent message) {
        if (message instanceof InputEvent.StopMessage) {
            stopReceived = true;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            waitingReceived = true;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            unitsReadyReceived = true;
        } else if (message instanceof InputEvent.Level) {
            InputEvent.Level level = (InputEvent.Level) message;
            levelMessages = levelMessages + 1;
            levelReading = level.value();
        } else if (message instanceof InputEvent.Steam) {
            InputEvent.Steam steam = (InputEvent.Steam) message;
            steamMessages = steamMessages + 1;
            steamReading = steam.value();
        } else if (message instanceof InputEvent.PumpState) {
            InputEvent.PumpState state = (InputEvent.PumpState) message;
            PumpReport report = state.report();
            if (validPump(report.pump())) {
                int n = report.pump() - 1;
                pumpStateMessages[n] = pumpStateMessages[n] + 1;
                pumpOn[n] = report.setting() == PumpSetting.PUMP_ON;
            } else {
                aberrantMessage = true;
            }
        } else if (message instanceof InputEvent.PumpControlState) {
            InputEvent.PumpControlState state = (InputEvent.PumpControlState) message;
            FlowReport report = state.report();
            if (validPump(report.pump())) {
                int n = report.pump() - 1;
                pumpControlStateMessages[n] = pumpControlStateMessages[n] + 1;
                flowing[n] = report.flow() == WaterFlow.FLOWING;
            } else {
                aberrantMessage = true;
            }
        } else if (message instanceof InputEvent.PumpRepaired) {
            InputEvent.PumpRepaired repaired = (InputEvent.PumpRepaired) message;
            markPump(pumpRepairReceived, repaired.pump());
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired repaired = (InputEvent.PumpControlRepaired) message;
            markPump(pumpControlRepairReceived, repaired.pump());
        } else if (message instanceof InputEvent.LevelRepaired) {
            levelRepairReceived = true;
        } else if (message instanceof InputEvent.SteamRepaired) {
            steamRepairReceived = true;
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement acknowledged = (InputEvent.PumpFailureAcknowledgement) message;
            markPump(pumpAcknowledgementReceived, acknowledged.pump());
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement acknowledged =
                    (InputEvent.PumpControlFailureAcknowledgement) message;
            markPump(pumpControlAcknowledgementReceived, acknowledged.pump());
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            levelAcknowledgementReceived = true;
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            steamAcknowledgementReceived = true;
        }
    }

    /**
     * Analyses the buffered transmission and publishes the results in the
     * sensor. Until STEAM_BOILER_WAITING has arrived only the STOP count is
     * kept; after a transmission failure the readings are not analysed.
     */
    public void analyse() {
        if (stopReceived) {
            if (consecutiveStops < BoilerConstants.STOP_REPETITIONS) {
                consecutiveStops = consecutiveStops + 1;
            }
        } else {
            consecutiveStops = 0;
        }
        sensor.stopCount = consecutiveStops;
        sensor.waitingReceived = waitingReceived;
        sensor.unitsReadyReceived = unitsReadyReceived;
        if (waitingReceived) {
            plantStarted = true;
        }
        if (!plantStarted) {
            sensor.transmissionFailed = false;
            return;
        }
        sensor.transmissionFailed = transmissionIncomplete();
        if (sensor.transmissionFailed) {
            return;
        }

        // Steam that left during the cycle just ended, from the last valid steam reading.
        double outflowMin = 0.0;
        double outflowMax = BoilerConstants.MAX_STEAM_W * CYCLE;
        if (steamHistoryValid) {
            outflowMin = steamOutflowMin(previousSteam);
            outflowMax = steamOutflowMax(previousSteam);
        }

        handleAcknowledgementsAndRepairs();
        if (!historyAvailable) {
            for (int n = 0; n < PUMPS; n++) {
                expectedPumpOn[n] = pumpOn[n];
            }
        }
        detectSteamFailure();
        detectPumpFailures();
        detectPumpControlFailures();

        // Which pumps are certainly / possibly pouring water now.
        boolean[] flowMin = new boolean[PUMPS];
        boolean[] flowMax = new boolean[PUMPS];
        int delivering = 0;
        for (int n = 0; n < PUMPS; n++) {
            if (!pumpControls[n].failed()) {
                flowMin[n] = flowing[n];
                flowMax[n] = flowing[n];
            } else if (!pumps[n].failed()) {
                flowMin[n] = false;
                flowMax[n] = pumpOn[n];
            } else {
                flowMin[n] = false;
                flowMax[n] = true;
            }
            boolean pouring = flowing[n];
            if (pumpControls[n].failed()) {
                pouring = !pumps[n].failed() && pumpOn[n];
            }
            if (pouring) {
                delivering = delivering + 1;
            }
        }
        sensor.throughput = BoilerConstants.PUMP_CAPACITY_P * delivering;

        updateLevel(outflowMin, outflowMax, flowMin, flowMax);
        predictNextCycle(flowMax);
        publishStatus();

        for (int n = 0; n < PUMPS; n++) {
            previousFlowMin[n] = flowMin[n];
            previousFlowMax[n] = flowMax[n];
        }
        if (steamDevice.failed()) {
            steamHistoryValid = false;
        } else {
            previousSteam = steamReading;
            steamHistoryValid = true;
        }
        historyAvailable = true;
    }

    /**
     * The failure announcements still awaiting acknowledgement and the repair
     * acknowledgements due in this cycle.
     */
    public List<OutputEvent> protocolMessages() {
        List<OutputEvent> messages = new ArrayList<>();
        for (int n = 0; n < PUMPS; n++) {
            int pump = n + 1;
            if (pumps[n].repairAcknowledgementDue()) {
                messages.add(new OutputEvent.PumpRepairedAcknowledgement(pump));
            }
            if (pumps[n].announcementPending()) {
                messages.add(new OutputEvent.PumpFailureDetection(pump));
            }
            if (pumpControls[n].repairAcknowledgementDue()) {
                messages.add(new OutputEvent.PumpControlRepairedAcknowledgement(pump));
            }
            if (pumpControls[n].announcementPending()) {
                messages.add(new OutputEvent.PumpControlFailureDetection(pump));
            }
        }
        if (levelDevice.repairAcknowledgementDue()) {
            messages.add(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (levelDevice.announcementPending()) {
            messages.add(new OutputEvent.LevelFailureDetection());
        }
        if (steamDevice.repairAcknowledgementDue()) {
            messages.add(new OutputEvent.SteamRepairedAcknowledgement());
        }
        if (steamDevice.announcementPending()) {
            messages.add(new OutputEvent.SteamFailureDetection());
        }
        return messages;
    }

    /** Records the pump orders among the messages the controller issued in this cycle. */
    public void recordOrders(List<OutputEvent> issued) {
        for (int n = 0; n < PUMPS; n++) {
            closeOrdered[n] = false;
        }
        for (OutputEvent message : issued) {
            if (message instanceof OutputEvent.OpenPump) {
                OutputEvent.OpenPump order = (OutputEvent.OpenPump) message;
                recordOrder(order.pump(), true);
            } else if (message instanceof OutputEvent.ClosePump) {
                OutputEvent.ClosePump order = (OutputEvent.ClosePump) message;
                recordOrder(order.pump(), false);
            }
        }
    }

    private void recordOrder(int pump, boolean open) {
        if (!validPump(pump)) {
            return;
        }
        int n = pump - 1;
        if (expectedPumpOn[n] != open) {
            expectedPumpOn[n] = open;
            transmissionsSinceOrder[n] = 0;
        }
        if (!open) {
            closeOrdered[n] = true;
        }
    }

    private boolean transmissionIncomplete() {
        boolean incomplete = aberrantMessage || levelMessages != 1 || steamMessages != 1;
        for (int n = 0; n < PUMPS; n++) {
            if (pumpStateMessages[n] != 1 || pumpControlStateMessages[n] != 1) {
                incomplete = true;
            }
        }
        return incomplete;
    }

    private void handleAcknowledgementsAndRepairs() {
        if (levelAcknowledgementReceived) {
            levelDevice.acknowledgeFailure();
        }
        if (steamAcknowledgementReceived) {
            steamDevice.acknowledgeFailure();
        }
        if (levelRepairReceived) {
            levelDevice.repair();
        }
        if (steamRepairReceived) {
            steamDevice.repair();
            steamHistoryValid = false;
        }
        for (int n = 0; n < PUMPS; n++) {
            if (pumpAcknowledgementReceived[n]) {
                pumps[n].acknowledgeFailure();
            }
            if (pumpControlAcknowledgementReceived[n]) {
                pumpControls[n].acknowledgeFailure();
            }
            if (pumpRepairReceived[n]) {
                pumps[n].repair();
                expectedPumpOn[n] = pumpOn[n];
            }
            if (pumpControlRepairReceived[n]) {
                pumpControls[n].repair();
            }
        }
    }

    private void detectSteamFailure() {
        if (steamDevice.failed()) {
            return;
        }
        boolean outOfRange = steamReading < 0.0 || steamReading > BoilerConstants.MAX_STEAM_W;
        boolean tooFast = steamHistoryValid
                && (steamReading > previousSteam + BoilerConstants.STEAM_INCREASE_U1 * CYCLE
                        || steamReading < previousSteam - BoilerConstants.STEAM_DECREASE_U2 * CYCLE);
        if (outOfRange || tooFast) {
            steamDevice.detectFailure();
        }
    }

    private void detectPumpFailures() {
        for (int n = 0; n < PUMPS; n++) {
            if (!pumps[n].failed() && pumpOn[n] != expectedPumpOn[n]) {
                pumps[n].detectFailure();
            }
        }
    }

    private void detectPumpControlFailures() {
        for (int n = 0; n < PUMPS; n++) {
            if (transmissionsSinceOrder[n] < SETTLING_TRANSMISSIONS) {
                transmissionsSinceOrder[n] = transmissionsSinceOrder[n] + 1;
            }
            boolean settled = transmissionsSinceOrder[n] >= SETTLING_TRANSMISSIONS;
            boolean judged = historyAvailable && settled && !pumps[n].failed() && !pumpControls[n].failed();
            if (judged && flowing[n] != pumpOn[n]) {
                pumpControls[n].detectFailure();
            }
        }
    }

    private void updateLevel(double outflowMin, double outflowMax, boolean[] flowMin, boolean[] flowMax) {
        double pumpVolume = BoilerConstants.PUMP_CAPACITY_P * CYCLE;
        double inflowMin = 0.0;
        double inflowMax = 0.0;
        for (int n = 0; n < PUMPS; n++) {
            if (previousFlowMin[n] && flowMin[n] && !closeOrdered[n]) {
                inflowMin = inflowMin + pumpVolume;
            }
            if ((previousFlowMax[n] && !closeOrdered[n]) || flowMax[n]) {
                inflowMax = inflowMax + pumpVolume;
            }
        }
        double windowLow = clampToCapacity(levelLow + inflowMin - outflowMax);
        double windowHigh = clampToCapacity(levelHigh + inflowMax - outflowMin);

        if (!levelDevice.failed()) {
            boolean outOfRange = levelReading < 0.0 || levelReading > BoilerConstants.CAPACITY_C;
            boolean incompatible = historyAvailable && !levelRepairReceived
                    && (levelReading < windowLow || levelReading > windowHigh);
            if (outOfRange || incompatible) {
                levelDevice.detectFailure();
            }
        }
        if (!levelDevice.failed()) {
            levelLow = levelReading;
            levelHigh = levelReading;
        } else if (historyAvailable) {
            levelLow = windowLow;
            levelHigh = windowHigh;
        } else {
            levelLow = 0.0;
            levelHigh = BoilerConstants.CAPACITY_C;
        }
        sensor.level = levelReading;
        sensor.estimateLow = levelLow;
        sensor.estimateHigh = levelHigh;
    }

    private void predictNextCycle(boolean[] flowMax) {
        double nextOutflowMin = 0.0;
        double nextOutflowMax = BoilerConstants.MAX_STEAM_W * CYCLE;
        if (!steamDevice.failed()) {
            nextOutflowMin = steamOutflowMin(steamReading);
            nextOutflowMax = steamOutflowMax(steamReading);
        }
        // A pump that is off now cannot pour during the next cycle: a start order
        // takes one full cycle of pressure balancing.
        double supply = 0.0;
        for (int n = 0; n < PUMPS; n++) {
            if (pumpOn[n] || flowMax[n]) {
                supply = supply + BoilerConstants.PUMP_CAPACITY_P * CYCLE;
            }
        }
        sensor.worstLow = levelLow - nextOutflowMax;
        sensor.worstHigh = levelHigh + supply - nextOutflowMin;
    }

    private void publishStatus() {
        sensor.steam = steamReading;
        sensor.levelFailed = levelDevice.failed();
        sensor.steamFailed = steamDevice.failed();
        boolean anyPump = false;
        boolean anyPumpControl = false;
        for (int n = 0; n < PUMPS; n++) {
            if (pumps[n].failed()) {
                anyPump = true;
            }
            if (pumpControls[n].failed()) {
                anyPumpControl = true;
            }
        }
        sensor.anyPumpFailed = anyPump;
        sensor.anyPumpControlFailed = anyPumpControl;
    }

    private void markPump(boolean[] received, int pump) {
        if (validPump(pump)) {
            received[pump - 1] = true;
        } else {
            aberrantMessage = true;
        }
    }

    private static boolean validPump(int pump) {
        return pump >= 1 && pump <= PUMPS;
    }

    /** Most steam that can leave in one cycle starting from rate {@code rate}. */
    private static double steamOutflowMax(double rate) {
        double rising = rate * CYCLE + 0.5 * BoilerConstants.STEAM_INCREASE_U1 * CYCLE * CYCLE;
        return Math.min(BoilerConstants.MAX_STEAM_W * CYCLE, rising);
    }

    /** Least steam that can leave in one cycle starting from rate {@code rate}. */
    private static double steamOutflowMin(double rate) {
        double falling = rate * CYCLE - 0.5 * BoilerConstants.STEAM_DECREASE_U2 * CYCLE * CYCLE;
        return Math.max(0.0, falling);
    }

    private static double clampToCapacity(double quantity) {
        return Math.max(0.0, Math.min(BoilerConstants.CAPACITY_C, quantity));
    }
}
