package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Actuator that receives advised velocity and heading from the LRE
 * (LRE-DM8) and stores the latest values for the autopilot.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastAdvVel;

    @RoboChartType("real")
    private double lastAdvHdng;

    public void receive(OutputEvent event) {
        if (event instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel av = (OutputEvent.AdvVel) event;
            this.lastAdvVel = av.value();
        } else if (event instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng ah = (OutputEvent.AdvHdng) event;
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
