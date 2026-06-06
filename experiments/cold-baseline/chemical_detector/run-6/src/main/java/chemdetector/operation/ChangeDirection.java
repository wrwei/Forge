package chemdetector.operation;

import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.domain.Angle;
import chemdetector.domain.Loc;
import chemdetector.vehicle.Vehicle;

/**
 * CD-OP4 — changeDirection(l : Loc).
 *
 * <ul>
 *   <li>{@code l == left}  → move(lv, Right)</li>
 *   <li>{@code l == right} → move(lv, Left)</li>
 *   <li>{@code l == front} → move(lv, Back)</li>
 * </ul>
 */
public final class ChangeDirection {

    private final Vehicle vehicle;
    private Loc l = Loc.front;
    @RoboChartType("real")
    private double lv = Constants.lv;
    private Angle chosen = Angle.Front;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    /**
     * Set the operand for the next compute() call.
     */
    public void setL(Loc l) {
        this.l = l;
    }

    public void compute() {
        this.lv = Constants.lv;
        this.chosen = Angle.Front;
        if (this.l == Loc.left) {
            this.chosen = Angle.Right;
        }
        if (this.l == Loc.right) {
            this.chosen = Angle.Left;
        }
        if (this.l == Loc.front) {
            this.chosen = Angle.Back;
        }
        this.vehicle.move(this.lv, this.chosen);
    }

    public Angle chosen() {
        return chosen;
    }
}
