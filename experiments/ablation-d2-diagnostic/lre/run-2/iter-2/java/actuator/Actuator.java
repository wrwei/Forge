package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

/** Receives advised velocity and heading and holds the latest values (LRE-DM8). */
public final class Actuator {

    @RoboChartType("real")
    private double advisedVel;

    @RoboChartType("real")
    private double advisedHdng;

    /** Applies an output event issued by the LRE controller. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.advVel) {
            OutputEvent.advVel command = (OutputEvent.advVel) output;
            this.advisedVel = command.value();
        } else if (output instanceof OutputEvent.advHdng) {
            OutputEvent.advHdng command = (OutputEvent.advHdng) output;
            this.advisedHdng = command.value();
        }
    }

    /** The latest advised velocity, in m/s. */
    @RoboChartType("real")
    public double advisedVel() {
        return advisedVel;
    }

    /** The latest advised heading, in degrees. */
    @RoboChartType("real")
    public double advisedHdng() {
        return advisedHdng;
    }
}
