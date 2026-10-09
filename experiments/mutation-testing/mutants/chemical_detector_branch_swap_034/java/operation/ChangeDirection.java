package chemdetector.operation;

import chemdetector.constants.ChemDetectorConstants;
import chemdetector.data.Angle;
import chemdetector.data.Loc;
import chemdetector.vehicle.Vehicle;

/**
 * CD-OP4: turn away from an obstacle hit on side {@code l}.
 * <ul>
 *   <li>l == left  &rarr; move(lv, Right)</li>
 *   <li>l == right &rarr; move(lv, Left)</li>
 *   <li>l == front &rarr; move(lv, Back)</li>
 * </ul>
 * The single public {@link #compute(Loc)} method delegates straight to the
 * Vehicle's {@code move} operation; no intermediate locals are used.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void compute(Loc l) {
        if (l == Loc.left) {
            this.vehicle.move(ChemDetectorConstants.lv, Angle.Right);
        } else if (l == Loc.front) {
            this.vehicle.move(ChemDetectorConstants.lv, Angle.Back);
        } else if (l == Loc.right) {
            this.vehicle.move(ChemDetectorConstants.lv, Angle.Left);
        }
    }
}
