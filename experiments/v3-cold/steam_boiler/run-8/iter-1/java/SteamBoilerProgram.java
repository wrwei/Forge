package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.OperatingMode;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;
import steam_boiler.sensor.PlantMonitor;

/**
 * The steam-boiler control program (SB-V1): wires the receive and analysis
 * stage, the controller and the transmission stage, and runs one cycle every
 * five seconds in three stages (SB-V3): receive all incoming messages, analyse
 * them, transmit all outgoing messages at once.
 */
public final class SteamBoilerProgram {

    private final PlantMonitor monitor = new PlantMonitor();
    private final BoilerActuator actuator = new BoilerActuator();
    private final SteamBoilerController controller =
            new SteamBoilerController(new BoilerSensor(monitor), actuator);

    /**
     * Runs one cycle. Once emergency stop has been entered the program has
     * stopped and transmits nothing more (SB-Beh22).
     *
     * @param transmission every message received in this cycle
     * @return every message to emit at the end of this cycle
     */
    public List<OutputEvent> cycle(List<InputEvent> transmission) {
        List<OutputEvent> outgoing = new ArrayList<>();
        if (controller.currentMode() == OperatingMode.EMERGENCY_STOP) {
            return outgoing;
        }
        monitor.beginCycle();
        for (InputEvent message : transmission) {
            monitor.receive(message);
        }
        monitor.analyse();
        controller.step(new InputEvent.Transmission());
        outgoing.addAll(actuator.takeMessages());
        outgoing.addAll(monitor.protocolMessages());
        monitor.recordSent(outgoing);
        return outgoing;
    }

    /**
     * The operating mode the program is in (SB-V2).
     *
     * @return the current mode
     */
    public OperatingMode operatingMode() {
        return controller.currentMode();
    }
}
