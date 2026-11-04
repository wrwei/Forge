package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advice and keeps the latest values for the autopilot.
 */
public final class Actuator {
    @RoboChartType("real") private double lastVelocity = 0.0;
    @RoboChartType("real") private double lastHeading = 0.0;

    /** Receives one output event of the LRE. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel advice = (OutputEvent.AdvVel) output;
            this.lastVelocity = advice.advisedVelocity();
        } else if (output instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng advice = (OutputEvent.AdvHdng) output;
            this.lastHeading = advice.advisedHeading();
        }
    }

    /** Latest advised velocity (m/s). */
    @RoboChartType("real") public double lastAdvisedVelocity() { return lastVelocity; }

    /** Latest advised heading (degrees). */
    @RoboChartType("real") public double lastAdvisedHeading() { return lastHeading; }
}
