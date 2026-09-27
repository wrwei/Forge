package chemical_detector.operation;

import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.DetectorConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;

/**
 * The changeDirection operation (CD-OP4): steers away from an obstacle at the linear
 * velocity lv — left obstacle turns right, right obstacle turns left, front turns back.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void changeDirection(Loc l) {
        if (l == Loc.left) {
            vehicle.move(DetectorConstants.LV, Angle.Right);
        } else if (l == Loc.right) {
            vehicle.move(DetectorConstants.LV, Angle.Left);
        } else {
            vehicle.move(DetectorConstants.LV, Angle.Back);
        }
    }
}
