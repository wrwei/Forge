package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.SteamBoilerActuator;
import steam_boiler.controller.OperatingMode;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program: one call per five-second cycle. A cycle receives the
 * incoming messages (all considered simultaneous), analyses them, and returns the outgoing
 * messages (all emitted together). Once in emergency stop the program has stopped and
 * answers nothing.
 */
public final class SteamBoilerProgram {

    private final BoilerSensor sensor = new BoilerSensor();
    private final SteamBoilerActuator actuator = new SteamBoilerActuator();
    private final SteamBoilerController controller = new SteamBoilerController(sensor, actuator);

    /** Runs one cycle: receive the transmission, analyse it, and return the outgoing one. */
    public List<OutputEvent> cycle(List<InputEvent> transmission) {
        if (controller.currentMode() == OperatingMode.EMERGENCY_STOP) {
            return new ArrayList<>();
        }
        sensor.startCycle(actuator.outgoing());
        actuator.startCycle();
        for (InputEvent message : transmission) {
            sensor.receive(message);
        }
        sensor.analyse();
        controller.step(new InputEvent.TransmissionReceived());
        if (controller.currentMode() == OperatingMode.EMERGENCY_STOP) {
            return actuator.outgoing();
        }
        for (OutputEvent message : sensor.protocolMessages()) {
            actuator.apply(message);
        }
        return actuator.outgoing();
    }

    /** The program's current operating mode. */
    public OperatingMode currentMode() {
        return controller.currentMode();
    }
}
