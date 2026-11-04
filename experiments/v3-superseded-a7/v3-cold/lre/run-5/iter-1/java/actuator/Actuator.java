package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's output events for the autopilot controller and keeps the
 * latest advised velocity and heading (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real") private double advisedVelocity;
    @RoboChartType("real") private double advisedHeading;

    /** Receives one output event from the LRE. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel velocityCommand = (OutputEvent.AdvVel) command;
            this.advisedVelocity = velocityCommand.value();
        } else if (command instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng headingCommand = (OutputEvent.AdvHdng) command;
            this.advisedHeading = headingCommand.value();
        }
    }

    /** The latest advised velocity, m/s. */
    @RoboChartType("real")
    public double advisedVelocity() {
        return advisedVelocity;
    }

    /** The latest advised heading, degrees. */
    @RoboChartType("real")
    public double advisedHeading() {
        return advisedHeading;
    }
}
