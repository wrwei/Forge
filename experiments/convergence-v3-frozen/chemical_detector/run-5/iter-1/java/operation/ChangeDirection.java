package chemical_detector.operation;

import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.ChemConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;

/**
 * The movement subsystem's changeDirection operation: steers away from an
 * obstacle at the linear-velocity constant.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    /**
     * Obstacle on the left turns the robot right, on the right turns it left,
     * in front turns it back.
     */
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
