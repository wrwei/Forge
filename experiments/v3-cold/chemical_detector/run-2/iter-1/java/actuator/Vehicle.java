package chemical_detector.actuator;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;

/**
 * The Vehicle's movement operations; records the last command issued.
 * At start-up the Vehicle is performing its unbounded random walk.
 */
public final class Vehicle {
    @RoboChartType("real") private double velocity = 0.0;
    private Angle heading = Angle.Front;
    private boolean randomWalking = true;
    private boolean shortRandomWalking = false;

    /**
     * Moves at a linear velocity in a body-relative direction until
     * superseded (CD-OP1); zero velocity facing Front halts the Vehicle.
     *
     * @param velocity linear velocity
     * @param direction body-relative direction
     */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.velocity = velocity;
        this.heading = direction;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** Starts the unbounded random-walk search (CD-OP2). */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** Performs a bounded random walk to get unstuck (CD-OP3). */
    public void shortRandomWalk() {
        this.randomWalking = false;
        this.shortRandomWalking = true;
    }

    /**
     * Steers away from an obstacle at the linear velocity (CD-OP4): an
     * obstacle on the left turns right, on the right turns left, in front
     * turns back.
     *
     * @param side side of the detected obstacle
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
     * Last commanded linear velocity.
     *
     * @return the velocity
     */
    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    /**
     * Last commanded direction.
     *
     * @return the direction
     */
    public Angle heading() {
        return heading;
    }

    /**
     * Whether the unbounded random walk is the current motion.
     *
     * @return true while random walking
     */
    public boolean randomWalking() {
        return randomWalking;
    }

    /**
     * Whether the bounded random walk was the last command.
     *
     * @return true after shortRandomWalk until the next command
     */
    public boolean shortRandomWalking() {
        return shortRandomWalking;
    }
}
