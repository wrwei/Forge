package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives advised velocity and heading from the LRE controller via output
 * events and stores the latest values for the autopilot controller (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastAdvVel;
    @RoboChartType("real")
    private double lastAdvHdng;

    /** Applies an output event, storing its advised value. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.advVel) {
            OutputEvent.advVel av = (OutputEvent.advVel) output;
            this.lastAdvVel = av.value();
        } else if (output instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng ah = (OutputEvent.advHdng) output;
            this.lastAdvHdng = ah.value();
        }
    }

    @RoboChartType("real")
    public double lastAdvVel() {
        return lastAdvVel;
    }

    @RoboChartType("real")
    public double lastAdvHdng() {
        return lastAdvHdng;
    }
}
