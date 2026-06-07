package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives advised velocity and heading from the LRE controller via output
 * events and stores the latest values for the autopilot controller.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastAdvVel;
    @RoboChartType("real")
    private double lastAdvHdng;

    /** Applies an output event, recording its advised value. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.advVel) {
            OutputEvent.advVel av = (OutputEvent.advVel) command;
            this.lastAdvVel = av.value();
        } else if (command instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng ah = (OutputEvent.advHdng) command;
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
