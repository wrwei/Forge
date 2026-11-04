package steam_boiler.sensor;

import java.util.List;
import steam_boiler.BoilerConstants;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.event.FlowObservation;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.event.PumpControlStateData;
import steam_boiler.event.PumpStateData;

/**
 * The program's view of the plant: the receive stage of each cycle (SB-V3)
 * and the analysis of what was received. It holds the current measures q, v
 * and p (SB-V5, SB-V6, SB-V7), the level estimate (SB-V8), the status of every
 * unit (SB-V9) and the STOP count (SB-V10), and judges unit and transmission
 * failures (SB-FR1 to SB-FR5). Pump numbers run from 1 to 4.
 *
 * <p>Per cycle: {@link #beginTransmission()}, {@link #receive(InputEvent)} for
 * every incoming message, {@link #analyse()}; after the transmit stage,
 * {@link #recordTransmission(List)} with the messages that were sent.
 */
public final class BoilerSensor {

    @RoboChartType("nat") private static final int PUMPS = BoilerConstants.PUMP_COUNT;
    @RoboChartType("real") private static final double T = BoilerConstants.CYCLE_PERIOD;
    @RoboChartType("real") private static final double U1 = BoilerConstants.GRADIENT_U1;
    @RoboChartType("real") private static final double U2 = BoilerConstants.GRADIENT_U2;
    @RoboChartType("real") private static final double W = BoilerConstants.STEAM_MAX_W;
    @RoboChartType("real") private static final double P = BoilerConstants.PUMP_CAPACITY_P;

    // Messages of the current transmission.
    private boolean stopReceived;
    private boolean waitingReceived;
    private boolean unitsReadyReceived;
    private boolean malformedMessage;
    private boolean levelSeen;
    private boolean steamSeen;
    @RoboChartType("real") private double levelReading;
    @RoboChartType("real") private double steamReading;
    private final boolean[] pumpStateSeen = new boolean[PUMPS];
    private final boolean[] pumpControlSeen = new boolean[PUMPS];
    private final boolean[] pumpOn = new boolean[PUMPS];
    private final boolean[] controlFlow = new boolean[PUMPS];
    private final boolean[] pumpRepairedReceived = new boolean[PUMPS];
    private final boolean[] controlRepairedReceived = new boolean[PUMPS];
    private final boolean[] pumpAckReceived = new boolean[PUMPS];
    private final boolean[] controlAckReceived = new boolean[PUMPS];
    private boolean levelRepairedReceived;
    private boolean steamRepairedReceived;
    private boolean levelAckReceived;
    private boolean steamAckReceived;

    // Memory carried from cycle to cycle.
    @RoboChartType("nat") private int stopCount;
    private boolean plantWaiting;
    private boolean programReadySent;
    private boolean operating;
    private final UnitStatus[] pumpUnits = new UnitStatus[PUMPS];
    private final UnitStatus[] controlUnits = new UnitStatus[PUMPS];
    private final UnitStatus levelUnit = new UnitStatus();
    private final UnitStatus steamUnit = new UnitStatus();
    private boolean previousKnown;
    private final boolean[] previousPumpOn = new boolean[PUMPS];
    private final boolean[] previousControlFlow = new boolean[PUMPS];
    private final boolean[] orderedOpen = new boolean[PUMPS];
    private final boolean[] orderedClose = new boolean[PUMPS];

    // Current measures (SB-V5 to SB-V8).
    @RoboChartType("real") private double q;
    @RoboChartType("real") private double v;
    @RoboChartType("real") private double p;
    @RoboChartType("real") private double levelLow;
    @RoboChartType("real") private double levelHigh = BoilerConstants.CAPACITY_C;
    private boolean levelBoundsKnown;
    @RoboChartType("real") private double steamLow;
    @RoboChartType("real") private double steamHigh = BoilerConstants.STEAM_MAX_W;
    @RoboChartType("real") private double outflowLow;
    @RoboChartType("real") private double outflowHigh;
    @RoboChartType("real") private double inflowLow;
    @RoboChartType("real") private double inflowHigh;
    @RoboChartType("real") private double sureInflowAhead;
    @RoboChartType("real") private double unstoppableInflowAhead;

    // Judgements of the current cycle, read by the controller.
    private boolean operatorStop;
    private boolean transmissionFailed;
    private boolean initialEmergencyFound;
    private boolean startConfirmedFound;
    private boolean drainWanted;
    private boolean fillWanted;
    private boolean runningEmergencyFound;
    private boolean estimateUnreliableFound;
    private boolean levelFailed;
    private boolean steamFailed;
    private boolean controlFailed;
    private boolean nonLevelFailed;
    private boolean levelRiskDetected;
    @RoboChartType("nat") private int pumpsNeeded;

    /** Creates the sensor with every unit believed sound. */
    public BoilerSensor() {
        for (int i = 0; i < PUMPS; i++) {
            pumpUnits[i] = new UnitStatus();
            controlUnits[i] = new UnitStatus();
        }
    }

    /** Starts the receive stage of a new cycle. */
    public void beginTransmission() {
        stopReceived = false;
        waitingReceived = false;
        unitsReadyReceived = false;
        malformedMessage = false;
        levelSeen = false;
        steamSeen = false;
        levelRepairedReceived = false;
        steamRepairedReceived = false;
        levelAckReceived = false;
        steamAckReceived = false;
        for (int i = 0; i < PUMPS; i++) {
            pumpStateSeen[i] = false;
            pumpControlSeen[i] = false;
            pumpRepairedReceived[i] = false;
            controlRepairedReceived[i] = false;
            pumpAckReceived[i] = false;
            controlAckReceived[i] = false;
            pumpUnits[i].beginCycle();
            controlUnits[i].beginCycle();
        }
        levelUnit.beginCycle();
        steamUnit.beginCycle();
    }

    /** Records one incoming message of the current transmission (SB-DM9). */
    public void receive(InputEvent message) {
        if (message instanceof InputEvent.StopRequest) {
            malformedMessage = malformedMessage || stopReceived;
            stopReceived = true;
        } else if (message instanceof InputEvent.SteamBoilerWaiting) {
            malformedMessage = malformedMessage || waitingReceived;
            waitingReceived = true;
        } else if (message instanceof InputEvent.PhysicalUnitsReady) {
            malformedMessage = malformedMessage || unitsReadyReceived;
            unitsReadyReceived = true;
        } else if (message instanceof InputEvent.PumpState) {
            InputEvent.PumpState state = (InputEvent.PumpState) message;
            receivePumpState(state.report());
        } else if (message instanceof InputEvent.PumpControlState) {
            InputEvent.PumpControlState state = (InputEvent.PumpControlState) message;
            receivePumpControlState(state.report());
        } else if (message instanceof InputEvent.Level) {
            InputEvent.Level level = (InputEvent.Level) message;
            malformedMessage = malformedMessage || levelSeen;
            levelSeen = true;
            levelReading = level.value();
        } else if (message instanceof InputEvent.Steam) {
            InputEvent.Steam steam = (InputEvent.Steam) message;
            malformedMessage = malformedMessage || steamSeen;
            steamSeen = true;
            steamReading = steam.value();
        } else if (message instanceof InputEvent.PumpRepaired) {
            InputEvent.PumpRepaired repaired = (InputEvent.PumpRepaired) message;
            markPumpMessage(pumpRepairedReceived, repaired.pump());
        } else if (message instanceof InputEvent.PumpControlRepaired) {
            InputEvent.PumpControlRepaired repaired = (InputEvent.PumpControlRepaired) message;
            markPumpMessage(controlRepairedReceived, repaired.pump());
        } else if (message instanceof InputEvent.LevelRepaired) {
            malformedMessage = malformedMessage || levelRepairedReceived;
            levelRepairedReceived = true;
        } else if (message instanceof InputEvent.SteamRepaired) {
            malformedMessage = malformedMessage || steamRepairedReceived;
            steamRepairedReceived = true;
        } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
            InputEvent.PumpFailureAcknowledgement ack = (InputEvent.PumpFailureAcknowledgement) message;
            markPumpMessage(pumpAckReceived, ack.pump());
        } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
            InputEvent.PumpControlFailureAcknowledgement ack =
                    (InputEvent.PumpControlFailureAcknowledgement) message;
            markPumpMessage(controlAckReceived, ack.pump());
        } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
            malformedMessage = malformedMessage || levelAckReceived;
            levelAckReceived = true;
        } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
            malformedMessage = malformedMessage || steamAckReceived;
            steamAckReceived = true;
        }
    }

    /**
     * Analyses the transmission received in this cycle: STOP count, transmission
     * check, acknowledgements and repairs, unit failure detection, measures,
     * level estimate, level risk and the number of pumps the regulation needs.
     * Before STEAM_BOILER_WAITING has arrived only the STOP count is kept
     * (SB-Beh2).
     */
    public void analyse() {
        if (stopReceived) {
            stopCount = stopCount + 1;
        } else {
            stopCount = 0;
        }
        if (waitingReceived) {
            plantWaiting = true;
        }
        if (levelSeen) {
            q = levelReading;
        }
        if (steamSeen) {
            v = steamReading;
        }
        boolean dataComplete = checkTransmission();
        if (plantWaiting && dataComplete) {
            handleAcknowledgements();
            handleRepairs();
            checkSteamDevice();
            checkPumps();
            checkPumpControllers();
            estimateInflow();
            checkLevelDevice();
            assessLevelRisk();
            planPumps();
            rememberCycle();
        }
        summarise();
    }

    /**
     * Learns which orders were sent in the transmit stage of this cycle, so
     * that the next cycle can judge the pumps' and controllers' answers.
     */
    public void recordTransmission(List<OutputEvent> sent) {
        for (int i = 0; i < PUMPS; i++) {
            orderedOpen[i] = false;
            orderedClose[i] = false;
        }
        for (OutputEvent message : sent) {
            if (message instanceof OutputEvent.OpenPump) {
                OutputEvent.OpenPump open = (OutputEvent.OpenPump) message;
                if (validPump(open.pump())) {
                    orderedOpen[open.pump() - 1] = true;
                }
            } else if (message instanceof OutputEvent.ClosePump) {
                OutputEvent.ClosePump close = (OutputEvent.ClosePump) message;
                if (validPump(close.pump())) {
                    orderedClose[close.pump() - 1] = true;
                }
            } else if (message instanceof OutputEvent.ProgramReady) {
                programReadySent = true;
            }
        }
    }

    // ---- Queries read by the controller's predicates and actions ----

    /** True when STOP has arrived on three successive transmissions (SB-Beh23). */
    public boolean stopRequestedThrice() {
        return operatorStop;
    }

    /** True when this transmission is judged failed (SB-FR5). */
    public boolean transmissionFailure() {
        return transmissionFailed;
    }

    /** True when STEAM_BOILER_WAITING is part of this transmission. */
    public boolean steamBoilerWaitingReceived() {
        return waitingReceived;
    }

    /**
     * Initialization must end in emergency stop: STOP on three successive
     * transmissions (SB-Beh23) or, once STEAM_BOILER_WAITING has arrived, a
     * transmission failure (SB-Beh9), a steam rate other than zero (SB-Beh3)
     * or a failed level measuring device (SB-Beh6).
     */
    public boolean initialEmergency() {
        return initialEmergencyFound;
    }

    /**
     * PHYSICAL_UNITS_READY has arrived in answer to PROGRAM_READY, after
     * STEAM_BOILER_WAITING (SB-Beh8).
     */
    public boolean startConfirmed() {
        return startConfirmedFound;
    }

    /** Before PROGRAM_READY: the level is above N2 and must be drained (SB-Beh4). */
    public boolean drainNeeded() {
        return drainWanted;
    }

    /** Before PROGRAM_READY: the level is below N1 and must be raised (SB-Beh5). */
    public boolean fillNeeded() {
        return fillWanted;
    }

    /**
     * Normal, degraded or rescue mode must end in emergency stop: STOP on three
     * successive transmissions (SB-Beh23), a transmission failure, including
     * STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY outside initialization
     * (SB-Beh14, SB-FR5), or a level that risks M1 or M2 (SB-Beh13, SB-Beh18).
     */
    public boolean runningEmergency() {
        return runningEmergencyFound;
    }

    /**
     * The level estimate cannot be trusted: the steam measuring device or a
     * pump controller is out of order (SB-Beh19, SB-Beh21).
     */
    public boolean estimateUnreliable() {
        return estimateUnreliableFound;
    }

    /** True when PHYSICAL_UNITS_READY is part of this transmission. */
    public boolean physicalUnitsReadyReceived() {
        return unitsReadyReceived;
    }

    /** True once STEAM_BOILER_WAITING has arrived, in this or an earlier transmission. */
    public boolean plantWaitingSignalled() {
        return plantWaiting;
    }

    /** True once PROGRAM_READY has been transmitted. */
    public boolean programReadyAnnounced() {
        return programReadySent;
    }

    /** q: the water level most recently reported (litre, SB-V5). */
    @RoboChartType("real")
    public double waterLevel() {
        return q;
    }

    /** v: the steam rate most recently reported (litre/sec, SB-V6). */
    @RoboChartType("real")
    public double steamRate() {
        return v;
    }

    /** True while the level measuring device is out of order. */
    public boolean levelDeviceFailed() {
        return levelFailed;
    }

    /** True while the steam measuring device is out of order. */
    public boolean steamDeviceFailed() {
        return steamFailed;
    }

    /** True while some pump controller is out of order. */
    public boolean pumpControlFailed() {
        return controlFailed;
    }

    /** True while some unit other than the level measuring device is out of order. */
    public boolean otherUnitFailed() {
        return nonLevelFailed;
    }

    /** True when the water level risks reaching M1 or M2 within one cycle (SB-Beh13). */
    public boolean levelRisk() {
        return levelRiskDetected;
    }

    /** Number of sound pumps the regulation wants switched on (SB-Beh10). */
    @RoboChartType("nat")
    public int pumpsRequired() {
        return pumpsNeeded;
    }

    // ---- Further measures and unit status ----

    /** p: the current throughput of the pumps taken together (litre/sec, SB-V7). */
    @RoboChartType("real")
    public double pumpThroughput() {
        return p;
    }

    /** Lower bound of the water level, estimated while the level device is out of order (SB-V8). */
    @RoboChartType("real")
    public double levelLowerBound() {
        return levelLow;
    }

    /** Upper bound of the water level, estimated while the level device is out of order (SB-V8). */
    @RoboChartType("real")
    public double levelUpperBound() {
        return levelHigh;
    }

    /** Number of successive transmissions that have carried STOP (SB-V10). */
    @RoboChartType("nat")
    public int stopCount() {
        return stopCount;
    }

    /** True when pump {@code pump} is believed sound. */
    public boolean pumpUsable(@RoboChartType("nat") int pump) {
        return validPump(pump) && !pumpUnits[pump - 1].outOfOrder();
    }

    /** True when pump {@code pump} reported itself on in this transmission. */
    public boolean pumpReportedOn(@RoboChartType("nat") int pump) {
        return validPump(pump) && pumpOn[pump - 1];
    }

    /** Status of pump {@code pump} (1 to 4). */
    public UnitStatus pumpStatus(@RoboChartType("nat") int pump) {
        return pumpUnits[pump - 1];
    }

    /** Status of the controller of pump {@code pump} (1 to 4). */
    public UnitStatus pumpControlStatus(@RoboChartType("nat") int pump) {
        return controlUnits[pump - 1];
    }

    /** Status of the level measuring device. */
    public UnitStatus levelStatus() {
        return levelUnit;
    }

    /** Status of the steam measuring device. */
    public UnitStatus steamStatus() {
        return steamUnit;
    }

    // ---- Receive stage helpers ----

    private void receivePumpState(PumpStateData report) {
        if (report == null || !validPump(report.pump()) || pumpStateSeen[report.pump() - 1]) {
            malformedMessage = true;
        } else {
            pumpStateSeen[report.pump() - 1] = true;
            pumpOn[report.pump() - 1] = report.switchedOn();
        }
    }

    private void receivePumpControlState(PumpControlStateData report) {
        if (report == null || !validPump(report.pump()) || pumpControlSeen[report.pump() - 1]) {
            malformedMessage = true;
        } else {
            pumpControlSeen[report.pump() - 1] = true;
            controlFlow[report.pump() - 1] = report.flow() == FlowObservation.WATER_FLOWING;
        }
    }

    private void markPumpMessage(boolean[] received, int pump) {
        if (!validPump(pump) || received[pump - 1]) {
            malformedMessage = true;
        } else {
            received[pump - 1] = true;
        }
    }

    private static boolean validPump(@RoboChartType("nat") int pump) {
        return pump >= 1 && pump <= PUMPS;
    }

    // ---- Analysis stage ----

    /**
     * SB-FR5: the four data messages must be present exactly once; a message is
     * aberrant when it is malformed or duplicated, when PHYSICAL_UNITS_READY
     * arrives before PROGRAM_READY was sent, when a failure acknowledgement
     * names a unit with no pending detection, or when a repair notification
     * names a unit that is not out of order. Returns whether the data messages
     * are complete.
     */
    private boolean checkTransmission() {
        boolean dataComplete = levelSeen && steamSeen;
        boolean aberrant = malformedMessage || (unitsReadyReceived && !programReadySent);
        for (int i = 0; i < PUMPS; i++) {
            dataComplete = dataComplete && pumpStateSeen[i] && pumpControlSeen[i];
            aberrant = aberrant
                    || (pumpAckReceived[i] && !pumpUnits[i].detectionPending())
                    || (controlAckReceived[i] && !controlUnits[i].detectionPending())
                    || (pumpRepairedReceived[i] && !pumpUnits[i].outOfOrder())
                    || (controlRepairedReceived[i] && !controlUnits[i].outOfOrder());
        }
        aberrant = aberrant
                || (levelAckReceived && !levelUnit.detectionPending())
                || (steamAckReceived && !steamUnit.detectionPending())
                || (levelRepairedReceived && !levelUnit.outOfOrder())
                || (steamRepairedReceived && !steamUnit.outOfOrder());
        transmissionFailed = !dataComplete || aberrant;
        return dataComplete;
    }

    /** SB-FR6: an acknowledgement ends the repetition of a failure detection. */
    private void handleAcknowledgements() {
        for (int i = 0; i < PUMPS; i++) {
            if (pumpAckReceived[i]) {
                pumpUnits[i].acknowledgeDetection();
            }
            if (controlAckReceived[i]) {
                controlUnits[i].acknowledgeDetection();
            }
        }
        if (levelAckReceived) {
            levelUnit.acknowledgeDetection();
        }
        if (steamAckReceived) {
            steamUnit.acknowledgeDetection();
        }
    }

    /** SB-FR7: a repaired unit is sound again and its repair must be acknowledged. */
    private void handleRepairs() {
        for (int i = 0; i < PUMPS; i++) {
            if (pumpRepairedReceived[i] && pumpUnits[i].outOfOrder()) {
                pumpUnits[i].repair();
            }
            if (controlRepairedReceived[i] && controlUnits[i].outOfOrder()) {
                controlUnits[i].repair();
            }
        }
        if (levelRepairedReceived && levelUnit.outOfOrder()) {
            levelUnit.repair();
        }
        if (steamRepairedReceived && steamUnit.outOfOrder()) {
            steamUnit.repair();
        }
    }

    /**
     * SB-FR4: the steam device fails when its reading leaves [0, W] or changes
     * faster than U1 (increase) or U2 (decrease) allow. Also computes the
     * bounds on the steam that left the boiler during the last cycle.
     */
    private void checkSteamDevice() {
        double previousLow = steamLow;
        double previousHigh = steamHigh;
        if (!steamUnit.outOfOrder() && !steamUnit.changedThisCycle()) {
            boolean outOfRange = steamReading < 0.0 || steamReading > W;
            boolean tooFast = previousKnown
                    && (steamReading > previousHigh + U1 * T || steamReading < previousLow - U2 * T);
            if (outOfRange || tooFast) {
                steamUnit.detectFailure();
            }
        }
        if (steamUnit.outOfOrder()) {
            steamLow = Math.max(0.0, previousLow - U2 * T);
            steamHigh = Math.min(W, previousHigh + U1 * T);
        } else {
            steamLow = steamReading;
            steamHigh = steamReading;
        }
        outflowLow = minimalOutflow(previousLow, steamLow);
        outflowHigh = maximalOutflow(previousHigh, steamHigh);
    }

    /**
     * SB-FR1: a pump fails when, in the transmission following an order, it
     * does not report the ordered state, or when its state changes without an
     * order.
     */
    private void checkPumps() {
        for (int i = 0; i < PUMPS; i++) {
            UnitStatus pump = pumpUnits[i];
            if (previousKnown && !pump.outOfOrder() && !pump.changedThisCycle()) {
                boolean expectedOn = previousPumpOn[i];
                if (orderedOpen[i]) {
                    expectedOn = true;
                }
                if (orderedClose[i]) {
                    expectedOn = false;
                }
                if (pumpOn[i] != expectedOn) {
                    pump.detectFailure();
                }
            }
        }
    }

    /**
     * SB-FR2: a pump controller fails when its observation disagrees with the
     * state of its sound pump, outside the window that follows an order (the
     * first transmission after an order is not judged, the second is: SB-Beh25).
     * A disagreement without an order is a spontaneous change.
     */
    private void checkPumpControllers() {
        for (int i = 0; i < PUMPS; i++) {
            UnitStatus control = controlUnits[i];
            UnitStatus pump = pumpUnits[i];
            boolean pumpTrusted = !pump.outOfOrder() && !pump.changedThisCycle();
            boolean settled = !orderedOpen[i] && !orderedClose[i];
            if (previousKnown && pumpTrusted && settled
                    && !control.outOfOrder() && !control.changedThisCycle()) {
                if (controlFlow[i] != pumpOn[i]) {
                    control.detectFailure();
                }
            }
        }
    }

    /**
     * SB-V7, SB-Beh25: bounds on the water the pumps poured during the last
     * cycle, the current throughput p, and the inflow the next cycle is sure of
     * (sound pumps that are on now and kept on: their balancing is over by the
     * next cycle) or cannot prevent (pumps out of order). A pump that has just
     * been switched on pours nothing for one cycle; a pump switched off stops
     * at once.
     */
    private void estimateInflow() {
        double low = 0.0;
        double high = 0.0;
        double current = 0.0;
        double sure = 0.0;
        double unstoppable = 0.0;
        for (int i = 0; i < PUMPS; i++) {
            UnitStatus control = controlUnits[i];
            UnitStatus pump = pumpUnits[i];
            boolean controlTrusted = !control.outOfOrder() && !control.changedThisCycle();
            boolean pumpTrusted = !pump.outOfOrder() && !pump.changedThisCycle();
            boolean pouringNow = false;
            if (controlTrusted && previousKnown) {
                pouringNow = controlFlow[i];
                if (controlFlow[i] && previousControlFlow[i]) {
                    low = low + P;
                    high = high + P;
                } else if (controlFlow[i] || previousControlFlow[i]) {
                    high = high + P;
                }
            } else if (pumpTrusted && previousKnown) {
                pouringNow = pumpOn[i] && previousPumpOn[i];
                if (pouringNow) {
                    low = low + P;
                    high = high + P;
                }
            } else {
                high = high + P;
            }
            if (pouringNow) {
                current = current + P;
            }
            if (pumpOn[i] && !pump.outOfOrder()) {
                sure = sure + P;
            }
            if (pump.outOfOrder()) {
                unstoppable = unstoppable + P;
            }
        }
        inflowLow = low;
        inflowHigh = high;
        p = current;
        sureInflowAhead = sure;
        unstoppableInflowAhead = unstoppable;
    }

    /**
     * SB-FR3, SB-V8: the level device fails when its reading leaves [0, C] or,
     * once the plant is operating, lies outside the range the level could have
     * reached from the previous cycle. While it is out of order the level is
     * kept as worst-case bounds advanced from steam and pump information
     * (SB-Beh19: one litre pumped is one litre of boiler content).
     */
    private void checkLevelDevice() {
        double predictedLow = levelLow - outflowHigh + T * inflowLow;
        double predictedHigh = levelHigh - outflowLow + T * inflowHigh;
        boolean dynamicsKnown = previousKnown && operating && levelBoundsKnown;
        if (!levelUnit.outOfOrder() && !levelUnit.changedThisCycle()) {
            boolean outOfRange = levelReading < 0.0 || levelReading > BoilerConstants.CAPACITY_C;
            boolean incompatible = dynamicsKnown
                    && (levelReading < predictedLow || levelReading > predictedHigh);
            if (outOfRange || incompatible) {
                levelUnit.detectFailure();
            }
        }
        if (!levelUnit.outOfOrder()) {
            levelLow = levelReading;
            levelHigh = levelReading;
            levelBoundsKnown = true;
        } else if (dynamicsKnown) {
            levelLow = Math.max(0.0, predictedLow);
            levelHigh = Math.min(BoilerConstants.CAPACITY_C, predictedHigh);
        } else {
            levelLow = 0.0;
            levelHigh = BoilerConstants.CAPACITY_C;
        }
    }

    /**
     * SB-V4, SB-Beh13: the level risks a limit when, under worst-case steam
     * behaviour over the next cycle and with the best pump action available
     * (newly opened pumps pour nothing for one cycle), it could reach M1 or M2.
     */
    private void assessLevelRisk() {
        double lowestAhead = levelLow - maximalOutflowAhead(steamHigh) + T * sureInflowAhead;
        double highestAhead = levelHigh - minimalOutflowAhead(steamLow) + T * unstoppableInflowAhead;
        levelRiskDetected = levelLow <= BoilerConstants.LIMIT_M1
                || levelHigh >= BoilerConstants.LIMIT_M2
                || lowestAhead <= BoilerConstants.LIMIT_M1
                || highestAhead >= BoilerConstants.LIMIT_M2;
    }

    /**
     * SB-Beh10, SB-Beh15, SB-Beh19, SB-Beh25: the running pumps are kept while
     * the level, predicted over the next two cycles, stays between N1 and N2;
     * otherwise the number of sound pumps is chosen that brings the predicted
     * level closest to the middle of [N1, N2]. Pumps on now pour during the
     * next cycle; pumps switched on now pour only from the cycle after.
     */
    private void planPumps() {
        int usable = 0;
        int running = 0;
        for (int i = 0; i < PUMPS; i++) {
            if (!pumpUnits[i].outOfOrder()) {
                usable = usable + 1;
                if (pumpOn[i]) {
                    running = running + 1;
                }
            }
        }
        double middle = (levelLow + levelHigh) / 2.0;
        double steamMiddle = (steamLow + steamHigh) / 2.0;
        double target = (BoilerConstants.NORMAL_N1 + BoilerConstants.NORMAL_N2) / 2.0;
        double nextLevel = levelAfterOneCycle(middle, steamMiddle, running, running);
        double laterLevel = levelAfterTwoCycles(middle, steamMiddle, running, running);
        boolean staysNormal = withinNormal(nextLevel) && withinNormal(laterLevel);
        int required = running;
        if (!staysNormal) {
            double bestDistance = Math.abs(laterLevel - target);
            for (int pumps = 0; pumps <= usable; pumps++) {
                double distance = Math.abs(levelAfterTwoCycles(middle, steamMiddle, pumps, running) - target);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    required = pumps;
                }
            }
        }
        if (required > usable) {
            required = usable;
        }
        pumpsNeeded = required;
    }

    private static boolean withinNormal(@RoboChartType("real") double level) {
        return level >= BoilerConstants.NORMAL_N1 && level <= BoilerConstants.NORMAL_N2;
    }

    @RoboChartType("real") 
    private static double levelAfterOneCycle(@RoboChartType("real") double level, @RoboChartType("real") double steam,
            @RoboChartType("nat") int pumps, @RoboChartType("nat") int running) {
        return level - T * steam + T * P * Math.min(pumps, running);
    }

    @RoboChartType("real") 
    private static double levelAfterTwoCycles(@RoboChartType("real") double level, @RoboChartType("real") double steam,
            @RoboChartType("nat") int pumps, @RoboChartType("nat") int running) {
        return levelAfterOneCycle(level, steam, pumps, running) - T * steam + T * P * pumps;
    }

    private void rememberCycle() {
        for (int i = 0; i < PUMPS; i++) {
            previousPumpOn[i] = pumpOn[i];
            previousControlFlow[i] = controlFlow[i];
        }
        previousKnown = true;
        if (unitsReadyReceived && programReadySent) {
            operating = true;
        }
    }

    private void summarise() {
        operatorStop = stopCount >= BoilerConstants.STOP_REPETITIONS;
        levelFailed = levelUnit.outOfOrder();
        steamFailed = steamUnit.outOfOrder();
        boolean anyControl = false;
        boolean anyPump = false;
        for (int i = 0; i < PUMPS; i++) {
            anyControl = anyControl || controlUnits[i].outOfOrder();
            anyPump = anyPump || pumpUnits[i].outOfOrder();
        }
        controlFailed = anyControl;
        nonLevelFailed = steamFailed || anyControl || anyPump;
        initialEmergencyFound = operatorStop
                || (plantWaiting && (transmissionFailed || v != 0.0 || levelFailed));
        startConfirmedFound = plantWaiting && unitsReadyReceived && programReadySent;
        drainWanted = !programReadySent && q > BoilerConstants.NORMAL_N2;
        fillWanted = !programReadySent && q < BoilerConstants.NORMAL_N1;
        runningEmergencyFound = operatorStop || transmissionFailed || waitingReceived
                || unitsReadyReceived || levelRiskDetected;
        estimateUnreliableFound = steamFailed || controlFailed;
    }

    // ---- Worst-case steam dynamics (SB-DM2) ----

    /** Largest steam outflow over one cycle between end-point rates bounded by start and end. */
    @RoboChartType("real") 
    private static double maximalOutflow(@RoboChartType("real") double start, @RoboChartType("real") double end) {
        double peak = (T * U1 * U2 + start * U2 + end * U1) / (U1 + U2);
        if (peak < start || peak < end) {
            return T * Math.max(start, end);
        }
        return (peak * peak - start * start) / (2.0 * U1) + (peak * peak - end * end) / (2.0 * U2);
    }

    /** Smallest steam outflow over one cycle between end-point rates bounded by start and end. */
    @RoboChartType("real") 
    private static double minimalOutflow(@RoboChartType("real") double start, @RoboChartType("real") double end) {
        double trough = (start * U1 + end * U2 - T * U1 * U2) / (U1 + U2);
        if (trough > start || trough > end) {
            return T * Math.min(start, end);
        }
        double area = (start * start - trough * trough) / (2.0 * U2)
                + (end * end - trough * trough) / (2.0 * U1);
        return Math.max(0.0, area);
    }

    /** Largest steam outflow over the next cycle from rate start, rising at U1 up to W. */
    @RoboChartType("real") 
    private static double maximalOutflowAhead(@RoboChartType("real") double start) {
        double rampTime = (W - start) / U1;
        if (rampTime >= T) {
            return T * start + 0.5 * U1 * T * T;
        }
        if (rampTime < 0.0) {
            rampTime = 0.0;
        }
        return start * rampTime + 0.5 * U1 * rampTime * rampTime + W * (T - rampTime);
    }

    /** Smallest steam outflow over the next cycle from rate start, falling at U2 down to 0. */
    @RoboChartType("real") 
    private static double minimalOutflowAhead(@RoboChartType("real") double start) {
        double fallTime = start / U2;
        if (fallTime >= T) {
            return T * start - 0.5 * U2 * T * T;
        }
        return start * fallTime - 0.5 * U2 * fallTime * fallTime;
    }
}
