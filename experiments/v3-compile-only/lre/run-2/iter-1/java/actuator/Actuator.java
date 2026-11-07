package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/**
 * Receives the LRE's advice for the autopilot controller and keeps the latest
 * advised velocity and heading.
 */
public final class Actuator {

    @RoboChartType("real")
    private double advisedVel = 0.0;

    @RoboChartType("real")
    private double advisedHdng = 0.0;

    /**
     * Receives one output event from the LRE.
     *
     * @param output the advice: an advised velocity or an advised heading
     */
    public void send(OutputEvent output) {
        if (output instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel velocityAdvice = (OutputEvent.AdvVel) output;
            this.advisedVel = velocityAdvice.velocity();
        } else if (output instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng headingAdvice = (OutputEvent.AdvHdng) output;
            this.advisedHdng = headingAdvice.heading();
        }
    }

    /**
     * The latest advised velocity.
     *
     * @return velocity, m/s
     */
    @RoboChartType("real")
    public double advisedVel() {
        return advisedVel;
    }

    /**
     * The latest advised heading.
     *
     * @return heading, degrees
     */
    @RoboChartType("real")
    public double advisedHdng() {
        return advisedHdng;
    }
}
