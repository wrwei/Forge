package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

public final class Actuator {

    @RoboChartType("real")
    private double advisedVel;

    @RoboChartType("real")
    private double advisedHdng;

    public void receive(OutputEvent event) {
        if (event instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel advVel = (OutputEvent.AdvVel) event;
            this.advisedVel = advVel.value();
        } else if (event instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng advHdng = (OutputEvent.AdvHdng) event;
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
