package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.OperatingMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program's mode logic (SB-V1, SB-V2). Stepped once
 * per five-second cycle with the event the unit monitor derives from the
 * cycle's transmission (SB-V3). The controller decides the operating mode, the
 * pump command, the evacuation valve and the start-up handshake; the program's
 * transmission stage announces the mode (SB-Beh24) and turns the pump command
 * into OPEN_PUMP / CLOSE_PUMP messages.
 *
 * <p>Within a mode the branches are listed by precedence (SB-FR8): the
 * conditions that demand emergency stop in every mode arrive as their own
 * events and come first; the guards of the remaining branches repeat the
 * negations of the conditions above them, so that for each event the branches
 * are mutually exclusive and together cover every case.
 */
public final class SteamBoilerController {

    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    /** Initialisation: STEAM_BOILER_WAITING has arrived (SB-Beh2). */
    private boolean steamBoilerWaitingSeen = false;
    /** Initialisation: PROGRAM_READY has been announced (SB-Beh7). */
    private boolean readyAnnounced = false;
    /** The evacuation valve has been ordered open (SB-DM7, SB-Beh4). */
    private boolean valveOpen = false;
    /** The pump command: every pump to be open (true) or closed (false) (SB-DM3, SB-Beh5, SB-Beh10). */
    private boolean pumpsOpen = false;

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

    /** The current pump command: true when every pump is to be open. */
    public boolean pumpsOpen() {
        return pumpsOpen;
    }

    /** One control cycle: react to the analysed transmission. */
    public void step(InputEvent event) {
        // --- Named predicates ---
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
        boolean plantWaiting = steamBoilerWaitingSeen;
        boolean programReady = readyAnnounced;
        boolean valveIsOpen = valveOpen;

        // --- Mode-nested transitions ---
        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.StopConfirmed) {
                // SB-Beh23: STOP on three successive transmissions.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.TransmissionFault) {
                // SB-Beh9.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.SteamBoilerWaiting && !plantWaiting) {
                // SB-Beh2: STEAM_BOILER_WAITING has arrived; the checks start.
                currentMode = OperatingMode.INITIALIZATION;
                steamBoilerWaitingSeen = true;
            } else if (event instanceof InputEvent.PhysicalUnitsReady && programReady && levelDeviceFailed) {
                // SB-Beh6.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.PhysicalUnitsReady && programReady && !levelDeviceFailed
                    && steamDeviceFailed) {
                // SB-Beh3.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.PhysicalUnitsReady && programReady && !levelDeviceFailed
                    && !steamDeviceFailed && !otherUnitFailed) {
                // SB-Beh8: every unit sound.
                currentMode = OperatingMode.NORMAL;
            } else if (event instanceof InputEvent.PhysicalUnitsReady && programReady && !levelDeviceFailed
                    && !steamDeviceFailed && otherUnitFailed) {
                // SB-Beh8: some unit defective.
                currentMode = OperatingMode.DEGRADED;
            } else if (event instanceof InputEvent.Transmission && !plantWaiting) {
                // SB-Beh2: nothing happens until STEAM_BOILER_WAITING arrives.
                currentMode = OperatingMode.INITIALIZATION;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && levelDeviceFailed) {
                // SB-Beh6.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !levelDeviceFailed
                    && steamDeviceFailed) {
                // SB-Beh3: steam rate not zero, the steam device is judged defective.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !levelDeviceFailed
                    && !steamDeviceFailed && programReady) {
                // SB-Beh7: repeat PROGRAM_READY until PHYSICAL_UNITS_READY.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !levelDeviceFailed
                    && !steamDeviceFailed && !programReady && levelAboveN2 && !valveIsOpen) {
                // SB-Beh4: drain the excess through the evacuation valve.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Valve());
                valveOpen = true;
                pumpsOpen = false;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !levelDeviceFailed
                    && !steamDeviceFailed && !programReady && levelAboveN2 && valveIsOpen) {
                // SB-Beh4: keep draining while the level is excessive.
                currentMode = OperatingMode.INITIALIZATION;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !levelDeviceFailed
                    && !steamDeviceFailed && !programReady && !levelAboveN2 && valveIsOpen) {
                // SB-Beh4: the level is no longer excessive; close the valve.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Valve());
                valveOpen = false;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !levelDeviceFailed
                    && !steamDeviceFailed && !programReady && !levelAboveN2 && levelBelowN1 && !valveIsOpen) {
                // SB-Beh5: fill with the pumps.
                currentMode = OperatingMode.INITIALIZATION;
                pumpsOpen = true;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !levelDeviceFailed
                    && !steamDeviceFailed && !programReady && !levelAboveN2 && !levelBelowN1 && !valveIsOpen) {
                // SB-Beh7: level between N1 and N2; stop filling and announce PROGRAM_READY.
                currentMode = OperatingMode.INITIALIZATION;
                pumpsOpen = false;
                actuator.apply(new OutputEvent.ProgramReady());
                readyAnnounced = true;
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.StopConfirmed) {
                // SB-Beh23.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.TransmissionFault) {
                // SB-Beh14.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.LevelRisk) {
                // SB-Beh13.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.Transmission && levelDeviceFailed) {
                // SB-Beh11 (SB-FR8: rescue dominates degraded).
                currentMode = OperatingMode.RESCUE;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && otherUnitFailed) {
                // SB-Beh12.
                currentMode = OperatingMode.DEGRADED;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && !otherUnitFailed
                    && levelMayFallBelowN1) {
                // SB-Beh10: the level could fall below N1, switch the pumps on.
                currentMode = OperatingMode.NORMAL;
                pumpsOpen = true;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && !otherUnitFailed
                    && !levelMayFallBelowN1 && levelMayRiseAboveN2) {
                // SB-Beh10: the level could rise above N2, switch the pumps off.
                currentMode = OperatingMode.NORMAL;
                pumpsOpen = false;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && !otherUnitFailed
                    && !levelMayFallBelowN1 && !levelMayRiseAboveN2) {
                // SB-Beh10: the level stays between N1 and N2, leave the pumps as they are.
                currentMode = OperatingMode.NORMAL;
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.StopConfirmed) {
                // SB-Beh23.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.TransmissionFault) {
                // SB-Beh18.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.LevelRisk) {
                // SB-Beh18.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.Transmission && levelDeviceFailed) {
                // SB-Beh17.
                currentMode = OperatingMode.RESCUE;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && !otherUnitFailed) {
                // SB-Beh16: every failed unit repaired.
                currentMode = OperatingMode.NORMAL;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && otherUnitFailed
                    && levelMayFallBelowN1) {
                // SB-Beh15: same regulation as normal mode.
                currentMode = OperatingMode.DEGRADED;
                pumpsOpen = true;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && otherUnitFailed
                    && !levelMayFallBelowN1 && levelMayRiseAboveN2) {
                currentMode = OperatingMode.DEGRADED;
                pumpsOpen = false;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && otherUnitFailed
                    && !levelMayFallBelowN1 && !levelMayRiseAboveN2) {
                currentMode = OperatingMode.DEGRADED;
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.StopConfirmed) {
                // SB-Beh23.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.TransmissionFault) {
                // SB-Beh21: transmission failure.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.LevelRisk) {
                // SB-Beh21: the estimated level risks M1 or M2.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.Transmission && steamDeviceFailed) {
                // SB-Beh21: steam device failed, the estimate is no longer trustworthy.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.Transmission && !steamDeviceFailed && pumpControllerFailed) {
                // SB-Beh21: pump-controller information no longer reliable.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.Transmission && !steamDeviceFailed && !pumpControllerFailed
                    && !levelDeviceFailed && !otherUnitFailed) {
                // SB-Beh20: level device repaired, no other unit defective.
                currentMode = OperatingMode.NORMAL;
            } else if (event instanceof InputEvent.Transmission && !steamDeviceFailed && !pumpControllerFailed
                    && !levelDeviceFailed && otherUnitFailed) {
                // SB-Beh20: level device repaired, another unit still defective.
                currentMode = OperatingMode.DEGRADED;
            } else if (event instanceof InputEvent.Transmission && !steamDeviceFailed && !pumpControllerFailed
                    && levelDeviceFailed && levelMayFallBelowN1) {
                // SB-Beh19: regulate on the estimated level.
                currentMode = OperatingMode.RESCUE;
                pumpsOpen = true;
            } else if (event instanceof InputEvent.Transmission && !steamDeviceFailed && !pumpControllerFailed
                    && levelDeviceFailed && !levelMayFallBelowN1 && levelMayRiseAboveN2) {
                currentMode = OperatingMode.RESCUE;
                pumpsOpen = false;
            } else if (event instanceof InputEvent.Transmission && !steamDeviceFailed && !pumpControllerFailed
                    && levelDeviceFailed && !levelMayFallBelowN1 && !levelMayRiseAboveN2) {
                currentMode = OperatingMode.RESCUE;
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            if (event instanceof InputEvent.Transmission) {
                // SB-Beh22: the control task has ended; the program only goes on announcing its mode (SB-Beh24).
                currentMode = OperatingMode.EMERGENCY_STOP;
            }
        }
    }
}
