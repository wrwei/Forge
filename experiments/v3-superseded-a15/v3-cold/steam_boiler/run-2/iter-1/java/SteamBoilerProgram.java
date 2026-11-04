package steam_boiler;

import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.domain.OperatingMode;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program: the sensor, the mode controller and the actuator, driven once
 * per five-second cycle (SB-V1, SB-V3).
 */
public final class SteamBoilerProgram {

    private final BoilerSensor sensor = new BoilerSensor();
    private final BoilerActuator actuator = new BoilerActuator();
    private final SteamBoilerController controller = new SteamBoilerController(sensor, actuator);

    /**
     * Runs one cycle: receives the incoming messages, analyses them, and returns the outgoing
     * messages, all emitted together. Once emergency stop has been entered the program has stopped
     * and a cycle does nothing (SB-Beh22).
     */
    public List<OutputEvent> runCycle(List<InputEvent> transmission) {
        if (controller.currentMode() == OperatingMode.EMERGENCY_STOP) {
            return List.of();
        }
        List<OutputEvent> previousOrders = actuator.takePending();
        sensor.receive(transmission, previousOrders);
        for (OutputEvent message : sensor.unitMessages()) {
            actuator.apply(message);
        }
        controller.step(new InputEvent.TransmissionReceived());
        return actuator.pendingMessages();
    }

    /** The program's current operating mode (SB-V2). */
    public OperatingMode operatingMode() {
        return controller.currentMode();
    }
}
