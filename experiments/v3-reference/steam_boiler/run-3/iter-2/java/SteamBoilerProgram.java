package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.domain.OperatingMode;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.message.UnitMessage;
import steam_boiler.monitor.PlantMonitor;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program (SB-V1): runs the five-second cycle of reception,
 * analysis and transmission (SB-V3) over the plant monitor, the mode controller and
 * the actuator. Once emergency stop is entered the program has stopped (SB-Beh22).
 */
public final class SteamBoilerProgram {

    private final PlantMonitor monitor = new PlantMonitor();
    private final BoilerActuator actuator = new BoilerActuator();
    private final SteamBoilerController controller =
            new SteamBoilerController(new BoilerSensor(monitor), actuator);

    /**
     * Runs one cycle: receives the incoming messages (all considered simultaneous),
     * analyses them, and returns the outgoing messages (all emitted simultaneously).
     * Returns no message once the program has stopped.
     */
    public List<OutputEvent> runCycle(List<UnitMessage> incoming) {
        if (controller.currentMode() == OperatingMode.EMERGENCY_STOP) {
            return new ArrayList<>();
        }
        monitor.receive(incoming);
        controller.step(new InputEvent.TransmissionReceived());
        for (OutputEvent message : monitor.protocolMessages()) {
            actuator.apply(message);
        }
        List<OutputEvent> outgoing = actuator.transmit();
        monitor.recordTransmitted(outgoing);
        return outgoing;
    }

    /** The program's current operating mode (SB-V2). */
    public OperatingMode currentMode() {
        return controller.currentMode();
    }
}
