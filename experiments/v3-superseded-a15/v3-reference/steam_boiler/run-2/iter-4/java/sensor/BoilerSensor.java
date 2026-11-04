package steam_boiler.sensor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.BoilerConstants;
import steam_boiler.domain.PumpId;
import steam_boiler.event.OutputEvent;

/**
 * The program's view of the physical units. During the reception stage of a cycle the incoming
 * messages are handed to the {@code receive...} methods; {@link #analyse()} then checks the
 * transmission, counts STOP messages, judges every unit, keeps the unit-status bookkeeping and
 * the water-level estimate, and the controller reads the results through the query methods.
 */
public final class BoilerSensor {
    /** Number of slices used to integrate the steam-rate envelope over one cycle. */
    @RoboChartType("nat") private static final int ESTIMATION_SLICES = 100;

    // Messages of the current transmission.
    private boolean stopInbox = false;
    private boolean waitingInbox = false;
    private boolean readyInbox = false;
    @RoboChartType("nat") private int levelInboxCount = 0;
    @RoboChartType("nat") private int steamInboxCount = 0;
    private boolean levelRepairedInbox = false;
    private boolean steamRepairedInbox = false;
    private boolean levelAckInbox = false;
    private boolean steamAckInbox = false;
    private final PumpMonitor[] pumpMonitors;

    // Readings: q and v as last reported, and the last steam rate accepted as valid.
    @RoboChartType("real") private double reportedLevel = 0.0;
    @RoboChartType("real") private double reportedSteam = 0.0;
    @RoboChartType("real") private double referenceSteam = 0.0;
    private boolean steamReferenceValid = false;
    private boolean levelReferenceValid = false;

    // Status of the level and steam measuring devices.
    private final UnitStatus levelStatus = new UnitStatus();
    private final UnitStatus steamStatus = new UnitStatus();
    private boolean levelRepairAckDue = false;
    private boolean steamRepairAckDue = false;

    // Pump throughput p, the level bounds, and the levels reachable within the coming cycle.
    @RoboChartType("real") private double throughput = 0.0;
    @RoboChartType("real") private double lowerLevel = 0.0;
    @RoboChartType("real") private double upperLevel = BoilerConstants.CAPACITY_C;
    @RoboChartType("real") private double estimatedLevel = 0.0;
    @RoboChartType("real") private double lowestReachable = 0.0;
    @RoboChartType("real") private double highestReachable = BoilerConstants.CAPACITY_C;

    // Results that concern the transmission as a whole.
    private boolean transmissionFault = false;
    @RoboChartType("nat") private int stopRun = 0;
    private boolean waitingArrived = false;
    private boolean readyArrived = false;
    private boolean monitoring = false;
    private boolean drainOpen = false;
    private boolean controllersHealthy = true;
    private boolean othersHealthy = true;
    private boolean levelHealthy = true;
    private boolean steamHealthy = true;

    // Boolean results the controller's guards read, fixed at the end of each analysis.
    private boolean stopDemandedFlag = false;
    private boolean steamNonZeroFlag = false;
    private boolean belowBandFlag = false;
    private boolean aboveBandFlag = false;
    private boolean limitRiskFlag = false;

    /** Creates the view of a plant in which every unit is believed sound. */
    public BoilerSensor() {
        pumpMonitors = new PumpMonitor[BoilerConstants.PUMP_COUNT];
        for (PumpId pump : PumpId.values()) {
            pumpMonitors[pump.ordinal()] = new PumpMonitor(pump);
        }
    }

    // ---------------------------------------------------------------- reception

    /** STOP. */
    public void receiveStop() {
        stopInbox = true;
    }

    /** STEAM_BOILER_WAITING. */
    public void receiveSteamBoilerWaiting() {
        waitingInbox = true;
    }

    /** PHYSICAL_UNITS_READY. */
    public void receivePhysicalUnitsReady() {
        readyInbox = true;
    }

    /** PUMP_STATE(n, b). */
    public void receivePumpState(PumpId pump, boolean on) {
        pumpMonitors[pump.ordinal()].receiveState(on);
    }

    /** PUMP_CONTROL_STATE(n, b). */
    public void receivePumpControlState(PumpId pump, boolean flowing) {
        pumpMonitors[pump.ordinal()].receiveControlState(flowing);
    }

    /** LEVEL(v). */
    public void receiveLevel(@RoboChartType("real") double litres) {
        levelInboxCount = levelInboxCount + 1;
        reportedLevel = litres;
    }

    /** STEAM(v). */
    public void receiveSteam(@RoboChartType("real") double litresPerSecond) {
        steamInboxCount = steamInboxCount + 1;
        reportedSteam = litresPerSecond;
    }

    /** PUMP_REPAIRED(n). */
    public void receivePumpRepaired(PumpId pump) {
        pumpMonitors[pump.ordinal()].receiveRepaired();
    }

    /** PUMP_CONTROL_REPAIRED(n). */
    public void receivePumpControlRepaired(PumpId pump) {
        pumpMonitors[pump.ordinal()].receiveControlRepaired();
    }

    /** LEVEL_REPAIRED. */
    public void receiveLevelRepaired() {
        levelRepairedInbox = true;
    }

    /** STEAM_REPAIRED. */
    public void receiveSteamRepaired() {
        steamRepairedInbox = true;
    }

    /** PUMP_FAILURE_ACKNOWLEDGEMENT(n). */
    public void receivePumpFailureAcknowledgement(PumpId pump) {
        pumpMonitors[pump.ordinal()].receiveFailureAcknowledgement();
    }

    /** PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT(n). */
    public void receivePumpControlFailureAcknowledgement(PumpId pump) {
        pumpMonitors[pump.ordinal()].receiveControlFailureAcknowledgement();
    }

    /** LEVEL_FAILURE_ACKNOWLEDGEMENT. */
    public void receiveLevelFailureAcknowledgement() {
        levelAckInbox = true;
    }

    /** STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT. */
    public void receiveSteamOutcomeFailureAcknowledgement() {
        steamAckInbox = true;
    }

    // ---------------------------------------------------------------- analysis

    /** Analyses the messages received in this cycle; called once per cycle, before the controller. */
    public void analyse() {
        transmissionFault = levelInboxCount != 1 || steamInboxCount != 1;
        for (PumpMonitor monitor : pumpMonitors) {
            if (!monitor.dataComplete()) {
                transmissionFault = true;
            }
        }
        if (stopInbox) {
            stopRun = stopRun + 1;
        } else {
            stopRun = 0;
        }
        waitingArrived = waitingInbox;
        readyArrived = readyInbox;
        if (waitingInbox && !monitoring) {
            monitoring = true;
            levelReferenceValid = false;
            steamReferenceValid = false;
        }
        processProtocol();
        if (!transmissionFault) {
            analyseReadings();
        }
        summariseResults();
        clearInbox();
    }

    /** Records the commands the controller issued this cycle, which the next analysis judges against. */
    public void recordCommands(List<OutputEvent> commands) {
        for (OutputEvent command : commands) {
            if (command instanceof OutputEvent.OpenPump) {
                OutputEvent.OpenPump open = (OutputEvent.OpenPump) command;
                pumpMonitors[open.pump().ordinal()].ordered(true);
            } else if (command instanceof OutputEvent.ClosePump) {
                OutputEvent.ClosePump close = (OutputEvent.ClosePump) command;
                pumpMonitors[close.pump().ordinal()].ordered(false);
            } else if (command instanceof OutputEvent.Valve) {
                drainOpen = !drainOpen;
            }
        }
    }

    /**
     * The failure detections still unacknowledged, and the acknowledgements of the repairs
     * received in this cycle, to be sent with this cycle's transmission.
     */
    public List<OutputEvent> protocolMessages() {
        List<OutputEvent> messages = new ArrayList<>();
        if (levelStatus.announcementPending()) {
            messages.add(new OutputEvent.LevelFailureDetection());
        }
        if (steamStatus.announcementPending()) {
            messages.add(new OutputEvent.SteamFailureDetection());
        }
        if (levelRepairAckDue) {
            messages.add(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (steamRepairAckDue) {
            messages.add(new OutputEvent.SteamRepairedAcknowledgement());
        }
        for (PumpMonitor monitor : pumpMonitors) {
            monitor.addProtocolMessages(messages);
        }
        return messages;
    }

    private void processProtocol() {
        levelRepairAckDue = false;
        steamRepairAckDue = false;
        if (levelAckInbox && !levelStatus.acknowledgeDetection()) {
            transmissionFault = true;
        }
        if (steamAckInbox && !steamStatus.acknowledgeDetection()) {
            transmissionFault = true;
        }
        if (levelRepairedInbox) {
            if (levelStatus.markRepaired()) {
                levelRepairAckDue = true;
                levelReferenceValid = false;
            } else {
                transmissionFault = true;
            }
        }
        if (steamRepairedInbox) {
            if (steamStatus.markRepaired()) {
                steamRepairAckDue = true;
                steamReferenceValid = false;
            } else {
                transmissionFault = true;
            }
        }
        for (PumpMonitor monitor : pumpMonitors) {
            if (!monitor.processProtocol()) {
                transmissionFault = true;
            }
        }
    }

    private void analyseReadings() {
        double cycle = BoilerConstants.CYCLE_SECONDS;
        double pumpVolume = BoilerConstants.PUMP_CAPACITY_P * cycle;

        // The steam that can have left since the previous analysis: bounded by both readings when
        // both are valid, by the earlier one alone otherwise.
        double previousSteam = referenceSteam;
        boolean previousValid = steamReferenceValid;
        judgeSteam();
        double steamOutMax = BoilerConstants.STEAM_MAX_W * cycle;
        double steamOutMin = 0.0;
        if (previousValid && steamReferenceValid) {
            steamOutMax = steamVolumeBetween(previousSteam, referenceSteam, true);
            steamOutMin = steamVolumeBetween(previousSteam, referenceSteam, false);
        } else if (previousValid) {
            steamOutMax = maximalSteamVolume(previousSteam);
            steamOutMin = minimalSteamVolume(previousSteam);
        }

        // The level the boiler can have reached since the previous analysis.
        double inflowMin = 0.0;
        double inflowMax = 0.0;
        for (PumpMonitor monitor : pumpMonitors) {
            if (monitor.surelyPouring()) {
                inflowMin = inflowMin + pumpVolume;
            }
            if (monitor.possiblyPouring()) {
                inflowMax = inflowMax + pumpVolume;
            }
        }
        double reachedLower = lowerLevel - steamOutMax + inflowMin;
        double reachedUpper = upperLevel - steamOutMin + inflowMax;
        if (drainOpen) {
            reachedLower = 0.0;
        }
        reachedLower = clampToCapacity(reachedLower);
        reachedUpper = clampToCapacity(reachedUpper);

        for (PumpMonitor monitor : pumpMonitors) {
            monitor.judge(monitoring);
        }
        judgeLevel(reachedLower, reachedUpper);

        // The level the boiler can reach within the coming cycle, under worst-case steam, counting
        // only pumps already past their start-up latency and assuming defective pumps keep pouring.
        throughput = 0.0;
        double uncontrolledVolume = 0.0;
        for (PumpMonitor monitor : pumpMonitors) {
            if (monitor.surelyPouring()) {
                throughput = throughput + BoilerConstants.PUMP_CAPACITY_P;
            }
            if (!monitor.pumpSound()) {
                uncontrolledVolume = uncontrolledVolume + pumpVolume;
            }
        }
        double nextOutMax = BoilerConstants.STEAM_MAX_W * cycle;
        double nextOutMin = 0.0;
        if (steamReferenceValid) {
            nextOutMax = maximalSteamVolume(referenceSteam);
            nextOutMin = minimalSteamVolume(referenceSteam);
        }
        lowestReachable = Math.min(lowerLevel, lowerLevel - nextOutMax + throughput * cycle);
        highestReachable = Math.max(upperLevel, upperLevel - nextOutMin + uncontrolledVolume);
    }

    private void judgeSteam() {
        if (!steamStatus.sound()) {
            return;
        }
        double cycle = BoilerConstants.CYCLE_SECONDS;
        boolean valid = reportedSteam >= 0.0 && reportedSteam <= BoilerConstants.STEAM_MAX_W;
        if (valid && steamReferenceValid) {
            valid = reportedSteam <= referenceSteam + BoilerConstants.STEAM_INCREASE_U1 * cycle
                    && reportedSteam >= referenceSteam - BoilerConstants.STEAM_DECREASE_U2 * cycle;
        }
        if (valid) {
            referenceSteam = reportedSteam;
            steamReferenceValid = true;
        } else {
            steamReferenceValid = false;
            if (monitoring) {
                steamStatus.markFailed();
            }
        }
    }

    private void judgeLevel(@RoboChartType("real") double reachedLower,
            @RoboChartType("real") double reachedUpper) {
        if (levelStatus.sound()) {
            boolean valid = reportedLevel >= 0.0 && reportedLevel <= BoilerConstants.CAPACITY_C;
            if (valid && monitoring && levelReferenceValid) {
                valid = reportedLevel >= reachedLower && reportedLevel <= reachedUpper;
            }
            if (valid) {
                lowerLevel = reportedLevel;
                upperLevel = reportedLevel;
                estimatedLevel = reportedLevel;
                levelReferenceValid = true;
                return;
            }
            levelReferenceValid = false;
            if (monitoring) {
                levelStatus.markFailed();
            }
        }
        lowerLevel = reachedLower;
        upperLevel = reachedUpper;
        estimatedLevel = (reachedLower + reachedUpper) / 2.0;
    }

    /** Most steam that can leave during one cycle that starts at the given rate (U1, W). */
    @RoboChartType("real")
    private static double maximalSteamVolume(@RoboChartType("real") double startRate) {
        double cycle = BoilerConstants.CYCLE_SECONDS;
        double rising = startRate * cycle + 0.5 * BoilerConstants.STEAM_INCREASE_U1 * cycle * cycle;
        return Math.min(rising, BoilerConstants.STEAM_MAX_W * cycle);
    }

    /** Least steam that can leave during one cycle that starts at the given rate (U2). */
    @RoboChartType("real")
    private static double minimalSteamVolume(@RoboChartType("real") double startRate) {
        double cycle = BoilerConstants.CYCLE_SECONDS;
        double falling = startRate * cycle - 0.5 * BoilerConstants.STEAM_DECREASE_U2 * cycle * cycle;
        return Math.max(falling, 0.0);
    }

    /**
     * Bound on the steam that left during a cycle that started and ended at the given rates: the
     * rate stays within the envelope the gradients U1 and U2 allow between the two readings, and
     * within [0, W]. The upper envelope is concave and the lower one convex, so the midpoint rule
     * over-estimates the first and under-estimates the second: both bounds are sound.
     */
    @RoboChartType("real")
    private static double steamVolumeBetween(@RoboChartType("real") double startRate,
            @RoboChartType("real") double endRate, boolean upper) {
        double cycle = BoilerConstants.CYCLE_SECONDS;
        double slice = cycle / ESTIMATION_SLICES;
        double volume = 0.0;
        for (int i = 0; i < ESTIMATION_SLICES; i++) {
            double t = (i + 0.5) * slice;
            double rate;
            if (upper) {
                double fromStart = startRate + BoilerConstants.STEAM_INCREASE_U1 * t;
                double fromEnd = endRate + BoilerConstants.STEAM_DECREASE_U2 * (cycle - t);
                rate = Math.min(Math.min(fromStart, fromEnd), BoilerConstants.STEAM_MAX_W);
            } else {
                double fromStart = startRate - BoilerConstants.STEAM_DECREASE_U2 * t;
                double fromEnd = endRate - BoilerConstants.STEAM_INCREASE_U1 * (cycle - t);
                rate = Math.max(Math.max(fromStart, fromEnd), 0.0);
            }
            volume = volume + rate * slice;
        }
        return volume;
    }

    @RoboChartType("real")
    private static double clampToCapacity(@RoboChartType("real") double litres) {
        if (litres < 0.0) {
            return 0.0;
        }
        if (litres > BoilerConstants.CAPACITY_C) {
            return BoilerConstants.CAPACITY_C;
        }
        return litres;
    }

    private void summariseResults() {
        stopDemandedFlag = stopRun >= BoilerConstants.STOP_REPETITIONS;
        steamNonZeroFlag = reportedSteam != 0.0;
        belowBandFlag = estimatedLevel < BoilerConstants.NORMAL_N1;
        aboveBandFlag = estimatedLevel > BoilerConstants.NORMAL_N2;
        limitRiskFlag = lowestReachable < BoilerConstants.LIMIT_M1
                || highestReachable > BoilerConstants.LIMIT_M2;
        levelHealthy = levelStatus.sound();
        steamHealthy = steamStatus.sound();
        controllersHealthy = true;
        othersHealthy = steamStatus.sound();
        for (PumpMonitor monitor : pumpMonitors) {
            if (!monitor.controlSound()) {
                controllersHealthy = false;
                othersHealthy = false;
            }
            if (!monitor.pumpSound()) {
                othersHealthy = false;
            }
        }
    }

    private void clearInbox() {
        stopInbox = false;
        waitingInbox = false;
        readyInbox = false;
        levelInboxCount = 0;
        steamInboxCount = 0;
        levelRepairedInbox = false;
        steamRepairedInbox = false;
        levelAckInbox = false;
        steamAckInbox = false;
        for (PumpMonitor monitor : pumpMonitors) {
            monitor.clearInbox();
        }
    }

    // ---------------------------------------------------------------- queries

    /** Number of successive transmissions, up to and including this one, that carried STOP. */
    @RoboChartType("nat")
    public int successiveStops() {
        return stopRun;
    }

    /** STOP has arrived on STOP_REPETITIONS successive transmissions. */
    public boolean operatorStopDemanded() {
        return stopDemandedFlag;
    }

    /** A data message was missing or duplicated, or a protocol message was aberrant. */
    public boolean transmissionFailure() {
        return transmissionFault;
    }

    /** STEAM_BOILER_WAITING arrived in this transmission. */
    public boolean steamBoilerWaitingReceived() {
        return waitingArrived;
    }

    /** PHYSICAL_UNITS_READY arrived in this transmission. */
    public boolean physicalUnitsReadyReceived() {
        return readyArrived;
    }

    /** v: the steam rate reported in this transmission. */
    @RoboChartType("real")
    public double steamRate() {
        return reportedSteam;
    }

    /** q while the level device is sound; otherwise the midpoint of the estimated bounds. */
    @RoboChartType("real")
    public double waterLevel() {
        return estimatedLevel;
    }

    /** Lowest level the boiler can reach by the end of the coming cycle. */
    @RoboChartType("real")
    public double lowestReachableLevel() {
        return lowestReachable;
    }

    /** Highest level the boiler can reach by the end of the coming cycle. */
    @RoboChartType("real")
    public double highestReachableLevel() {
        return highestReachable;
    }

    /** The level (q, or its estimate) is below the minimal normal level N1. */
    public boolean levelBelowNormalBand() {
        return belowBandFlag;
    }

    /** The level (q, or its estimate) is above the maximal normal level N2. */
    public boolean levelAboveNormalBand() {
        return aboveBandFlag;
    }

    /** Within the coming cycle the level can fall below M1 or rise above M2. */
    public boolean levelLimitAtRisk() {
        return limitRiskFlag;
    }

    /** The steam rate reported in this transmission is not zero. */
    public boolean steamDetected() {
        return steamNonZeroFlag;
    }

    /** p: the throughput of the pumps that are surely pouring. */
    @RoboChartType("real")
    public double pumpThroughput() {
        return throughput;
    }

    /** Lower bound of the water level (equal to q while the level device is sound). */
    @RoboChartType("real")
    public double levelLowerBound() {
        return lowerLevel;
    }

    /** Upper bound of the water level (equal to q while the level device is sound). */
    @RoboChartType("real")
    public double levelUpperBound() {
        return upperLevel;
    }

    /** The level measuring device is believed sound. */
    public boolean levelDeviceSound() {
        return levelHealthy;
    }

    /** The steam measuring device is believed sound. */
    public boolean steamDeviceSound() {
        return steamHealthy;
    }

    /** All four pump controllers are believed sound. */
    public boolean pumpControllersSound() {
        return controllersHealthy;
    }

    /** Every unit other than the level measuring device is believed sound. */
    public boolean otherUnitsSound() {
        return othersHealthy;
    }
}
