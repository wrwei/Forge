package steam_boiler;

import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.OperatingMode;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.event.IncomingMessage;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.PlantSensor;
import steam_boiler.sensor.TransmissionSensor;

/**
 * The steam-boiler control program: wires the units together and runs the
 * five-second cycle (receive, analyse, transmit).
 */
public final class SteamBoilerProgram {
    private final TransmissionSensor receiver = new TransmissionSensor();
    private final PlantSensor plant = new PlantSensor();
    private final BoilerActuator actuator = new BoilerActuator();
    private final SteamBoilerController controller = new SteamBoilerController(receiver, plant, actuator);

    /** Runs one cycle on an incoming transmission and returns the outgoing one. */
    public List<OutputEvent> cycle(List<IncomingMessage> messages) {
        // Receive: all messages of the transmission are taken in together.
        receiver.receive(messages);
        // Analyse: the plant is judged only once it is waiting, and no longer after emergency stop.
        boolean stoppedBefore = controller.currentMode() == OperatingMode.EMERGENCY_STOP;
        if (!stoppedBefore && receiver.boilerWaitingSeen()) {
            plant.analyse(receiver, actuator);
        }
        // Transmit: the controller's messages, then the failure and repair protocol.
        actuator.beginCycle();
        controller.step(new InputEvent.Transmission());
        boolean stoppedNow = controller.currentMode() == OperatingMode.EMERGENCY_STOP;
        if (!stoppedNow && receiver.boilerWaitingSeen()) {
            plant.announce(actuator);
        }
        return actuator.outgoing();
    }

    /** The current operating mode. */
    public OperatingMode currentMode() {
        return controller.currentMode();
    }
}
