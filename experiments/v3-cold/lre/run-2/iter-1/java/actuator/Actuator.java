package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advice and keeps the latest values for the autopilot
 * controller (LRE-DM8). Both values are 0.0 until the first advice arrives.
 */
public final class Actuator {

    @RoboChartType("real") private double advisedVelocity = 0.0;
    @RoboChartType("real") private double advisedHeading = 0.0;

    /** Receives one output event (advVel or advHdng). */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel velocity = (OutputEvent.AdvVel) command;
            this.advisedVelocity = velocity.value();
        } else if (command instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng heading = (OutputEvent.AdvHdng) command;
            this.advisedHeading = heading.value();
        }
    }

    /** Latest advised velocity, m/s. */
    @RoboChartType("real") public double advisedVelocity() {
        return advisedVelocity;
    }

    /** Latest advised heading, degrees. */
    @RoboChartType("real") public double advisedHeading() {
        return advisedHeading;
    }
}
