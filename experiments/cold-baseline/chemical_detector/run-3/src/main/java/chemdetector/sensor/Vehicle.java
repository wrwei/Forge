package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.RoboChartWait;
import chemdetector.annotation.SensorService;
import chemdetector.constants.Constants;
import chemdetector.data.Angle;
import chemdetector.data.Loc;

/**
 * The Vehicle is the sensing-and-actuation surface of the
 * Chemical Detector (CD-ARCH1, CD-ARCH2). It exposes:
 *  - odometer reading (CD-Evt3) consumed via odometer()
 *  - movement operations move / randomWalk / shortRandomWalk
 *    (CD-OP1, CD-OP2, CD-OP3)
 *  - the flag event sink (CD-Evt7) reached by sendFlag()
 *  - a pause primitive (mapped to RoboChart wait via
 *    @RoboChartWait) used for evade and out periods
 */
@SensorService
public final class Vehicle {

    @RoboChartType("real")
    private double odometer;

    @RoboChartType("real")
    private double lastMoveLv;

    private Angle lastMoveAngle = Angle.Front;

    private boolean flagSignalled = false;

    private boolean randomWalking = false;

    private boolean shortRandomWalking = false;

    public Vehicle() {
        this.odometer = 0.0;
    }

    /** Update the platform's cumulative distance reading. */
    public void setOdometer(@RoboChartType("real") double value) {
        this.odometer = value;
    }

    /** CD-Evt3: current cumulative distance travelled. */
    @RoboChartType("real")
    public double odometer() {
        return odometer;
    }

    /**
     * CD-OP1: move with linear velocity lv in direction a.
     * Calling move(0, Front) halts the platform.
     */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.lastMoveLv = lv;
        this.lastMoveAngle = a;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** CD-OP2: unbounded random-walk search. */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** CD-OP3: bounded random-walk recovery manoeuvre. */
    public void shortRandomWalk() {
        this.shortRandomWalking = true;
        this.randomWalking = false;
    }

    /**
     * CD-OP4: changeDirection(l). Spec:
     *   l == Loc::left  -> move(lv, Angle::Right)
     *   l == Loc::right -> move(lv, Angle::Left)
     *   l == Loc::front -> move(lv, Angle::Back)
     */
    public void changeDirection(Loc l) {
        if (l == Loc.left) {
            this.move(Constants.LV, Angle.Right);
        } else if (l == Loc.right) {
            this.move(Constants.LV, Angle.Left);
        } else {
            this.move(Constants.LV, Angle.Back);
        }
    }

    /** CD-Evt7: emit flag — chemical source has been located. */
    public void sendFlag() {
        this.flagSignalled = true;
    }

    /** Bounded wait — the ETL maps this to RoboChart wait(N). */
    @RoboChartWait
    public void pause(@RoboChartType("nat") int durationMs) {
        // model-level no-op: maps to RoboChart wait(durationMs)
    }

    @RoboChartType("real")
    public double lastMoveLv() {
        return lastMoveLv;
    }

    public Angle lastMoveAngle() {
        return lastMoveAngle;
    }

    public boolean flagSignalled() {
        return flagSignalled;
    }

    public boolean randomWalking() {
        return randomWalking;
    }

    public boolean shortRandomWalking() {
        return shortRandomWalking;
    }

    /**
     * Convenience for test/demo: simulate an obstacle detection.
     * Loc is the side at which the obstacle was encountered.
     */
    public Loc dummyObstacle() {
        return Loc.front;
    }
}
