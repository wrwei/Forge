package chemdetector.operation;

import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.datatype.Angle;
import chemdetector.datatype.Loc;
import chemdetector.vehicle.Vehicle;

/**
 * changeDirection(l) operation: maps an obstacle Loc to the opposite
 * Angle and issues a move command at the configured linear velocity.
 *
 * Compute method assigns only to this.field; no intermediate locals.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;

    private Loc l = Loc.front;
    private Angle direction = Angle.Front;
    @RoboChartType("real")
    private double vel = Constants.lv;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void setL(Loc l) {
        this.l = l;
    }

    public void compute() {
        this.vel = Constants.lv;
        if (this.l == Loc.left) {
            this.direction = Angle.Right;
        } else if (this.l == Loc.right) {
            this.direction = Angle.Left;
        } else {
            this.direction = Angle.Back;
        }
        this.vehicle.move(this.vel, this.direction);
    }

    public Angle direction() { return direction; }
    public double vel() { return vel; }
}
