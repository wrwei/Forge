package chemical_detector.operation;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;

/**
 * The Vehicle's move operation (CD-OP1): holds the velocity and direction
 * command the Vehicle executes until a later move supersedes it. Zero velocity
 * facing Front halts the Vehicle.
 */
public final class Move {

    @RoboChartType("real") private double commandedVelocity = 0.0;
    private Angle commandedAngle = Angle.Front;

    /** Commands motion at {@code speed} in direction {@code heading}. */
    public void compute(@RoboChartType("real") double speed, Angle heading) {
        this.commandedVelocity = speed;
        this.commandedAngle = heading;
    }

    /** The commanded linear velocity. */
    @RoboChartType("real")
    public double commandedVelocity() {
        return commandedVelocity;
    }

    /** The commanded direction. */
    public Angle commandedAngle() {
        return commandedAngle;
    }
}
