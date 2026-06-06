package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives advised velocity and heading output events from the LRE and
 * stores the latest values for the autopilot controller (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastAdvVel;

    @RoboChartType("real")
    private double lastAdvHdng;

    /** Apply an output event, recording the advised value it carries. */
    public void apply(OutputEvent event) {
        if (event instanceof OutputEvent.advVel) {
            OutputEvent.advVel av = (OutputEvent.advVel) event;
            this.lastAdvVel = av.value();
        } else if (event instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng ah = (OutputEvent.advHdng) event;
            this.lastAdvHdng = ah.value();
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
