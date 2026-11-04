package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's output events and keeps the latest advised values for
 * the autopilot controller (LRE-DM8). Both values are zero until first advised.
 */
public final class Actuator {

    @RoboChartType("real") private double advisedVelocity = 0.0;
    @RoboChartType("real") private double advisedHeading = 0.0;

    /** Receives one output event of the LRE. */
    public void apply(OutputEvent event) {
        if (event instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel velocity = (OutputEvent.AdvVel) event;
            this.advisedVelocity = velocity.value();
        } else if (event instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng heading = (OutputEvent.AdvHdng) event;
            this.advisedHeading = heading.value();
        }
    }

    /** Latest advised velocity, in m/s. */
    @RoboChartType("real")
    public double advisedVelocity() {
        return advisedVelocity;
    }

    /** Latest advised heading, in degrees. */
    @RoboChartType("real")
    public double advisedHeading() {
        return advisedHeading;
    }
}
