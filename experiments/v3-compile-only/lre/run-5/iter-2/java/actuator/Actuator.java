package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advice for the autopilot controller and keeps the latest
 * advised velocity and heading (LRE-DM8).
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastVelocity;

    @RoboChartType("real")
    private double lastHeading;

    /**
     * Receives one output event from the LRE.
     *
     * @param command the advice issued
     */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel velocityAdvice = (OutputEvent.AdvVel) command;
            this.lastVelocity = velocityAdvice.value();
        } else if (command instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng headingAdvice = (OutputEvent.AdvHdng) command;
            this.lastHeading = headingAdvice.value();
        }
    }

    /** @return the latest advised velocity (m/s) */
    @RoboChartType("real")
    public double advisedVelocity() {
        return lastVelocity;
    }

    /** @return the latest advised heading (degrees) */
    @RoboChartType("real")
    public double advisedHeading() {
        return lastHeading;
    }
}
