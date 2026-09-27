package chemical_detector.operation;

import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.ChemConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.Loc;

/**
 * The changeDirection operation: steers away from an obstacle at linear
 * velocity {@code LV}. Left obstacle turns right, right turns left, front turns back.
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
        } else if (l == Loc.front) {
            vehicle.move(ChemConstants.LV, Angle.Back);
        }
    }
}
