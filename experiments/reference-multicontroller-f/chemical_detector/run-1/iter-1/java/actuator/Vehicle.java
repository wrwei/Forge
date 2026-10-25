package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;

/**
 * Movement surface of the Vehicle. Records the last command issued so the platform
 * (or a test) can act on it.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;
    private Angle direction = Angle.Front;
    private boolean randomWalking;
    private boolean shortRandomWalking;
    @RoboChartType("nat")
    private int lastPause;
    private boolean flagged;

    /** Moves at {@code linearVelocity} towards {@code angle}; zero velocity halts the Vehicle. */
    public void move(@RoboChartType("real") double linearVelocity, Angle angle) {
        this.velocity = linearVelocity;
        this.direction = angle;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** Starts an unbounded random walk. */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** Starts a bounded random walk. */
    public void shortRandomWalk() {
        this.shortRandomWalking = true;
        this.randomWalking = false;
    }

    /** Steers away from an obstacle on {@code side}: left turns Right, right turns Left, front turns Back. */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            move(ChemConstants.LV, Angle.Right);
        } else if (side == Loc.right) {
            move(ChemConstants.LV, Angle.Left);
        } else {
            move(ChemConstants.LV, Angle.Back);
        }
    }

    /** Waits {@code duration} time units before the controller's next behaviour. */
    public void pause(@RoboChartType("nat") int duration) {
        this.lastPause = duration;
    }

    /** Receives the flag signal: the chemical source has been located. */
    public void flag() {
        this.flagged = true;
    }

    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    public Angle direction() {
        return direction;
    }

    public boolean randomWalking() {
        return randomWalking;
    }

    public boolean shortRandomWalking() {
        return shortRandomWalking;
    }

    @RoboChartType("nat")
    public int lastPause() {
        return lastPause;
    }

    public boolean flagged() {
        return flagged;
    }
}
