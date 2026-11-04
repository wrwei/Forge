package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advice and keeps the latest advised velocity and heading
 * for the autopilot controller. Both are zero until the first advice arrives.
 */
public final class Actuator {

    @RoboChartType("real") private double latestVelocity = 0.0;
    @RoboChartType("real") private double latestHeading = 0.0;

    /** Receives one output event from the LRE. */
    public void apply(OutputEvent event) {
        if (event instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel advice = (OutputEvent.AdvVel) event;
            this.latestVelocity = advice.velocity();
        } else if (event instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng advice = (OutputEvent.AdvHdng) event;
            this.latestHeading = advice.heading();
        }
    }

    /** The latest advised velocity (m/s). */
    @RoboChartType("real") public double latestVelocity() {
        return latestVelocity;
    }

    /** The latest advised heading (degrees). */
    @RoboChartType("real") public double latestHeading() {
        return latestHeading;
    }
}
