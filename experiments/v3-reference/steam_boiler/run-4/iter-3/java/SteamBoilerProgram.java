package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.domain.BoilerMode;
import steam_boiler.event.InputEvent;
import steam_boiler.event.Message;
import steam_boiler.event.PlantMessage;
import steam_boiler.event.ProtocolMessage;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program (SB-V1): wires the sensor, the controller
 * and the actuator, and runs one cycle per incoming transmission in the three
 * stages of SB-V3 (receive, analyse, transmit).
 */
public final class SteamBoilerProgram {

    private final BoilerSensor sensor = new BoilerSensor();
    private final BoilerActuator actuator = new BoilerActuator();
    private final SteamBoilerController controller = new SteamBoilerController(sensor, actuator);

    /**
     * Runs one cycle on the messages of one transmission and returns the
     * messages to transmit, all emitted together. Once the program is in
     * emergency stop its control task has ended (SB-Beh22) and it transmits
     * nothing more.
     */
    public List<Message> cycle(List<PlantMessage> transmission) {
        if (controller.currentMode() == BoilerMode.EMERGENCY_STOP) {
            return new ArrayList<>();
        }
        // Receive stage: every message of the transmission counts as simultaneous.
        sensor.beginCycle();
        actuator.beginCycle();
        for (PlantMessage message : transmission) {
            sensor.receive(message);
        }
        // Analysis stage: failure detection, then the mode decision.
        sensor.analyse();
        controller.step(new InputEvent.TransmissionReceived());
        for (ProtocolMessage message : sensor.protocolMessages()) {
            actuator.announce(message);
        }
        // Transmit stage.
        List<Message> transmitted = actuator.outgoing();
        sensor.recordTransmitted(transmitted);
        return transmitted;
    }

    /** The program's current operating mode. */
    public BoilerMode currentMode() {
        return controller.currentMode();
    }
}
