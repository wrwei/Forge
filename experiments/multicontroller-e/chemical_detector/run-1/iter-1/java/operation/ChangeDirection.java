package chemical_detector.operation;

import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.ChemConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.Loc;

/**
 * Steers away from an obstacle at the linear-velocity constant: an obstacle on
 * the left turns the robot right, one on the right turns it left, and one in
 * front turns it back.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void changeDirection(Loc l) {
        if (l == Loc.left) {
            vehicle.move(ChemConstants.LV, Angle.Right);
        } else if (l == Loc.right) {
            vehicle.move(ChemConstants.LV, Angle.Left);
        } else {
            vehicle.move(ChemConstants.LV, Angle.Back);
        }
    }
}
