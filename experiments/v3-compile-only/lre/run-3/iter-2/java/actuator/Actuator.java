package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * The actuator interface to the autopilot controller (LRE-DM8): receives the
 * LRE's output events and keeps the latest advised velocity and heading.
 */
public final class Actuator {

    @RoboChartType("real") private double lastAdvVel = 0.0;
    @RoboChartType("real") private double lastAdvHdng = 0.0;

    /** Receives one output event from the LRE controller. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel advice = (OutputEvent.AdvVel) output;
            this.lastAdvVel = advice.value();
        } else if (output instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng advice = (OutputEvent.AdvHdng) output;
            this.lastAdvHdng = advice.value();
        }
    }

    /** The latest advised velocity, m/s. */
    @RoboChartType("real")
    public double lastAdvVel() {
        return lastAdvVel;
    }

    /** The latest advised heading, degrees. */
    @RoboChartType("real")
    public double lastAdvHdng() {
        return lastAdvHdng;
    }
}
