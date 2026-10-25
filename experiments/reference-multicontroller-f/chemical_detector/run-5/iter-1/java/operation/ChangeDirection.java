package chemical_detector.operation;

import chemical_detector.actuator.Vehicle;
import chemical_detector.constants.DetectorConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.Loc;

/** Local operation changeDirection(l): steers away from the side on which an obstacle was detected. */
public final class ChangeDirection {

    private final Vehicle vehicle;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    /** Obstacle on the left turns right, on the right turns left, in front turns back, at speed lv. */
    public void changeDirection(Loc l) {
        if (l == Loc.left) {
            vehicle.move(DetectorConstants.LV, Angle.Right);
        } else if (l == Loc.right) {
            vehicle.move(DetectorConstants.LV, Angle.Left);
        } else if (l == Loc.front) {
            vehicle.move(DetectorConstants.LV, Angle.Back);
        }
    }
}
