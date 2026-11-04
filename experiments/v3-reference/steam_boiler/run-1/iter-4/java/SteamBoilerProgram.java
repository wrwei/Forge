package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.actuator.PumpRegulator;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.BoilerMode;
import steam_boiler.sensor.BoilerSensor;
import steam_boiler.sensor.PlantMonitor;

/**
 * The steam-boiler control program as seen by the message transmission
 * system: one call of {@link #cycle} per five-second cycle.
 */
public final class SteamBoilerProgram {

    private final BoilerActuator actuator = new BoilerActuator();
    private final BoilerSensor sensor = new BoilerSensor();
    private final PlantMonitor monitor = new PlantMonitor(sensor, actuator);
    private final SteamBoilerController controller = new SteamBoilerController(sensor, actuator);
    private final PumpRegulator regulator = new PumpRegulator(sensor, actuator);

    /**
     * Runs one cycle: receives the incoming messages, analyses them, chooses
     * the mode, regulates the valve and pumps, and returns the outgoing
     * messages to be emitted together. Once the program is in emergency stop
     * it has stopped and sends nothing more.
     */
    public List<OutputEvent> cycle(List<InputEvent> messages) {
        if (controller.currentMode() == BoilerMode.EMERGENCY_STOP) {
            return new ArrayList<>();
        }
        for (InputEvent message : messages) {
            monitor.receive(message);
        }
        monitor.analyse();
        controller.step(new InputEvent.Transmission());
        regulator.regulate();
        return actuator.transmit();
    }

    /** The program's current operating mode. */
    public BoilerMode mode() {
        return controller.currentMode();
    }
}
