package chemical_detector.operation;

import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.ChemicalDetectorConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;

/**
 * The movement subsystem's changeDirection operation: steers away from an obstacle at
 * the linear-velocity constant.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    /** Obstacle on the left turns right, on the right turns left, in front turns back. */
    public void changeDirection(Loc l) {
        if (l == Loc.left) {
            vehicle.move(ChemicalDetectorConstants.LV, Angle.Right);
        } else if (l == Loc.right) {
            vehicle.move(ChemicalDetectorConstants.LV, Angle.Left);
        } else if (l == Loc.front) {
            vehicle.move(ChemicalDetectorConstants.LV, Angle.Back);
        }
    }
}
