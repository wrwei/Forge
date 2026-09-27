package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.data.Angle;
import chemical_detector.data.Loc;

/** Actuation surface of the Vehicle: motion and timing primitives. */
public final class Vehicle {

    @RoboChartType("real")
    private double lastVelocity = 0.0;

    private Angle lastAngle = Angle.Front;

    private boolean walking = false;

    /** Move at the given velocity in the given body-relative direction. */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.lastVelocity = lv;
        this.lastAngle = a;
        this.walking = false;
    }

    /** Unbounded random-walk search. */
    public void randomWalk() {
        this.walking = true;
    }

    /** Bounded random-walk recovery manoeuvre. */
    public void shortRandomWalk() {
        this.walking = true;
    }

    /** Steer away from an obstacle on the given side, at the given velocity. */
    public void changeDirection(@RoboChartType("real") double lv, Loc l) {
        if (l == Loc.left) {
            move(lv, Angle.Right);
        } else if (l == Loc.right) {
            move(lv, Angle.Left);
        } else {
            move(lv, Angle.Back);
        }
    }

    /** Let the given number of time units pass. */
    public void pause(@RoboChartType("nat") int duration) {
        this.walking = false;
    }

    @RoboChartType("real")
    public double lastVelocity() {
        return lastVelocity;
    }

    public Angle lastAngle() {
        return lastAngle;
    }

    public boolean walking() {
        return walking;
    }
}
