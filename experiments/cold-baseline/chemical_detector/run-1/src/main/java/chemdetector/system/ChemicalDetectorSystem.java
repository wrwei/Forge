package chemdetector.system;

import chemdetector.actuator.GasAnalysisOutput;
import chemdetector.actuator.Vehicle;
import chemdetector.datatype.Angle;
import chemdetector.event.InputEvent;
import chemdetector.gasanalysis.controller.GasAnalysisController;
import chemdetector.movement.controller.MovementController;
import chemdetector.operation.ChangeDirection;
import chemdetector.sensor.Clock;
import chemdetector.sensor.Sensor;

/**
 * System harness composing the two controllers with their actuators
 * and forwarding the inter-controller signals from the gas-analysis
 * subsystem into the movement subsystem.
 */
public final class ChemicalDetectorSystem {

    private final Vehicle vehicle;
    private final Sensor sensor;
    private final Clock clock;
    private final ChangeDirection changeDirection;
    private final GasAnalysisOutput gaOut;
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;

    public ChemicalDetectorSystem() {
        this.vehicle = new Vehicle();
        this.sensor = new Sensor();
        this.clock = new Clock();
        this.changeDirection = new ChangeDirection(vehicle);
        this.gaOut = new GasAnalysisOutput();
        this.gasAnalysis = new GasAnalysisController(sensor, gaOut);
        this.movement = new MovementController(vehicle, changeDirection, clock);
    }

    /**
     * Forward a boundary event to the appropriate controller, then drain any
     * pending inter-controller signals from the gas-analysis subsystem into
     * the movement subsystem.
     */
    public void step(InputEvent event) {
        if (event instanceof InputEvent.Gas) {
            gasAnalysis.step(event);
        } else if (event instanceof InputEvent.Obstacle || event instanceof InputEvent.Odometer) {
            movement.step(event);
        } else {
            // turn/stop/resume from the environment are not expected as boundary
            // inputs; they are produced by the gas-analysis subsystem.
        }
        drainSharedSignals();
    }

    private void drainSharedSignals() {
        if (gaOut.stopPending()) {
            movement.step(new InputEvent.Stop());
        }
        if (gaOut.resumePending()) {
            movement.step(new InputEvent.Resume());
        }
        Angle t = gaOut.lastTurn();
        if (t != null) {
            movement.step(new InputEvent.Turn(t));
        }
        gaOut.clearPending();
    }

    public GasAnalysisController gasAnalysis() { return gasAnalysis; }
    public MovementController movement() { return movement; }
    public Vehicle vehicle() { return vehicle; }
}
