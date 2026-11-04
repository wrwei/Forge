package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.ModeValue;
import steam_boiler.mode.OperatingMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program's mode logic (SB-V1, SB-V2). Stepped once
 * per five-second cycle, after the cycle's transmission has been received and
 * analysed (SB-V3). Each branch sends MODE(m) for the mode it enters
 * (SB-Beh24) and then the mode's other outgoing messages.
 */
public final class SteamBoilerController {

    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    /** Initialisation: STEAM_BOILER_WAITING has arrived (SB-Beh2). */
    private boolean steamBoilerWaitingSeen = false;
    /** Initialisation: PROGRAM_READY has been announced (SB-Beh7). */
    private boolean readyAnnounced = false;
    /** The evacuation valve has been ordered open (SB-DM7, SB-Beh4). */
    private boolean valveOpen = false;

    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /** Creates the controller in initialization mode (SB-Beh1). */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode. */
    public OperatingMode currentMode() {
        return currentMode;
    }

    /** One control cycle: analyse the received transmission and issue the outgoing messages. */
    public void step(InputEvent event) {
        // --- Named predicates ---
        boolean stopDemanded = sensor.operatorStopConfirmed();
        boolean transmissionFailed = sensor.transmissionFailure();
        boolean plantWaitingNow = sensor.steamBoilerWaitingReceived();
        boolean unitsReadyNow = sensor.physicalUnitsReadyReceived();
        boolean steamNotZero = sensor.steamReading() != 0.0;
        boolean levelDeviceFailed = sensor.levelDeviceOutOfOrder();
        boolean steamDeviceFailed = sensor.steamDeviceOutOfOrder();
        boolean pumpControllerFailed = sensor.anyPumpControllerOutOfOrder();
        boolean otherUnitFailed = sensor.anyPumpOutOfOrder() || pumpControllerFailed || steamDeviceFailed;
        // Initialisation adjusts on the level reading itself (SB-Beh4, SB-Beh5, SB-Beh7).
        boolean levelBelowN1 = sensor.levelReading() < SteamBoilerConstants.MINIMAL_NORMAL_N1;
        // N1 < N2, so !levelBelowN1 is implied; stated to keep the branches disjoint in the model.
        boolean levelAboveN2 = !levelBelowN1 && sensor.levelReading() > SteamBoilerConstants.MAXIMAL_NORMAL_N2;
        boolean levelWithinNormal = !levelBelowN1 && !levelAboveN2;
        // The operating modes regulate on the level predicted until the next transmission,
        // so that the pumps' start-up latency is anticipated (SB-Beh10, SB-Beh19, SB-Beh25).
        boolean levelMayFallBelowN1 = sensor.predictedMinLevel() < SteamBoilerConstants.MINIMAL_NORMAL_N1;
        boolean levelMayRiseAboveN2 = !levelMayFallBelowN1 && sensor.predictedMaxLevel() > SteamBoilerConstants.MAXIMAL_NORMAL_N2;
        boolean levelStaysNormal = !levelMayFallBelowN1 && !levelMayRiseAboveN2;
        // When the program closes the pumps in this cycle, the closed-pump predictions apply (SB-V4, SB-Beh13).
        boolean riskBelowM1 = (levelMayRiseAboveN2 && sensor.predictedMinLevelPumpsClosed() <= SteamBoilerConstants.MINIMAL_LIMIT_M1)
                || (!levelMayRiseAboveN2 && sensor.predictedMinLevel() <= SteamBoilerConstants.MINIMAL_LIMIT_M1);
        boolean riskAboveM2 = (levelMayRiseAboveN2 && sensor.predictedMaxLevelPumpsClosed() >= SteamBoilerConstants.MAXIMAL_LIMIT_M2)
                || (!levelMayRiseAboveN2 && sensor.predictedMaxLevel() >= SteamBoilerConstants.MAXIMAL_LIMIT_M2);
        boolean plantWaiting = steamBoilerWaitingSeen || plantWaitingNow;
        boolean initAbort = stopDemanded || transmissionFailed || (plantWaiting && (steamNotZero || levelDeviceFailed));
        boolean initAwaitingUnits = !initAbort && plantWaiting && readyAnnounced;
        boolean initAdjusting = !initAbort && plantWaiting && !readyAnnounced;
        boolean operatingEmergency = stopDemanded || transmissionFailed || riskBelowM1 || riskAboveM2;
        boolean rescueEmergency = operatingEmergency || steamDeviceFailed || pumpControllerFailed;
        boolean normalRegulating = !operatingEmergency && !levelDeviceFailed && !otherUnitFailed;
        boolean degradedRegulating = !operatingEmergency && !levelDeviceFailed && otherUnitFailed;
        boolean rescueRegulating = !rescueEmergency && levelDeviceFailed;

        // --- Mode-nested transitions ---
        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.Transmission && initAbort && valveOpen) {
                // SB-Beh3, SB-Beh6, SB-Beh9, SB-Beh23: abort; the valve is closed on the way out.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
                actuator.apply(new OutputEvent.Valve());
                valveOpen = false;
            } else if (event instanceof InputEvent.Transmission && initAbort && !valveOpen) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !initAbort && !plantWaiting) {
                // SB-Beh2: nothing happens until STEAM_BOILER_WAITING arrives.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
            } else if (event instanceof InputEvent.Transmission && initAwaitingUnits && unitsReadyNow && !otherUnitFailed) {
                // SB-Beh8: every unit sound.
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && initAwaitingUnits && unitsReadyNow && otherUnitFailed) {
                // SB-Beh8: some unit defective.
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && initAwaitingUnits && !unitsReadyNow) {
                // SB-Beh7: repeat PROGRAM_READY until PHYSICAL_UNITS_READY.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.Transmission && initAdjusting && levelAboveN2 && !valveOpen) {
                // SB-Beh4: drain the excess through the evacuation valve.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                valveOpen = true;
                steamBoilerWaitingSeen = true;
            } else if (event instanceof InputEvent.Transmission && initAdjusting && levelAboveN2 && valveOpen) {
                // SB-Beh4: keep draining.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                steamBoilerWaitingSeen = true;
            } else if (event instanceof InputEvent.Transmission && initAdjusting && levelBelowN1 && valveOpen) {
                // SB-Beh4, SB-Beh5: drained too far; close the valve and fill.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                valveOpen = false;
                steamBoilerWaitingSeen = true;
            } else if (event instanceof InputEvent.Transmission && initAdjusting && levelBelowN1 && !valveOpen) {
                // SB-Beh5: fill with the pumps.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                steamBoilerWaitingSeen = true;
            } else if (event instanceof InputEvent.Transmission && initAdjusting && levelWithinNormal && valveOpen) {
                // SB-Beh7: level between N1 and N2; stop adjusting and announce PROGRAM_READY.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.ProgramReady());
                valveOpen = false;
                readyAnnounced = true;
                steamBoilerWaitingSeen = true;
            } else if (event instanceof InputEvent.Transmission && initAdjusting && levelWithinNormal && !valveOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.ProgramReady());
                readyAnnounced = true;
                steamBoilerWaitingSeen = true;
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.Transmission && operatingEmergency) {
                // SB-Beh13, SB-Beh14, SB-Beh23.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !operatingEmergency && levelDeviceFailed) {
                // SB-Beh11 (SB-FR8: rescue dominates degraded).
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && !operatingEmergency && !levelDeviceFailed && otherUnitFailed) {
                // SB-Beh12.
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && normalRegulating && levelMayFallBelowN1) {
                // SB-Beh10: the level could fall below N1, switch the pumps on.
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && normalRegulating && levelMayRiseAboveN2) {
                // SB-Beh10: the level could rise above N2, switch the pumps off.
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && normalRegulating && levelStaysNormal) {
                // SB-Beh10: the level stays between N1 and N2, leave the pumps as they are.
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.Transmission && operatingEmergency) {
                // SB-Beh18, SB-Beh23.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !operatingEmergency && levelDeviceFailed) {
                // SB-Beh17.
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && !operatingEmergency && !levelDeviceFailed && !otherUnitFailed) {
                // SB-Beh16: every failed unit repaired.
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && degradedRegulating && levelMayFallBelowN1) {
                // SB-Beh15: same regulation as normal mode.
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && degradedRegulating && levelMayRiseAboveN2) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && degradedRegulating && levelStaysNormal) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.Transmission && rescueEmergency) {
                // SB-Beh21, SB-Beh23 (SB-FR8: emergency stop dominates).
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !rescueEmergency && !levelDeviceFailed && !otherUnitFailed) {
                // SB-Beh20: level device repaired, no other unit defective.
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && !rescueEmergency && !levelDeviceFailed && otherUnitFailed) {
                // SB-Beh20: level device repaired, another unit still defective.
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && rescueRegulating && levelMayFallBelowN1) {
                // SB-Beh19: regulate on the estimated level.
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && rescueRegulating && levelMayRiseAboveN2) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && rescueRegulating && levelStaysNormal) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // SB-Beh22: terminal; the program's control task has ended.
        }
    }
}
