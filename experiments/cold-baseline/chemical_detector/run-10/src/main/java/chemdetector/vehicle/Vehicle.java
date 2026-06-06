package chemdetector.vehicle;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.RoboChartWait;
import chemdetector.datatype.Angle;

/**
 * Vehicle abstraction (CD-ARCH2 subsystem 1). Exposes the movement
 * operations used by the controllers: move (CD-OP1), randomWalk (CD-OP2),
 * shortRandomWalk (CD-OP3). Also provides the {@link #pause(int)} wait
 * primitive consumed by Avoiding / GettingOut entry actions.
 *
 * <p>This is a pure facade: state is tracked for inspection only.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastLv;

    private Angle lastAngle;
    private boolean randomWalking;
    private boolean shortRandomWalking;
    private boolean flagRaised;

    public Vehicle() {
        this.lastLv = 0.0;
        this.lastAngle = Angle.Front;
        this.randomWalking = false;
        this.shortRandomWalking = false;
        this.flagRaised = false;
    }

    /** CD-OP1. move(lv, a). */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.lastLv = lv;
        this.lastAngle = a;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** CD-OP2. randomWalk(). */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** CD-OP3. shortRandomWalk(). */
    public void shortRandomWalk() {
        this.shortRandomWalking = true;
        this.randomWalking = false;
    }

    /**
     * Wait primitive. Annotated so the M2M rewrites calls to
     * {@code vehicle.pause(n)} into RoboChart {@code wait(n)}.
     */
    @RoboChartWait
    public void pause(@RoboChartType("nat") int durationTicks) {
        // No-op in the Java model; tick semantics live in the formal model.
    }

    public void raiseFlag() {
        this.flagRaised = true;
    }

    @RoboChartType("real")
    public double lastLv() {
        return lastLv;
    }

    public Angle lastAngle() {
        return lastAngle;
    }

    public boolean randomWalking() {
        return randomWalking;
    }

    public boolean shortRandomWalking() {
        return shortRandomWalking;
    }

    public boolean flagRaised() {
        return flagRaised;
    }
}
