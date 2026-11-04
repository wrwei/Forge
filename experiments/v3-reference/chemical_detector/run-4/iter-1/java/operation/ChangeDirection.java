package chemical_detector.operation;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Loc;

/**
 * The changeDirection operation (CD-OP4): steer away from an obstacle at the linear
 * velocity lv. An obstacle on the left turns the robot Right, on the right turns it
 * Left, in front turns it Back.
 */
public final class ChangeDirection {

    @RoboChartType("real") private double evadeVelocity = 0.0;
    private boolean turnRight = false;
    private boolean turnLeft = false;
    private boolean turnBack = false;

    public ChangeDirection() {
    }

    /** Steers away from an obstacle on the given side. */
    public void compute(Loc side) {
        this.evadeVelocity = ChemicalDetectorConstants.LV;
        this.turnRight = side == Loc.left;
        this.turnLeft = side == Loc.right;
        this.turnBack = side == Loc.front;
    }

    /** The velocity of the evasive move. */
    @RoboChartType("real")
    public double evadeVelocity() {
        return evadeVelocity;
    }

    /** Whether the evasive move turns Right. */
    public boolean turnRight() {
        return turnRight;
    }

    /** Whether the evasive move turns Left. */
    public boolean turnLeft() {
        return turnLeft;
    }

    /** Whether the evasive move turns Back. */
    public boolean turnBack() {
        return turnBack;
    }
}
