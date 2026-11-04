package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/** Receives the LRE's advice and keeps the latest values for the autopilot controller. */
public final class Actuator {

    @RoboChartType("real") private double advisedVelocity;
    @RoboChartType("real") private double advisedHeading;

    /** Delivers one output event (advVel or advHdng) to the autopilot side. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel velocity = (OutputEvent.AdvVel) command;
            this.advisedVelocity = velocity.value();
        } else if (command instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng heading = (OutputEvent.AdvHdng) command;
            this.advisedHeading = heading.value();
        }
    }

    /** The latest advised velocity (m/s); 0 before any advice. */
    @RoboChartType("real")
    public double advisedVelocity() {
        return advisedVelocity;
    }

    /** The latest advised heading (degrees); 0 before any advice. */
    @RoboChartType("real")
    public double advisedHeading() {
        return advisedHeading;
    }
}
