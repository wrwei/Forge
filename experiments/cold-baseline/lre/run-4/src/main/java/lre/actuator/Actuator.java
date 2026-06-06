package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Actuator that receives advised velocity and heading from the LRE controller.
 * See LRE-DM8.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastAdvisedVelocity;

    @RoboChartType("real")
    private double lastAdvisedHeading;

    public Actuator() {
        this.lastAdvisedVelocity = 0.0;
        this.lastAdvisedHeading = 0.0;
    }

    public void receive(OutputEvent event) {
        if (event instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel av = (OutputEvent.AdvVel) event;
            this.lastAdvisedVelocity = av.value();
        } else if (event instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng ah = (OutputEvent.AdvHdng) event;
            this.lastAdvisedHeading = ah.value();
        }
    }

    @RoboChartType("real")
    public double lastAdvisedVelocity() { return lastAdvisedVelocity; }

    @RoboChartType("real")
    public double lastAdvisedHeading() { return lastAdvisedHeading; }
}
