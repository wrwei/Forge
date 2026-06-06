package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Actuator receiving advised velocity and heading from the LRE via output
 * events and storing the latest values for the autopilot controller (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real")
    private double advisedVelocity;
    @RoboChartType("real")
    private double advisedHeading;

    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.advVel) {
            OutputEvent.advVel v = (OutputEvent.advVel) command;
            this.advisedVelocity = v.value();
        } else if (command instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng h = (OutputEvent.advHdng) command;
            this.advisedHeading = h.value();
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
