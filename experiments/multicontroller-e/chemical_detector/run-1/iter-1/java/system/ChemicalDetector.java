package chemical_detector.system;

import chemical_detector.actuator.CommandChannel;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.controller.GasAnalysis;
import chemical_detector.controller.Movement;
import chemical_detector.data.Chem;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.Sensor;

/**
 * The Chemical Detector: wires the Vehicle's sensing and actuation surface to
 * the gas-analysis and movement subsystems, which communicate only through the
 * turn, stop and resume commands.
 */
public final class ChemicalDetector {

    /** Event-free cycles needed for gas analysis to finish classifying one reading. */
    @RoboChartType("nat")
    private static final int ANALYSIS_CYCLES = 2;

    private final Sensor sensor;
    private final Clock timer;
    private final Vehicle vehicle;
    private final CommandChannel commands;
    private final GasAnalysis gasAnalysis;
    private final Movement movement;

    public ChemicalDetector(Chem target) {
        this.sensor = new Sensor(target);
        this.timer = new Clock();
        this.vehicle = new Vehicle();
        this.commands = new CommandChannel();
        this.gasAnalysis = new GasAnalysis(sensor, commands);
        this.movement = new Movement(sensor, timer, vehicle, new ChangeDirection(vehicle));
    }

    /**
     * Delivers an event from the Vehicle. A gas reading is classified to
     * completion and every resulting command is passed to the movement
     * subsystem; any other event goes to the movement subsystem directly.
     */
    public void deliver(InputEvent event) {
        if (event instanceof InputEvent.Gas) {
            gasAnalysis.step(event);
            forwardCommands();
            for (var cycle = 0; cycle < ANALYSIS_CYCLES; cycle++) {
                gasAnalysis.step(null);
                forwardCommands();
            }
        } else {
            driveMovement(event);
        }
    }

    /** Records the Vehicle's cumulative distance travelled. */
    public void odometer(@RoboChartType("real") double distance) {
        sensor.updateOdometer(distance);
    }

    /** Advances time by {@code elapsed} units. */
    public void advanceTime(@RoboChartType("nat") long elapsed) {
        timer.advance(elapsed);
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

    private void forwardCommands() {
        for (var command : commands.drain()) {
            if (command instanceof OutputEvent.Turn) {
                OutputEvent.Turn turn = (OutputEvent.Turn) command;
                driveMovement(new InputEvent.Turn(turn.direction()));
            } else if (command instanceof OutputEvent.Stop) {
                driveMovement(new InputEvent.Stop());
            } else if (command instanceof OutputEvent.Resume) {
                driveMovement(new InputEvent.Resume());
            }
        }
    }

    /** Steps the movement subsystem on {@code event}, then lets any guard-only transition fire. */
    private void driveMovement(InputEvent event) {
        movement.step(event);
        movement.step(null);
    }
}
