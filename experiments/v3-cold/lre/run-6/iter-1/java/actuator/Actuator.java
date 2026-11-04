package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advised velocity and heading (LRE-DM8) and keeps the
 * latest values for the autopilot controller.
 */
public final class Actuator {

    @RoboChartType("real") private double velocity = 0.0;
    @RoboChartType("real") private double heading = 0.0;

    /** Receives one output event from the LRE. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel velocityCommand = (OutputEvent.AdvVel) command;
            this.velocity = velocityCommand.value();
        } else if (command instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng headingCommand = (OutputEvent.AdvHdng) command;
            this.heading = headingCommand.value();
        }
    }

    /** Latest advised velocity, m/s (0 before any advice). */
    @RoboChartType("real")
    public double advisedVelocity() {
        return velocity;
    }

    /** Latest advised heading, degrees (0 before any advice). */
    @RoboChartType("real")
    public double advisedHeading() {
        return heading;
    }
}
