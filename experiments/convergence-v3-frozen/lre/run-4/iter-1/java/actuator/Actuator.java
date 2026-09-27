package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/** Holds the latest advised velocity and heading for the autopilot (LRE-DM8). */
public final class Actuator {

    @RoboChartType("real")
    private double advisedVel;

    @RoboChartType("real")
    private double advisedHdng;

    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel advVel = (OutputEvent.AdvVel) output;
            this.advisedVel = advVel.value();
        } else if (output instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng advHdng = (OutputEvent.AdvHdng) output;
            this.advisedHdng = advHdng.value();
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
