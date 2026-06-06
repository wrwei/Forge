package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Actuator (LRE-DM8): receives advised velocity and heading from the
 * LRE controller via OutputEvent values and stores the latest values
 * for the autopilot controller.
 */
public final class Actuator {

    @RoboChartType("real")
    private double advisedVel;

    @RoboChartType("real")
    private double advisedHdng;

    public void receive(OutputEvent event) {
        if (event instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel av = (OutputEvent.AdvVel) event;
            this.advisedVel = av.value();
        } else if (event instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng ah = (OutputEvent.AdvHdng) event;
            this.advisedHdng = ah.value();
        }
    }

    @RoboChartType("real")
    public double advisedVel() { return advisedVel; }

    @RoboChartType("real")
    public double advisedHdng() { return advisedHdng; }
}
