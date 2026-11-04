package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.OperatingMode;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program and its physical-unit interfaces (SB-V1), run one
 * cycle at a time (SB-V3): it receives all messages of a transmission, analyses them,
 * then transmits all outgoing messages at once. Once in emergency stop the program
 * has stopped and transmits nothing more (SB-Beh22).
 */
public final class SteamBoilerProgram {

    private final BoilerSensor sensor = new BoilerSensor();
    private final BoilerActuator actuator = new BoilerActuator();
    private final SteamBoilerController controller = new SteamBoilerController(sensor, actuator);

    /** Runs one cycle on the messages of one incoming transmission and returns the outgoing one. */
    public List<OutputEvent> cycle(List<InputEvent> messages) {
        if (controller.currentMode() == OperatingMode.EMERGENCY_STOP) {
            return new ArrayList<>();
        }
        sensor.beginTransmission();
        for (InputEvent message : messages) {
            sensor.receive(message);
        }
        sensor.analyse(controller.lowerLevelBound(), controller.upperLevelBound(), controller.lastSteamRate());
        actuator.beginTransmission();
        controller.step(new InputEvent.Transmission());
        if (controller.currentMode() != OperatingMode.EMERGENCY_STOP) {
            sensor.recordOrders(actuator.outgoing());
            for (OutputEvent message : sensor.unitMessages()) {
                actuator.apply(message);
            }
        }
        return actuator.outgoing();
    }

    /** The program's current operating mode. */
    public OperatingMode currentMode() {
        return controller.currentMode();
    }
}
