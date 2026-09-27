package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives advised velocity and heading from the LRE and stores the
 * latest values for the autopilot controller (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real")
    private double advisedVel;

    @RoboChartType("real")
    private double advisedHdng;

    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.advVel) {
            OutputEvent.advVel velCommand = (OutputEvent.advVel) output;
            this.advisedVel = velCommand.value();
        } else if (output instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng hdngCommand = (OutputEvent.advHdng) output;
            this.advisedHdng = hdngCommand.value();
        }
    }

    @RoboChartType("real")
    public double advisedVel() {
        return advisedVel;
    }

    @RoboChartType("real")
    public double advisedHdng() {
        return advisedHdng;
    }
}
