package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemicalDetectorConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.Loc;

/**
 * The sensing-and-actuation surface of the robot (CD-ARCH1, CD-ARCH2): it
 * accepts movement commands, reports the cumulative distance travelled, and
 * receives the success flag.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastVelocity;

    private Angle lastAngle = Angle.Front;

    private Loc lastAvoidedSide = Loc.front;

    private boolean randomWalking;

    private boolean flagged;

    @RoboChartType("real")
    private double travelled;

    /** Move at {@code velocity} in body-relative direction {@code a} (CD-OP1). */
    public void move(@RoboChartType("real") double velocity, Angle a) {
        this.lastVelocity = velocity;
        this.lastAngle = a;
        this.randomWalking = false;
    }

    /** Unbounded random-walk search (CD-OP2). */
    public void randomWalk() {
        this.randomWalking = true;
    }

    /** Bounded random-walk recovery manoeuvre (CD-OP3). */
    public void shortRandomWalk() {
        this.randomWalking = true;
    }

    /** Steer away from an obstacle detected on side {@code side} (CD-OP4). */
    public void changeDirection(Loc side) {
        this.lastAvoidedSide = side;
        if (side == Loc.left) {
            this.move(ChemicalDetectorConstants.LV, Angle.Right);
        } else if (side == Loc.right) {
            this.move(ChemicalDetectorConstants.LV, Angle.Left);
        } else if (side == Loc.front) {
            this.move(ChemicalDetectorConstants.LV, Angle.Back);
        }
    }

    /** Let {@code duration} time units pass before the next behaviour fires. */
    public void pause(@RoboChartType("nat") int duration) {
        this.travelled = this.travelled + duration * this.lastVelocity;
    }

    /** Signal that the chemical source has been located (CD-Evt7). */
    public void flag() {
        this.flagged = true;
    }

    /** Cumulative distance travelled, as reported by the odometer (CD-Evt3). */
    @RoboChartType("real")
    public double odometer() {
        return this.travelled;
    }

    /** Test/simulation hook: advance the odometer by {@code distance}. */
    void advance(@RoboChartType("real") double distance) {
        this.travelled = this.travelled + distance;
    }
}
