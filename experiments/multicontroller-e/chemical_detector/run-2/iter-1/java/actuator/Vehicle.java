package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.DetectorConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.Loc;

/**
 * The robotic platform's movement surface. Records the last command issued so the
 * platform (or a test) can inspect it.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;
    private Angle heading = Angle.Front;
    @RoboChartType("nat")
    private int randomWalks;
    @RoboChartType("nat")
    private int shortRandomWalks;
    @RoboChartType("nat")
    private int lastPause;

    /** move (CD-OP1): travel at velocity {@code lv} in direction {@code a} until superseded. */
    public void move(@RoboChartType("real") double lv, Angle a) {
        velocity = lv;
        heading = a;
    }

    /** randomWalk (CD-OP2): unbounded random-walk search. */
    public void randomWalk() {
        randomWalks = randomWalks + 1;
    }

    /** shortRandomWalk (CD-OP3): bounded random-walk recovery manoeuvre. */
    public void shortRandomWalk() {
        shortRandomWalks = shortRandomWalks + 1;
    }

    /**
     * changeDirection (CD-OP4): steer away from an obstacle at linear velocity lv —
     * left turns Right, right turns Left, front turns Back.
     */
    public void changeDirection(Loc l) {
        if (l == Loc.left) {
            move(DetectorConstants.LV, Angle.Right);
        } else if (l == Loc.right) {
            move(DetectorConstants.LV, Angle.Left);
        } else {
            move(DetectorConstants.LV, Angle.Back);
        }
    }

    /** Blocks the caller's behaviour for {@code duration} clock units (RoboChart {@code wait}). */
    public void pause(@RoboChartType("nat") int duration) {
        lastPause = duration;
    }

    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    public Angle heading() {
        return heading;
    }

    @RoboChartType("nat")
    public int randomWalks() {
        return randomWalks;
    }

    @RoboChartType("nat")
    public int shortRandomWalks() {
        return shortRandomWalks;
    }

    @RoboChartType("nat")
    public int lastPause() {
        return lastPause;
    }
}
