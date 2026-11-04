package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.event.OutputEvent;

/**
 * Output port of the movement subsystem: sends the flag event to the Vehicle
 * (CD-Evt7) and holds the subsystem for a given time (the waits of CD-Const3 and
 * CD-Const6). Records the last value of each.
 */
public final class MovementActuator {

    private OutputEvent lastOutput = new OutputEvent.Flag();
    @RoboChartType("nat") private int lastPause = 0;

    /** Sends an event to the Vehicle. */
    public void apply(OutputEvent output) {
        this.lastOutput = output;
    }

    /** Holds the movement subsystem for the given number of ticks. */
    public void pause(@RoboChartType("nat") int duration) {
        this.lastPause = duration;
    }

    /** The last event sent. */
    public OutputEvent lastOutput() {
        return lastOutput;
    }

    /** The duration of the last hold. */
    @RoboChartType("nat")
    public int lastPause() {
        return lastPause;
    }
}
