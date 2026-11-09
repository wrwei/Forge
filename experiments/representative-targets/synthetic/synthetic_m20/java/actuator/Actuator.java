package syn20.actuator;

import syn20.annotation.RoboChartType;
import syn20.event.OutputEvent;

public final class Actuator {

    @RoboChartType("real")
    private double lastLv = 0.0;

    @RoboChartType("real")
    private double lastAv = 0.0;

    public void apply(OutputEvent.Move move) {
        this.lastLv = move.lv();
        this.lastAv = move.av();
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
