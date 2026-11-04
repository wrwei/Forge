package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;

/** Odometer reading and obstacle-evasion heading for the movement subsystem. */
public final class NavigationSensor {

    @RoboChartType("real") private double distance = 0.0;

    /** Records the latest cumulative distance reported by the odometer. */
    public void updateOdometer(@RoboChartType("real") double distance) {
        this.distance = distance;
    }

    /** Cumulative distance travelled by the Vehicle (CD-Evt3). */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }

    /**
     * Heading that steers away from an obstacle on side {@code l} (CD-OP4):
     * left turns Right, right turns Left, front turns Back.
     */
    public Angle evasionAngle(Loc l) {
        if (l == Loc.left) {
            return Angle.Right;
        }
        if (l == Loc.right) {
            return Angle.Left;
        }
        return Angle.Back;
    }
}
