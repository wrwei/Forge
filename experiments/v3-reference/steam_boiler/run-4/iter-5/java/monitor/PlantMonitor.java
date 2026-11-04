package steam_boiler.monitor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.BoilerConstants;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.BoilerMode;
import steam_boiler.domain.FlowObservation;
import steam_boiler.domain.LevelEstimate;
import steam_boiler.domain.UnitStatus;
import steam_boiler.event.Message;
import steam_boiler.event.OutputEvent;
import steam_boiler.event.PlantMessage;
import steam_boiler.event.ProtocolMessage;
import steam_boiler.sensor.BoilerSensor;

/**
 * The program's knowledge of the plant. It receives the messages of each
 * transmission (receive stage, SB-V3), analyses them (failure detection
 * SB-FR1 to SB-FR5, unit bookkeeping SB-V9, level estimate SB-V8, stop count
 * SB-V10), produces the failure/repair protocol messages (SB-FR6, SB-FR7) and
 * publishes the boolean judgements the controller's guards use to the
 * {@link BoilerSensor}. It learns the program's mode and whether
 * PROGRAM_READY has been sent from the messages the program transmits.
 */
public final class PlantMonitor {

    @RoboChartType("nat") private static final int PUMPS = BoilerConstants.PUMP_COUNT;
    @RoboChartType("real") private static final double DT = BoilerConstants.CYCLE_DURATION;
    /** Absorbs floating-point rounding when a reading is compared with a computed bound. */
    @RoboChartType("real") private static final double TOLERANCE = 1.0E-9;
    private static final UnitStatus SOUND = new UnitStatus(false, true);
    private static final UnitStatus DETECTED = new UnitStatus(true, false);

    // ---- contents of the current transmission (receive stage) ----
    private boolean stopPresent = false;
    private boolean waitingPresent = false;
    private boolean readyPresent = false;
    private boolean levelSeen = false;
    private boolean steamSeen = false;
    @RoboChartType("real") private double levelValue = 0.0;
    @RoboChartType("real") private double steamValue = 0.0;
    private final boolean[] pumpStateSeen = new boolean[PUMPS];
    private final boolean[] runningReport = new boolean[PUMPS];
    private final boolean[] controlStateSeen = new boolean[PUMPS];
    private final boolean[] flowReport = new boolean[PUMPS];
    private final boolean[] pumpRepairedSeen = new boolean[PUMPS];
    private final boolean[] controlRepairedSeen = new boolean[PUMPS];
    private final boolean[] pumpAckSeen = new boolean[PUMPS];
    private final boolean[] controlAckSeen = new boolean[PUMPS];
    private boolean levelRepairedSeen = false;
    private boolean steamRepairedSeen = false;
    private boolean levelAckSeen = false;
    private boolean steamAckSeen = false;
    /** A message was duplicated or named a pump that does not exist. */
    private boolean malformed = false;
    private boolean levelJustRepaired = false;
    private final List<ProtocolMessage> repairAcknowledgements = new ArrayList<>();

    // ---- unit bookkeeping (SB-V9) ----
    private UnitStatus levelUnit = SOUND;
    private UnitStatus steamUnit = SOUND;
    private final UnitStatus[] pumpUnits = new UnitStatus[PUMPS];
    private final UnitStatus[] controlUnits = new UnitStatus[PUMPS];

    // ---- expectations used by failure detection (SB-FR1, SB-FR2) ----
    private final boolean[] pumpObserved = new boolean[PUMPS];
    private final boolean[] expectedRunning = new boolean[PUMPS];
    private final boolean[] previousRunning = new boolean[PUMPS];
    private final boolean[] flowObserved = new boolean[PUMPS];
    private final boolean[] expectedFlow = new boolean[PUMPS];
    private final boolean[] previousFlow = new boolean[PUMPS];
    /** An order that changed pump n's state was in the last transmission. */
    private final boolean[] orderedLastCycle = new boolean[PUMPS];
    private boolean valveOpen = false;

    // ---- what the program has transmitted so far ----
    private BoilerMode announcedMode = BoilerMode.INITIALIZATION;
    private boolean programReadySent = false;
    /** STEAM_BOILER_WAITING has arrived during initialization (SB-Beh2). */
    private boolean plantWaiting = false;

    // ---- program variables ----
    @RoboChartType("nat") private int stopCount = 0;
    private boolean transmissionFailed = false;
    @RoboChartType("real") private double lastLevel = 0.0;
    @RoboChartType("real") private double lastSteam = 0.0;
    @RoboChartType("real") private double trustedSteam = 0.0;
    private boolean steamTrusted = false;
    private LevelEstimate estimate = new LevelEstimate(0.0, BoilerConstants.CAPACITY);
    private boolean levelGrounded = false;
    @RoboChartType("real") private double throughput = 0.0;

    // ---- results of the last analysis ----
    private boolean levelOutOfOrder = false;
    private boolean steamOutOfOrder = false;
    @RoboChartType("nat") private int failedPumps = 0;
    @RoboChartType("nat") private int failedControls = 0;
    @RoboChartType("real") private double currentLevel = 0.0;
    @RoboChartType("real") private double minimumNext = 0.0;
    @RoboChartType("real") private double maximumNext = BoilerConstants.CAPACITY;

    // ---- boolean judgements the controller's guards use ----
    private boolean initialisationAbort = false;
    private boolean operationAbort = false;
    private boolean rescueAbort = false;
    private boolean otherUnitOutOfOrder = false;
    private boolean underNormalBand = false;
    private boolean overNormalBand = false;

    /** Creates the monitor with every unit believed sound. */
    public PlantMonitor() {
        for (int i = 0; i < PUMPS; i++) {
            pumpUnits[i] = SOUND;
            controlUnits[i] = SOUND;
        }
    }

    // =====================================================================
    // Receive stage
    // =====================================================================

    /** Clears the record of the previous transmission. */
    public void beginCycle() {
        stopPresent = false;
        waitingPresent = false;
        readyPresent = false;
        levelSeen = false;
        steamSeen = false;
        for (int i = 0; i < PUMPS; i++) {
            pumpStateSeen[i] = false;
            controlStateSeen[i] = false;
            pumpRepairedSeen[i] = false;
            controlRepairedSeen[i] = false;
            pumpAckSeen[i] = false;
            controlAckSeen[i] = false;
        }
        levelRepairedSeen = false;
        steamRepairedSeen = false;
        levelAckSeen = false;
        steamAckSeen = false;
        malformed = false;
        levelJustRepaired = false;
        repairAcknowledgements.clear();
    }

    /** Records one message of the current transmission. */
    public void receive(PlantMessage message) {
        if (message instanceof PlantMessage.StopRequest) {
            stopPresent = true;
        } else if (message instanceof PlantMessage.SteamBoilerWaiting) {
            waitingPresent = true;
        } else if (message instanceof PlantMessage.PhysicalUnitsReady) {
            readyPresent = true;
        } else if (message instanceof PlantMessage.Level) {
            PlantMessage.Level reading = (PlantMessage.Level) message;
            malformed = malformed || levelSeen;
            levelSeen = true;
            levelValue = reading.value();
        } else if (message instanceof PlantMessage.Steam) {
            PlantMessage.Steam reading = (PlantMessage.Steam) message;
            malformed = malformed || steamSeen;
            steamSeen = true;
            steamValue = reading.value();
        } else if (message instanceof PlantMessage.PumpState) {
            PlantMessage.PumpState reading = (PlantMessage.PumpState) message;
            int index = reading.value().pumpNumber() - 1;
            if (validIndex(index)) {
                malformed = malformed || pumpStateSeen[index];
                pumpStateSeen[index] = true;
                runningReport[index] = reading.value().pumpRunning();
            } else {
                malformed = true;
            }
        } else if (message instanceof PlantMessage.PumpControlState) {
            PlantMessage.PumpControlState reading = (PlantMessage.PumpControlState) message;
            int index = reading.value().controlledPump() - 1;
            if (validIndex(index)) {
                malformed = malformed || controlStateSeen[index];
                controlStateSeen[index] = true;
                flowReport[index] = reading.value().observation() == FlowObservation.WATER_FLOWS;
            } else {
                malformed = true;
            }
        } else if (message instanceof PlantMessage.PumpRepaired) {
            PlantMessage.PumpRepaired repaired = (PlantMessage.PumpRepaired) message;
            markPump(pumpRepairedSeen, repaired.pump());
        } else if (message instanceof PlantMessage.PumpControlRepaired) {
            PlantMessage.PumpControlRepaired repaired = (PlantMessage.PumpControlRepaired) message;
            markPump(controlRepairedSeen, repaired.pump());
        } else if (message instanceof PlantMessage.LevelRepaired) {
            levelRepairedSeen = true;
        } else if (message instanceof PlantMessage.SteamRepaired) {
            steamRepairedSeen = true;
        } else if (message instanceof PlantMessage.PumpFailureAcknowledgement) {
            PlantMessage.PumpFailureAcknowledgement ack = (PlantMessage.PumpFailureAcknowledgement) message;
            markPump(pumpAckSeen, ack.pump());
        } else if (message instanceof PlantMessage.PumpControlFailureAcknowledgement) {
            PlantMessage.PumpControlFailureAcknowledgement ack =
                    (PlantMessage.PumpControlFailureAcknowledgement) message;
            markPump(controlAckSeen, ack.pump());
        } else if (message instanceof PlantMessage.LevelFailureAcknowledgement) {
            levelAckSeen = true;
        } else if (message instanceof PlantMessage.SteamOutcomeFailureAcknowledgement) {
            steamAckSeen = true;
        }
    }

    // =====================================================================
    // Analysis stage
    // =====================================================================

    /** Analyses the complete transmission (after the receive stage). */
    public void analyse() {
        // SB-V10: successive transmissions carrying STOP.
        if (stopPresent) {
            stopCount = stopCount + 1;
        } else {
            stopCount = 0;
        }

        // SB-FR5: the four data messages must be present for every unit.
        boolean missing = !levelSeen || !steamSeen;
        for (int i = 0; i < PUMPS; i++) {
            if (!pumpStateSeen[i] || !controlStateSeen[i]) {
                missing = true;
            }
        }
        // SB-FR6, SB-FR7: acknowledgements and repairs; an unexpected one is aberrant.
        boolean unexpectedAck = processAcknowledgements();
        boolean unexpectedRepair = processRepairs();
        // SB-FR5: the start-up messages belong to initialization, and
        // PHYSICAL_UNITS_READY can only answer a PROGRAM_READY already sent.
        boolean initializing = announcedMode == BoilerMode.INITIALIZATION;
        boolean startupMessageAberrant = (readyPresent && !programReadySent)
                || ((waitingPresent || readyPresent) && !initializing);
        transmissionFailed = missing || malformed || unexpectedAck || unexpectedRepair || startupMessageAberrant;
        if (waitingPresent && initializing) {
            plantWaiting = true;
        }
        if (transmissionFailed) {
            summarise();
            return;
        }

        double steamAtStart = trustedSteam;
        boolean steamAtStartKnown = steamTrusted;
        judgeSteam();
        judgePumps();
        judgePumpControls();
        advanceLevel(steamAtStart, steamAtStartKnown);
        computeThroughput();
        predictNextCycle();
        for (int i = 0; i < PUMPS; i++) {
            previousRunning[i] = runningReport[i];
            previousFlow[i] = flowReport[i];
        }
        summarise();
    }

    /** Failure acknowledgements: each must answer an announcement still pending. */
    private boolean processAcknowledgements() {
        boolean unexpected = false;
        for (int i = 0; i < PUMPS; i++) {
            if (pumpAckSeen[i]) {
                if (awaitingAcknowledgement(pumpUnits[i])) {
                    pumpUnits[i] = new UnitStatus(true, true);
                } else {
                    unexpected = true;
                }
            }
            if (controlAckSeen[i]) {
                if (awaitingAcknowledgement(controlUnits[i])) {
                    controlUnits[i] = new UnitStatus(true, true);
                } else {
                    unexpected = true;
                }
            }
        }
        if (levelAckSeen) {
            if (awaitingAcknowledgement(levelUnit)) {
                levelUnit = new UnitStatus(true, true);
            } else {
                unexpected = true;
            }
        }
        if (steamAckSeen) {
            if (awaitingAcknowledgement(steamUnit)) {
                steamUnit = new UnitStatus(true, true);
            } else {
                unexpected = true;
            }
        }
        return unexpected;
    }

    /** Repair notifications: the unit is sound again and the repair is acknowledged. */
    private boolean processRepairs() {
        boolean unexpected = false;
        for (int i = 0; i < PUMPS; i++) {
            if (pumpRepairedSeen[i]) {
                if (pumpUnits[i].outOfOrder()) {
                    pumpUnits[i] = SOUND;
                    pumpObserved[i] = false;
                    repairAcknowledgements.add(new ProtocolMessage.PumpRepairedAcknowledgement(i + 1));
                } else {
                    unexpected = true;
                }
            }
            if (controlRepairedSeen[i]) {
                if (controlUnits[i].outOfOrder()) {
                    controlUnits[i] = SOUND;
                    flowObserved[i] = false;
                    repairAcknowledgements.add(new ProtocolMessage.PumpControlRepairedAcknowledgement(i + 1));
                } else {
                    unexpected = true;
                }
            }
        }
        if (levelRepairedSeen) {
            if (levelUnit.outOfOrder()) {
                levelUnit = SOUND;
                levelJustRepaired = true;
                repairAcknowledgements.add(new ProtocolMessage.LevelRepairedAcknowledgement());
            } else {
                unexpected = true;
            }
        }
        if (steamRepairedSeen) {
            if (steamUnit.outOfOrder()) {
                steamUnit = SOUND;
                steamTrusted = false;
                repairAcknowledgements.add(new ProtocolMessage.SteamRepairedAcknowledgement());
            } else {
                unexpected = true;
            }
        }
        return unexpected;
    }

    /**
     * SB-FR4: static range [0, W] and the gradients U1 and U2. SB-Beh3: once
     * the plant is waiting in initialization, any non-zero steam rate means
     * the steam device is defective.
     */
    private void judgeSteam() {
        lastSteam = steamValue;
        if (steamUnit.outOfOrder()) {
            steamTrusted = false;
            return;
        }
        boolean startupSteamNonZero = announcedMode == BoilerMode.INITIALIZATION && plantWaiting
                && steamValue != 0.0;
        boolean outOfRange = steamValue < 0.0 || steamValue > BoilerConstants.MAX_STEAM || startupSteamNonZero;
        boolean tooFast = steamTrusted
                && (steamValue > trustedSteam + BoilerConstants.STEAM_INCREASE_GRADIENT * DT + TOLERANCE
                    || steamValue < trustedSteam - BoilerConstants.STEAM_DECREASE_GRADIENT * DT - TOLERANCE);
        if (outOfRange || tooFast) {
            steamUnit = DETECTED;
            steamTrusted = false;
        } else {
            trustedSteam = steamValue;
            steamTrusted = true;
        }
    }

    /**
     * SB-FR1: a pump must report the state it was last ordered into (the order
     * having been sent in the previous transmission), and must not change state
     * when no order was sent.
     */
    private void judgePumps() {
        for (int i = 0; i < PUMPS; i++) {
            if (pumpUnits[i].outOfOrder()) {
                continue;
            }
            if (!pumpObserved[i]) {
                expectedRunning[i] = runningReport[i];
                pumpObserved[i] = true;
            } else if (runningReport[i] != expectedRunning[i]) {
                pumpUnits[i] = DETECTED;
            }
        }
    }

    /**
     * SB-FR2: from the second transmission after an order, a pump controller
     * must report the flow the order implies, provided the pump itself is
     * known to work; it must not change its report spontaneously. The first
     * transmission after an order is not judged (pump start-up latency,
     * SB-Beh25).
     */
    private void judgePumpControls() {
        for (int i = 0; i < PUMPS; i++) {
            if (controlUnits[i].outOfOrder()) {
                continue;
            }
            if (!flowObserved[i] || pumpUnits[i].outOfOrder()) {
                expectedFlow[i] = flowReport[i];
                flowObserved[i] = true;
            } else if (!orderedLastCycle[i] && flowReport[i] != expectedFlow[i]) {
                controlUnits[i] = DETECTED;
            }
        }
    }

    /**
     * SB-FR3, SB-V8, SB-Beh19, SB-Beh25: advances the level bounds over the
     * elapsed cycle under worst-case steam dynamics and the water the pumps can
     * have supplied (one litre pumped is one litre in the boiler), then judges
     * the level reading against the static range [0, C] and those bounds.
     */
    private void advanceLevel(@RoboChartType("real") double steamAtStart, boolean steamAtStartKnown) {
        double perPump = BoilerConstants.PUMP_CAPACITY * DT;
        double inLow = 0.0;
        double inHigh = 0.0;
        for (int i = 0; i < PUMPS; i++) {
            if (!controlUnits[i].outOfOrder()) {
                // Water flowed for the whole cycle only if it flowed at both ends and no order intervened.
                if (previousFlow[i] && flowReport[i] && !orderedLastCycle[i]) {
                    inLow = inLow + perPump;
                }
                if (previousFlow[i] || flowReport[i]) {
                    inHigh = inHigh + perPump;
                }
            } else if (!pumpUnits[i].outOfOrder()) {
                if (previousRunning[i] || runningReport[i]) {
                    inHigh = inHigh + perPump;
                }
            } else {
                inHigh = inHigh + perPump;
            }
        }
        double outLow = 0.0;
        double outHigh = BoilerConstants.MAX_STEAM * DT;
        if (steamAtStartKnown) {
            outLow = Math.max(0.0, steamAtStart * DT - 0.5 * BoilerConstants.STEAM_DECREASE_GRADIENT * DT * DT);
            outHigh = Math.min(outHigh, steamAtStart * DT + 0.5 * BoilerConstants.STEAM_INCREASE_GRADIENT * DT * DT);
        }

        double lower = 0.0;
        double upper = BoilerConstants.CAPACITY;
        if (levelGrounded) {
            lower = clampLevel(estimate.lowerBound() + inLow - outHigh);
            upper = clampLevel(estimate.upperBound() + inHigh - outLow);
            if (valveOpen) {
                // The evacuation rate is not specified: the valve may have drained any quantity.
                lower = 0.0;
            }
        }

        if (levelUnit.outOfOrder()) {
            estimate = new LevelEstimate(lower, upper);
            return;
        }
        boolean outOfRange = levelValue < 0.0 || levelValue > BoilerConstants.CAPACITY;
        boolean unreachable = levelGrounded && !levelJustRepaired
                && (levelValue < lower - TOLERANCE || levelValue > upper + TOLERANCE);
        if (outOfRange || unreachable) {
            levelUnit = DETECTED;
            estimate = new LevelEstimate(lower, upper);
        } else {
            lastLevel = levelValue;
            estimate = new LevelEstimate(levelValue, levelValue);
            levelGrounded = true;
        }
    }

    /** SB-V7: p from the pump-controller reports, or the pump reports where a controller is out of order. */
    private void computeThroughput() {
        int pouring = 0;
        for (int i = 0; i < PUMPS; i++) {
            if (!controlUnits[i].outOfOrder()) {
                if (flowReport[i]) {
                    pouring = pouring + 1;
                }
            } else if (!pumpUnits[i].outOfOrder() && runningReport[i] && previousRunning[i]) {
                pouring = pouring + 1;
            }
        }
        throughput = pouring * BoilerConstants.PUMP_CAPACITY;
    }

    /**
     * SB-V4, SB-Beh13: the range the level can reach by the end of the next
     * cycle under the worst behaviour of the units: for the minimum, maximal
     * steam and no pump pouring; for the maximum, minimal steam and every pump
     * pouring.
     */
    private void predictNextCycle() {
        double outLow = 0.0;
        double outHigh = BoilerConstants.MAX_STEAM * DT;
        if (steamTrusted) {
            outLow = Math.max(0.0, trustedSteam * DT - 0.5 * BoilerConstants.STEAM_DECREASE_GRADIENT * DT * DT);
            outHigh = Math.min(outHigh, trustedSteam * DT + 0.5 * BoilerConstants.STEAM_INCREASE_GRADIENT * DT * DT);
        }
        double allPumps = BoilerConstants.PUMP_COUNT * BoilerConstants.PUMP_CAPACITY * DT;
        minimumNext = estimate.lowerBound() - outHigh;
        maximumNext = estimate.upperBound() + allPumps - outLow;
    }

    /** Fixes the answers to the controller's queries for this cycle. */
    private void summarise() {
        levelOutOfOrder = levelUnit.outOfOrder();
        steamOutOfOrder = steamUnit.outOfOrder();
        int pumps = 0;
        int controls = 0;
        for (int i = 0; i < PUMPS; i++) {
            if (pumpUnits[i].outOfOrder()) {
                pumps = pumps + 1;
            }
            if (controlUnits[i].outOfOrder()) {
                controls = controls + 1;
            }
        }
        failedPumps = pumps;
        failedControls = controls;
        if (levelOutOfOrder) {
            currentLevel = (estimate.lowerBound() + estimate.upperBound()) / 2.0;
        } else {
            currentLevel = lastLevel;
        }

        boolean shutdown = stopCount >= BoilerConstants.STOP_LIMIT || transmissionFailed;
        boolean levelRisk = minimumNext < BoilerConstants.MIN_LIMIT || maximumNext > BoilerConstants.MAX_LIMIT;
        boolean estimateUntrustworthy = steamOutOfOrder || failedControls > 0;
        // SB-Beh3, SB-Beh6, SB-Beh9, SB-Beh23.
        initialisationAbort = shutdown || (plantWaiting && (levelOutOfOrder || steamOutOfOrder));
        // SB-Beh13, SB-Beh14, SB-Beh18, SB-Beh23; SB-FR8: losing the level device
        // when the estimate cannot be trusted leaves no rescue to fall back on.
        operationAbort = shutdown || levelRisk || (levelOutOfOrder && estimateUntrustworthy);
        // SB-Beh21, SB-Beh23.
        rescueAbort = shutdown || levelRisk || estimateUntrustworthy;
        otherUnitOutOfOrder = steamOutOfOrder || failedPumps > 0 || failedControls > 0;
        underNormalBand = currentLevel < BoilerConstants.MIN_NORMAL;
        overNormalBand = currentLevel > BoilerConstants.MAX_NORMAL;
    }

    // =====================================================================
    // Transmit stage
    // =====================================================================

    /**
     * Failure detections to repeat until acknowledged (SB-FR6) and repair
     * acknowledgements due this cycle (SB-FR7).
     */
    public List<ProtocolMessage> protocolMessages() {
        List<ProtocolMessage> messages = new ArrayList<>();
        for (int i = 0; i < PUMPS; i++) {
            if (awaitingAcknowledgement(pumpUnits[i])) {
                messages.add(new ProtocolMessage.PumpFailureDetection(i + 1));
            }
        }
        for (int i = 0; i < PUMPS; i++) {
            if (awaitingAcknowledgement(controlUnits[i])) {
                messages.add(new ProtocolMessage.PumpControlFailureDetection(i + 1));
            }
        }
        if (awaitingAcknowledgement(levelUnit)) {
            messages.add(new ProtocolMessage.LevelFailureDetection());
        }
        if (awaitingAcknowledgement(steamUnit)) {
            messages.add(new ProtocolMessage.SteamFailureDetection());
        }
        messages.addAll(repairAcknowledgements);
        return messages;
    }

    /**
     * Records the messages the program transmitted at the end of this cycle:
     * pump orders set what the next transmissions must show (SB-FR1, SB-FR2),
     * VALVE toggles the evacuation valve, and MODE and PROGRAM_READY tell
     * which start-up messages the plant may send next (SB-FR5).
     */
    public void recordTransmitted(List<Message> transmitted) {
        for (int i = 0; i < PUMPS; i++) {
            orderedLastCycle[i] = false;
        }
        for (Message message : transmitted) {
            if (message instanceof OutputEvent.ModeMessage) {
                OutputEvent.ModeMessage modeMessage = (OutputEvent.ModeMessage) message;
                announcedMode = modeMessage.value();
            } else if (message instanceof OutputEvent.ProgramReady) {
                programReadySent = true;
            } else if (message instanceof OutputEvent.OpenPump) {
                OutputEvent.OpenPump order = (OutputEvent.OpenPump) message;
                recordOrder(order.pump() - 1, true);
            } else if (message instanceof OutputEvent.ClosePump) {
                OutputEvent.ClosePump order = (OutputEvent.ClosePump) message;
                recordOrder(order.pump() - 1, false);
            } else if (message instanceof OutputEvent.Valve) {
                valveOpen = !valveOpen;
            }
        }
    }

    /** An order counts for failure detection only when it changes the pump's expected state. */
    private void recordOrder(int index, boolean running) {
        if (!validIndex(index)) {
            return;
        }
        if (!pumpObserved[index] || expectedRunning[index] != running) {
            expectedRunning[index] = running;
            pumpObserved[index] = true;
            expectedFlow[index] = running;
            flowObserved[index] = true;
            orderedLastCycle[index] = true;
        }
    }

    // =====================================================================
    // Boolean judgements published to the sensor for the controller's guards
    // =====================================================================

    /** Publishes this cycle's judgements to the sensor the controller reads. */
    public void publish(BoilerSensor sensor) {
        sensor.update(initialisationAbort, operationAbort, rescueAbort, plantWaiting, readyPresent,
                levelOutOfOrder, otherUnitOutOfOrder, underNormalBand, overNormalBand);
    }

    /**
     * Emergency stop is due in initialization (SB-Beh3, SB-Beh6, SB-Beh9,
     * SB-Beh23): STOP on three successive transmissions, a transmission
     * failure, or, once the plant is waiting, a defective level device or a
     * steam device that is defective or reports a non-zero rate.
     */
    public boolean initialisationEmergency() {
        return initialisationAbort;
    }

    /**
     * Emergency stop is due in normal or degraded mode (SB-Beh13, SB-Beh14,
     * SB-Beh18, SB-Beh23, SB-FR8): STOP on three successive transmissions, a
     * transmission failure, a level at risk of reaching M1 or M2, or a level
     * device failure while the steam device or a pump controller is out of
     * order.
     */
    public boolean operationEmergency() {
        return operationAbort;
    }

    /**
     * Emergency stop is due in rescue mode (SB-Beh21, SB-Beh23): STOP on three
     * successive transmissions, a transmission failure, an estimated level at
     * risk of reaching M1 or M2, or the steam device or a pump controller out
     * of order.
     */
    public boolean rescueEmergency() {
        return rescueAbort;
    }

    /** SB-Beh2: STEAM_BOILER_WAITING has arrived during initialization. */
    public boolean steamBoilerWaitingReceived() {
        return plantWaiting;
    }

    /** SB-Beh8: PHYSICAL_UNITS_READY is part of the current transmission. */
    public boolean physicalUnitsReadyReceived() {
        return readyPresent;
    }

    /** SB-FR3, SB-V9: the level measuring device is out of order. */
    public boolean levelUnitFailed() {
        return levelOutOfOrder;
    }

    /** SB-V9: some unit other than the level measuring device (a pump, a pump controller, the steam device) is out of order. */
    public boolean nonLevelUnitFailed() {
        return otherUnitOutOfOrder;
    }

    /** SB-Beh4, SB-Beh5, SB-Beh10: the water level (or, in rescue, its estimate) is below N1. */
    public boolean levelUnderNormalBand() {
        return underNormalBand;
    }

    /** SB-Beh4, SB-Beh10: the water level (or, in rescue, its estimate) is above N2. */
    public boolean levelOverNormalBand() {
        return overNormalBand;
    }

    // =====================================================================
    // Other program variables
    // =====================================================================

    /** SB-V10: number of successive transmissions that carried STOP. */
    @RoboChartType("nat")
    public int consecutiveStops() {
        return stopCount;
    }

    /** SB-FR5: the current transmission is missing an indispensable message or carries an aberrant one. */
    public boolean transmissionFailure() {
        return transmissionFailed;
    }

    /** SB-FR4, SB-V9: the steam measuring device is out of order. */
    public boolean steamUnitFailed() {
        return steamOutOfOrder;
    }

    /** SB-FR1, SB-V9: number of pumps out of order. */
    @RoboChartType("nat")
    public int failedPumpCount() {
        return failedPumps;
    }

    /** SB-FR2, SB-V9: number of pump controllers out of order. */
    @RoboChartType("nat")
    public int failedPumpControlCount() {
        return failedControls;
    }

    /** SB-V6: v, the steam rate most recently reported (litre/sec). */
    @RoboChartType("real")
    public double steamRate() {
        return lastSteam;
    }

    /** SB-V5, SB-V8: q while the level device works, the middle of the estimate otherwise (litre). */
    @RoboChartType("real")
    public double waterLevel() {
        return currentLevel;
    }

    /** SB-V4, SB-Beh13: the least level the boiler can hold after one more cycle of worst-case evolution. */
    @RoboChartType("real")
    public double minimumLevelNextCycle() {
        return minimumNext;
    }

    /** SB-V4, SB-Beh13: the greatest level the boiler can hold after one more cycle of worst-case evolution. */
    @RoboChartType("real")
    public double maximumLevelNextCycle() {
        return maximumNext;
    }

    /** SB-V7: p, the current throughput of the pumps taken together (litre/sec). */
    @RoboChartType("real")
    public double pumpThroughput() {
        return throughput;
    }

    /** SB-V8: the computed lower and upper bound on the water level. */
    public LevelEstimate levelEstimate() {
        return estimate;
    }

    /** SB-V9: status of pump n (1 to 4). */
    public UnitStatus pumpStatus(@RoboChartType("nat") int pumpNumber) {
        return pumpUnits[checkedIndex(pumpNumber)];
    }

    /** SB-V9: status of the controller of pump n (1 to 4). */
    public UnitStatus pumpControlStatus(@RoboChartType("nat") int pumpNumber) {
        return controlUnits[checkedIndex(pumpNumber)];
    }

    /** SB-V9: status of the level measuring device. */
    public UnitStatus levelStatus() {
        return levelUnit;
    }

    /** SB-V9: status of the steam measuring device. */
    public UnitStatus steamStatus() {
        return steamUnit;
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private static boolean awaitingAcknowledgement(UnitStatus status) {
        return status.outOfOrder() && !status.detectionAcknowledged();
    }

    private static boolean validIndex(int index) {
        return index >= 0 && index < PUMPS;
    }

    private static int checkedIndex(int pumpNumber) {
        int index = pumpNumber - 1;
        if (!validIndex(index)) {
            throw new IllegalArgumentException("no pump " + pumpNumber);
        }
        return index;
    }

    private void markPump(boolean[] seen, int pumpNumber) {
        int index = pumpNumber - 1;
        if (validIndex(index)) {
            malformed = malformed || seen[index];
            seen[index] = true;
        } else {
            malformed = true;
        }
    }

    @RoboChartType("real")
    private static double clampLevel(@RoboChartType("real") double litres) {
        return Math.min(BoilerConstants.CAPACITY, Math.max(0.0, litres));
    }
}
