package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/** Receives the LRE's advice and keeps the latest values for the autopilot controller. */
public final class Actuator {

    @RoboChartType("real") private double lastVelocity = 0.0;
    @RoboChartType("real") private double lastHeading = 0.0;

    /** Records an advised velocity or heading. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel advice = (OutputEvent.AdvVel) output;
            this.lastVelocity = advice.value();
        } else if (output instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng advice = (OutputEvent.AdvHdng) output;
            this.lastHeading = advice.value();
        }
    }

    /** The latest advised velocity, m/s. */
    @RoboChartType("real")
    public double lastVelocity() {
        return lastVelocity;
    }

    /** The latest advised heading, degrees. */
    @RoboChartType("real")
    public double lastHeading() {
        return lastHeading;
    }
}
