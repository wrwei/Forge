package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.event.OutputEvent;

/**
 * Output port of the gas-analysis subsystem. It receives the turn, stop and
 * resume events the subsystem sends to the movement subsystem and records the
 * last one and how many have been sent.
 */
public final class GasAnalysisActuator {

    private OutputEvent lastSent = new OutputEvent.Resume();
    @RoboChartType("nat") private int sentCount = 0;

    /** Sends an event to the movement subsystem. */
    public void apply(OutputEvent signal) {
        lastSent = signal;
        sentCount = sentCount + 1;
    }

    /** The last event sent; meaningful only once {@link #sentCount()} is positive. */
    public OutputEvent lastSent() {
        return lastSent;
    }

    /** How many events have been sent. */
    @RoboChartType("nat")
    public int sentCount() {
        return sentCount;
    }
}
