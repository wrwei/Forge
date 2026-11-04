package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.actuator.Transmitter;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.OperatingMode;
import steam_boiler.sensor.MessageReceiver;
import steam_boiler.sensor.PlantSensor;
import steam_boiler.sensor.UnitStatusTable;

/**
 * The steam-boiler control program, wired to the message transmission system. Each call of
 * {@link #runCycle} is one five-second cycle: receive every incoming message, analyse, then
 * transmit every outgoing message together.
 */
public final class SteamBoilerProgram {

    private final PlantSensor sensor = new PlantSensor();
    private final UnitStatusTable units = new UnitStatusTable();
    private final BoilerActuator actuator = new BoilerActuator();
    private final Transmitter transmitter = new Transmitter(actuator, units);
    private final MessageReceiver receiver = new MessageReceiver(sensor, units, transmitter);
    private final SteamBoilerController controller = new SteamBoilerController(sensor, actuator);

    /**
     * Runs one cycle on the messages of the cycle's incoming transmission and returns the
     * cycle's outgoing transmission. Once in emergency stop the program has stopped and sends
     * nothing.
     */
    public List<OutputEvent> runCycle(List<InputEvent> incoming) {
        if (controller.currentMode() == OperatingMode.EMERGENCY_STOP) {
            return new ArrayList<>();
        }
        receiver.receive(incoming);
        controller.step(new InputEvent.TransmissionReceived());
        return transmitter.transmit();
    }

    /** The program's current operating mode. */
    public OperatingMode currentMode() {
        return controller.currentMode();
    }
}
