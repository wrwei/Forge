package chemdetector.operation;

import chemdetector.constants.Constants;
import chemdetector.datatype.Angle;
import chemdetector.datatype.Loc;
import chemdetector.vehicle.Vehicle;

/**
 * CD-OP4. changeDirection(l : Loc) — turn the Vehicle away from an obstacle.
 *
 * <p>Specified mapping: left -> Right, right -> Left, front -> Back.
 *
 * <p>The {@link #compute()} method follows the strict "direct
 * this.field = expression" form required by the codegen rules.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;
    private Loc l;
    private Angle chosen;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.l = Loc.front;
        this.chosen = Angle.Back;
    }

    public void setL(Loc l) {
        this.l = l;
    }

    public void compute() {
        // CD-OP4 mapping expressed as a single nested-conditional expression.
        if (this.l == Loc.left) {
            this.chosen = Angle.Right;
        } else if (this.l == Loc.right) {
            this.chosen = Angle.Left;
        } else {
            this.chosen = Angle.Back;
        }
        this.vehicle.move(Constants.lv, this.chosen);
    }

    public Angle chosen() {
        return chosen;
    }
}
