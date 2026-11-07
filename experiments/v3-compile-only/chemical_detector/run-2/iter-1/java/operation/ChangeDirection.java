package chemical_detector.operation;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Loc;

/**
 * The changeDirection(l) operation (CD-OP4): steers away from an obstacle at
 * the linear velocity lv. An obstacle on the left turns the robot Right, on the
 * right turns it Left, and in front turns it Back.
 */
public final class ChangeDirection {

    @RoboChartType("real") private double escapeVelocity = 0.0;
    private boolean escapeRight = false;
    private boolean escapeLeft = false;
    private boolean escapeBack = false;

    /** Computes the escape command for an obstacle on {@code obstacleSide}. */
    public void compute(Loc obstacleSide) {
        this.escapeVelocity = ChemicalDetectorConstants.LV;
        this.escapeRight = obstacleSide == Loc.left;
        this.escapeLeft = obstacleSide == Loc.right;
        this.escapeBack = obstacleSide == Loc.front;
    }

    /** The escape velocity (lv). */
    @RoboChartType("real")
    public double escapeVelocity() {
        return escapeVelocity;
    }

    /** True when the robot must turn Right (obstacle on the left). */
    public boolean escapeRight() {
        return escapeRight;
    }

    /** True when the robot must turn Left (obstacle on the right). */
    public boolean escapeLeft() {
        return escapeLeft;
    }

    /** True when the robot must turn Back (obstacle in front). */
    public boolean escapeBack() {
        return escapeBack;
    }
}
