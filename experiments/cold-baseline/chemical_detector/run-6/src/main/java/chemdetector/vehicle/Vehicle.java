package chemdetector.vehicle;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.RoboChartWait;
import chemdetector.domain.Angle;
import chemdetector.event.VehicleOutputEvent;

/**
 * Vehicle actuator surface. Receives movement commands from the controller
 * and the {@code flag} event when the chemical source has been confirmed.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastVelocity = 0.0;
    private Angle lastAngle = Angle.Front;
    private boolean flagged = false;
    private boolean randomWalkActive = false;
    private boolean shortRandomWalkActive = false;

    /**
     * CD-OP1 — move with linear velocity lv in direction a.
     */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.lastVelocity = lv;
        this.lastAngle = a;
        this.randomWalkActive = false;
        this.shortRandomWalkActive = false;
    }

    /**
     * CD-OP2 — unbounded random-walk search.
     */
    public void randomWalk() {
        this.randomWalkActive = true;
        this.shortRandomWalkActive = false;
    }

    /**
     * CD-OP3 — bounded random-walk for stuck-recovery.
     */
    public void shortRandomWalk() {
        this.shortRandomWalkActive = true;
        this.randomWalkActive = false;
    }

    /**
     * RoboChart {@code wait(n)} primitive. The method body is intentionally
     * empty; the duration is recorded for inspection.
     */
    @RoboChartWait
    public void pause(@RoboChartType("nat") int duration) {
        // semantically: hold the current behaviour for `duration` time units.
        // The pipeline transforms calls to this method into RoboChart `wait`.
    }

    /**
     * Receives the CD-Evt7 flag event.
     */
    public void apply(VehicleOutputEvent event) {
        if (event instanceof VehicleOutputEvent.Flag) {
            this.flagged = true;
        }
    }

    @RoboChartType("real")
    public double lastVelocity() {
        return lastVelocity;
    }

    public Angle lastAngle() {
        return lastAngle;
    }

    public boolean flagged() {
        return flagged;
    }

    public boolean randomWalkActive() {
        return randomWalkActive;
    }

    public boolean shortRandomWalkActive() {
        return shortRandomWalkActive;
    }
}
