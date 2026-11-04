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
 *
 * <p>Within a mode the branches are listed by precedence (SB-FR8); each guard
 * repeats the negations of the conditions above it, so the branches are
 * mutually exclusive and together cover every case.
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
        boolean levelDeviceFailed = sensor.levelDeviceOutOfOrder();
        boolean steamDeviceFailed = sensor.steamDeviceOutOfOrder();
        boolean pumpControllerFailed = sensor.pumpControllerOutOfOrder();
        boolean otherUnitFailed = sensor.otherUnitOutOfOrder();
        // Initialisation adjusts on the level reading (SB-Beh4, SB-Beh5, SB-Beh7).
        boolean levelBelowN1 = sensor.levelBelowNormal();
        boolean levelAboveN2 = sensor.levelAboveNormal();
        // The operating modes regulate on the level predicted until the next
        // transmission, anticipating the pumps' start-up latency (SB-Beh25).
        boolean levelMayFallBelowN1 = sensor.levelMayFallBelowNormal();
        boolean levelMayRiseAboveN2 = sensor.levelMayRiseAboveNormal();
        boolean levelRisk = sensor.levelRiskOfLimit();
        boolean plantWaiting = steamBoilerWaitingSeen;
        boolean programReady = readyAnnounced;
        boolean valveIsOpen = valveOpen;

        // --- Mode-nested transitions ---
        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.Transmission && stopDemanded) {
                // SB-Beh23.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && transmissionFailed) {
                // SB-Beh9.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && plantWaiting && levelDeviceFailed) {
                // SB-Beh6.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && plantWaiting && !levelDeviceFailed && steamDeviceFailed) {
                // SB-Beh3: steam rate not zero, the steam device is judged defective.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !plantWaiting && !plantWaitingNow) {
                // SB-Beh2: nothing happens until STEAM_BOILER_WAITING arrives.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !plantWaiting && plantWaitingNow) {
                // SB-Beh2: STEAM_BOILER_WAITING has arrived; the checks start.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                steamBoilerWaitingSeen = true;
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && plantWaiting && !levelDeviceFailed && !steamDeviceFailed
                    && programReady && unitsReadyNow && !otherUnitFailed) {
                // SB-Beh8: every unit sound.
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && plantWaiting && !levelDeviceFailed && !steamDeviceFailed
                    && programReady && unitsReadyNow && otherUnitFailed) {
                // SB-Beh8: some unit defective.
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && plantWaiting && !levelDeviceFailed && !steamDeviceFailed
                    && programReady && !unitsReadyNow) {
                // SB-Beh7: repeat PROGRAM_READY until PHYSICAL_UNITS_READY.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && plantWaiting && !levelDeviceFailed && !steamDeviceFailed
                    && !programReady && levelAboveN2 && !valveIsOpen) {
                // SB-Beh4: drain the excess through the evacuation valve.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                valveOpen = true;
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && plantWaiting && !levelDeviceFailed && !steamDeviceFailed
                    && !programReady && levelAboveN2 && valveIsOpen) {
                // SB-Beh4: keep draining while the level is excessive.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && plantWaiting && !levelDeviceFailed && !steamDeviceFailed
                    && !programReady && !levelAboveN2 && valveIsOpen) {
                // SB-Beh4: the level is no longer excessive; close the valve.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
                valveOpen = false;
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && plantWaiting && !levelDeviceFailed && !steamDeviceFailed
                    && !programReady && !levelAboveN2 && levelBelowN1 && !valveIsOpen) {
                // SB-Beh5: fill with the pumps.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && plantWaiting && !levelDeviceFailed && !steamDeviceFailed
                    && !programReady && !levelAboveN2 && !levelBelowN1 && !valveIsOpen) {
                // SB-Beh7: level between N1 and N2; stop filling and announce PROGRAM_READY.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.ProgramReady());
                readyAnnounced = true;
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.Transmission && stopDemanded) {
                // SB-Beh23.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && transmissionFailed) {
                // SB-Beh14.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && levelRisk) {
                // SB-Beh13.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && levelDeviceFailed) {
                // SB-Beh11 (SB-FR8: rescue dominates degraded).
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && !levelDeviceFailed && otherUnitFailed) {
                // SB-Beh12.
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && !levelDeviceFailed && !otherUnitFailed
                    && levelMayFallBelowN1) {
                // SB-Beh10: the level could fall below N1, switch the pumps on.
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && !levelDeviceFailed && !otherUnitFailed
                    && !levelMayFallBelowN1 && levelMayRiseAboveN2) {
                // SB-Beh10: the level could rise above N2, switch the pumps off.
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && !levelDeviceFailed && !otherUnitFailed
                    && !levelMayFallBelowN1 && !levelMayRiseAboveN2) {
                // SB-Beh10: the level stays between N1 and N2, leave the pumps as they are.
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.Transmission && stopDemanded) {
                // SB-Beh23.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && transmissionFailed) {
                // SB-Beh18.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && levelRisk) {
                // SB-Beh18.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && levelDeviceFailed) {
                // SB-Beh17.
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && !levelDeviceFailed && !otherUnitFailed) {
                // SB-Beh16: every failed unit repaired.
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && !levelDeviceFailed && otherUnitFailed
                    && levelMayFallBelowN1) {
                // SB-Beh15: same regulation as normal mode.
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && !levelDeviceFailed && otherUnitFailed
                    && !levelMayFallBelowN1 && levelMayRiseAboveN2) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && !levelDeviceFailed && otherUnitFailed
                    && !levelMayFallBelowN1 && !levelMayRiseAboveN2) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.Transmission && stopDemanded) {
                // SB-Beh23.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && transmissionFailed) {
                // SB-Beh21: transmission failure.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && levelRisk) {
                // SB-Beh21: the estimated level risks M1 or M2.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && steamDeviceFailed) {
                // SB-Beh21: steam device failed, the estimate is no longer trustworthy.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && !steamDeviceFailed && pumpControllerFailed) {
                // SB-Beh21: pump-controller information no longer reliable.
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && !steamDeviceFailed && !pumpControllerFailed
                    && !levelDeviceFailed && !otherUnitFailed) {
                // SB-Beh20: level device repaired, no other unit defective.
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && !steamDeviceFailed && !pumpControllerFailed
                    && !levelDeviceFailed && otherUnitFailed) {
                // SB-Beh20: level device repaired, another unit still defective.
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && !steamDeviceFailed && !pumpControllerFailed
                    && levelDeviceFailed && levelMayFallBelowN1) {
                // SB-Beh19: regulate on the estimated level.
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && !steamDeviceFailed && !pumpControllerFailed
                    && levelDeviceFailed && !levelMayFallBelowN1 && levelMayRiseAboveN2) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && !transmissionFailed
                    && !levelRisk && !steamDeviceFailed && !pumpControllerFailed
                    && levelDeviceFailed && !levelMayFallBelowN1 && !levelMayRiseAboveN2) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // SB-Beh22: terminal; the program's control task has ended.
        }
    }
}
