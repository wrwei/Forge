package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advice and keeps the latest values for the autopilot
 * controller (LRE-DM8). Both values are zero until the first advice.
 */
public final class Actuator {

    @RoboChartType("real") private double advisedVelocity = 0.0;
    @RoboChartType("real") private double advisedHeading = 0.0;

    /** Delivers one output event of the LRE. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel advice = (OutputEvent.AdvVel) command;
            this.advisedVelocity = advice.value();
        } else if (command instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng advice = (OutputEvent.AdvHdng) command;
            this.advisedHeading = advice.value();
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
