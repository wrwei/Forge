package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.RoboChartWait;
import chemdetector.domain.Angle;
import chemdetector.domain.Loc;
import chemdetector.event.OutputEvent;

/**
 * Vehicle actuator + inter-controller signalling surface.
 *
 * Boundary outputs:
 *   - move(lv, a)            (CD-OP1)
 *   - randomWalk()           (CD-OP2)
 *   - shortRandomWalk()      (CD-OP3)
 *   - changeDirection(l)     (CD-OP4)
 *   - flag                   (CD-Evt7)
 *
 * Inter-controller outputs (forwarded via {@link #send(OutputEvent)}):
 *   - turn   (CD-Evt4)
 *   - stop   (CD-Evt5)
 *   - resume (CD-Evt6)
 */
public final class Actuator {

    private OutputEvent lastEvent;

    @RoboChartType("real")
    private double lastMoveVelocity;

    private Angle lastMoveAngle = Angle.Front;
    private boolean flagRaised;

    public void apply(OutputEvent event) {
        this.lastEvent = event;
    }

    public void send(OutputEvent event) {
        this.lastEvent = event;
    }

    public OutputEvent lastEvent() {
        return lastEvent;
    }

    /**
     * CD-OP1 — set linear velocity {@code lv} in direction {@code a}.
     * Subsequent calls override the previous command; calling
     * {@code move(0, Angle.Front)} halts the platform.
     */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.lastMoveVelocity = lv;
        this.lastMoveAngle = a;
    }

    /**
     * CD-OP2 — unbounded random-walk search.
     */
    public void randomWalk() {
        // boundary effect: random-walk continues until next command
    }

    /**
     * CD-OP3 — bounded recovery random-walk.
     */
    public void shortRandomWalk() {
        // boundary effect: short random-walk runs for outPeriod time
    }

    /**
     * CD-Evt7 — chemical source confirmed.
     */
    public void flag() {
        this.flagRaised = true;
        this.lastEvent = new OutputEvent.Flag();
    }

    @RoboChartType("real")
    public double lastMoveVelocity() {
        return lastMoveVelocity;
    }

    public Angle lastMoveAngle() {
        return lastMoveAngle;
    }

    public boolean flagRaised() {
        return flagRaised;
    }

    /**
     * RoboChart wait primitive — annotated so the ETL recognises it
     * regardless of the Java name.
     */
    @RoboChartWait
    public void pause(@RoboChartType("nat") int duration) {
        // boundary effect: block for the given RoboChart-time units
    }
}
