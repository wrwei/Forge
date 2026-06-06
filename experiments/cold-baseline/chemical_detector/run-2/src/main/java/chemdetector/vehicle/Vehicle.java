package chemdetector.vehicle;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.RoboChartWait;
import chemdetector.constants.Constants;
import chemdetector.data.Angle;
import chemdetector.data.Loc;

/**
 * The Vehicle is the sensing-and-actuation surface of the Chemical
 * Detector (CD-ARCH2). It exposes the movement operations move,
 * randomWalk, shortRandomWalk and changeDirection (CD-OP1..4) and
 * keeps a record of the last command issued for inspection and
 * testing.
 *
 * <p>The Vehicle is invoked by the movement subsystem and is not
 * itself a controller — there is no step() method here.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastLv;

    private Angle lastAngle = Angle.Front;

    private String lastCommand = "idle";

    private boolean flagged = false;

    /** CD-OP1: move with linear velocity {@code lv} in direction {@code a}. */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.lastLv = lv;
        this.lastAngle = a;
        this.lastCommand = "move";
    }

    /** CD-OP2: unbounded random-walk search. */
    public void randomWalk() {
        this.lastCommand = "randomWalk";
    }

    /** CD-OP3: bounded recovery random walk. */
    public void shortRandomWalk() {
        this.lastCommand = "shortRandomWalk";
    }

    /**
     * CD-OP4: turn away from an obstacle reported on side {@code l}.
     * Specification is encoded as a pure if-else chain so the
     * RoboChart operation body extracted by the ETL matches the
     * requirement verbatim.
     */
    public void changeDirection(Loc l) {
        if (l == Loc.left) {
            move(Constants.lv, Angle.Right);
        } else if (l == Loc.right) {
            move(Constants.lv, Angle.Left);
        } else if (l == Loc.front) {
            move(Constants.lv, Angle.Back);
        }
    }

    /**
     * Bounded delay used by the movement subsystem (CD-MV-FR4 in the
     * Avoiding entry, CD-MV-FR7 in the GettingOut entry). The
     * RoboChart wait primitive is matched by {@link RoboChartWait}.
     */
    @RoboChartWait
    public void pause(@RoboChartType("nat") int ticks) {
        // Bounded delay; no-op in this baseline.
    }

    /** Records receipt of the flag signal (CD-Evt7). */
    public void raiseFlag() {
        this.flagged = true;
    }

    public boolean isFlagged() {
        return flagged;
    }

    @RoboChartType("real")
    public double lastLv() {
        return lastLv;
    }

    public Angle lastAngle() {
        return lastAngle;
    }

    public String lastCommand() {
        return lastCommand;
    }
}
