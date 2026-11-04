package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advice for the autopilot controller through output
 * events and keeps the latest advised velocity and heading (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real") private double advisedVelocity;
    @RoboChartType("real") private double advisedHeading;

    /** Records an advised velocity or heading issued by the controller. */
    public void apply(OutputEvent event) {
        if (event instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel advice = (OutputEvent.AdvVel) event;
            this.advisedVelocity = advice.value();
        } else if (event instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng advice = (OutputEvent.AdvHdng) event;
            this.advisedHeading = advice.value();
        }
    }

    /** The latest advised velocity, in m/s (0 before any advice). */
    @RoboChartType("real")
    public double advisedVelocity() {
        return advisedVelocity;
    }

    /** The latest advised heading, in degrees (0 before any advice). */
    @RoboChartType("real")
    public double advisedHeading() {
        return advisedHeading;
    }
}
