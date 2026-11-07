package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advice to the autopilot controller and keeps the latest
 * advised velocity and heading (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real") private double latestVelocity = 0.0;
    @RoboChartType("real") private double latestHeading = 0.0;

    /** Delivers one output event to the autopilot controller. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel velocityAdvice = (OutputEvent.AdvVel) output;
            this.latestVelocity = velocityAdvice.value();
        } else if (output instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng headingAdvice = (OutputEvent.AdvHdng) output;
            this.latestHeading = headingAdvice.value();
        }
    }

    /** The latest advised velocity in m/s (0 before any advice). */
    @RoboChartType("real")
    public double latestVelocity() {
        return latestVelocity;
    }

    /** The latest advised heading in degrees (0 before any advice). */
    @RoboChartType("real")
    public double latestHeading() {
        return latestHeading;
    }
}
