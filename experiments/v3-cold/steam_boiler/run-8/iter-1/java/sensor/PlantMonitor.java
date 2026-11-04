package steam_boiler.sensor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.BoilerConstants;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.OperatingMode;
import steam_boiler.event.FlowReading;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.event.PumpReading;

/**
 * Receive and analysis stages of each cycle (SB-V3). The monitor buffers the
 * messages of one transmission, judges unit and transmission failures
 * (SB-FR1 to SB-FR5), keeps the unit status table (SB-V9), the STOP count
 * (SB-V10), the current measures q, v, p (SB-V5 to SB-V7) and the level
 * estimate (SB-V8), and produces the failure-announcement and
 * repair-acknowledgement messages (SB-FR6, SB-FR7). It learns what the program
 * sent (pump orders, valve, PROGRAM_READY, MODE) through {@link #recordSent}.
 */
public final class PlantMonitor {

    // ---- messages of the current transmission ----
    private boolean stopReceived;
    private boolean waitingReceived;
    private boolean unitsReadyReceived;
    private boolean levelReceived;
    @RoboChartType("real")
    private double levelMessage;
    private boolean steamReceived;
    @RoboChartType("real")
    private double steamMessage;
    private boolean levelRepairReceived;
    private boolean steamRepairReceived;
    private boolean levelAckReceived;
    private boolean steamAckReceived;
    private boolean aberrantReceived;
    private final boolean[] pumpStateReceived = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] pumpOpenMessage = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] flowStateReceived = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] flowMessage = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] pumpRepairReceived = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] controlRepairReceived = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] pumpAckReceived = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] controlAckReceived = new boolean[BoilerConstants.PUMP_COUNT];

    // ---- knowledge kept across cycles ----
    private OperatingMode modeLastSent = OperatingMode.INITIALIZATION;
    private boolean plantWaitingLatched;
    private boolean programReadyAnnounced;
    private boolean evacuationValveOpen;
    private boolean baselineEstablished;
    private boolean transmissionFailed;
    @RoboChartType("nat")
    private int successiveStops;
    @RoboChartType("real")
    private double levelQuantity;
    @RoboChartType("real")
    private double steamQuantity;
    @RoboChartType("real")
    private double lastValidSteam;
    private boolean steamHistoryValid;
    @RoboChartType("real")
    private double pumpQuantity;
    @RoboChartType("real")
    private double levelLow;
    @RoboChartType("real")
    private double levelHigh = BoilerConstants.CAPACITY;
    @RoboChartType("real")
    private double elapsedInflowMin;
    @RoboChartType("real")
    private double elapsedInflowMax;
    private final boolean[] pumpOrderedOpen = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] pumpOpen = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] waterFlowing = new boolean[BoilerConstants.PUMP_COUNT];
    private final UnitStatus[] pumpStatus = new UnitStatus[BoilerConstants.PUMP_COUNT];
    private final UnitStatus[] controlStatus = new UnitStatus[BoilerConstants.PUMP_COUNT];
    private final UnitStatus levelStatus = new UnitStatus();
    private final UnitStatus steamStatus = new UnitStatus();

    /** Creates a monitor in the start-up state: every unit sound, nothing received. */
    public PlantMonitor() {
        for (int slot = 0; slot < BoilerConstants.PUMP_COUNT; slot++) {
            pumpStatus[slot] = new UnitStatus();
            controlStatus[slot] = new UnitStatus();
        }
    }

    // =====================================================================
    // Receive stage
    // =====================================================================

    /** Starts the receive stage of a new cycle: forgets the previous transmission. */
    public void beginCycle() {
        stopReceived = false;
        waitingReceived = false;
        unitsReadyReceived = false;
        levelReceived = false;
        steamReceived = false;
        levelRepairReceived = false;
        steamRepairReceived = false;
        levelAckReceived = false;
        steamAckReceived = false;
        aberrantReceived = false;
        for (int slot = 0; slot < BoilerConstants.PUMP_COUNT; slot++) {
            pumpStateReceived[slot] = false;
            flowStateReceived[slot] = false;
            pumpRepairReceived[slot] = false;
            controlRepairReceived[slot] = false;
            pumpAckReceived[slot] = false;
            controlAckReceived[slot] = false;
        }
    }

    /**
     * Records one incoming message of the current transmission (SB-DM9). A
     * duplicated data message or an unknown pump number is aberrant (SB-FR5).
     *
     * @param message the received message
     */
    public void receive(InputEvent message) {
        if (message instanceof InputEvent.StopRequest) {
            stopReceived = true;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            waitingReceived = true;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            unitsReadyReceived = true;
        } else if (message instanceof InputEvent.Level) {
            InputEvent.Level level = (InputEvent.Level) message;
            if (levelReceived) {
                aberrantReceived = true;
            }
            levelReceived = true;
            levelMessage = level.measuredLevel();
        } else if (message instanceof InputEvent.Steam) {
            InputEvent.Steam steam = (InputEvent.Steam) message;
            if (steamReceived) {
                aberrantReceived = true;
            }
            steamReceived = true;
            steamMessage = steam.measuredSteam();
        } else if (message instanceof InputEvent.PumpState) {
            InputEvent.PumpState pumpState = (InputEvent.PumpState) message;
            PumpReading reading = pumpState.reading();
            if (isPumpNumber(reading.pumpIndex())) {
                int slot = reading.pumpIndex() - 1;
                if (pumpStateReceived[slot]) {
                    aberrantReceived = true;
                }
                pumpStateReceived[slot] = true;
                pumpOpenMessage[slot] = reading.pumpOpen();
            } else {
                aberrantReceived = true;
            }
        } else if (message instanceof InputEvent.PumpControlState) {
            InputEvent.PumpControlState controlState = (InputEvent.PumpControlState) message;
            FlowReading reading = controlState.reading();
            if (isPumpNumber(reading.controllerIndex())) {
                int slot = reading.controllerIndex() - 1;
                if (flowStateReceived[slot]) {
                    aberrantReceived = true;
                }
                flowStateReceived[slot] = true;
                flowMessage[slot] = reading.waterFlowing();
            } else {
                aberrantReceived = true;
            }
        } else if (message instanceof InputEvent.PumpRepaired) {
            InputEvent.PumpRepaired repaired = (InputEvent.PumpRepaired) message;
            if (isPumpNumber(repaired.pumpNumber())) {
                pumpRepairReceived[repaired.pumpNumber() - 1] = true;
            } else {
                aberrantReceived = true;
            }
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired repaired = (InputEvent.PumpControlRepaired) message;
            if (isPumpNumber(repaired.pumpNumber())) {
                controlRepairReceived[repaired.pumpNumber() - 1] = true;
            } else {
                aberrantReceived = true;
            }
        } else if (message instanceof InputEvent.LevelRepaired) {
            levelRepairReceived = true;
        } else if (message instanceof InputEvent.SteamRepaired) {
            steamRepairReceived = true;
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement ack = (InputEvent.PumpFailureAcknowledgement) message;
            if (isPumpNumber(ack.pumpNumber())) {
                pumpAckReceived[ack.pumpNumber() - 1] = true;
            } else {
                aberrantReceived = true;
            }
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement ack =
                    (InputEvent.PumpControlFailureAcknowledgement) message;
            if (isPumpNumber(ack.pumpNumber())) {
                controlAckReceived[ack.pumpNumber() - 1] = true;
            } else {
                aberrantReceived = true;
            }
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            levelAckReceived = true;
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            steamAckReceived = true;
        }
        // InputEvent.Transmission only marks the end of the receive stage.
    }

    // =====================================================================
    // Analysis stage
    // =====================================================================

    /**
     * Analysis stage of the cycle: call once, after every message of the
     * transmission has been received and before the controller is stepped.
     */
    public void analyse() {
        // SB-V10: successive transmissions carrying STOP.
        if (stopReceived) {
            successiveStops = successiveStops + 1;
        } else {
            successiveStops = 0;
        }
        boolean initialising = modeLastSent == OperatingMode.INITIALIZATION;
        // SB-FR5: start-up messages outside their place in initialization are aberrant.
        if (waitingReceived && !initialising) {
            aberrantReceived = true;
        }
        if (unitsReadyReceived && !(initialising && programReadyAnnounced)) {
            aberrantReceived = true;
        }
        if (waitingReceived && initialising) {
            plantWaitingLatched = true;
        }
        // SB-Beh2: until STEAM_BOILER_WAITING has arrived only STOP is analysed.
        if (!plantWaitingLatched) {
            transmissionFailed = false;
            return;
        }
        // SB-FR5: the four data messages are indispensable in every transmission.
        transmissionFailed = aberrantReceived || !allDataMessagesReceived();
        if (transmissionFailed) {
            return;
        }
        processAcknowledgements();
        processRepairs();
        double outflowMin = steamOutflowMin();
        double outflowMax = steamOutflowMax();
        analysePumps();
        analyseLevel(outflowMin, outflowMax);
        analyseSteam();
        baselineEstablished = true;
    }

    private boolean allDataMessagesReceived() {
        boolean complete = levelReceived && steamReceived;
        for (int slot = 0; slot < BoilerConstants.PUMP_COUNT; slot++) {
            complete = complete && pumpStateReceived[slot] && flowStateReceived[slot];
        }
        return complete;
    }

    private void processAcknowledgements() {
        for (int slot = 0; slot < BoilerConstants.PUMP_COUNT; slot++) {
            if (pumpAckReceived[slot]) {
                pumpStatus[slot].acknowledgeFailure();
            }
            if (controlAckReceived[slot]) {
                controlStatus[slot].acknowledgeFailure();
            }
        }
        if (levelAckReceived) {
            levelStatus.acknowledgeFailure();
        }
        if (steamAckReceived) {
            steamStatus.acknowledgeFailure();
        }
    }

    private void processRepairs() {
        for (int slot = 0; slot < BoilerConstants.PUMP_COUNT; slot++) {
            if (pumpRepairReceived[slot]) {
                pumpStatus[slot].repair();
            }
            if (controlRepairReceived[slot]) {
                controlStatus[slot].repair();
            }
        }
        if (levelRepairReceived) {
            levelStatus.repair();
        }
        if (steamRepairReceived) {
            steamStatus.repair();
        }
    }

    /**
     * Pump and pump-controller failure detection (SB-FR1, SB-FR2), the water
     * poured during the elapsed cycle (SB-V8) and the throughput p (SB-V7).
     */
    private void analysePumps() {
        double litresPerPump = BoilerConstants.PUMP_CAPACITY * BoilerConstants.CYCLE_DURATION;
        elapsedInflowMin = 0.0;
        elapsedInflowMax = 0.0;
        pumpQuantity = 0.0;
        for (int slot = 0; slot < BoilerConstants.PUMP_COUNT; slot++) {
            boolean open = pumpOpenMessage[slot];
            boolean flow = flowMessage[slot];
            if (!baselineEstablished) {
                pumpOrderedOpen[slot] = open;
                pumpOpen[slot] = open;
                waterFlowing[slot] = flow;
            }
            if (pumpRepairReceived[slot]) {
                pumpOrderedOpen[slot] = open;
            }
            boolean previouslyOpen = pumpOpen[slot];
            boolean previouslyFlowing = waterFlowing[slot];
            // SB-FR1: the pump must show the state last ordered, and never change by itself.
            if (!pumpStatus[slot].isOutOfOrder() && open != pumpOrderedOpen[slot]) {
                pumpStatus[slot].detectFailure();
            }
            // SB-FR2: flow must be shown from the second transmission after a start
            // order, and gone from the second transmission after a stop order.
            boolean steadilyOpen = previouslyOpen && open;
            boolean steadilyClosed = !previouslyOpen && !open;
            boolean pumpKnownSound = baselineEstablished && !pumpStatus[slot].isOutOfOrder();
            if (pumpKnownSound && !controlStatus[slot].isOutOfOrder()) {
                if ((steadilyOpen && !flow) || (steadilyClosed && flow)) {
                    controlStatus[slot].detectFailure();
                }
            }
            // SB-Beh25: a pump just switched on poured nothing during the elapsed cycle.
            if (!controlStatus[slot].isOutOfOrder()) {
                if (steadilyOpen && previouslyFlowing && flow) {
                    elapsedInflowMin = elapsedInflowMin + litresPerPump;
                }
                if (previouslyFlowing || flow) {
                    elapsedInflowMax = elapsedInflowMax + litresPerPump;
                }
                if (flow) {
                    pumpQuantity = pumpQuantity + BoilerConstants.PUMP_CAPACITY;
                }
            } else {
                if (previouslyOpen || open) {
                    elapsedInflowMax = elapsedInflowMax + litresPerPump;
                }
                if (!pumpStatus[slot].isOutOfOrder() && steadilyOpen) {
                    pumpQuantity = pumpQuantity + BoilerConstants.PUMP_CAPACITY;
                }
            }
            pumpOpen[slot] = open;
            waterFlowing[slot] = flow;
        }
    }

    /**
     * Level-device failure detection (SB-FR3) and the level knowledge: the
     * reading q when the device is sound, otherwise the estimate (SB-V8).
     */
    private void analyseLevel(double outflowMin, double outflowMax) {
        double plausibleLow = levelLow - outflowMax;
        double plausibleHigh = levelHigh
                + BoilerConstants.PUMP_COUNT * BoilerConstants.PUMP_CAPACITY * BoilerConstants.CYCLE_DURATION
                - outflowMin;
        if (evacuationValveOpen) {
            // The drain rate of the evacuation valve is not specified.
            plausibleLow = 0.0;
        }
        levelQuantity = levelMessage;
        if (!levelStatus.isOutOfOrder()) {
            boolean outsideStaticRange = levelMessage < 0.0 || levelMessage > BoilerConstants.CAPACITY;
            boolean incompatible = levelMessage < plausibleLow || levelMessage > plausibleHigh;
            if (outsideStaticRange || incompatible) {
                levelStatus.detectFailure();
            }
        }
        if (!levelStatus.isOutOfOrder()) {
            levelLow = levelMessage;
            levelHigh = levelMessage;
        } else {
            // SB-Beh19: worst-case advance; one litre pumped is one litre of content.
            levelLow = Math.max(0.0, levelLow + elapsedInflowMin - outflowMax);
            levelHigh = Math.min(BoilerConstants.CAPACITY, levelHigh + elapsedInflowMax - outflowMin);
            levelHigh = Math.max(levelHigh, levelLow);
        }
    }

    /** Steam-device failure detection (SB-FR4) and the steam rate v (SB-V6). */
    private void analyseSteam() {
        double cycle = BoilerConstants.CYCLE_DURATION;
        steamQuantity = steamMessage;
        if (!steamStatus.isOutOfOrder()) {
            boolean outsideStaticRange = steamMessage < 0.0 || steamMessage > BoilerConstants.MAX_STEAM_RATE;
            boolean incompatible = steamHistoryValid
                    && (steamMessage > lastValidSteam + BoilerConstants.MAX_STEAM_INCREASE * cycle
                    || steamMessage < lastValidSteam - BoilerConstants.MAX_STEAM_DECREASE * cycle);
            if (outsideStaticRange || incompatible) {
                steamStatus.detectFailure();
            }
        }
        if (!steamStatus.isOutOfOrder()) {
            lastValidSteam = steamMessage;
            steamHistoryValid = true;
        } else {
            steamHistoryValid = false;
        }
    }

    private double steamOutflowMax() {
        double cycle = BoilerConstants.CYCLE_DURATION;
        double ceiling = BoilerConstants.MAX_STEAM_RATE * cycle;
        if (!steamHistoryValid) {
            return ceiling;
        }
        return Math.min(ceiling,
                lastValidSteam * cycle + 0.5 * BoilerConstants.MAX_STEAM_INCREASE * cycle * cycle);
    }

    private double steamOutflowMin() {
        double cycle = BoilerConstants.CYCLE_DURATION;
        if (!steamHistoryValid) {
            return 0.0;
        }
        return Math.max(0.0,
                lastValidSteam * cycle - 0.5 * BoilerConstants.MAX_STEAM_DECREASE * cycle * cycle);
    }

    private static boolean isPumpNumber(int number) {
        return number >= 1 && number <= BoilerConstants.PUMP_COUNT;
    }

    // =====================================================================
    // Transmission stage
    // =====================================================================

    /**
     * Failure announcements still unacknowledged (SB-FR6) and repair
     * acknowledgements due (SB-FR7), to be emitted in this cycle.
     *
     * @return the protocol messages of this cycle
     */
    public List<OutputEvent> protocolMessages() {
        List<OutputEvent> messages = new ArrayList<>();
        for (int slot = 0; slot < BoilerConstants.PUMP_COUNT; slot++) {
            int number = slot + 1;
            if (pumpStatus[slot].isAwaitingAcknowledgement()) {
                messages.add(new OutputEvent.PumpFailureDetection(number));
            }
            if (pumpStatus[slot].takeRepairAcknowledgement()) {
                messages.add(new OutputEvent.PumpRepairedAcknowledgement(number));
            }
            if (controlStatus[slot].isAwaitingAcknowledgement()) {
                messages.add(new OutputEvent.PumpControlFailureDetection(number));
            }
            if (controlStatus[slot].takeRepairAcknowledgement()) {
                messages.add(new OutputEvent.PumpControlRepairedAcknowledgement(number));
            }
        }
        if (levelStatus.isAwaitingAcknowledgement()) {
            messages.add(new OutputEvent.LevelFailureDetection());
        }
        if (levelStatus.takeRepairAcknowledgement()) {
            messages.add(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (steamStatus.isAwaitingAcknowledgement()) {
            messages.add(new OutputEvent.SteamFailureDetection());
        }
        if (steamStatus.takeRepairAcknowledgement()) {
            messages.add(new OutputEvent.SteamRepairedAcknowledgement());
        }
        return messages;
    }

    /**
     * Records the messages the program emitted in this cycle: the mode, pump
     * orders, valve requests and PROGRAM_READY.
     *
     * @param sent the outgoing messages of the cycle
     */
    public void recordSent(List<OutputEvent> sent) {
        for (OutputEvent message : sent) {
            if (message instanceof OutputEvent.Mode) {
                OutputEvent.Mode modeMessage = (OutputEvent.Mode) message;
                modeLastSent = modeMessage.mode();
            } else if (message instanceof OutputEvent.ProgramReady) {
                programReadyAnnounced = true;
            } else if (message instanceof OutputEvent.Valve) {
                evacuationValveOpen = !evacuationValveOpen;
            } else if (message instanceof OutputEvent.OpenPump) {
                OutputEvent.OpenPump order = (OutputEvent.OpenPump) message;
                if (isPumpNumber(order.pumpNumber())) {
                    pumpOrderedOpen[order.pumpNumber() - 1] = true;
                }
            } else if (message instanceof OutputEvent.ClosePump) {
                OutputEvent.ClosePump order = (OutputEvent.ClosePump) message;
                if (isPumpNumber(order.pumpNumber())) {
                    pumpOrderedOpen[order.pumpNumber() - 1] = false;
                }
            }
        }
    }

    // =====================================================================
    // Queries (served to the controller through BoilerSensor)
    // =====================================================================

    int successiveStopCount() {
        return successiveStops;
    }

    boolean transmissionFailed() {
        return transmissionFailed;
    }

    boolean plantWaiting() {
        return plantWaitingLatched;
    }

    boolean unitsReady() {
        return unitsReadyReceived;
    }

    double levelQuantity() {
        return levelQuantity;
    }

    double steamQuantity() {
        return steamQuantity;
    }

    double pumpQuantity() {
        return pumpQuantity;
    }

    boolean levelOutOfOrder() {
        return levelStatus.isOutOfOrder();
    }

    boolean steamOutOfOrder() {
        return steamStatus.isOutOfOrder();
    }

    boolean anyPumpOutOfOrder() {
        boolean failed = false;
        for (int slot = 0; slot < BoilerConstants.PUMP_COUNT; slot++) {
            failed = failed || pumpStatus[slot].isOutOfOrder();
        }
        return failed;
    }

    boolean anyControlOutOfOrder() {
        boolean failed = false;
        for (int slot = 0; slot < BoilerConstants.PUMP_COUNT; slot++) {
            failed = failed || controlStatus[slot].isOutOfOrder();
        }
        return failed;
    }

    boolean valveIsOpen() {
        return evacuationValveOpen;
    }

    int openPumpCount() {
        int count = 0;
        for (int slot = 0; slot < BoilerConstants.PUMP_COUNT; slot++) {
            if (pumpOpen[slot]) {
                count = count + 1;
            }
        }
        return count;
    }

    double lowerBound() {
        return levelLow;
    }

    double upperBound() {
        return levelHigh;
    }

    double referenceLevel() {
        if (levelStatus.isOutOfOrder()) {
            return (levelLow + levelHigh) / 2.0;
        }
        return levelQuantity;
    }

    double nextSteamOutflowMax() {
        return steamOutflowMax();
    }

    double nextSteamOutflowMin() {
        return steamOutflowMin();
    }

    double nextPumpInflowMax() {
        int pouring = 0;
        for (int slot = 0; slot < BoilerConstants.PUMP_COUNT; slot++) {
            if (pumpOpen[slot] || pumpStatus[slot].isOutOfOrder()) {
                pouring = pouring + 1;
            }
        }
        return pouring * BoilerConstants.PUMP_CAPACITY * BoilerConstants.CYCLE_DURATION;
    }
}
