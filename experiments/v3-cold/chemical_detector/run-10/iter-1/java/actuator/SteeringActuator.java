package chemical_detector.actuator;

import chemical_detector.controller.DetectorConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;

/** The movement subsystem's changeDirection operation (CD-OP4). */
public final class SteeringActuator {

    private final VehicleActuator vehicle;

    /** Creates the operation over the Vehicle it steers. */
    public SteeringActuator(VehicleActuator vehicle) {
        this.vehicle = vehicle;
    }

    /**
     * CD-OP4: steer away from an obstacle on the given side at the linear
     * velocity: left turns Right, right turns Left, front turns Back.
     */
    public void changeDirection(Loc avoidSide) {
        if (avoidSide == Loc.left) {
            vehicle.move(DetectorConstants.LV, Angle.Right);
        } else if (avoidSide == Loc.right) {
            vehicle.move(DetectorConstants.LV, Angle.Left);
        } else {
            vehicle.move(DetectorConstants.LV, Angle.Back);
        }
    }
}
