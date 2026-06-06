package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Actuator for the SRanger differential-drive layer.
 * Stores the last issued Move(lv, av) command for inspection.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLv = 0.0;

    @RoboChartType("real")
    private double lastAv = 0.0;

    public void apply(OutputEvent event) {
        if (event instanceof OutputEvent.Move) {
            OutputEvent.Move m = (OutputEvent.Move) event;
            this.lastLv = m.lv();
            this.lastAv = m.av();
        }
    }

    @RoboChartType("real")
    public double lastLv() { return lastLv; }

    @RoboChartType("real")
    public double lastAv() { return lastAv; }
}
