package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.RoboChartWait;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;

/**
 * The sensing-and-actuation surface (CD-ARCH2). Provides the movement
 * operations the movement subsystem calls (CD-OP1..4) and the odometer
 * reading (CD-Evt3, modelled as a sensor read).
 */
public final class Vehicle {

    /** move(lv, a) — travel with linear velocity lv in direction a (CD-OP1). */
    public void move(@RoboChartType("real") double lv, Angle a) {
    }

    /** randomWalk() — unbounded random-walk search (CD-OP2). */
    public void randomWalk() {
    }

    /** shortRandomWalk() — bounded recovery random-walk (CD-OP3). */
    public void shortRandomWalk() {
    }

    /** changeDirection(l) — steer away from an obstacle on side l (CD-OP4). */
    public void changeDirection(Loc l) {
    }

    /** odometer — cumulative distance travelled (CD-Evt3). */
    @RoboChartType("real")
    public double odometer() {
        return 0.0;
    }

    /** wait(duration) — bounded delay (timing primitive). */
    @RoboChartWait
    public void pause(@RoboChartType("nat") int duration) {
    }
}
