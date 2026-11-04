package steam_boiler;

import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.BoilerMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program (SB-V1): one sensor (receiving side), one controller (mode
 * logic) and one actuator (sending side), driven in the five-second cycle of SB-V3 — receive
 * the incoming messages, analyse them, transmit the outgoing messages.
 */
public final class SteamBoilerProgram {

    private final BoilerSensor sensor;
    private final BoilerActuator actuator;
    private final SteamBoilerController controller;

    /** Creates the program in initialization mode with every unit believed sound. */
    public SteamBoilerProgram() {
        this.sensor = new BoilerSensor();
        this.actuator = new BoilerActuator();
        this.controller = new SteamBoilerController(sensor, actuator);
    }

    /**
     * Runs one cycle on the transmission received from the physical units and returns the
     * transmission to send back. Once emergency stop has been entered the control task has
     * ended (SB-Beh22): the transmission carries only MODE(EMERGENCY_STOP) (SB-Beh24).
     */
    public List<OutputEvent> runCycle(List<InputEvent> messages) {
        actuator.beginTransmission();
        sensor.receive(messages);
        controller.step(new InputEvent.TransmissionReceived());
        if (controller.currentMode() != BoilerMode.EMERGENCY_STOP) {
            List<OutputEvent> unitMessages = sensor.unitProtocolMessages();
            for (OutputEvent message : unitMessages) {
                actuator.apply(message);
            }
        }
        List<OutputEvent> outgoing = actuator.transmission();
        sensor.recordOrders(outgoing);
        return outgoing;
    }

    /** The program's current operating mode. */
    public BoilerMode currentMode() {
        return controller.currentMode();
    }
}
