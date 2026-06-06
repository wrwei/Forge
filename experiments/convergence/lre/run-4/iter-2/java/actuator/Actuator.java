package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Actuator interface: receives advised velocity and heading output events
 * from the LRE and stores the latest values for the autopilot controller
 * (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastAdvVel;

    @RoboChartType("real")
    private double lastAdvHdng;

    /** Applies an output event, recording the advised value. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel av = (OutputEvent.AdvVel) command;
            this.lastAdvVel = av.value();
        } else if (command instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng ah = (OutputEvent.AdvHdng) command;
            this.lastAdvHdng = ah.value();
        }
    }

    /** Last advised velocity, m/s. */
    public double lastAdvVel() {
        return lastAdvVel;
    }

    /** Last advised heading, degrees. */
    public double lastAdvHdng() {
        return lastAdvHdng;
    }
}
