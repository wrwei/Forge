package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the advised velocity and heading from the LRE controller and holds
 * the latest value of each for the autopilot controller (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real")
    private double advisedVel;

    @RoboChartType("real")
    private double advisedHdng;

    /** Applies one output event, updating the advice it carries. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.advVel) {
            OutputEvent.advVel av = (OutputEvent.advVel) output;
            this.advisedVel = av.value();
        } else if (output instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng ah = (OutputEvent.advHdng) output;
            this.advisedHdng = ah.value();
        }
    }

    /** The most recently advised velocity, m/s. */
    @RoboChartType("real")
    public double advisedVel() {
        return advisedVel;
    }

    /** The most recently advised heading, degrees. */
    @RoboChartType("real")
    public double advisedHdng() {
        return advisedHdng;
    }
}
