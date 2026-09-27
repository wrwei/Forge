package chemical_detector.operation;

import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.DetectorConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.Loc;

/**
 * Steers away from a detected obstacle at the linear velocity: an obstacle on the left
 * turns the robot right, on the right turns it left, in front turns it back.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            vehicle.move(DetectorConstants.LV, Angle.Right);
        } else if (side == Loc.right) {
            vehicle.move(DetectorConstants.LV, Angle.Left);
        } else {
            vehicle.move(DetectorConstants.LV, Angle.Back);
        }
    }
}
