package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advice for the autopilot controller and keeps the
 * latest values (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real") private double advisedVelocity;
    @RoboChartType("real") private double advisedHeading;

    /** Records an advVel or advHdng output event. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel velocityAdvice = (OutputEvent.AdvVel) command;
            this.advisedVelocity = velocityAdvice.value();
        } else if (command instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng headingAdvice = (OutputEvent.AdvHdng) command;
            this.advisedHeading = headingAdvice.value();
        }
    }

    /** The latest advised velocity, m/s (0 before any advice). */
    @RoboChartType("real")
    public double advisedVelocity() {
        return advisedVelocity;
    }

    /** The latest advised heading, degrees (0 before any advice). */
    @RoboChartType("real")
    public double advisedHeading() {
        return advisedHeading;
    }
}
