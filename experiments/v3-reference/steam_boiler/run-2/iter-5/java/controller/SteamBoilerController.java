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
 * conditions that demand emergency stop arrive as their own events and come
 * first; the guards of the remaining branches repeat the negations of the
 * conditions above them, so that for each event the branches are mutually
 * exclusive. A cycle that matches no branch changes nothing.
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
        boolean otherUnitFailed = sensor.otherUnitOutOfOrder();
        // Initialisation: the reading against N1 and N2 (SB-Beh4, SB-Beh5, SB-Beh7).
        // Other modes: the level predicted until the next transmission, which
        // anticipates the pumps' start-up latency (SB-Beh10, SB-Beh19, SB-Beh25).
        boolean levelTooLow = sensor.levelTooLow();
        boolean levelTooHigh = sensor.levelTooHigh();
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
            } else if (event instanceof InputEvent.StartUpFailure) {
                // SB-Beh3, SB-Beh6: level device failed, or steam rate not zero.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.SteamBoilerWaiting) {
                // SB-Beh2: STEAM_BOILER_WAITING has arrived; the adjustment starts. Before it, nothing happens.
                currentMode = OperatingMode.INITIALIZATION;
                steamBoilerWaitingSeen = true;
            } else if (event instanceof InputEvent.PhysicalUnitsReady && programReady && !otherUnitFailed) {
                // SB-Beh8: every unit sound.
                currentMode = OperatingMode.NORMAL;
            } else if (event instanceof InputEvent.PhysicalUnitsReady && programReady && otherUnitFailed) {
                // SB-Beh8: some unit defective.
                currentMode = OperatingMode.DEGRADED;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && programReady) {
                // SB-Beh7: repeat PROGRAM_READY until PHYSICAL_UNITS_READY.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !programReady && levelTooHigh
                    && !valveIsOpen) {
                // SB-Beh4: drain the excess through the evacuation valve (kept open while the level is excessive).
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Valve());
                valveOpen = true;
                pumpsOpen = false;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !programReady && !levelTooHigh
                    && valveIsOpen) {
                // SB-Beh4: the level is no longer excessive; close the valve.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Valve());
                valveOpen = false;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !programReady && !levelTooHigh
                    && levelTooLow && !valveIsOpen) {
                // SB-Beh5: fill with the pumps.
                currentMode = OperatingMode.INITIALIZATION;
                pumpsOpen = true;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !programReady && !levelTooHigh
                    && !levelTooLow && !valveIsOpen) {
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
                    && levelTooLow) {
                // SB-Beh10: the level could fall below N1, switch the pumps on.
                currentMode = OperatingMode.NORMAL;
                pumpsOpen = true;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && !otherUnitFailed
                    && !levelTooLow && levelTooHigh) {
                // SB-Beh10: the level could rise above N2, switch the pumps off. Otherwise the pumps stay as they are.
                currentMode = OperatingMode.NORMAL;
                pumpsOpen = false;
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
                    && levelTooLow) {
                // SB-Beh15: same regulation as normal mode.
                currentMode = OperatingMode.DEGRADED;
                pumpsOpen = true;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && otherUnitFailed
                    && !levelTooLow && levelTooHigh) {
                currentMode = OperatingMode.DEGRADED;
                pumpsOpen = false;
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
            } else if (event instanceof InputEvent.EstimateUnreliable) {
                // SB-Beh21: steam device or pump controller failed.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && !otherUnitFailed) {
                // SB-Beh20: level device repaired, no other unit defective.
                currentMode = OperatingMode.NORMAL;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && otherUnitFailed) {
                // SB-Beh20: level device repaired, another unit still defective.
                currentMode = OperatingMode.DEGRADED;
            } else if (event instanceof InputEvent.Transmission && levelDeviceFailed && levelTooLow) {
                // SB-Beh19: regulate on the estimated level.
                currentMode = OperatingMode.RESCUE;
                pumpsOpen = true;
            } else if (event instanceof InputEvent.Transmission && levelDeviceFailed && !levelTooLow
                    && levelTooHigh) {
                currentMode = OperatingMode.RESCUE;
                pumpsOpen = false;
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            if (event instanceof InputEvent.Transmission) {
                // SB-Beh22: the control task has ended; the program only goes on announcing its mode (SB-Beh24).
                currentMode = OperatingMode.EMERGENCY_STOP;
            }
        }
    }
}
