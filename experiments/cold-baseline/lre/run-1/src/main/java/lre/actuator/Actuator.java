package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Actuator (LRE-DM8). Receives advised velocity and heading from the LRE
 * via output events (advVel, advHdng) and stores the latest values for the autopilot.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastAdvVel;

    @RoboChartType("real")
    private double lastAdvHdng;

    public void receive(OutputEvent payload) {
        if (payload instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel av = (OutputEvent.AdvVel) payload;
            this.lastAdvVel = av.value();
        } else if (payload instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng ah = (OutputEvent.AdvHdng) payload;
            this.lastAdvHdng = ah.value();
        }
    }

    @RoboChartType("real")
    public double lastAdvVel() { return lastAdvVel; }

    @RoboChartType("real")
    public double lastAdvHdng() { return lastAdvHdng; }
}
