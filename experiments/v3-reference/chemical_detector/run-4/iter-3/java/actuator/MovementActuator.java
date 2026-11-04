package chemical_detector.actuator;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.OutputEvent;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.operation.Move;
import chemical_detector.operation.RandomWalk;
import chemical_detector.operation.ShortRandomWalk;

/**
 * The Vehicle port of the movement subsystem. It receives the flag event (CD-Evt7)
 * and the movement commands, records the commanded velocity and heading, and invokes
 * the Vehicle's operation (CD-OP1..4). It also holds the subsystem for a given time
 * (the waits of CD-Const3 and CD-Const6).
 */
public final class MovementActuator {

    private final Move move;
    private final RandomWalk randomWalk;
    private final ShortRandomWalk shortRandomWalk;
    private final ChangeDirection changeDirection;
    private OutputEvent lastOutput = new OutputEvent.Halt();
    @RoboChartType("real") private double commandedVelocity = 0.0;
    private Angle commandedHeading = Angle.Front;
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
            commandedVelocity = ChemicalDetectorConstants.LV;
            commandedHeading = command.direction();
            move.compute();
        } else if (output instanceof OutputEvent.Halt) {
            commandedVelocity = 0.0;
            commandedHeading = Angle.Front;
            move.compute();
        } else if (output instanceof OutputEvent.RandomWalk) {
            randomWalk.compute();
        } else if (output instanceof OutputEvent.ShortRandomWalk) {
            shortRandomWalk.compute();
        } else if (output instanceof OutputEvent.ChangeDirection) {
            OutputEvent.ChangeDirection command = (OutputEvent.ChangeDirection) output;
            commandedVelocity = ChemicalDetectorConstants.LV;
            commandedHeading = awayFrom(command.side());
            changeDirection.compute();
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

    /** The velocity of the current motion command. */
    @RoboChartType("real")
    public double commandedVelocity() {
        return commandedVelocity;
    }

    /** The heading of the current motion command. */
    public Angle commandedHeading() {
        return commandedHeading;
    }

    /** The duration of the last hold. */
    @RoboChartType("nat")
    public int lastPause() {
        return lastPause;
    }

    private Angle awayFrom(Loc side) {
        Angle heading = Angle.Back;
        if (side == Loc.left) {
            heading = Angle.Right;
        } else if (side == Loc.right) {
            heading = Angle.Left;
        }
        return heading;
    }
}
