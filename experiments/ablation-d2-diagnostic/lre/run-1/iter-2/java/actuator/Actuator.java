package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the advised velocity and heading the LRE issues to the autopilot
 * controller and keeps the latest value of each (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastAdvVel;

    @RoboChartType("real")
    private double lastAdvHdng;

    /** Records an output event issued by the controller. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.advVel) {
            OutputEvent.advVel advisedVel = (OutputEvent.advVel) output;
            this.lastAdvVel = advisedVel.value();
        } else if (output instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng advisedHdng = (OutputEvent.advHdng) output;
            this.lastAdvHdng = advisedHdng.value();
        }
    }

    /** Last advised velocity, in m/s. */
    @RoboChartType("real")
    public double lastAdvVel() {
        return lastAdvVel;
    }

    /** Last advised heading, in degrees. */
    @RoboChartType("real")
    public double lastAdvHdng() {
        return lastAdvHdng;
    }
}
