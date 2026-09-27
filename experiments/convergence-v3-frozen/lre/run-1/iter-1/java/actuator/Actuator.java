package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/** Holds the latest advised velocity and heading for the autopilot controller. */
public final class Actuator {

    @RoboChartType("real")
    private double advisedVel;

    @RoboChartType("real")
    private double advisedHdng;

    /** Receives one output event from the LRE controller. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.advVel) {
            OutputEvent.advVel advised = (OutputEvent.advVel) output;
            this.advisedVel = advised.value();
        } else if (output instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng advised = (OutputEvent.advHdng) output;
            this.advisedHdng = advised.value();
        }
    }

    @RoboChartType("real")
    public double advisedVel() {
        return advisedVel;
    }

    @RoboChartType("real")
    public double advisedHdng() {
        return advisedHdng;
    }
}
