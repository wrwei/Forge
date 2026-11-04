package chemical_detector.actuator;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.event.OutputEvent;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.operation.Move;
import chemical_detector.operation.RandomWalk;
import chemical_detector.operation.ShortRandomWalk;

/**
 * The Vehicle port of the movement subsystem. It receives the flag event (CD-Evt7)
 * and the movement commands, and carries each command out through the Vehicle's
 * operation (CD-OP1..4). It also holds the subsystem for a given time (the waits of
 * CD-Const3 and CD-Const6). Records the last value of each.
 */
public final class MovementActuator {

    private final Move move;
    private final RandomWalk randomWalk;
    private final ShortRandomWalk shortRandomWalk;
    private final ChangeDirection changeDirection;
    private OutputEvent lastOutput = new OutputEvent.Halt();
    @RoboChartType("nat") private int lastPause = 0;

    public MovementActuator(Move move, RandomWalk randomWalk, ShortRandomWalk shortRandomWalk,
                            ChangeDirection changeDirection) {
        this.move = move;
        this.randomWalk = randomWalk;
        this.shortRandomWalk = shortRandomWalk;
        this.changeDirection = changeDirection;
    }

    /** Sends an event or command to the Vehicle. */
    public void apply(OutputEvent output) {
        this.lastOutput = output;
        if (output instanceof OutputEvent.Move) {
            OutputEvent.Move command = (OutputEvent.Move) output;
            move.compute(ChemicalDetectorConstants.LV, command.direction());
        } else if (output instanceof OutputEvent.Halt) {
            move.compute(0.0, Angle.Front);
        } else if (output instanceof OutputEvent.RandomWalk) {
            randomWalk.compute();
        } else if (output instanceof OutputEvent.ShortRandomWalk) {
            shortRandomWalk.compute();
        } else if (output instanceof OutputEvent.ChangeDirection) {
            OutputEvent.ChangeDirection command = (OutputEvent.ChangeDirection) output;
            changeDirection.compute(command.side());
        }
    }

    /** Holds the movement subsystem for the given number of ticks. */
    public void pause(@RoboChartType("nat") int duration) {
        this.lastPause = duration;
    }

    /** The last event or command sent. */
    public OutputEvent lastOutput() {
        return lastOutput;
    }

    /** The duration of the last hold. */
    @RoboChartType("nat")
    public int lastPause() {
        return lastPause;
    }
}
