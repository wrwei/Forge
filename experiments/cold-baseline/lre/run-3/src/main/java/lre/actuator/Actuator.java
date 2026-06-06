package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * LRE-DM8: Receives output events (advVel, advHdng) from the LRE
 * and stores the latest advised values for the autopilot.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastAdvVel;

    @RoboChartType("real")
    private double lastAdvHdng;

    public Actuator() {
        this.lastAdvVel = 0.0;
        this.lastAdvHdng = 0.0;
    }

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
    public double lastAdvVel() { return lastAdvVel; }

    @RoboChartType("real")
    public double lastAdvHdng() { return lastAdvHdng; }
}
