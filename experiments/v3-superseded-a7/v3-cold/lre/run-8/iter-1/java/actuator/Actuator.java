package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advice (advVel, advHdng) and keeps the latest values for
 * the autopilot controller.
 */
public final class Actuator {

    @RoboChartType("real") private double advisedVelocity = 0.0;
    @RoboChartType("real") private double advisedHeading = 0.0;

    /** Records the value carried by an output event. */
    public void apply(OutputEvent advice) {
        if (advice instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel advVelEvent = (OutputEvent.AdvVel) advice;
            this.advisedVelocity = advVelEvent.value();
        } else if (advice instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng advHdngEvent = (OutputEvent.AdvHdng) advice;
            this.advisedHeading = advHdngEvent.value();
        }
    }

    /** The latest advised velocity (m/s). */
    @RoboChartType("real")
    public double advisedVelocity() {
        return advisedVelocity;
    }

    /** The latest advised heading (degrees). */
    @RoboChartType("real")
    public double advisedHeading() {
        return advisedHeading;
    }
}
