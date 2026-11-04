package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.BoilerMode;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program: wires the sensor, the controller and the actuator, and
 * runs the three stages of each five-second cycle (receive, analyse, transmit). All incoming
 * messages of a cycle are taken as received together, and all outgoing messages are emitted
 * together. Once in emergency stop the program has stopped and transmits nothing more.
 */
public final class SteamBoilerProgram {

    private final BoilerSensor sensor = new BoilerSensor();
    private final BoilerActuator actuator = new BoilerActuator();
    private final SteamBoilerController controller = new SteamBoilerController(sensor, actuator);

    /** Runs one five-second cycle on the incoming transmission and returns the outgoing one. */
    public List<OutputEvent> cycle(List<InputEvent> incoming) {
        List<OutputEvent> outgoing = new ArrayList<>();
        if (controller.currentMode() == BoilerMode.EMERGENCY_STOP) {
            return outgoing;
        }
        // Stage 1: receive the incoming messages.
        sensor.beginCycle();
        for (InputEvent message : incoming) {
            sensor.receive(message);
        }
        // Stage 2: analyse the information.
        sensor.analyse();
        actuator.clear();
        controller.step(new InputEvent.TransmissionReceived());
        // Stage 3: transmit the outgoing messages.
        outgoing.addAll(actuator.issued());
        outgoing.addAll(sensor.unitProtocolMessages());
        for (OutputEvent message : outgoing) {
            sensor.recordSent(message);
        }
        return outgoing;
    }

    /** The program's current operating mode. */
    public BoilerMode currentMode() {
        return controller.currentMode();
    }
}
