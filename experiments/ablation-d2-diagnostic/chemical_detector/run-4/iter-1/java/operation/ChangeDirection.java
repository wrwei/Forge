package chemical_detector.operation;

import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.Constants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;

/**
 * The changeDirection operation (CD-OP4): steers away from a detected obstacle at the
 * linear velocity. Left obstacle turns Right, right obstacle turns Left, front obstacle
 * turns Back.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    /** Issues the evasive move for an obstacle on side {@code side}. */
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
