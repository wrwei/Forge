package steam_boiler.controller;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * One five-second cycle of the program: the incoming messages, already handed to the sensor, are
 * analysed, the controller is stepped, and the outgoing messages are emitted together.
 */
public final class ControlCycle {
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;
    private final SteamBoilerController controller;

    /** Wires a cycle around the given units; all three must be shared with the environment. */
    public ControlCycle(BoilerSensor sensor, BoilerActuator actuator, SteamBoilerController controller) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.controller = controller;
    }

    /** Runs the analysis and transmission stages; returns this cycle's outgoing messages. */
    public List<OutputEvent> runCycle() {
        List<OutputEvent> transmission = new ArrayList<>();
        if (controller.currentMode() == BoilerMode.EMERGENCY_STOP) {
            // No control any more: the controller only reports MODE(emergency stop).
            controller.step(new InputEvent.MessagesReceived());
            transmission.addAll(actuator.takeIssued());
            return transmission;
        }
        sensor.analyse();
        controller.step(new InputEvent.MessagesReceived());
        List<OutputEvent> commands = actuator.takeIssued();
        sensor.recordCommands(commands);
        transmission.addAll(commands);
        transmission.addAll(sensor.protocolMessages());
        return transmission;
    }
}
