package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * SR-DM6: Receives Move(lv, av) events and stores the last issued command.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLv;

    @RoboChartType("real")
    private double lastAv;

    public Actuator() {
        this.lastLv = 0.0;
        this.lastAv = 0.0;
    }

    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) output;
            this.lastLv = move.lv();
            this.lastAv = move.av();
        }
    }

    @RoboChartType("real")
    public double lastLv() {
        return this.lastLv;
    }

    @RoboChartType("real")
    public double lastAv() {
        return this.lastAv;
    }
}
