package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives advised velocity and heading from the LRE via output events and
 * stores the latest values for the autopilot controller (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastAdvisedVel;
    @RoboChartType("real")
    private double lastAdvisedHdng;

    /** Applies an output event, recording the advised value it carries. */
    public void apply(OutputEvent outputEvent) {
        if (outputEvent instanceof OutputEvent.advVel) {
            OutputEvent.advVel adv = (OutputEvent.advVel) outputEvent;
            this.lastAdvisedVel = adv.value();
        } else if (outputEvent instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng adv = (OutputEvent.advHdng) outputEvent;
            this.lastAdvisedHdng = adv.value();
        }
    }

    /** Latest advised velocity (m/s). */
    public double lastAdvisedVel() {
        return lastAdvisedVel;
    }

    /** Latest advised heading (degrees). */
    public double lastAdvisedHdng() {
        return lastAdvisedHdng;
    }
}
