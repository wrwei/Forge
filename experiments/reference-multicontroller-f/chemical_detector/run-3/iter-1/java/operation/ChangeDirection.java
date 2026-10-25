package chemical_detector.operation;

import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.Constants;
import chemical_detector.types.Angle;
import chemical_detector.types.Loc;

/**
 * Steers away from an obstacle at the linear-velocity constant.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    /** Obstacle on the left turns right, on the right turns left, in front turns back. */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            vehicle.move(Constants.LV, Angle.Right);
        } else if (side == Loc.right) {
            vehicle.move(Constants.LV, Angle.Left);
        } else {
            vehicle.move(Constants.LV, Angle.Back);
        }
    }
}
