package steam_boiler;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.BoilerMode;
import steam_boiler.sensor.BoilerSensor;
import steam_boiler.sensor.CycleAnalyser;

/**
 * The steam-boiler control program: runs one five-second cycle at a time,
 * receiving the incoming transmission, analysing it, letting the controller
 * decide, and returning the outgoing transmission. Once in emergency stop the
 * program has stopped and emits nothing further.
 */
public final class SteamBoilerProgram {

    private final BoilerSensor sensor = new BoilerSensor();
    private final CycleAnalyser analyser = new CycleAnalyser(sensor);
    private final BoilerActuator actuator = new BoilerActuator();
    private final SteamBoilerController controller = new SteamBoilerController(sensor, actuator);

    /** Runs one cycle on the messages received in it and returns the messages to send. */
    public List<OutputEvent> runCycle(List<InputEvent> incoming) {
        List<OutputEvent> outgoing = new ArrayList<>();
        if (controller.currentMode() == BoilerMode.EMERGENCY_STOP) {
            return outgoing;
        }
        analyser.beginCycle();
        for (InputEvent message : incoming) {
            analyser.receive(message);
        }
        analyser.analyse();
        controller.step(new InputEvent.TransmissionReceived());
        List<OutputEvent> issued = actuator.takeIssued();
        analyser.recordOrders(issued);
        outgoing.addAll(issued);
        if (controller.currentMode() != BoilerMode.EMERGENCY_STOP) {
            outgoing.addAll(analyser.protocolMessages());
        }
        return outgoing;
    }

    /** The current operating mode. */
    public BoilerMode currentMode() {
        return controller.currentMode();
    }
}
