package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.DetectorConstants;
import chemical_detector.types.Angle;
import chemical_detector.types.Loc;

/** Movement surface of the Vehicle; records the last command issued for inspection. */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;

    private Angle heading = Angle.Front;

    private boolean randomWalking;

    private boolean shortRandomWalking;

    @RoboChartType("nat")
    private int lastPause;

    /** Moves at the given velocity in the given body-relative direction until superseded. */
    public void move(@RoboChartType("real") double speed, Angle direction) {
        this.velocity = speed;
        this.heading = direction;
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

    /** Steers away from an obstacle on the given side at the linear velocity. */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            move(DetectorConstants.lv, Angle.Right);
        } else if (side == Loc.right) {
            move(DetectorConstants.lv, Angle.Left);
        } else {
            move(DetectorConstants.lv, Angle.Back);
        }
    }

    /** Waits for the given duration before the next behaviour. */
    public void pause(@RoboChartType("nat") int duration) {
        this.lastPause = duration;
    }

    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    public Angle heading() {
        return heading;
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
}
