package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/** Holds the latest velocity and heading advised to the autopilot controller. */
public final class Actuator {

    @RoboChartType("real")
    private double advisedVel;

    @RoboChartType("real")
    private double advisedHdng;

    public void apply(OutputEvent outputEvent) {
        if (outputEvent instanceof OutputEvent.advVel) {
            OutputEvent.advVel advised = (OutputEvent.advVel) outputEvent;
            this.advisedVel = advised.value();
        } else if (outputEvent instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng advised = (OutputEvent.advHdng) outputEvent;
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
