package chemical_detector.operation;

import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.Constants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;

/**
 * Steers away from a detected obstacle at the linear-velocity constant:
 * left turns Right, right turns Left, front turns Back.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    /** Moves away from an obstacle detected on side {@code l}. */
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
