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
 * The steam-boiler control program (SB-V1). Each call to
 * {@link #runCycle(List)} is one five-second cycle (SB-V3): the program
 * receives every message of the incoming transmission, analyses it, and
 * transmits all its outgoing messages at once. Once emergency stop has been
 * entered the program has stopped and sends nothing more (SB-Beh22).
 */
public final class SteamBoilerProgram {

    private final BoilerSensor sensor;
    private final BoilerActuator actuator;
    private final SteamBoilerController controller;

    /** Creates the program in initialization mode. */
    public SteamBoilerProgram() {
        this.sensor = new BoilerSensor();
        this.actuator = new BoilerActuator(sensor);
        this.controller = new SteamBoilerController(sensor, actuator);
    }

    /** Runs one cycle on the incoming transmission and returns the outgoing one. */
    public List<OutputEvent> runCycle(List<InputEvent> transmission) {
        if (controller.currentMode() == OperatingMode.EMERGENCY_STOP) {
            return new ArrayList<>();
        }
        sensor.beginTransmission();
        for (InputEvent message : transmission) {
            sensor.receive(message);
        }
        sensor.analyse();
        controller.step(new InputEvent.TransmissionReceived());
        boolean stopped = controller.currentMode() == OperatingMode.EMERGENCY_STOP;
        if (!stopped && sensor.plantWaitingSignalled()) {
            actuator.announceUnitStatus();
        }
        List<OutputEvent> sent = actuator.transmit();
        sensor.recordTransmission(sent);
        return sent;
    }

    /** The program's current operating mode. */
    public OperatingMode currentMode() {
        return controller.currentMode();
    }
}
