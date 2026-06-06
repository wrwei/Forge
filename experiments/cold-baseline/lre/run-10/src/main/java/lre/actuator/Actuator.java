package lre.actuator;

import lre.annotation.RoboChartType;
import lre.event.OutputEvent;

public final class Actuator {

    @RoboChartType("real")
    private double lastAdvVel;

    @RoboChartType("real")
    private double lastAdvHdng;

    public Actuator() {
        this.lastAdvVel = 0.0;
        this.lastAdvHdng = 0.0;
    }

    public void receive(OutputEvent event) {
        if (event instanceof OutputEvent.AdvVel) {
            OutputEvent.AdvVel v = (OutputEvent.AdvVel) event;
            this.lastAdvVel = v.value();
        } else if (event instanceof OutputEvent.AdvHdng) {
            OutputEvent.AdvHdng h = (OutputEvent.AdvHdng) event;
            this.lastAdvHdng = h.value();
        }
    }

    @RoboChartType("real")
    public double lastAdvVel() { return lastAdvVel; }

    @RoboChartType("real")
    public double lastAdvHdng() { return lastAdvHdng; }
}
