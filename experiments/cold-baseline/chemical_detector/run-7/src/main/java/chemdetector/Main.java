package chemdetector;

import chemdetector.actuator.Actuator;
import chemdetector.clock.SystemClock;
import chemdetector.controller.GasAnalysisController;
import chemdetector.controller.MovementController;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.sensor.Sensor;

/**
 * Smoke-test driver. Wires sensor / actuator / clock and drives a
 * handful of canned events through the two controllers, forwarding
 * gas-analysis output events to the movement subsystem.
 */
public final class Main {

    public static void main(String[] args) {
        Sensor sensor = new Sensor();
        Actuator actuator = new Actuator();
        SystemClock clock = new SystemClock();
        GasAnalysisController ga = new GasAnalysisController(sensor, actuator);
        MovementController mv = new MovementController(sensor, actuator, clock);

        // One sense-analyse-act cycle (no gas yet, so GA emits resume).
        ga.step(new InputEvent.Gas(java.util.List.of()));
        ga.step(new InputEvent.Resume());

        OutputEvent last = actuator.lastEvent();
        if (last instanceof OutputEvent.Resume) {
            mv.step(new InputEvent.Resume());
        } else if (last instanceof OutputEvent.Turn) {
            OutputEvent.Turn t = (OutputEvent.Turn) last;
            mv.step(new InputEvent.Turn(t.a()));
        } else if (last instanceof OutputEvent.Stop) {
            mv.step(new InputEvent.Stop());
        }
    }

    private Main() {
    }
}
