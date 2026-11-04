package chemical_detector.actuator;

import chemical_detector.domain.Angle;
import chemical_detector.event.OutputEvent;

/**
 * Receives the turn, stop and resume signals the gas-analysis subsystem sends
 * to the movement subsystem; records the last direction sent and whether
 * stop has been sent.
 */
public final class GasAnalysisOutput {
    private Angle lastTurn = Angle.Front;
    private boolean stopSent = false;

    /** Sends a signal to the movement subsystem. */
    public void apply(OutputEvent signal) {
        if (signal instanceof OutputEvent.Turn) {
            OutputEvent.Turn turnSignal = (OutputEvent.Turn) signal;
            this.lastTurn = turnSignal.direction();
        } else if (signal instanceof OutputEvent.Stop) {
            this.stopSent = true;
        }
    }

    /** Direction carried by the last turn signal sent. */
    public Angle lastTurn() {
        return lastTurn;
    }

    /** True once stop has been sent. */
    public boolean isStopSent() {
        return stopSent;
    }
}
