package chemical_detector.system;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.controller.GasAnalysis;
import chemical_detector.controller.Movement;
import chemical_detector.domain.Chem;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.Sensor;
import java.util.List;

/**
 * The Chemical Detector: the Vehicle plus the gas-analysis and movement
 * subsystems. The subsystems communicate only through turn, stop and resume;
 * the movement subsystem's flag is delivered to the Vehicle.
 */
public final class ChemicalDetector {

    private final Sensor sensor;
    private final Clock timer;
    private final Vehicle vehicle;
    private final Actuator gasAnalysisOutputs;
    private final Actuator movementOutputs;
    private final GasAnalysis gasAnalysis;
    private final Movement movement;

    /** Creates a detector searching for the chemical {@code target}. */
    public ChemicalDetector(Chem target) {
        this.sensor = new Sensor(target);
        this.timer = new Clock();
        this.vehicle = new Vehicle();
        this.gasAnalysisOutputs = new Actuator();
        this.movementOutputs = new Actuator();
        this.gasAnalysis = new GasAnalysis(sensor, gasAnalysisOutputs);
        this.movement = new Movement(sensor, vehicle, new ChangeDirection(vehicle), movementOutputs, timer);
    }

    /**
     * Runs one control cycle. {@code platformEvent} is the Vehicle event of this
     * cycle (Gas, Obstacle, or Tick when there is none), {@code distanceTravelled}
     * the current odometer reading and {@code elapsed} the time since the last cycle.
     */
    public void cycle(InputEvent platformEvent,
                      @RoboChartType("real") double distanceTravelled,
                      @RoboChartType("nat") long elapsed) {
        timer.advance(elapsed);
        sensor.updateOdometer(distanceTravelled);
        var tick = new InputEvent.Tick();
        if (platformEvent instanceof InputEvent.Gas) {
            gasAnalysis.step(platformEvent);
            movement.step(tick);
        } else if (platformEvent instanceof InputEvent.Obstacle) {
            gasAnalysis.step(tick);
            movement.step(platformEvent);
        } else {
            gasAnalysis.step(tick);
            movement.step(tick);
        }
        deliver(gasAnalysisOutputs.drain());
        for (OutputEvent output : movementOutputs.drain()) {
            if (output instanceof OutputEvent.Flag) {
                vehicle.flag();
            }
        }
    }

    public Vehicle vehicle() {
        return vehicle;
    }

    public GasAnalysis gasAnalysis() {
        return gasAnalysis;
    }

    public Movement movement() {
        return movement;
    }

    private void deliver(List<OutputEvent> outputs) {
        for (OutputEvent output : outputs) {
            if (output instanceof OutputEvent.Turn) {
                OutputEvent.Turn turn = (OutputEvent.Turn) output;
                movement.step(new InputEvent.Turn(turn.angle()));
            } else if (output instanceof OutputEvent.Stop) {
                movement.step(new InputEvent.Stop());
            } else if (output instanceof OutputEvent.Resume) {
                movement.step(new InputEvent.Resume());
            }
        }
    }
}
