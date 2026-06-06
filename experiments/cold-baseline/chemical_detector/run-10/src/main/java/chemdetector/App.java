package chemdetector;

import chemdetector.actuator.Actuator;
import chemdetector.clock.Clock;
import chemdetector.controller.EventBus;
import chemdetector.controller.GasAnalysisController;
import chemdetector.controller.MovementController;
import chemdetector.event.MovementInputEvent;
import chemdetector.operation.ChangeDirection;
import chemdetector.sensor.Sensor;
import chemdetector.vehicle.Vehicle;

/**
 * Minimal demo entry point — wires the two controllers together and
 * runs a single empty step. Not part of the formal model; exists only
 * to satisfy a compilable application boundary.
 */
public final class App {

    public static void main(String[] args) {
        Vehicle vehicle = new Vehicle();
        Sensor sensor = new Sensor();
        Actuator actuator = new Actuator(vehicle);
        ChangeDirection changeDirection = new ChangeDirection(vehicle);
        Clock T = new Clock();
        EventBus bus = new EventBus();

        GasAnalysisController ga = new GasAnalysisController(sensor, bus);
        MovementController mv = new MovementController(vehicle, actuator, changeDirection, T);

        // Process any pending shared events from the bus.
        MovementInputEvent next = bus.poll();
        while (next != null) {
            mv.step(next);
            next = bus.poll();
        }

        // Drive a no-op step so both controllers are exercised at least once.
        ga.step(null);
        mv.step(null);
    }

    private App() {
    }
}
