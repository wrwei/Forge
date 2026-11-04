package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.domain.OperatingMode;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;
import steam_boiler.sensor.IncomingTransmission;

/**
 * The steam-boiler control program, linked to the physical units by the message
 * transmission system. Each five-second cycle has three stages: receive the
 * incoming messages, analyse them, transmit the outgoing messages. Once the
 * program has entered emergency stop it no longer takes part in cycles.
 */
public final class SteamBoilerProgram {

    private final IncomingTransmission transmission = new IncomingTransmission();
    private final BoilerSensor sensor = new BoilerSensor(transmission);
    private final BoilerActuator actuator = new BoilerActuator();
    private final SteamBoilerController controller = new SteamBoilerController(sensor, actuator);

    /**
     * Runs one cycle.
     *
     * @param incoming every message of the cycle's incoming transmission
     * @return every message of the cycle's outgoing transmission; empty once stopped
     */
    public List<OutputEvent> runCycle(List<InputEvent> incoming) {
        boolean stoppedBefore = controller.currentMode() == OperatingMode.EMERGENCY_STOP;
        if (stoppedBefore) {
            return new ArrayList<>();
        }
        transmission.clear();
        actuator.clear();
        for (InputEvent message : incoming) {
            transmission.receive(message);
        }
        sensor.analyse();
        controller.step(new InputEvent.TransmissionReceived());
        boolean stoppedNow = controller.currentMode() == OperatingMode.EMERGENCY_STOP;
        if (!stoppedNow) {
            for (OutputEvent message : sensor.protocolMessages()) {
                actuator.apply(message);
            }
        }
        sensor.endCycle(actuator.outgoingMessages());
        return actuator.outgoingMessages();
    }

    /** The program's current operating mode. */
    public OperatingMode currentMode() {
        return controller.currentMode();
    }
}
