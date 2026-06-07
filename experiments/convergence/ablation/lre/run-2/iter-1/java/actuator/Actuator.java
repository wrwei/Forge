package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives advised velocity and heading from the LRE controller via
 * output events and stores the latest values for the autopilot
 * controller (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real")
    private double advisedVelocity;

    @RoboChartType("real")
    private double advisedHeading;

    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.advVel) {
            OutputEvent.advVel av = (OutputEvent.advVel) command;
            this.advisedVelocity = av.value();
        } else if (command instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng ah = (OutputEvent.advHdng) command;
            this.advisedHeading = ah.value();
        }
    }

    public double advisedVelocity() {
        return advisedVelocity;
    }

    public double advisedHeading() {
        return advisedHeading;
    }
}
