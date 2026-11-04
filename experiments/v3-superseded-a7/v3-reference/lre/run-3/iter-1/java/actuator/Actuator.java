package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advice (advVel, advHdng) and keeps the latest values for
 * the autopilot controller (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real") private double advisedVelocity = 0.0;
    @RoboChartType("real") private double advisedHeading = 0.0;

    /** Delivers one output event and records its value. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel velocity = (OutputEvent.AdvVel) output;
            this.advisedVelocity = velocity.value();
        } else if (output instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng heading = (OutputEvent.AdvHdng) output;
            this.advisedHeading = heading.value();
        }
    }

    /** Latest advised velocity, m/s. */
    @RoboChartType("real")
    public double advisedVelocity() {
        return advisedVelocity;
    }

    /** Latest advised heading, degrees. */
    @RoboChartType("real")
    public double advisedHeading() {
        return advisedHeading;
    }
}
