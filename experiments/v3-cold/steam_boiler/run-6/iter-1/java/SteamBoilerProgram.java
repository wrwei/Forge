package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.OperatingMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program: wires the sensor, the controller and the
 * actuator, and runs one five-second cycle at a time in its three stages:
 * receive the incoming messages, analyse them, transmit the outgoing messages.
 * Once emergency stop has been entered the program has stopped and takes part
 * in no further cycle.
 */
public final class SteamBoilerProgram {

    private final BoilerActuator actuator;
    private final BoilerSensor sensor;
    private final SteamBoilerController controller;

    /** Creates the program in initialization mode. */
    public SteamBoilerProgram() {
        this.actuator = new BoilerActuator();
        this.sensor = new BoilerSensor(actuator);
        this.controller = new SteamBoilerController(sensor, actuator);
    }

    /**
     * Runs one cycle.
     *
     * @param transmission the messages received in this cycle, all considered
     *                     received simultaneously
     * @return the messages emitted at the end of this cycle, all considered
     *         emitted simultaneously; empty once the program has stopped
     */
    public List<OutputEvent> runCycle(List<InputEvent> transmission) {
        if (controller.currentMode() == OperatingMode.EMERGENCY_STOP) {
            return new ArrayList<>();
        }
        sensor.beginCycle();
        for (InputEvent message : transmission) {
            sensor.receive(message);
        }
        sensor.analyse();
        controller.step(new InputEvent.Cycle());
        List<OutputEvent> unitMessages = sensor.collectUnitStatusMessages();
        for (OutputEvent message : unitMessages) {
            actuator.apply(message);
        }
        return actuator.transmit();
    }

    /** @return the program's current operating mode */
    public OperatingMode currentMode() {
        return controller.currentMode();
    }
}
