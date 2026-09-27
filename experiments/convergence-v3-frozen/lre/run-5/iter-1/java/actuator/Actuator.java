package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/** Holds the latest advised velocity and heading for the autopilot controller. */
public final class Actuator {

    @RoboChartType("real")
    private double advisedVelocity;

    @RoboChartType("real")
    private double advisedHeading;

    /** Receives an output event from the LRE controller. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel advVel = (OutputEvent.AdvVel) output;
            this.advisedVelocity = advVel.value();
        } else if (output instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng advHdng = (OutputEvent.AdvHdng) output;
            this.advisedHeading = advHdng.value();
        }
    }

    @RoboChartType("real")
    public double advisedVelocity() {
        return advisedVelocity;
    }

    @RoboChartType("real")
    public double advisedHeading() {
        return advisedHeading;
    }
}
