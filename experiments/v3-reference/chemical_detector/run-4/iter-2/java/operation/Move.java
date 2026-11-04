package chemical_detector.operation;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;

/**
 * The Vehicle's move operation (CD-OP1): move at a linear velocity in a body-relative
 * direction until superseded; zero velocity facing Front halts. Records the command.
 */
public final class Move {

    @RoboChartType("real") private double velocity = 0.0;
    private Angle direction = Angle.Front;

    public Move() {
    }

    /** Commands motion at {@code speed} towards {@code heading}. */
    public void compute(@RoboChartType("real") double speed, Angle heading) {
        this.velocity = speed;
        this.direction = heading;
    }

    /** The commanded linear velocity. */
    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    /** The commanded direction. */
    public Angle direction() {
        return direction;
    }
}
