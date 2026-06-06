package chemdetector;

import chemdetector.actuator.Vehicle;
import chemdetector.clock.Clock;
import chemdetector.controller.GasAnalysis;
import chemdetector.controller.Movement;
import chemdetector.datatype.Chem;
import chemdetector.event.GasAnalysisInputEvent;
import chemdetector.event.MovementInputEvent;
import chemdetector.sensor.GasSensorService;

/**
 * Top-level wiring for the Chemical Detector. Composes the Vehicle, the gas-analysis
 * sensor service, the Clock, and the two controllers (GasAnalysis + Movement) into one
 * runnable surface. The two controllers communicate via shared events through the
 * GasAnalysis controller's reference to Movement.
 */
public final class ChemDetectorSystem {

    private final Vehicle vehicle;
    private final Clock clock;
    private final GasSensorService sensor;
    private final Movement movement;
    private final GasAnalysis gasAnalysis;

    public ChemDetectorSystem(Chem target) {
        this.vehicle = new Vehicle();
        this.clock = new Clock();
        this.sensor = new GasSensorService(target);
        this.movement = new Movement(vehicle, clock);
        this.gasAnalysis = new GasAnalysis(sensor, movement);
    }

    public Vehicle vehicle() { return vehicle; }
    public GasAnalysis gasAnalysis() { return gasAnalysis; }
    public Movement movement() { return movement; }

    /** Deliver a Vehicle-sourced gas event to the gas-analysis subsystem. */
    public void deliverGas(GasAnalysisInputEvent event) {
        gasAnalysis.step(event);
    }

    /** Deliver a Vehicle-sourced obstacle/odometer event to the movement subsystem. */
    public void deliverMovement(MovementInputEvent event) {
        movement.step(event);
    }
}
