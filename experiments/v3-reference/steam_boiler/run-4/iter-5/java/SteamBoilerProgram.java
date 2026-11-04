package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.actuator.DeviceCommands;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.domain.BoilerMode;
import steam_boiler.event.InputEvent;
import steam_boiler.event.Message;
import steam_boiler.event.PlantMessage;
import steam_boiler.event.ProtocolMessage;
import steam_boiler.monitor.PlantMonitor;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program (SB-V1): wires the plant monitor, the
 * sensor, the controller, the device commands and the actuator, and runs one
 * cycle per incoming transmission in the three stages of SB-V3 (receive,
 * analyse, transmit).
 */
public final class SteamBoilerProgram {

    private final PlantMonitor monitor = new PlantMonitor();
    private final BoilerSensor sensor = new BoilerSensor();
    private final OutgoingMessages transmission = new OutgoingMessages();
    private final BoilerActuator actuator = new BoilerActuator(transmission);
    private final SteamBoilerController controller = new SteamBoilerController(sensor, actuator);
    private final DeviceCommands commands = new DeviceCommands(sensor, actuator);

    /**
     * Runs one cycle on the messages of one transmission and returns the
     * messages to transmit, all emitted together. Once the program is in
     * emergency stop its control task has ended (SB-Beh22) and it transmits
     * nothing more.
     */
    public List<Message> cycle(List<PlantMessage> incoming) {
        if (controller.currentMode() == BoilerMode.EMERGENCY_STOP) {
            return new ArrayList<>();
        }
        // Receive stage: every message of the transmission counts as simultaneous.
        monitor.beginCycle();
        transmission.clear();
        for (PlantMessage message : incoming) {
            monitor.receive(message);
        }
        // Analysis stage: failure detection, the mode decision, then the device commands.
        monitor.analyse();
        monitor.publish(sensor);
        controller.step(new InputEvent.TransmissionReceived());
        commands.issue(controller.currentMode());
        for (ProtocolMessage message : monitor.protocolMessages()) {
            transmission.add(message);
        }
        // Transmit stage.
        List<Message> transmitted = transmission.messages();
        monitor.recordTransmitted(transmitted);
        return transmitted;
    }

    /** The program's current operating mode. */
    public BoilerMode currentMode() {
        return controller.currentMode();
    }

    /** The plant monitor: unit status, level estimate and the other program variables (SB-V5 to SB-V10). */
    public PlantMonitor monitor() {
        return monitor;
    }
}
