package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's output events and keeps the latest advised velocity and
 * heading for the autopilot controller (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real") private double lastAdvisedVelocity = 0.0;
    @RoboChartType("real") private double lastAdvisedHeading = 0.0;

    /** Delivers one output event to the autopilot side. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel velocityAdvice = (OutputEvent.AdvVel) output;
            this.lastAdvisedVelocity = velocityAdvice.advisedVelocity();
        } else if (output instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng headingAdvice = (OutputEvent.AdvHdng) output;
            this.lastAdvisedHeading = headingAdvice.advisedHeading();
        }
    }

    /** The latest advised velocity (m/s). */
    @RoboChartType("real")
    public double lastAdvisedVelocity() {
        return lastAdvisedVelocity;
    }

    /** The latest advised heading (degrees). */
    @RoboChartType("real")
    public double lastAdvisedHeading() {
        return lastAdvisedHeading;
    }
}
