package sranger;

import sranger.actuator.Actuator;
import sranger.clock.Clock;
import sranger.controller.SRangerController;
import sranger.event.InputEvent;
import sranger.sensor.Sensor;

/**
 * Demo entry point — wires the controller together and drives it through
 * a short simulated sequence. Not part of the formal model.
 */
public final class Main {

    public static void main(String[] args) {
        Sensor sensor = new Sensor();
        Actuator actuator = new Actuator();
        Clock clock = new Clock();
        SRangerController controller = new SRangerController(sensor, actuator, clock);

        // Tick in Moving with clear path.
        sensor.update(2.0);
        controller.step(new InputEvent.Tick());

        // Obstacle detected — Moving -> Turning.
        sensor.update(0.3);
        controller.step(new InputEvent.Obstacle());

        // Tick during turn (time not yet elapsed).
        clock.advance(0.5);
        controller.step(new InputEvent.Tick());

        // Turn duration elapses — autonomous Turning -> Moving.
        clock.advance(2.0);
        controller.step(new InputEvent.Tick());

        // Operator endTask — Moving -> Final.
        controller.step(new InputEvent.EndTask());

        System.out.println("Final mode: " + controller.currentMode());
        System.out.println("Last command: lv=" + actuator.lastLv() + " av=" + actuator.lastAv());
    }

    private Main() {}
}
