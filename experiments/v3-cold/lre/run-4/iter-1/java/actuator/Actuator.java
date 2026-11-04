package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advice for the autopilot controller and keeps the latest
 * advised velocity and heading (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real") private double lastVelocity = 0.0;
    @RoboChartType("real") private double lastHeading = 0.0;

    /** Receives one advice event. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel velocityAdvice = (OutputEvent.AdvVel) output;
            this.lastVelocity = velocityAdvice.value();
        } else if (output instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng headingAdvice = (OutputEvent.AdvHdng) output;
            this.lastHeading = headingAdvice.value();
        }
    }

    /** The latest advised velocity, m/s. */
    @RoboChartType("real")
    public double lastVelocity() {
        return lastVelocity;
    }

    /** The latest advised heading, degrees. */
    @RoboChartType("real")
    public double lastHeading() {
        return lastHeading;
    }
}
