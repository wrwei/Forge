package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives advised velocity and heading from the LRE controller via
 * output events and stores the latest values for the autopilot
 * controller (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastAdvVel;
    @RoboChartType("real")
    private double lastAdvHdng;

    /** Applies an output event, recording the advised value. */
    public void apply(OutputEvent outputEvent) {
        if (outputEvent instanceof OutputEvent.advVel) {
            OutputEvent.advVel advised = (OutputEvent.advVel) outputEvent;
            this.lastAdvVel = advised.value();
        } else if (outputEvent instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng advised = (OutputEvent.advHdng) outputEvent;
            this.lastAdvHdng = advised.value();
        }
    }

    /** Latest advised velocity, m/s. */
    public double lastAdvVel() {
        return lastAdvVel;
    }

    /** Latest advised heading, degrees. */
    public double lastAdvHdng() {
        return lastAdvHdng;
    }
}
