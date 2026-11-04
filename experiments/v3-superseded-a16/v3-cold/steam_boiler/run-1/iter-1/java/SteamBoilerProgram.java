package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.BoilerMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program: one call of {@link #cycle} per five-second cycle.
 * A cycle receives the incoming messages, analyses them, and transmits the outgoing ones.
 */
public final class SteamBoilerProgram {
    private final BoilerSensor sensor = new BoilerSensor();
    private final BoilerActuator actuator = new BoilerActuator();
    private final SteamBoilerController controller = new SteamBoilerController(sensor, actuator);

    /**
     * Runs one cycle on the messages of one transmission and returns the messages to send.
     * Once in emergency stop the control task has ended: incoming messages are no longer
     * analysed and only the mode is reported.
     */
    public List<OutputEvent> cycle(List<InputEvent> incoming) {
        boolean stopped = controller.currentMode() == BoilerMode.EMERGENCY_STOP;
        boolean controlling = !stopped;
        if (controlling) {
            sensor.receive(incoming);
            sensor.analyse();
        }
        controller.step(new InputEvent.Transmission());
        if (controlling) {
            List<OutputEvent> status = sensor.statusMessages();
            for (OutputEvent message : status) {
                actuator.apply(message);
            }
        }
        List<OutputEvent> outgoing = actuator.transmit();
        if (controlling) {
            sensor.recordTransmission(outgoing);
        }
        return new ArrayList<>(outgoing);
    }

    /** The controller's current state. */
    public BoilerMode mode() {
        return controller.currentMode();
    }
}
