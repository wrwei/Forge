package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advice for the autopilot controller through the output
 * events advVel and advHdng, and keeps the latest value of each (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real") private double advisedVelocity;
    @RoboChartType("real") private double advisedHeading;

    /** Delivers one output event to the autopilot side. */
    public void apply(OutputEvent event) {
        if (event instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel advice = (OutputEvent.AdvVel) event;
            this.advisedVelocity = advice.value();
        } else if (event instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng advice = (OutputEvent.AdvHdng) event;
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
