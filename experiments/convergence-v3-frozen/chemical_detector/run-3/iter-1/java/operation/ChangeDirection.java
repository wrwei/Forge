package chemical_detector.operation;

import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.ChemConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;

/**
 * The changeDirection(l : Loc) operation: steers away from a detected obstacle
 * at the linear-velocity constant.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    /** Obstacle on the left turns right, on the right turns left, in front turns back. */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            this.vehicle.move(ChemConstants.LV, Angle.Right);
        } else if (side == Loc.right) {
            this.vehicle.move(ChemConstants.LV, Angle.Left);
        } else {
            this.vehicle.move(ChemConstants.LV, Angle.Back);
        }
    }
}
