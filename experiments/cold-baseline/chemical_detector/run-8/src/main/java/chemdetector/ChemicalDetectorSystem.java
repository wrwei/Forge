package chemdetector;

import chemdetector.actuator.Actuator;
import chemdetector.controller.gas.GasAnalysisController;
import chemdetector.controller.movement.MovementController;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.operation.ChangeDirection;
import chemdetector.sensor.Sensor;
import chemdetector.sensor.SystemClock;
import chemdetector.vehicle.Vehicle;

/**
 * Top-level wiring of the Chemical Detector system. The two
 * controllers communicate via the shared Actuator queue: events
 * emitted by the gas-analysis subsystem are routed into the
 * movement subsystem on the next tick.
 */
public final class ChemicalDetectorSystem {

    private final Sensor sensor;
    private final Vehicle vehicle;
    private final Actuator actuator;
    private final ChangeDirection changeDir;
    private final SystemClock tick;
    private final GasAnalysisController gas;
    private final MovementController movement;

    public ChemicalDetectorSystem() {
        this.sensor = new Sensor();
        this.vehicle = new Vehicle();
        this.actuator = new Actuator();
        this.changeDir = new ChangeDirection(vehicle);
        this.tick = new SystemClock();
        this.gas = new GasAnalysisController(sensor, actuator);
        this.movement = new MovementController(vehicle, actuator, changeDir, tick);
    }

    public GasAnalysisController gas() { return gas; }
    public MovementController movement() { return movement; }
    public Vehicle vehicle() { return vehicle; }
    public Actuator actuator() { return actuator; }

    /**
     * Process one input event: deliver it to both controllers (each
     * controller filters by event type) and then deliver any
     * gas-analysis outputs to the movement controller as input.
     */
    public void step(InputEvent event) {
        gas.step(event);
        movement.step(event);
        // Forward any gas-analysis-emitted events to the movement subsystem.
        var emitted = actuator.emitted();
        for (int idx = 0; idx < emitted.size(); idx++) {
            OutputEvent out = emitted.get(idx);
            if (out instanceof OutputEvent.Turn) {
                OutputEvent.Turn ot = (OutputEvent.Turn) out;
                movement.step(new InputEvent.Turn(ot.payload()));
            } else if (out instanceof OutputEvent.Stop) {
                movement.step(new InputEvent.Stop());
            } else if (out instanceof OutputEvent.Resume) {
                movement.step(new InputEvent.Resume());
            }
        }
        actuator.clear();
    }
}
