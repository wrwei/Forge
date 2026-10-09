package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Actuator interface for the SRanger controller (SR-DM6).
 *
 * <p>Stores the last issued {@link OutputEvent.Move} command and exposes its linear
 * and angular components for inspection.</p>
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLv = 0.0;

    @RoboChartType("real")
    private double lastAv = 0.0;

    /**
     * Receive a Move command from the controller and store its lv / av.
     */
    public void apply(OutputEvent.Move move) {
        this.lastLv = move.lv();
        this.lastAv = move.av();
    }

    /** Last commanded linear velocity. */
    @RoboChartType("real")
    public double lastLv() {
        return lastLv;
    }

    /** Last commanded angular velocity. */
    @RoboChartType("real")
    public double lastAv() {
        return lastAv;
    }
}
