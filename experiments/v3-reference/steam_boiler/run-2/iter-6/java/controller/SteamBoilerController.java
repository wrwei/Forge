package steam_boiler.controller;

import steam_boiler.event.InputEvent;
import steam_boiler.mode.OperatingMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program's mode logic (SB-V1, SB-V2). Stepped once
 * per five-second cycle with the event the unit monitor derives from the
 * cycle's transmission (SB-V3). The controller decides the operating mode and,
 * in initialisation, the evacuation valve, the pumps and the PROGRAM_READY
 * handshake; the program's transmission stage turns these decisions into the
 * outgoing messages and applies the regulation of the operating modes.
 *
 * <p>Within a mode the branches are listed by precedence (SB-FR8): a
 * condition demanding emergency stop arrives as its own event and comes
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
    /** Initialisation: the evacuation valve is to be open (SB-DM7, SB-Beh4). */
    private boolean valveOpen = false;
    /** Initialisation: every pump is to be open (true) or closed (false) (SB-DM3, SB-Beh5). */
    private boolean pumpsOpen = false;

    private final BoilerSensor sensor;

    /** Creates the controller in initialization mode (SB-Beh1). */
    public SteamBoilerController(BoilerSensor sensor) {
        this.sensor = sensor;
    }

    /** The current operating mode. */
    public OperatingMode currentMode() {
        return currentMode;
    }

    /** Whether PROGRAM_READY has been announced, to be repeated until PHYSICAL_UNITS_READY. */
    public boolean readyAnnounced() {
        return readyAnnounced;
    }

    /** Whether the evacuation valve is to be open. */
    public boolean valveOpen() {
        return valveOpen;
    }

    /** Whether every pump is to be open during initialisation. */
    public boolean pumpsOpen() {
        return pumpsOpen;
    }

    /** One control cycle: react to the analysed transmission. */
    public void step(InputEvent event) {
        // --- Named predicates ---
        boolean levelDeviceFailed = sensor.levelDeviceOutOfOrder();
        boolean otherUnitFailed = sensor.otherUnitOutOfOrder();
        boolean levelBelowN1 = sensor.levelBelowNormal();
        boolean levelAboveN2 = sensor.levelAboveNormal();
        boolean plantWaiting = steamBoilerWaitingSeen;
        boolean programReady = readyAnnounced;

        // --- Mode-nested transitions ---
        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.EmergencyStopDemanded) {
                // SB-Beh3, SB-Beh6, SB-Beh9, SB-Beh23.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.SteamBoilerWaiting) {
                // SB-Beh2: STEAM_BOILER_WAITING has arrived; before it, nothing happens.
                currentMode = OperatingMode.INITIALIZATION;
                steamBoilerWaitingSeen = true;
            } else if (event instanceof InputEvent.PhysicalUnitsReady && programReady && !otherUnitFailed) {
                // SB-Beh8: every unit sound.
                currentMode = OperatingMode.NORMAL;
            } else if (event instanceof InputEvent.PhysicalUnitsReady && programReady && otherUnitFailed) {
                // SB-Beh8: some unit defective.
                currentMode = OperatingMode.DEGRADED;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !programReady && levelAboveN2) {
                // SB-Beh4: drain the excess through the evacuation valve.
                currentMode = OperatingMode.INITIALIZATION;
                valveOpen = true;
                pumpsOpen = false;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !programReady && !levelAboveN2 && levelBelowN1) {
                // SB-Beh5: fill with the pumps.
                currentMode = OperatingMode.INITIALIZATION;
                valveOpen = false;
                pumpsOpen = true;
            } else if (event instanceof InputEvent.Transmission && plantWaiting && !programReady && !levelAboveN2 && !levelBelowN1) {
                // SB-Beh7: level between N1 and N2; stop adjusting and announce PROGRAM_READY.
                currentMode = OperatingMode.INITIALIZATION;
                valveOpen = false;
                pumpsOpen = false;
                readyAnnounced = true;
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.EmergencyStopDemanded) {
                // SB-Beh13, SB-Beh14, SB-Beh23.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.Transmission && levelDeviceFailed) {
                // SB-Beh11 (SB-FR8: rescue dominates degraded).
                currentMode = OperatingMode.RESCUE;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && otherUnitFailed) {
                // SB-Beh12.
                currentMode = OperatingMode.DEGRADED;
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.EmergencyStopDemanded) {
                // SB-Beh18, SB-Beh23.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.Transmission && levelDeviceFailed) {
                // SB-Beh17.
                currentMode = OperatingMode.RESCUE;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && !otherUnitFailed) {
                // SB-Beh16: every failed unit repaired.
                currentMode = OperatingMode.NORMAL;
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.EmergencyStopDemanded) {
                // SB-Beh21, SB-Beh23.
                currentMode = OperatingMode.EMERGENCY_STOP;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && !otherUnitFailed) {
                // SB-Beh20: level device repaired, no other unit defective.
                currentMode = OperatingMode.NORMAL;
            } else if (event instanceof InputEvent.Transmission && !levelDeviceFailed && otherUnitFailed) {
                // SB-Beh20: level device repaired, another unit still defective.
                currentMode = OperatingMode.DEGRADED;
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            if (event instanceof InputEvent.Transmission) {
                // SB-Beh22: the control task has ended; the program only goes on announcing its mode (SB-Beh24).
                currentMode = OperatingMode.EMERGENCY_STOP;
            }
        }
    }
}
