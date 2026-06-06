package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.data.Angle;
import chemdetector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The Vehicle — the sensing-and-actuation surface that provides movement
 * operations (move, randomWalk, shortRandomWalk) and consumes the flag
 * boundary event. Last-issued values are retained for inspection / testing.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastVelocity;
    private Angle lastDirection = Angle.Front;
    private String lastAction = "NONE";
    private final List<OutputEvent> emitted = new ArrayList<>();

    /** CD-OP1 — move(lv, a). */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.lastVelocity = lv;
        this.lastDirection = a;
        this.lastAction = "move";
    }

    /** CD-OP2 — randomWalk (unbounded). */
    public void randomWalk() {
        this.lastAction = "randomWalk";
    }

    /** CD-OP3 — shortRandomWalk (bounded). */
    public void shortRandomWalk() {
        this.lastAction = "shortRandomWalk";
    }

    /** Receives the flag boundary event (CD-Evt7). */
    public void emit(OutputEvent e) {
        emitted.add(e);
    }

    @RoboChartType("real")
    public double lastVelocity() {
        return lastVelocity;
    }

    public Angle lastDirection() {
        return lastDirection;
    }

    public String lastAction() {
        return lastAction;
    }

    public List<OutputEvent> emitted() {
        return emitted;
    }
}
