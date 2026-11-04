package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;

/**
 * Outbound link of the gas-analysis subsystem: carries turn, stop and resume
 * to the movement subsystem (CD-ARCH2). Records the last event sent.
 */
public final class AnalysisActuator {

    private OutputEvent lastSent;
    private int sentCount = 0;

    /**
     * Sends an event to the movement subsystem.
     *
     * @param sent the turn, stop or resume event
     */
    public void apply(OutputEvent sent) {
        this.lastSent = sent;
        this.sentCount = sentCount + 1;
    }

    /**
     * The last event sent; meaningful once {@link #sentCount()} is positive.
     *
     * @return the last event sent
     */
    public OutputEvent lastSent() {
        return lastSent;
    }

    /**
     * The number of events sent so far.
     *
     * @return the count of events sent
     */
    public int sentCount() {
        return sentCount;
    }
}
