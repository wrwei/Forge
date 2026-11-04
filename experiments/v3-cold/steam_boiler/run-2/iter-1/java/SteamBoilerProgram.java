package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.BoilerMode;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program: one call of {@link #cycle} per five-second cycle.
 * A cycle receives every incoming message (taken as simultaneous), analyses them,
 * steps the controller once and returns every outgoing message (emitted together).
 */
public final class SteamBoilerProgram {
    private final BoilerSensor sensor = new BoilerSensor();
    private final BoilerActuator actuator = new BoilerActuator();
    private final SteamBoilerController controller = new SteamBoilerController(sensor, actuator);

    /**
     * Runs one cycle. Once emergency stop has been entered the program has stopped and
     * sends nothing further.
     *
     * @param received the messages of this cycle's incoming transmission
     * @return the messages of this cycle's outgoing transmission
     */
    public List<OutputEvent> cycle(List<InputEvent> received) {
        if (controller.currentMode() == BoilerMode.EMERGENCY_STOP) {
            return new ArrayList<>();
        }
        sensor.beginCycle();
        for (InputEvent message : received) {
            sensor.receive(message);
        }
        sensor.analyse();
        controller.step(new InputEvent.Transmission());
        List<OutputEvent> transmitted = actuator.drain();
        transmitted.addAll(sensor.unitMessages());
        sensor.completeCycle(transmitted);
        return transmitted;
    }

    /** The program's current operating mode. */
    public BoilerMode currentMode() {
        return controller.currentMode();
    }
}
