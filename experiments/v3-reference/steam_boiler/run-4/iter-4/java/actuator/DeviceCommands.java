package steam_boiler.actuator;

import steam_boiler.BoilerConstants;
import steam_boiler.domain.BoilerMode;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * The device commands of a cycle, issued after the controller has decided the
 * mode: the start-up sequence of initialization (SB-Beh4, SB-Beh5, SB-Beh7)
 * and the level regulation of normal, degraded and rescue mode (SB-Beh10,
 * SB-Beh15, SB-Beh19). In emergency stop nothing is commanded (SB-Beh22).
 */
public final class DeviceCommands {

    private final BoilerSensor sensor;
    private final BoilerActuator actuator;
    /** The evacuation valve has been ordered open (SB-DM7). */
    private boolean valveOpen = false;

    /** Creates the command stage over the program's sensor and actuator. */
    public DeviceCommands(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** Issues the cycle's device commands for the mode the controller has just decided. */
    public void issue(BoilerMode mode) {
        boolean below = sensor.levelUnderNormalBand();
        boolean above = sensor.levelOverNormalBand();
        if (mode == BoilerMode.INITIALIZATION) {
            // SB-Beh2: nothing happens before STEAM_BOILER_WAITING.
            if (!sensor.steamBoilerWaitingReceived()) {
                return;
            }
            if (above && !valveOpen) {
                // SB-Beh4: drain an excessive level through the evacuation valve.
                valveOpen = true;
                actuator.apply(new OutputEvent.Valve());
            } else if (!above && valveOpen) {
                // The level is no longer excessive: close the valve.
                valveOpen = false;
                actuator.apply(new OutputEvent.Valve());
            } else if (below) {
                // SB-Beh5: fill a low level with the pumps.
                orderAllPumps(true);
            } else if (!above) {
                // SB-Beh7: the level is within [N1, N2]; stop filling and announce readiness,
                // repeated on every cycle until PHYSICAL_UNITS_READY.
                orderAllPumps(false);
                actuator.apply(new OutputEvent.ProgramReady());
            }
            // Otherwise the valve is open and the level still above N2: keep draining.
        } else if (mode == BoilerMode.NORMAL || mode == BoilerMode.DEGRADED || mode == BoilerMode.RESCUE) {
            // SB-Beh10, SB-Beh15, SB-Beh19: switch the pumps on below N1 and off above N2
            // (in rescue mode the level is the estimate's middle).
            if (below) {
                orderAllPumps(true);
            } else if (above) {
                orderAllPumps(false);
            }
        }
    }

    /** Whether the evacuation valve has been ordered open. */
    public boolean valveOpen() {
        return valveOpen;
    }

    private void orderAllPumps(boolean open) {
        for (int pump = 1; pump <= BoilerConstants.PUMP_COUNT; pump++) {
            if (open) {
                actuator.apply(new OutputEvent.OpenPump(pump));
            } else {
                actuator.apply(new OutputEvent.ClosePump(pump));
            }
        }
    }
}
