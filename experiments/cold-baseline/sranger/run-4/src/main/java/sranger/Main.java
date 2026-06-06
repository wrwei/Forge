package sranger;

import sranger.actuator.Actuator;
import sranger.clock.Clock;
import sranger.controller.SRangerController;
import sranger.event.InputEvent;
import sranger.sensor.Sensor;

/**
 * Minimal demonstration entry point for the SRanger controller.
 */
public final class Main {

    public static void main(String[] args) {
        var sensor = new Sensor();
        var actuator = new Actuator();
        var clock = new Clock();
        var controller = new SRangerController(sensor, actuator, clock);

        sensor.update(2.0);
        controller.step(new InputEvent.Tick());
        controller.step(new InputEvent.Obstacle());
        controller.step(new InputEvent.Tick());
        controller.step(new InputEvent.EndTask());

        System.out.println("Mode = " + controller.currentMode()
                + " lv=" + actuator.lastLv()
                + " av=" + actuator.lastAv());
    }
}
