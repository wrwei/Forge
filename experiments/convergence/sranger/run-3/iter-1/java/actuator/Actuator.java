package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Receives the combined linear/angular Move command from the controller and
 * stores the latest issued values for inspection (SR-DM6). The controller
 * issues Move via {@link #apply(OutputEvent)}; the same event expresses
 * forward motion, turning in place, and stopping.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLv;

    @RoboChartType("real")
    private double lastAv;

    /** Apply the latest output command from the controller. */
    public void apply(OutputEvent event) {
        if (event instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) event;
            this.lastLv = move.lv();
            this.lastAv = move.av();
        }
    }

    @RoboChartType("real")
    public double lastLv() {
        return lastLv;
    }

    @RoboChartType("real")
    public double lastAv() {
        return lastAv;
    }
}
