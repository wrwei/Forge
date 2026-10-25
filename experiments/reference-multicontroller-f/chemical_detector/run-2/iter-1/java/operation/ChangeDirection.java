package chemical_detector.operation;

import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.Constants;
import chemical_detector.types.Angle;
import chemical_detector.types.Loc;

/**
 * Steers away from a detected obstacle at the linear-velocity constant.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    /** Obstacle on the left turns Right, on the right turns Left, in front turns Back. */
    public void changeDirection(Loc l) {
        if (l == Loc.left) {
            vehicle.move(Constants.LV, Angle.Right);
        } else if (l == Loc.right) {
            vehicle.move(Constants.LV, Angle.Left);
        } else if (l == Loc.front) {
            vehicle.move(Constants.LV, Angle.Back);
        }
    }
}
