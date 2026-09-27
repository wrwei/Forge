package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/** Receives advised velocity and heading from the LRE and holds the latest values. */
public final class Actuator {

    @RoboChartType("real")
    private double advisedVel;

    @RoboChartType("real")
    private double advisedHdng;

    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.advVel) {
            OutputEvent.advVel av = (OutputEvent.advVel) output;
            this.advisedVel = av.value();
        } else if (output instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng ah = (OutputEvent.advHdng) output;
            this.advisedHdng = ah.value();
        }
    }

    public double advisedVel() {
        return advisedVel;
    }

    public double advisedHdng() {
        return advisedHdng;
    }
}
