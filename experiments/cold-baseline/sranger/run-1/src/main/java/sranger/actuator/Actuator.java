package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * SR-DM6: stores the last Move(lv, av) command issued by the controller.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLv = 0.0;

    @RoboChartType("real")
    private double lastAv = 0.0;

    public void apply(OutputEvent event) {
        if (event instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) event;
            this.lastLv = move.lv();
            this.lastAv = move.av();
        }
    }

    @RoboChartType("real")
    public double lastLv() { return lastLv; }

    @RoboChartType("real")
    public double lastAv() { return lastAv; }
}
