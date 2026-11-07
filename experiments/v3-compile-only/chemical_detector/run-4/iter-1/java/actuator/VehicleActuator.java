package chemical_detector.actuator;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.OutputEvent;

/**
 * Motion operations and signals of the Vehicle (CD-OP1 to CD-OP4, CD-Evt7).
 * It records the last command issued. At power-on the Vehicle is performing
 * its unbounded random walk, the during behaviour of the initial movement
 * state Waiting.
 */
public final class VehicleActuator {

    @RoboChartType("real") private double velocity = 0.0;
    private Angle heading = Angle.Front;
    private boolean randomWalking = true;
    @RoboChartType("nat") private int shortRandomWalks = 0;
    private boolean flagRaised = false;

    /**
     * move (CD-OP1): move at the given velocity in the given body-relative
     * direction until a later command supersedes it. Zero velocity facing
     * Front halts the Vehicle.
     */
    public void move(@RoboChartType("real") double velocity, Angle heading) {
        this.velocity = velocity;
        this.heading = heading;
        this.randomWalking = false;
    }

    /** randomWalk (CD-OP2): start an unbounded random-walk search. */
    public void randomWalk() {
        randomWalking = true;
    }

    /** shortRandomWalk (CD-OP3): perform a bounded random walk. */
    public void shortRandomWalk() {
        randomWalking = false;
        shortRandomWalks = shortRandomWalks + 1;
    }

    /**
     * changeDirection (CD-OP4): steer away from an obstacle at linear
     * velocity lv. An obstacle on the left turns the robot right, one on the
     * right turns it left, and one in front turns it back.
     */
    public void changeDirection(Loc obstacleSide) {
        if (obstacleSide == Loc.left) {
            move(ChemicalDetectorConstants.LV, Angle.Right);
        } else if (obstacleSide == Loc.right) {
            move(ChemicalDetectorConstants.LV, Angle.Left);
        } else {
            move(ChemicalDetectorConstants.LV, Angle.Back);
        }
    }

    /** Receives a signal from the movement subsystem: flag (CD-Evt7). */
    public void apply(OutputEvent signal) {
        if (signal instanceof OutputEvent.Flag) {
            flagRaised = true;
        }
    }

    /** Velocity of the last move command. */
    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    /** Heading of the last move command. */
    public Angle heading() {
        return heading;
    }

    /** Whether the Vehicle is performing an unbounded random walk. */
    public boolean randomWalking() {
        return randomWalking;
    }

    /** Number of short random walks performed. */
    @RoboChartType("nat")
    public int shortRandomWalks() {
        return shortRandomWalks;
    }

    /** Whether the flag has been raised (the source has been located). */
    public boolean flagRaised() {
        return flagRaised;
    }
}
