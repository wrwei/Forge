package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.domain.OperatingMode;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.monitor.PlantMonitor;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program (SB-V1). Each call of {@link #runCycle} is one five-second
 * cycle in three stages (SB-V3): receive the incoming messages, analyse them (the plant monitor,
 * then the controller), and transmit the outgoing messages, all at once. Once the program has
 * entered emergency stop it has stopped and transmits nothing more (SB-Beh22).
 */
public final class SteamBoilerProgram {

    private final PlantMonitor monitor;
    private final BoilerActuator actuator;
    private final SteamBoilerController controller;

    /** Creates the program in initialization mode (SB-Beh1). */
    public SteamBoilerProgram() {
        BoilerSensor sensor = new BoilerSensor();
        this.monitor = new PlantMonitor(sensor);
        this.actuator = new BoilerActuator();
        this.controller = new SteamBoilerController(sensor, actuator);
    }

    /**
     * Runs one cycle.
     *
     * @param incoming the messages received in this cycle
     * @return the messages transmitted in this cycle; none once the program has stopped
     */
    public List<OutputEvent> runCycle(List<InputEvent> incoming) {
        if (hasStopped()) {
            return new ArrayList<>();
        }
        monitor.beginCycle();
        for (InputEvent message : incoming) {
            monitor.receive(message);
        }
        monitor.analyse();
        actuator.clear();
        controller.step(new InputEvent.CycleStart());
        List<OutputEvent> unitMessages = monitor.unitStatusMessages();
        for (OutputEvent message : unitMessages) {
            actuator.apply(message);
        }
        List<OutputEvent> outgoing = actuator.outgoing();
        monitor.recordTransmission(outgoing);
        return outgoing;
    }

    /** @return the program's current operating mode. */
    public OperatingMode currentMode() {
        return controller.currentMode();
    }

    /** @return whether the program has stopped in emergency stop mode (SB-Beh22). */
    public boolean hasStopped() {
        return controller.currentMode() == OperatingMode.EMERGENCY_STOP;
    }
}
