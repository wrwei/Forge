package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.event.InputEvent;
import steam_boiler.event.ModeValue;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program (SB-V1, SB-V2, SB-V4). {@link #step} is called once
 * per cycle with the {@link InputEvent.Transmission} event, after the sensor has
 * received and analysed the cycle's incoming transmission; every transition sends
 * MODE(m) (SB-Beh24). Within a mode the guards are mutually exclusive, ordered by
 * severity: emergency stop, then rescue, then degraded, then regulation (SB-FR8).
 */
public final class SteamBoilerController {

    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    /** q: the level most recently reported by a sound level device (SB-V5). */
    @RoboChartType("real") private double levelReading = 0.0;
    /** v: the steam rate most recently reported (SB-V6). */
    @RoboChartType("real") private double steamReading = 0.0;
    /** p: throughput of the pumps known to pour water during the coming cycle (SB-V7, SB-Beh25). */
    @RoboChartType("real") private double pumpThroughput = 0.0;
    /** Lower bound on the water level (SB-V8); the reading itself while the level device works. */
    @RoboChartType("real") private double levelLow = 0.0;
    /** Upper bound on the water level (SB-V8); the reading itself while the level device works. */
    @RoboChartType("real") private double levelHigh = BoilerConstants.CAPACITY_C;
    /** Whether STEAM_BOILER_WAITING has arrived (SB-Beh2). */
    private boolean boilerWaiting = false;
    /** Whether PROGRAM_READY has been sent (SB-Beh7). */
    private boolean readySent = false;
    /** Whether the evacuation valve has been ordered open (SB-DM7, SB-Beh4). */
    private boolean valveOpen = false;
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /** Creates the controller in initialization mode (SB-Beh1). */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode. */
    public OperatingMode currentMode() { return currentMode; }

    /** Lower bound on the water level held at the end of the last cycle (litre). */
    @RoboChartType("real") public double lowerLevelBound() { return levelLow; }

    /** Upper bound on the water level held at the end of the last cycle (litre). */
    @RoboChartType("real") public double upperLevelBound() { return levelHigh; }

    /** Steam rate recorded at the end of the last cycle (litre/sec). */
    @RoboChartType("real") public double lastSteamRate() { return steamReading; }

    /** Performs one cycle's analysis and decisions. */
    public void step(InputEvent event) {
        // SB-Beh23: STOP on three successive transmissions.
        boolean operatorStop = sensor.successiveStopCount() >= BoilerConstants.STOP_LIMIT;
        // SB-FR5: transmission failure, plus the messages aberrant in the current mode.
        boolean transmissionFailed = sensor.transmissionFault();
        boolean unitsReadyNow = sensor.unitsReadyPresent();
        boolean readyUnrequested = unitsReadyNow && !readySent;
        boolean initTransmissionFailure = transmissionFailed || readyUnrequested;
        boolean startupMessageLate = sensor.boilerWaitingPresent() || unitsReadyNow;
        boolean runTransmissionFailure = transmissionFailed || startupMessageLate;
        // Initialization (SB-Beh2, SB-Beh3).
        boolean plantWaiting = boilerWaiting || sensor.boilerWaitingPresent();
        boolean steamNotZero = sensor.steamValue() != 0.0;
        // Unit soundness (SB-V9).
        boolean levelUnitFailed = sensor.levelDefective();
        boolean steamUnitFailed = sensor.steamDefective();
        boolean pumpUnitFailed = sensor.anyPumpDefective();
        boolean pumpControlUnitFailed = sensor.anyPumpControlDefective();
        boolean otherUnitFailed = steamUnitFailed || pumpUnitFailed || pumpControlUnitFailed;
        // Level against the normal bounds (SB-Beh4, SB-Beh5, SB-Beh7, SB-Beh10).
        boolean levelAboveN2 = sensor.levelValue() > BoilerConstants.NORMAL_N2;
        boolean levelBelowN1 = sensor.levelValue() < BoilerConstants.NORMAL_N1;
        boolean initEmergency = operatorStop || initTransmissionFailure
                || (plantWaiting && (steamNotZero || levelUnitFailed));
        // SB-V4, SB-Beh13: worst case over the next cycle from the reading (no water in, or all pumps pouring).
        boolean measuredRiskLow = (!steamUnitFailed && sensor.levelValue()
                - BoilerConstants.CYCLE_SECONDS * sensor.steamValue() - BoilerConstants.STEAM_RISE_MARGIN
                <= BoilerConstants.LIMIT_M1)
                || (steamUnitFailed && sensor.levelValue() - BoilerConstants.STEAM_MAX_OUTFLOW
                <= BoilerConstants.LIMIT_M1);
        boolean measuredRiskHigh = (!steamUnitFailed && sensor.levelValue() + BoilerConstants.PUMPS_MAX_INFLOW
                - BoilerConstants.CYCLE_SECONDS * sensor.steamValue() + BoilerConstants.STEAM_FALL_MARGIN
                >= BoilerConstants.LIMIT_M2)
                || (steamUnitFailed && sensor.levelValue() + BoilerConstants.PUMPS_MAX_INFLOW
                >= BoilerConstants.LIMIT_M2);
        boolean readingRisk = !levelUnitFailed && (measuredRiskLow || measuredRiskHigh);
        // SB-Beh19, SB-Beh21: the estimate is trustworthy only with a sound steam device and pump controllers.
        boolean estimateUnreliable = levelUnitFailed && (steamUnitFailed || pumpControlUnitFailed);
        // SB-V8, SB-Beh19: the estimate advanced over the elapsed cycle, then the worst case over the next.
        boolean estimatedRiskLow = levelLow + BoilerConstants.CYCLE_SECONDS * pumpThroughput
                - BoilerConstants.CYCLE_SECONDS * steamReading - BoilerConstants.STEAM_RISE_MARGIN
                - BoilerConstants.CYCLE_SECONDS * sensor.steamValue() - BoilerConstants.STEAM_RISE_MARGIN
                <= BoilerConstants.LIMIT_M1;
        boolean estimatedRiskHigh = levelHigh + BoilerConstants.CYCLE_SECONDS * sensor.openThroughput()
                - BoilerConstants.CYCLE_SECONDS * steamReading + BoilerConstants.STEAM_FALL_MARGIN
                + BoilerConstants.PUMPS_MAX_INFLOW
                - BoilerConstants.CYCLE_SECONDS * sensor.steamValue() + BoilerConstants.STEAM_FALL_MARGIN
                >= BoilerConstants.LIMIT_M2;
        boolean estimateRisk = levelUnitFailed && (estimatedRiskLow || estimatedRiskHigh);
        boolean levelRisk = readingRisk || estimateUnreliable || estimateRisk;
        boolean runEmergency = operatorStop || runTransmissionFailure || levelRisk;
        // SB-Beh19: regulation on the midpoint of the advanced estimate (sum of bounds against 2 * N).
        boolean estimateBelowN1 = levelLow + BoilerConstants.CYCLE_SECONDS * pumpThroughput
                - BoilerConstants.CYCLE_SECONDS * steamReading - BoilerConstants.STEAM_RISE_MARGIN
                + levelHigh + BoilerConstants.CYCLE_SECONDS * sensor.openThroughput()
                - BoilerConstants.CYCLE_SECONDS * steamReading + BoilerConstants.STEAM_FALL_MARGIN
                < 2.0 * BoilerConstants.NORMAL_N1;
        boolean estimateAboveN2 = levelLow + BoilerConstants.CYCLE_SECONDS * pumpThroughput
                - BoilerConstants.CYCLE_SECONDS * steamReading - BoilerConstants.STEAM_RISE_MARGIN
                + levelHigh + BoilerConstants.CYCLE_SECONDS * sensor.openThroughput()
                - BoilerConstants.CYCLE_SECONDS * steamReading + BoilerConstants.STEAM_FALL_MARGIN
                > 2.0 * BoilerConstants.NORMAL_N2;

        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.Transmission && operatorStop) {
                // SB-Beh23
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && initTransmissionFailure && !operatorStop) {
                // SB-Beh9
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && plantWaiting && steamNotZero
                    && !operatorStop && !initTransmissionFailure) {
                // SB-Beh3
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && plantWaiting && levelUnitFailed && !steamNotZero
                    && !operatorStop && !initTransmissionFailure) {
                // SB-Beh6
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !initEmergency
                    && unitsReadyNow && !otherUnitFailed) {
                // SB-Beh8: every unit sound
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_NORMAL));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                levelLow = sensor.levelValue();
                levelHigh = sensor.levelValue();
                pumpThroughput = sensor.flowingThroughput();
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !initEmergency
                    && unitsReadyNow && otherUnitFailed) {
                // SB-Beh8: some unit defective
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_DEGRADED));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                levelLow = sensor.levelValue();
                levelHigh = sensor.levelValue();
                pumpThroughput = sensor.flowingThroughput();
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !initEmergency
                    && !unitsReadyNow && levelAboveN2 && !valveOpen) {
                // SB-Beh4: open the evacuation valve
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                boilerWaiting = true;
                valveOpen = true;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !initEmergency
                    && !unitsReadyNow && levelAboveN2 && valveOpen) {
                // SB-Beh4: keep draining
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                boilerWaiting = true;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !initEmergency
                    && !unitsReadyNow && levelBelowN1 && !valveOpen) {
                // SB-Beh5: fill
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                boilerWaiting = true;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !initEmergency
                    && !unitsReadyNow && levelBelowN1 && valveOpen) {
                // SB-Beh4, SB-Beh5: drained too far; close the valve and fill
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                boilerWaiting = true;
                valveOpen = false;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !initEmergency
                    && !unitsReadyNow && !levelAboveN2 && !levelBelowN1 && valveOpen) {
                // SB-Beh4, SB-Beh7: level reached; close the valve and announce readiness
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.ProgramReady());
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                boilerWaiting = true;
                valveOpen = false;
                readySent = true;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !initEmergency
                    && !unitsReadyNow && !levelAboveN2 && !levelBelowN1 && !valveOpen) {
                // SB-Beh7: level between N1 and N2; announce readiness until PHYSICAL_UNITS_READY
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.ProgramReady());
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                boilerWaiting = true;
                readySent = true;
            } else if (event instanceof InputEvent.Transmission && !plantWaiting
                    && !operatorStop && !initTransmissionFailure) {
                // SB-Beh2: nothing else happens until STEAM_BOILER_WAITING arrives
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_INITIALIZATION));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.Transmission && operatorStop) {
                // SB-Beh23
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && runTransmissionFailure && !operatorStop) {
                // SB-Beh14
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && levelRisk
                    && !operatorStop && !runTransmissionFailure) {
                // SB-Beh13
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && !runEmergency && levelUnitFailed) {
                // SB-Beh11: start estimating the level
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_RESCUE));
                levelLow = levelLow + BoilerConstants.CYCLE_SECONDS * pumpThroughput
                        - BoilerConstants.CYCLE_SECONDS * steamReading - BoilerConstants.STEAM_RISE_MARGIN;
                levelHigh = levelHigh + BoilerConstants.CYCLE_SECONDS * sensor.openThroughput()
                        - BoilerConstants.CYCLE_SECONDS * steamReading + BoilerConstants.STEAM_FALL_MARGIN;
                steamReading = sensor.steamValue();
                pumpThroughput = sensor.flowingThroughput();
            } else if (event instanceof InputEvent.Transmission && !runEmergency && !levelUnitFailed
                    && otherUnitFailed) {
                // SB-Beh12
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_DEGRADED));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                levelLow = sensor.levelValue();
                levelHigh = sensor.levelValue();
                pumpThroughput = sensor.flowingThroughput();
            } else if (event instanceof InputEvent.Transmission && !runEmergency && !levelUnitFailed
                    && !otherUnitFailed && levelBelowN1) {
                // SB-Beh10: level below N1; switch the pumps on
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_NORMAL));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                levelLow = sensor.levelValue();
                levelHigh = sensor.levelValue();
                pumpThroughput = sensor.flowingThroughput();
            } else if (event instanceof InputEvent.Transmission && !runEmergency && !levelUnitFailed
                    && !otherUnitFailed && levelAboveN2) {
                // SB-Beh10: level above N2; switch the pumps off
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_NORMAL));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                levelLow = sensor.levelValue();
                levelHigh = sensor.levelValue();
                pumpThroughput = 0.0;
            } else if (event instanceof InputEvent.Transmission && !runEmergency && !levelUnitFailed
                    && !otherUnitFailed && !levelBelowN1 && !levelAboveN2) {
                // SB-Beh10: level between N1 and N2; pumps unchanged
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_NORMAL));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                levelLow = sensor.levelValue();
                levelHigh = sensor.levelValue();
                pumpThroughput = sensor.flowingThroughput();
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.Transmission && operatorStop) {
                // SB-Beh23
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && runTransmissionFailure && !operatorStop) {
                // SB-Beh18: transmission failure
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && levelRisk
                    && !operatorStop && !runTransmissionFailure) {
                // SB-Beh18: level risks M1 or M2
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && !runEmergency && levelUnitFailed) {
                // SB-Beh17: start estimating the level
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_RESCUE));
                levelLow = levelLow + BoilerConstants.CYCLE_SECONDS * pumpThroughput
                        - BoilerConstants.CYCLE_SECONDS * steamReading - BoilerConstants.STEAM_RISE_MARGIN;
                levelHigh = levelHigh + BoilerConstants.CYCLE_SECONDS * sensor.openThroughput()
                        - BoilerConstants.CYCLE_SECONDS * steamReading + BoilerConstants.STEAM_FALL_MARGIN;
                steamReading = sensor.steamValue();
                pumpThroughput = sensor.flowingThroughput();
            } else if (event instanceof InputEvent.Transmission && !runEmergency && !levelUnitFailed
                    && !otherUnitFailed) {
                // SB-Beh16: every failed unit repaired
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_NORMAL));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                levelLow = sensor.levelValue();
                levelHigh = sensor.levelValue();
                pumpThroughput = sensor.flowingThroughput();
            } else if (event instanceof InputEvent.Transmission && !runEmergency && !levelUnitFailed
                    && otherUnitFailed && levelBelowN1) {
                // SB-Beh15: regulate as in normal mode; level below N1
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                levelLow = sensor.levelValue();
                levelHigh = sensor.levelValue();
                pumpThroughput = sensor.flowingThroughput();
            } else if (event instanceof InputEvent.Transmission && !runEmergency && !levelUnitFailed
                    && otherUnitFailed && levelAboveN2) {
                // SB-Beh15: level above N2
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                levelLow = sensor.levelValue();
                levelHigh = sensor.levelValue();
                pumpThroughput = 0.0;
            } else if (event instanceof InputEvent.Transmission && !runEmergency && !levelUnitFailed
                    && otherUnitFailed && !levelBelowN1 && !levelAboveN2) {
                // SB-Beh15: level between N1 and N2
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_DEGRADED));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                levelLow = sensor.levelValue();
                levelHigh = sensor.levelValue();
                pumpThroughput = sensor.flowingThroughput();
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.Transmission && operatorStop) {
                // SB-Beh23
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && runTransmissionFailure && !operatorStop) {
                // SB-Beh21: transmission failure
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && estimateUnreliable
                    && !operatorStop && !runTransmissionFailure) {
                // SB-Beh21: steam device or pump-controller failure
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && (readingRisk || estimateRisk)
                    && !estimateUnreliable && !operatorStop && !runTransmissionFailure) {
                // SB-Beh21: (estimated) level risks M1 or M2
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && !runEmergency && !levelUnitFailed
                    && !otherUnitFailed) {
                // SB-Beh20: level device repaired, no other unit defective
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_NORMAL));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                levelLow = sensor.levelValue();
                levelHigh = sensor.levelValue();
                pumpThroughput = sensor.flowingThroughput();
            } else if (event instanceof InputEvent.Transmission && !runEmergency && !levelUnitFailed
                    && otherUnitFailed) {
                // SB-Beh20: level device repaired, some other unit defective
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_DEGRADED));
                levelReading = sensor.levelValue();
                steamReading = sensor.steamValue();
                levelLow = sensor.levelValue();
                levelHigh = sensor.levelValue();
                pumpThroughput = sensor.flowingThroughput();
            } else if (event instanceof InputEvent.Transmission && !runEmergency && levelUnitFailed
                    && estimateBelowN1) {
                // SB-Beh19: estimated level below N1
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_RESCUE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                levelLow = levelLow + BoilerConstants.CYCLE_SECONDS * pumpThroughput
                        - BoilerConstants.CYCLE_SECONDS * steamReading - BoilerConstants.STEAM_RISE_MARGIN;
                levelHigh = levelHigh + BoilerConstants.CYCLE_SECONDS * sensor.openThroughput()
                        - BoilerConstants.CYCLE_SECONDS * steamReading + BoilerConstants.STEAM_FALL_MARGIN;
                steamReading = sensor.steamValue();
                pumpThroughput = sensor.flowingThroughput();
            } else if (event instanceof InputEvent.Transmission && !runEmergency && levelUnitFailed
                    && estimateAboveN2 && !estimateBelowN1) {
                // SB-Beh19: estimated level above N2
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_RESCUE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                levelLow = levelLow + BoilerConstants.CYCLE_SECONDS * pumpThroughput
                        - BoilerConstants.CYCLE_SECONDS * steamReading - BoilerConstants.STEAM_RISE_MARGIN;
                levelHigh = levelHigh + BoilerConstants.CYCLE_SECONDS * sensor.openThroughput()
                        - BoilerConstants.CYCLE_SECONDS * steamReading + BoilerConstants.STEAM_FALL_MARGIN;
                steamReading = sensor.steamValue();
                pumpThroughput = 0.0;
            } else if (event instanceof InputEvent.Transmission && !runEmergency && levelUnitFailed
                    && !estimateBelowN1 && !estimateAboveN2) {
                // SB-Beh19: estimated level between N1 and N2
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeAnnouncement(ModeValue.MODE_RESCUE));
                levelLow = levelLow + BoilerConstants.CYCLE_SECONDS * pumpThroughput
                        - BoilerConstants.CYCLE_SECONDS * steamReading - BoilerConstants.STEAM_RISE_MARGIN;
                levelHigh = levelHigh + BoilerConstants.CYCLE_SECONDS * sensor.openThroughput()
                        - BoilerConstants.CYCLE_SECONDS * steamReading + BoilerConstants.STEAM_FALL_MARGIN;
                steamReading = sensor.steamValue();
                pumpThroughput = sensor.flowingThroughput();
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // SB-Beh22: terminal; the program has stopped and the plant is in the
            // physical environment's hands, so no transition leaves this mode.
        }
    }
}
