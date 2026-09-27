package chemical_detector;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.controller.GasAnalysis;
import chemical_detector.controller.Movement;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.VehicleSensors;
import chemical_detector.types.Chem;
import chemical_detector.types.GasSensor;
import chemical_detector.types.Loc;
import java.util.List;

/**
 * The composed Chemical Detector: routes Vehicle inputs to the two subsystems and
 * gas-analysis outputs (turn, stop, resume) to the movement subsystem.
 */
public final class ChemicalDetector {

    private static final int SETTLE_CYCLES = 3;

    private final VehicleSensors sensors;
    private final Clock timer = new Clock();
    private final Vehicle vehicle = new Vehicle();
    private final Actuator gasOutputs = new Actuator();
    private final Actuator movementOutputs = new Actuator();
    private final GasAnalysis gasAnalysis;
    private final Movement movement;

    public ChemicalDetector(Chem target) {
        this.sensors = new VehicleSensors(target);
        this.gasAnalysis = new GasAnalysis(sensors, gasOutputs);
        this.movement = new Movement(sensors, vehicle, movementOutputs, timer);
    }

    /** Delivers a gas reading and lets gas analysis run to its next waiting point. */
    public void receiveGas(List<GasSensor> reading) {
        gasAnalysis.step(new InputEvent.gas(reading));
        routeGasOutputs();
        for (int i = 0; i < SETTLE_CYCLES; i++) {
            gasAnalysis.step(null);
            routeGasOutputs();
        }
    }

    /** Delivers an obstacle detection to the movement subsystem. */
    public void receiveObstacle(Loc side) {
        deliverToMovement(new InputEvent.obstacle(side));
    }

    /** Runs one cycle with no input, advancing time by the given amount first. */
    public void idle(@RoboChartType("nat") long elapsed) {
        timer.advance(elapsed);
        deliverToMovement(null);
    }

    /** Output events the movement subsystem has sent to the Vehicle since the last call. */
    public List<OutputEvent> drainVehicleSignals() {
        return movementOutputs.drain();
    }

    public GasAnalysis gasAnalysis() {
        return gasAnalysis;
    }

    public Movement movement() {
        return movement;
    }

    public Vehicle vehicle() {
        return vehicle;
    }

    public VehicleSensors sensors() {
        return sensors;
    }

    private void routeGasOutputs() {
        for (OutputEvent out : gasOutputs.drain()) {
            if (out instanceof OutputEvent.turn) {
                OutputEvent.turn command = (OutputEvent.turn) out;
                deliverToMovement(new InputEvent.turn(command.a()));
            } else if (out instanceof OutputEvent.stop) {
                deliverToMovement(new InputEvent.stop());
            } else if (out instanceof OutputEvent.resume) {
                deliverToMovement(new InputEvent.resume());
            }
        }
    }

    private void deliverToMovement(InputEvent event) {
        movement.step(event);
        movement.step(null);
    }
}
