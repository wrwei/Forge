package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives advised velocity and heading from the LRE controller via
 * output events and stores the latest values for the autopilot
 * controller.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastAdvVel;

    @RoboChartType("real")
    private double lastAdvHdng;

    /** Applies an output event, storing the advised value. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.advVel) {
            OutputEvent.advVel adv = (OutputEvent.advVel) output;
            this.lastAdvVel = adv.value();
        } else if (output instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng adv = (OutputEvent.advHdng) output;
            this.lastAdvHdng = adv.value();
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
