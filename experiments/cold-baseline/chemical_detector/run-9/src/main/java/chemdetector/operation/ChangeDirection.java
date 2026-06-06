package chemdetector.operation;

import chemdetector.actuator.Vehicle;
import chemdetector.constants.Constants;
import chemdetector.datatype.Angle;
import chemdetector.datatype.Loc;

/**
 * CD-OP4 changeDirection. Steers the Vehicle away from the obstacle side:
 * left -> move Right, right -> move Left, front -> move Back.
 */
public final class ChangeDirection {

    private final Vehicle vehicle;
    private Loc input = Loc.front;

    public ChangeDirection(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void setInput(Loc l) {
        this.input = l;
    }

    public void compute() {
        if (this.input == Loc.left) {
            this.vehicle.move(Constants.lv, Angle.Right);
        } else if (this.input == Loc.right) {
            this.vehicle.move(Constants.lv, Angle.Left);
        } else if (this.input == Loc.front) {
            this.vehicle.move(Constants.lv, Angle.Back);
        }
    }
}
