package chemdetector.operation;

import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.DetectorConstants;
import chemdetector.datatype.Angle;
import chemdetector.datatype.Loc;

/**
 * CD-OP4: changeDirection(l : Loc).
 *
 * Specified behaviour:
 *   - if l == Loc::left  then move(lv, Angle::Right)
 *   - if l == Loc::right then move(lv, Angle::Left)
 *   - if l == Loc::front then move(lv, Angle::Back)
 *
 * This is invoked from the Avoiding-state entry action of the movement controller.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void compute(Loc l) {
        if (l == Loc.left) {
            vehicle.move(DetectorConstants.lv, Angle.Right);
        } else if (l == Loc.right) {
            vehicle.move(DetectorConstants.lv, Angle.Left);
        } else if (l == Loc.front) {
            vehicle.move(DetectorConstants.lv, Angle.Back);
        }
    }

    @RoboChartType("real")
    public double lv() {
        return DetectorConstants.lv;
    }
}
