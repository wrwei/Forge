package chemical_detector.actuator;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.OutputEvent;

/**
 * Output surface of a controller: the Vehicle's movement operations, timed
 * waits, and the events the controller sends. Records the last command issued.
 */
public final class Actuator {
    @RoboChartType("real") private double velocity = 0.0;
    private Angle direction = Angle.Front;
    private boolean randomWalking = false;
    private boolean shortRandomWalking = false;
    @RoboChartType("nat") private int pendingPause = 0;
    private OutputEvent lastOutput = null;

    /**
     * Moves the Vehicle at a velocity in a body-relative direction until
     * superseded; zero velocity facing Front halts it.
     *
     * @param velocity the linear velocity
     * @param direction the direction relative to the body
     */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.velocity = velocity;
        this.direction = direction;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** Starts an unbounded random walk. */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** Performs a bounded (terminating) random walk. */
    public void shortRandomWalk() {
        this.shortRandomWalking = true;
        this.randomWalking = false;
    }

    /**
     * Steers away from an obstacle at linear velocity lv: an obstacle on the
     * left turns the robot right, on the right turns it left, in front turns
     * it back.
     *
     * @param side the side on which the obstacle was detected
     */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            move(ChemicalDetectorConstants.LV, Angle.Right);
        } else if (side == Loc.right) {
            move(ChemicalDetectorConstants.LV, Angle.Left);
        } else {
            move(ChemicalDetectorConstants.LV, Angle.Back);
        }
    }

    /**
     * Waits for a number of time units before the next behaviour fires.
     *
     * @param duration the time to wait
     */
    public void pause(@RoboChartType("nat") int duration) {
        this.pendingPause = duration;
    }

    /**
     * Sends an output event.
     *
     * @param command the event to send
     */
    public void apply(OutputEvent command) {
        this.lastOutput = command;
    }

    /** @return the velocity of the last move command */
    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    /** @return the direction of the last move command */
    public Angle direction() {
        return direction;
    }

    /** @return whether an unbounded random walk was the last motion command */
    public boolean randomWalking() {
        return randomWalking;
    }

    /** @return whether a short random walk was the last motion command */
    public boolean shortRandomWalking() {
        return shortRandomWalking;
    }

    /** @return the duration of the last wait */
    @RoboChartType("nat")
    public int pendingPause() {
        return pendingPause;
    }

    /** @return the last event sent, or {@code null} if none has been sent */
    public OutputEvent lastOutput() {
        return lastOutput;
    }
}
