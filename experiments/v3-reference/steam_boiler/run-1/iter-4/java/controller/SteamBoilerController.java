package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.BoilerMode;
import steam_boiler.mode.OperatingMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program's mode logic. {@link #step} is called once
 * per cycle with the cycle's {@link InputEvent.Transmission}, after the
 * transmission's messages have been analysed into the sensor; it chooses the
 * mode for the cycle, announces it with MODE, and conducts the
 * PROGRAM_READY / PHYSICAL_UNITS_READY handshake. The valve and pump
 * commands of each cycle are issued afterwards by the pump regulator,
 * according to the mode announced here.
 */
public final class SteamBoilerController {

    private BoilerMode currentMode = BoilerMode.INITIALISATION;
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;
    private boolean programReadySent = false;

    /** Creates the controller in initialisation mode. */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode. */
    public BoilerMode currentMode() {
        return currentMode;
    }

    /**
     * Performs the analysis stage of one cycle. Within each mode the branches
     * are in priority order and cover every case, so the last branch, which
     * keeps the mode, needs no condition beyond the transmission itself.
     */
    public void step(InputEvent event) {
        boolean startUpAbort = sensor.startUpAbort();
        boolean startUpLevelReached = sensor.startUpLevelReached();
        boolean unitsReady = sensor.unitsReady();
        boolean emergencyCause = sensor.emergencyCause();
        boolean levelFailure = sensor.levelFailure();
        boolean unitFailure = sensor.unitFailure();
        boolean estimateUnreliable = sensor.estimateUnreliable();

        // Initialisation.
        boolean announceReady = !startUpAbort && startUpLevelReached && !programReadySent;
        boolean awaitingUnits = !startUpAbort && programReadySent;

        // Normal and degraded modes.
        boolean operatingEmergency = emergencyCause || (levelFailure && estimateUnreliable);
        boolean enterRescue = !emergencyCause && levelFailure && !estimateUnreliable;
        boolean someUnitDefective = !emergencyCause && !levelFailure && unitFailure;
        boolean allUnitsSound = !emergencyCause && !levelFailure && !unitFailure;

        // Rescue mode.
        boolean rescueEmergency = emergencyCause || estimateUnreliable;
        boolean levelDeviceRepaired = !emergencyCause && !estimateUnreliable && !levelFailure;

        if (currentMode == BoilerMode.INITIALISATION) {
            if (event instanceof InputEvent.Transmission && startUpAbort) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && announceReady) {
                currentMode = BoilerMode.INITIALISATION;
                programReadySent = true;
                actuator.apply(new OutputEvent.ProgramReady());
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && awaitingUnits && !unitsReady) {
                currentMode = BoilerMode.INITIALISATION;
                actuator.apply(new OutputEvent.ProgramReady());
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && awaitingUnits && unitsReady && !unitFailure) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && awaitingUnits && unitsReady && unitFailure) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission) {
                currentMode = BoilerMode.INITIALISATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            }

        } else if (currentMode == BoilerMode.NORMAL) {
            if (event instanceof InputEvent.Transmission && operatingEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && enterRescue) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && someUnitDefective) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            }

        } else if (currentMode == BoilerMode.DEGRADED) {
            if (event instanceof InputEvent.Transmission && operatingEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && enterRescue) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && allUnitsSound) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            }

        } else if (currentMode == BoilerMode.RESCUE) {
            if (event instanceof InputEvent.Transmission && rescueEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && levelDeviceRepaired && !unitFailure) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && levelDeviceRepaired && unitFailure) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            }

        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
            // Terminal: the program has stopped and the physical environment is
            // responsible for the plant; a transmission changes nothing.
            if (event instanceof InputEvent.Transmission) {
                currentMode = BoilerMode.EMERGENCY_STOP;
            }
        }
    }
}
