package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * The actuator (LRE-DM8): receives the LRE's output events and keeps the
 * latest advised velocity and heading for the autopilot controller.
 */
public final class Actuator {

    @RoboChartType("real") private double advisedVelocity;
    @RoboChartType("real") private double advisedHeading;

    /** Receives one output event from the LRE controller. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.advVel) {
            OutputEvent.advVel velocityAdvice = (OutputEvent.advVel) command;
            this.advisedVelocity = velocityAdvice.value();
        } else if (command instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng headingAdvice = (OutputEvent.advHdng) command;
            this.advisedHeading = headingAdvice.value();
        }
    }

    /** The latest advised velocity (m/s); zero before any advice. */
    @RoboChartType("real")
    public double advisedVelocity() {
        return advisedVelocity;
    }

    /** The latest advised heading (degrees); zero before any advice. */
    @RoboChartType("real")
    public double advisedHeading() {
        return advisedHeading;
    }
}
