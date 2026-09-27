package chemical_detector.system;

import chemical_detector.actuator.EventActuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementController;
import chemical_detector.data.Chem;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.GasSensorArray;
import chemical_detector.sensor.OdometerSensor;
import chemical_detector.timing.Clock;
import java.util.List;

/**
 * The Chemical Detector: the Vehicle surface plus the gas-analysis and movement
 * subsystems, which communicate only through the turn, stop and resume events.
 */
public final class ChemicalDetectorSystem {

    private final Vehicle vehicle;
    private final OdometerSensor odometerSensor;
    private final Clock timer;
    private final EventActuator gasAnalysisOutputs;
    private final EventActuator movementOutputs;
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;

    public ChemicalDetectorSystem(Chem target) {
        this.vehicle = new Vehicle();
        this.odometerSensor = new OdometerSensor();
        this.timer = new Clock();
        this.gasAnalysisOutputs = new EventActuator();
        this.movementOutputs = new EventActuator();
        this.gasAnalysis = new GasAnalysisController(new GasSensorArray(target), gasAnalysisOutputs);
        this.movement = new MovementController(vehicle, new ChangeDirection(vehicle), odometerSensor, timer,
                movementOutputs);
    }

    /**
     * Delivers one event from the Vehicle. A gas reading is analysed to
     * completion and the resulting commands are passed to the movement
     * subsystem; any other event goes to the movement subsystem.
     */
    public void dispatch(InputEvent input) {
        if (input instanceof InputEvent.gas) {
            gasAnalysis.step(input);
            gasAnalysis.step(null);
            gasAnalysis.step(null);
            forwardGasAnalysisOutputs();
        } else {
            movement.step(null);
            movement.step(input);
        }
    }

    /** Runs one control cycle in which no event arrived. */
    public void cycle() {
        gasAnalysis.step(null);
        forwardGasAnalysisOutputs();
        movement.step(null);
    }

    /** Advances platform time. */
    public void advanceTime(@RoboChartType("nat") long elapsed) {
        timer.advance(elapsed);
    }

    /** Records an odometer report from the Vehicle. */
    public void updateOdometer(@RoboChartType("real") double travelled) {
        odometerSensor.update(travelled);
    }

    public GasAnalysisController gasAnalysis() {
        return gasAnalysis;
    }

    public MovementController movement() {
        return movement;
    }

    public Vehicle vehicle() {
        return vehicle;
    }

    /** Signals the movement subsystem has sent to the Vehicle (the flag event). */
    public List<OutputEvent> vehicleSignals() {
        return movementOutputs.emitted();
    }

    private void forwardGasAnalysisOutputs() {
        for (var output : gasAnalysisOutputs.drain()) {
            movement.step(null);
            if (output instanceof OutputEvent.turn) {
                OutputEvent.turn turnOutput = (OutputEvent.turn) output;
                movement.step(new InputEvent.turn(turnOutput.direction()));
            } else if (output instanceof OutputEvent.stop) {
                movement.step(new InputEvent.stop());
            } else if (output instanceof OutputEvent.resume) {
                movement.step(new InputEvent.resume());
            }
        }
    }
}
