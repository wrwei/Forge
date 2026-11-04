package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.OperatingMode;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program (SB-V1). Each call of
 * {@link #runCycle(List)} is one five-second cycle (SB-V3): the program
 * receives all incoming messages, analyses them, and returns all outgoing
 * messages, emitted together.
 */
public final class SteamBoilerProgram {
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;
    private final SteamBoilerController controller;

    /** Creates the program in initialization mode. */
    public SteamBoilerProgram() {
        this.sensor = new BoilerSensor();
        this.actuator = new BoilerActuator();
        this.controller = new SteamBoilerController(sensor, actuator);
    }

    /**
     * Runs one cycle on the messages of one transmission and returns the
     * messages to transmit. Once in emergency stop the program has stopped
     * and returns no messages (SB-Beh22).
     */
    public List<OutputEvent> runCycle(List<InputEvent> incoming) {
        List<OutputEvent> outgoing = new ArrayList<>();
        if (controller.currentMode() == OperatingMode.EMERGENCY_STOP) {
            return outgoing;
        }
        sensor.beginCycle();
        for (InputEvent message : incoming) {
            sensor.receive(message);
        }
        sensor.analyse();
        controller.step(new InputEvent.TransmissionReceived());
        outgoing.addAll(actuator.takeOutgoing());
        sensor.appendProtocolMessages(outgoing);
        sensor.endCycle(outgoing);
        return outgoing;
    }

    /** The program's current operating mode. */
    public OperatingMode currentMode() {
        return controller.currentMode();
    }
}
