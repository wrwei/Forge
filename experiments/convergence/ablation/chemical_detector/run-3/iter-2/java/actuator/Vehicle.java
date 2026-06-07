package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.controller.Constants;
import chemdetector.domain.Angle;
import chemdetector.domain.Loc;

/** Sensing-and-actuation surface of the robot platform (CD-ARCH1, CD-ARCH2). */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity = 0.0;
    private Angle direction = Angle.Front;
    private boolean walking = false;

    /** Moves at the given linear velocity in the given body-relative direction (CD-OP1). */
    public void move(@RoboChartType("real") double newVelocity, Angle newDirection) {
        this.velocity = newVelocity;
        this.direction = newDirection;
        this.walking = false;
    }

    /** Unbounded random-walk search (CD-OP2). */
    public void randomWalk() {
        this.walking = true;
    }

    /** Bounded random-walk recovery manoeuvre (CD-OP3). */
    public void shortRandomWalk() {
        this.walking = true;
    }

    /** Steers away from an obstacle on the given side at the linear-velocity constant (CD-OP4). */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            move(Constants.LV, Angle.Right);
        } else if (side == Loc.right) {
            move(Constants.LV, Angle.Left);
        } else {
            move(Constants.LV, Angle.Back);
        }
    }

    /** Timing primitive; recognised by the pipeline as a RoboChart wait. */
    public void pause(@RoboChartType("nat") int duration) {
        // Time passage is modelled formally; no physical delay in library code.
    }

    public double velocity() {
        return velocity;
    }

    public Angle direction() {
        return direction;
    }

    public boolean walking() {
        return walking;
    }
}
