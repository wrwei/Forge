package steam_boiler.actuator;

import steam_boiler.event.OutputEvent;
import steam_boiler.mode.OperatingMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * Issues each cycle's valve and pump commands for the mode the controller
 * has just announced. In initialisation, once the plant is waiting and until
 * PROGRAM_READY has been transmitted, it drains an excessive level through
 * the evacuation valve and fills a low one with pump 1. In normal, degraded
 * and rescue mode it opens all four pumps when the best known level is below
 * N1 and closes them all when it is above N2.
 */
public final class PumpRegulator {

    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /** Creates a regulator reading the given sensor and commanding the given actuator. */
    public PumpRegulator(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** Issues this cycle's valve and pump commands; called after the controller has stepped. */
    public void regulate() {
        OperatingMode mode = actuator.lastMode();
        if (mode == OperatingMode.INITIALISATION_MODE) {
            adjustStartUpLevel();
        } else if (mode == OperatingMode.NORMAL_MODE || mode == OperatingMode.DEGRADED_MODE
                || mode == OperatingMode.RESCUE_MODE) {
            regulateLevel();
        }
    }

    private void adjustStartUpLevel() {
        if (!sensor.plantWaiting() || actuator.programReadyTransmitted()) {
            return;
        }
        if (sensor.levelAboveNormal()) {
            if (!actuator.valveOpened()) {
                actuator.apply(new OutputEvent.Valve());
            }
        } else {
            if (actuator.valveOpened()) {
                actuator.apply(new OutputEvent.Valve());
            }
            if (sensor.levelBelowNormal()) {
                actuator.apply(new OutputEvent.OpenPump(1));
            } else {
                actuator.apply(new OutputEvent.ClosePump(1));
            }
        }
    }

    private void regulateLevel() {
        if (sensor.levelBelowNormal()) {
            for (int pump = 1; pump <= 4; pump++) {
                actuator.apply(new OutputEvent.OpenPump(pump));
            }
        } else if (sensor.levelAboveNormal()) {
            for (int pump = 1; pump <= 4; pump++) {
                actuator.apply(new OutputEvent.ClosePump(pump));
            }
        }
    }
}
