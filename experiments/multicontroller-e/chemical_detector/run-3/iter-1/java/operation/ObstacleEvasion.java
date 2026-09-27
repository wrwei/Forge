package chemical_detector.operation;

import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.Constants;
import chemical_detector.types.Angle;
import chemical_detector.types.Loc;

/**
 * The changeDirection operation: steers away from an obstacle at the linear
 * velocity.
 */
public final class ObstacleEvasion {

    private final Vehicle vehicle;

    public ObstacleEvasion(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    /** Obstacle on the left turns right, on the right turns left, in front turns back. */
    public void changeDirection(Loc l) {
        if (l == Loc.left) {
            vehicle.move(Constants.LV, Angle.Right);
        } else if (l == Loc.right) {
            vehicle.move(Constants.LV, Angle.Left);
        } else {
            vehicle.move(Constants.LV, Angle.Back);
        }
    }
}
