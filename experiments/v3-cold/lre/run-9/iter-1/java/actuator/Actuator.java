package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's output events and keeps the latest advised velocity and
 * heading for the autopilot controller (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real") private double advisedVelocity = 0.0;
    @RoboChartType("real") private double advisedHeading = 0.0;

    /** Receives one output event from the controller. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel velocity = (OutputEvent.AdvVel) output;
            this.advisedVelocity = velocity.value();
        } else if (output instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng heading = (OutputEvent.AdvHdng) output;
            this.advisedHeading = heading.value();
        }
    }

    /** The latest advised velocity, in m/s (0 before any advice). */
    @RoboChartType("real")
    public double advisedVelocity() {
        return advisedVelocity;
    }

    /** The latest advised heading, in degrees (0 before any advice). */
    @RoboChartType("real")
    public double advisedHeading() {
        return advisedHeading;
    }
}
