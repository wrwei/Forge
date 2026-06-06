package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Actuator for the SRanger controller. Receives Move(lv, av) events
 * from the controller and stores the latest issued command.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLv = 0.0;

    @RoboChartType("real")
    private double lastAv = 0.0;

    /** Apply a Move command — stores the linear / angular velocities. */
    public void apply(OutputEvent.Move move) {
        this.lastLv = move.lv();
        this.lastAv = move.av();
    }

    /** Last commanded linear velocity (m/s). */
    @RoboChartType("real")
    public double lastLv() {
        return lastLv;
    }

    /** Last commanded angular velocity (rad/s). */
    @RoboChartType("real")
    public double lastAv() {
        return lastAv;
    }
}
