package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advised velocity and heading and keeps the latest of
 * each for the autopilot controller (LRE-DM8).
 */
public final class Actuator {

    // Zero until the LRE first advises a value.
    @RoboChartType("real") private double advisedVelocity = 0.0;
    @RoboChartType("real") private double advisedHeading = 0.0;

    /** Delivers an advVel or advHdng output. */
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
    @RoboChartType("real") public double advisedVelocity() { return advisedVelocity; }

    /** Latest advised heading, degrees. */
    @RoboChartType("real") public double advisedHeading() { return advisedHeading; }
}
