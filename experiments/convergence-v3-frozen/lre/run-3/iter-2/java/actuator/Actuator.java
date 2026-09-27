package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the advised velocity and heading from the Last Response Engine and
 * holds the latest value of each for the autopilot controller.
 */
public final class Actuator {

    @RoboChartType("real")
    private double advisedVel;

    @RoboChartType("real")
    private double advisedHdng;

    /** Applies one output event, updating the advised value it carries. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.advVel) {
            OutputEvent.advVel advisedVelocity = (OutputEvent.advVel) output;
            this.advisedVel = advisedVelocity.value();
        } else if (output instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng advisedHeading = (OutputEvent.advHdng) output;
            this.advisedHdng = advisedHeading.value();
        }
    }

    /** The latest advised velocity, in m/s. */
    @RoboChartType("real")
    public double advisedVel() {
        return advisedVel;
    }

    /** The latest advised heading, in degrees. */
    @RoboChartType("real")
    public double advisedHdng() {
        return advisedHdng;
    }
}
