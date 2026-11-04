package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advice for the autopilot controller and keeps the latest values (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real") private double advisedVelocity;
    @RoboChartType("real") private double advisedHeading;

    /** Receives one output event (advVel or advHdng) and stores its value. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel advice = (OutputEvent.AdvVel) command;
            this.advisedVelocity = advice.value();
        } else if (command instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng advice = (OutputEvent.AdvHdng) command;
            this.advisedHeading = advice.value();
        }
    }

    /** The latest advised velocity, in m/s. */
    @RoboChartType("real")
    public double advisedVelocity() {
        return advisedVelocity;
    }

    /** The latest advised heading, in degrees. */
    @RoboChartType("real")
    public double advisedHeading() {
        return advisedHeading;
    }
}
