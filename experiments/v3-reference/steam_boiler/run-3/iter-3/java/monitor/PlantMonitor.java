package steam_boiler.monitor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.BoilerConstants;
import steam_boiler.domain.ValvePosition;
import steam_boiler.event.OutputEvent;
import steam_boiler.message.MessageKind;
import steam_boiler.message.UnitMessage;

/**
 * Reception and plant analysis of the steam-boiler control program.
 *
 * <p>In each cycle (SB-V3) the whole incoming transmission is handed to
 * {@link #receive(List)}. The monitor checks the transmission (SB-FR5), counts the
 * successive transmissions carrying STOP (SB-V10), keeps the current measures q, v and
 * p and the level estimate (SB-V5 to SB-V8), judges unit failures (SB-FR1 to SB-FR4)
 * and keeps the status of every unit (SB-V9). The mode controller reads the results
 * through {@link steam_boiler.sensor.BoilerSensor}. The monitor then supplies the
 * cycle's failure announcements and repair acknowledgements (SB-FR6, SB-FR7) and is
 * told what was transmitted, so that the next cycle can judge the pumps' reactions to
 * the orders (SB-FR1, SB-FR2, SB-Beh25).
 *
 * <p>Failure judgement starts once STEAM_BOILER_WAITING has arrived: until then
 * nothing else happens in the program (SB-Beh2).
 */
public final class PlantMonitor {

    private final List<PumpUnit> pumps = new ArrayList<>();
    private final UnitHealth levelDevice = new UnitHealth();
    private final UnitHealth steamDevice = new UnitHealth();
    private final List<OutputEvent> repairAcknowledgements = new ArrayList<>();

    // --- reception of the current transmission ------------------------------------
    private boolean stopInTransmission = false;
    private boolean waitingInTransmission = false;
    private boolean unitsReadyInTransmission = false;
    private boolean malformedTransmission = false;
    private boolean levelRepairReported = false;
    private boolean steamRepairReported = false;
    private boolean levelAcknowledgementReported = false;
    private boolean steamAcknowledgementReported = false;
    @RoboChartType("nat") private int levelReports = 0;
    @RoboChartType("nat") private int steamReports = 0;
    @RoboChartType("real") private double levelReading = 0.0;
    @RoboChartType("real") private double steamReading = 0.0;

    // --- results of reception and analysis --------------------------------------------
    @RoboChartType("nat") private int successiveStopCount = 0;
    private boolean transmissionFailed = false;
    private boolean plantStarted = false;
    @RoboChartType("real") private double waterLevel = 0.0;
    @RoboChartType("real") private double steamRate = 0.0;
    @RoboChartType("real") private double pumpThroughput = 0.0;
    @RoboChartType("real") private double estimatedLevelLow = 0.0;
    @RoboChartType("real") private double estimatedLevelHigh = BoilerConstants.CAPACITY_C;

    // --- memory of the previous cycle ---------------------------------------------------
    @RoboChartType("real") private double previousSteamRate = 0.0;
    private boolean levelHistory = false;
    private boolean steamHistory = false;
    private ValvePosition valvePosition = ValvePosition.CLOSED;
    private boolean programReadySent = false;

    /** Creates the monitor with every unit believed sound. */
    public PlantMonitor() {
        for (int n = 1; n <= BoilerConstants.PUMP_COUNT; n++) {
            pumps.add(new PumpUnit(n));
        }
    }

    // =====================================================================================
    // Reception stage
    // =====================================================================================

    /**
     * Receives the messages of one incoming transmission, all considered received
     * simultaneously (SB-V3), and analyses them.
     */
    public void receive(List<UnitMessage> messages) {
        beginTransmission();
        for (UnitMessage message : messages) {
            accept(message);
        }
        completeTransmission();
    }

    private void beginTransmission() {
        stopInTransmission = false;
        waitingInTransmission = false;
        unitsReadyInTransmission = false;
        malformedTransmission = false;
        levelRepairReported = false;
        steamRepairReported = false;
        levelAcknowledgementReported = false;
        steamAcknowledgementReported = false;
        levelReports = 0;
        steamReports = 0;
        repairAcknowledgements.clear();
        for (PumpUnit pump : pumps) {
            pump.beginTransmission();
        }
    }

    private void accept(UnitMessage message) {
        MessageKind kind = message.kind();
        boolean perPump = kind == MessageKind.PUMP_STATE
                || kind == MessageKind.PUMP_CONTROL_STATE
                || kind == MessageKind.PUMP_REPAIRED
                || kind == MessageKind.PUMP_CONTROL_REPAIRED
                || kind == MessageKind.PUMP_FAILURE_ACKNOWLEDGEMENT
                || kind == MessageKind.PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT;
        if (perPump && !isPumpNumber(message.pump())) {
            // A pump number outside 1..4: the message's presence is aberrant (SB-FR5).
            malformedTransmission = true;
        } else if (kind == MessageKind.STOP_REQUEST) {
            stopInTransmission = true;
        } else if (kind == MessageKind.STEAM_BOILER_WAITING) {
            waitingInTransmission = true;
        } else if (kind == MessageKind.PHYSICAL_UNITS_READY) {
            unitsReadyInTransmission = true;
        } else if (kind == MessageKind.LEVEL_READING) {
            levelReports = levelReports + 1;
            levelReading = message.reading();
        } else if (kind == MessageKind.STEAM_READING) {
            steamReports = steamReports + 1;
            steamReading = message.reading();
        } else if (kind == MessageKind.PUMP_STATE) {
            pumpNumbered(message.pump()).reportPosition(message.position());
        } else if (kind == MessageKind.PUMP_CONTROL_STATE) {
            pumpNumbered(message.pump()).reportFlow(message.flow());
        } else if (kind == MessageKind.PUMP_REPAIRED) {
            pumpNumbered(message.pump()).reportPumpRepaired();
        } else if (kind == MessageKind.PUMP_CONTROL_REPAIRED) {
            pumpNumbered(message.pump()).reportControllerRepaired();
        } else if (kind == MessageKind.LEVEL_REPAIRED) {
            levelRepairReported = true;
        } else if (kind == MessageKind.STEAM_REPAIRED) {
            steamRepairReported = true;
        } else if (kind == MessageKind.PUMP_FAILURE_ACKNOWLEDGEMENT) {
            pumpNumbered(message.pump()).reportPumpAcknowledgement();
        } else if (kind == MessageKind.PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT) {
            pumpNumbered(message.pump()).reportControllerAcknowledgement();
        } else if (kind == MessageKind.LEVEL_FAILURE_ACKNOWLEDGEMENT) {
            levelAcknowledgementReported = true;
        } else if (kind == MessageKind.STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT) {
            steamAcknowledgementReported = true;
        }
    }

    private void completeTransmission() {
        // SB-V10: successive transmissions carrying STOP.
        if (stopInTransmission) {
            successiveStopCount = successiveStopCount + 1;
        } else {
            successiveStopCount = 0;
        }
        // SB-FR5: the four data messages must each be present, exactly once.
        boolean complete = levelReports == 1 && steamReports == 1;
        for (PumpUnit pump : pumps) {
            if (!pump.reportsComplete()) {
                complete = false;
            }
        }
        transmissionFailed = malformedTransmission || !complete;
        // SB-V5, SB-V6: the most recent readings.
        if (levelReports == 1) {
            waterLevel = levelReading;
        }
        if (steamReports == 1) {
            steamRate = steamReading;
        }
        if (waitingInTransmission) {
            plantStarted = true;
        }
        if (plantStarted && !transmissionFailed) {
            analyse();
        }
    }

    // =====================================================================================
    // Analysis stage
    // =====================================================================================

    private void analyse() {
        // SB-FR6: acknowledgements of announced failure detections.
        if (levelAcknowledgementReported) {
            levelDevice.acknowledgeFailure();
        }
        if (steamAcknowledgementReported) {
            steamDevice.acknowledgeFailure();
        }
        for (PumpUnit pump : pumps) {
            pump.handleAcknowledgements();
        }
        // SB-FR7: repair notifications make the unit sound again and are acknowledged.
        boolean levelRepaired = false;
        if (levelRepairReported) {
            levelDevice.repair();
            repairAcknowledgements.add(new OutputEvent.LevelRepairedAcknowledgement());
            levelRepaired = true;
        }
        boolean steamRepaired = false;
        if (steamRepairReported) {
            steamDevice.repair();
            repairAcknowledgements.add(new OutputEvent.SteamRepairedAcknowledgement());
            steamRepaired = true;
        }
        for (PumpUnit pump : pumps) {
            pump.handleRepairs(repairAcknowledgements);
        }
        // SB-FR4, SB-FR1, SB-FR2, SB-FR3 and SB-V8.
        checkSteam(steamRepaired);
        for (PumpUnit pump : pumps) {
            pump.checkReports();
        }
        updateLevel(levelRepaired);
        // SB-V7: throughput of the pumps that are on and seen flowing.
        int delivering = 0;
        for (PumpUnit pump : pumps) {
            if (pump.delivering()) {
                delivering = delivering + 1;
            }
        }
        pumpThroughput = BoilerConstants.PUMP_CAPACITY_P * delivering;
        // Memory for the next cycle.
        if (steamDevice.sound()) {
            previousSteamRate = steamRate;
            steamHistory = true;
        } else {
            steamHistory = false;
        }
        levelHistory = true;
        for (PumpUnit pump : pumps) {
            pump.endTransmission();
        }
    }

    // SB-FR4: static range [0, W] and the gradients U1 and U2.
    private void checkSteam(boolean freshlyRepaired) {
        if (steamDevice.sound()) {
            double cycle = BoilerConstants.CYCLE_SECONDS;
            boolean outsideRange = steamRate < 0.0 || steamRate > BoilerConstants.STEAM_MAX_W;
            boolean tooFastUp = steamRate > previousSteamRate + BoilerConstants.STEAM_INCREASE_MAX_U1 * cycle;
            boolean tooFastDown = steamRate < previousSteamRate - BoilerConstants.STEAM_DECREASE_MAX_U2 * cycle;
            boolean incompatible = steamHistory && !freshlyRepaired && (tooFastUp || tooFastDown);
            if (outsideRange || incompatible) {
                steamDevice.fail();
            }
        }
    }

    // SB-FR3 and SB-V8: predicts the level range reachable over the cycle just elapsed
    // from the previous range, the pumps (with their start-up latency, SB-Beh25) and the
    // steam dynamics; judges the reading against it, or replaces the reading by it.
    private void updateLevel(boolean freshlyRepaired) {
        double cycle = BoilerConstants.CYCLE_SECONDS;
        double maxSteamOut = BoilerConstants.STEAM_MAX_W * cycle;
        int surely = 0;
        int possibly = 0;
        for (PumpUnit pump : pumps) {
            if (pump.surelyPouredLastCycle()) {
                surely = surely + 1;
            }
            if (pump.possiblyPouredLastCycle()) {
                possibly = possibly + 1;
            }
        }
        double steamOutLow = 0.0;
        double steamOutHigh = maxSteamOut;
        if (steamHistory && steamDevice.sound()) {
            // The steam rate moved from the previous to the current reading with a
            // bounded gradient; its integral lies within U*T*T/4 of the trapezoid.
            double gradient = Math.max(BoilerConstants.STEAM_INCREASE_MAX_U1, BoilerConstants.STEAM_DECREASE_MAX_U2);
            double mean = cycle * (previousSteamRate + steamRate) / 2.0;
            double spread = gradient * cycle * cycle / 4.0;
            steamOutLow = Math.max(0.0, mean - spread);
            steamOutHigh = Math.min(maxSteamOut, mean + spread);
        }
        double pumped = BoilerConstants.PUMP_CAPACITY_P * cycle;
        double predictedLow = estimatedLevelLow + pumped * surely - steamOutHigh;
        double predictedHigh = estimatedLevelHigh + pumped * possibly - steamOutLow;
        if (valvePosition == ValvePosition.OPEN) {
            // The evacuation rate is unknown: any decrease is possible.
            predictedLow = 0.0;
        }
        if (levelDevice.sound()) {
            boolean outsideRange = waterLevel < 0.0 || waterLevel > BoilerConstants.CAPACITY_C;
            boolean unreachable = waterLevel < predictedLow || waterLevel > predictedHigh;
            boolean incompatible = levelHistory && !freshlyRepaired && unreachable;
            if (outsideRange || incompatible) {
                levelDevice.fail();
            }
        }
        if (levelDevice.sound()) {
            estimatedLevelLow = waterLevel;
            estimatedLevelHigh = waterLevel;
        } else if (levelHistory) {
            estimatedLevelLow = withinCapacity(predictedLow);
            estimatedLevelHigh = withinCapacity(predictedHigh);
        } else {
            estimatedLevelLow = 0.0;
            estimatedLevelHigh = BoilerConstants.CAPACITY_C;
        }
    }

    @RoboChartType("real")
    private static double withinCapacity(@RoboChartType("real") double litres) {
        return Math.min(BoilerConstants.CAPACITY_C, Math.max(0.0, litres));
    }

    private static boolean isPumpNumber(int n) {
        return n >= 1 && n <= BoilerConstants.PUMP_COUNT;
    }

    private PumpUnit pumpNumbered(int n) {
        return pumps.get(n - 1);
    }

    @RoboChartType("real")
    private double levelMidpoint() {
        return (estimatedLevelLow + estimatedLevelHigh) / 2.0;
    }

    // =====================================================================================
    // Results read by the controller (through BoilerSensor)
    // =====================================================================================

    /** Number of successive transmissions, up to this one, that carried STOP (SB-V10). */
    @RoboChartType("nat")
    public int successiveStopCount() {
        return successiveStopCount;
    }

    /** A data message is missing or duplicated, or an aberrant message arrived (SB-FR5). */
    public boolean transmissionFailed() {
        return transmissionFailed;
    }

    /**
     * STOP has arrived in this and the two preceding transmissions (SB-Beh23, SB-V10), or
     * the transmission system has failed (SB-FR5).
     */
    public boolean stopOrTransmissionFailure() {
        return successiveStopCount >= BoilerConstants.STOP_REPETITIONS || transmissionFailed;
    }

    /** STEAM_BOILER_WAITING has arrived, in this or an earlier transmission (SB-Beh2). */
    public boolean steamBoilerWaitingReceived() {
        return plantStarted;
    }

    /** PHYSICAL_UNITS_READY is in this transmission (SB-Beh8). */
    public boolean physicalUnitsReadyReceived() {
        return unitsReadyInTransmission;
    }

    /** PROGRAM_READY has been sent in an earlier transmission (SB-Beh7). */
    public boolean programReadySent() {
        return programReadySent;
    }

    /** The evacuation valve has been requested open and not yet closed (SB-Beh4, SB-DM7). */
    public boolean evacuationValveOpen() {
        return valvePosition == ValvePosition.OPEN;
    }

    /**
     * Initialization must end in emergency stop: operator stop or transmission failure
     * (SB-Beh23, SB-Beh9), PHYSICAL_UNITS_READY before PROGRAM_READY was sent (an aberrant
     * message, SB-FR5), or, once STEAM_BOILER_WAITING has arrived, a non-zero steam rate
     * or a failed level measuring device (SB-Beh3, SB-Beh6).
     */
    public boolean initialisationAbort() {
        return stopOrTransmissionFailure()
                || (unitsReadyInTransmission && !programReadySent)
                || (plantStarted && startupCheckFailed());
    }

    /**
     * The start-up checks of initialization fail: the steam rate is not zero (SB-Beh3) or
     * the level measuring device is out of order (SB-Beh6).
     */
    public boolean startupCheckFailed() {
        return !steamRateZero() || levelDeviceDefective();
    }

    /**
     * Emergency stop is demanded in normal, degraded or rescue mode: operator stop or
     * transmission failure (SB-Beh23, SB-FR5), an initialization message whose presence is
     * aberrant outside initialization (SB-FR5), or a level risking M1 or M2 (SB-V4,
     * SB-Beh13, SB-Beh14, SB-Beh18, SB-Beh21).
     */
    public boolean operatingEmergency() {
        return stopOrTransmissionFailure() || waitingInTransmission || unitsReadyInTransmission || levelLimitRisk();
    }

    /** Some unit other than the level measuring device is out of order (SB-V9, SB-Beh8, SB-Beh12). */
    public boolean otherUnitDefective() {
        return steamDeviceDefective() || pumpDefective() || pumpControllerDefective();
    }

    /**
     * The level estimate cannot be trusted: the steam measuring device or a pump
     * controller is out of order (SB-Beh19, SB-Beh21).
     */
    public boolean estimateUnreliable() {
        return steamDeviceDefective() || pumpControllerDefective();
    }

    /** The level measuring device is out of order (SB-V9, SB-FR3). */
    public boolean levelDeviceDefective() {
        return !levelDevice.sound();
    }

    /** The steam measuring device is out of order (SB-V9, SB-FR4). */
    public boolean steamDeviceDefective() {
        return !steamDevice.sound();
    }

    /** At least one pump is out of order (SB-V9, SB-FR1). */
    public boolean pumpDefective() {
        boolean defective = false;
        for (PumpUnit pump : pumps) {
            if (!pump.pumpSound()) {
                defective = true;
            }
        }
        return defective;
    }

    /** At least one pump controller is out of order (SB-V9, SB-FR2). */
    public boolean pumpControllerDefective() {
        boolean defective = false;
        for (PumpUnit pump : pumps) {
            if (!pump.controllerSound()) {
                defective = true;
            }
        }
        return defective;
    }

    /** The reported steam rate is exactly zero (SB-Beh3). */
    public boolean steamRateZero() {
        return steamRate == 0.0;
    }

    /** The level (reading, or estimate midpoint while the device is out of order) is below N1. */
    public boolean levelBelowNormal() {
        return levelMidpoint() < BoilerConstants.NORMAL_MIN_N1;
    }

    /** The level (reading, or estimate midpoint while the device is out of order) is above N2. */
    public boolean levelAboveNormal() {
        return levelMidpoint() > BoilerConstants.NORMAL_MAX_N2;
    }

    /**
     * The level expected at the end of the coming cycle, if the pumps now on stay on and
     * none is switched on (a pump switched on now pours nothing during it, SB-Beh25), is
     * below N1 (SB-Beh10, SB-Beh15, SB-Beh19).
     */
    public boolean predictedLevelBelowNormal() {
        return expectedNextLevel() < BoilerConstants.NORMAL_MIN_N1;
    }

    /**
     * The level expected at the end of the coming cycle, if the pumps now on stay on, is
     * above N2 (SB-Beh10, SB-Beh15, SB-Beh19).
     */
    public boolean predictedLevelAboveNormal() {
        return expectedNextLevel() > BoilerConstants.NORMAL_MAX_N2;
    }

    @RoboChartType("real")
    private double expectedNextLevel() {
        double cycle = BoilerConstants.CYCLE_SECONDS;
        int pouring = 0;
        for (PumpUnit pump : pumps) {
            if (pump.surelyPoursNextCycle(false)) {
                pouring = pouring + 1;
            }
        }
        double steamOut = BoilerConstants.STEAM_MAX_W * cycle / 2.0;
        if (steamDevice.sound()) {
            steamOut = steamRate * cycle;
        }
        return levelMidpoint() + BoilerConstants.PUMP_CAPACITY_P * cycle * pouring - steamOut;
    }

    /**
     * The level risks reaching M1 or M2 before the next cycle under worst-case behaviour
     * of steam and pumps (SB-V4, SB-Beh13, SB-Beh18, SB-Beh21). The pumps' contribution
     * follows the regulation of this cycle: every pump is closed when the predicted level
     * is above N2 (taking effect at once), and a pump switched on now pours nothing
     * during the coming cycle (SB-Beh25).
     */
    public boolean levelLimitRisk() {
        double cycle = BoilerConstants.CYCLE_SECONDS;
        double maxSteamOut = BoilerConstants.STEAM_MAX_W * cycle;
        boolean closing = predictedLevelAboveNormal();
        int surely = 0;
        int possibly = 0;
        for (PumpUnit pump : pumps) {
            if (pump.surelyPoursNextCycle(closing)) {
                surely = surely + 1;
            }
            if (pump.possiblyPoursNextCycle(closing)) {
                possibly = possibly + 1;
            }
        }
        double steamOutLow = 0.0;
        double steamOutHigh = maxSteamOut;
        if (steamDevice.sound()) {
            steamOutLow = Math.max(0.0, cycle * steamRate - BoilerConstants.STEAM_DECREASE_MAX_U2 * cycle * cycle / 2.0);
            steamOutHigh = Math.min(maxSteamOut, cycle * steamRate + BoilerConstants.STEAM_INCREASE_MAX_U1 * cycle * cycle / 2.0);
        }
        double pumped = BoilerConstants.PUMP_CAPACITY_P * cycle;
        double nextLow = estimatedLevelLow + pumped * surely - steamOutHigh;
        double nextHigh = estimatedLevelHigh + pumped * possibly - steamOutLow;
        boolean beyondNow = estimatedLevelLow <= BoilerConstants.LIMIT_MIN_M1
                || estimatedLevelHigh >= BoilerConstants.LIMIT_MAX_M2;
        boolean beyondNext = nextLow <= BoilerConstants.LIMIT_MIN_M1 || nextHigh >= BoilerConstants.LIMIT_MAX_M2;
        return beyondNow || beyondNext;
    }

    // =====================================================================================
    // Current measures (SB-V5 to SB-V8)
    // =====================================================================================

    /** q: the water level most recently reported, litres (SB-V5). */
    @RoboChartType("real")
    public double waterLevel() {
        return waterLevel;
    }

    /** v: the steam rate most recently reported, litres per second (SB-V6). */
    @RoboChartType("real")
    public double steamRate() {
        return steamRate;
    }

    /** p: current throughput of the pumps taken together, litres per second (SB-V7). */
    @RoboChartType("real")
    public double pumpThroughput() {
        return pumpThroughput;
    }

    /** Lower bound of the level estimate, litres (SB-V8). */
    @RoboChartType("real")
    public double estimatedLevelLow() {
        return estimatedLevelLow;
    }

    /** Upper bound of the level estimate, litres (SB-V8). */
    @RoboChartType("real")
    public double estimatedLevelHigh() {
        return estimatedLevelHigh;
    }

    // =====================================================================================
    // Transmission stage
    // =====================================================================================

    /**
     * Failure detections still unacknowledged (repeated every cycle, SB-FR6) and the
     * acknowledgements of this cycle's repair notifications (SB-FR7).
     */
    public List<OutputEvent> protocolMessages() {
        List<OutputEvent> messages = new ArrayList<>();
        for (PumpUnit pump : pumps) {
            if (pump.pumpAnnouncementDue()) {
                messages.add(new OutputEvent.PumpFailureDetection(pump.number()));
            }
            if (pump.controllerAnnouncementDue()) {
                messages.add(new OutputEvent.PumpControlFailureDetection(pump.number()));
            }
        }
        if (levelDevice.announcementDue()) {
            messages.add(new OutputEvent.LevelFailureDetection());
        }
        if (steamDevice.announcementDue()) {
            messages.add(new OutputEvent.SteamFailureDetection());
        }
        messages.addAll(repairAcknowledgements);
        return messages;
    }

    /**
     * Records the outgoing transmission of the cycle: pump orders (judged in the next
     * transmissions, SB-FR1, SB-FR2), valve requests (SB-Beh4) and PROGRAM_READY (SB-Beh7).
     */
    public void recordTransmitted(List<OutputEvent> sent) {
        for (PumpUnit pump : pumps) {
            pump.shiftOrders();
        }
        for (OutputEvent message : sent) {
            if (message instanceof OutputEvent.OpenPump) {
                OutputEvent.OpenPump open = (OutputEvent.OpenPump) message;
                if (isPumpNumber(open.pump())) {
                    pumpNumbered(open.pump()).recordOrder(PumpOrder.OPEN_ORDER);
                }
            } else if (message instanceof OutputEvent.ClosePump) {
                OutputEvent.ClosePump close = (OutputEvent.ClosePump) message;
                if (isPumpNumber(close.pump())) {
                    pumpNumbered(close.pump()).recordOrder(PumpOrder.CLOSE_ORDER);
                }
            } else if (message instanceof OutputEvent.ProgramReady) {
                programReadySent = true;
            } else if (message instanceof OutputEvent.Valve) {
                if (valvePosition == ValvePosition.OPEN) {
                    valvePosition = ValvePosition.CLOSED;
                } else {
                    valvePosition = ValvePosition.OPEN;
                }
            }
        }
    }
}
