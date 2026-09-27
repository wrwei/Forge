package chemical_detector.operation;

import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.Constants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;

/**
 * Steers away from a detected obstacle at the linear velocity: an obstacle on the
 * left turns the robot right, on the right turns it left, and in front turns it back.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

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
