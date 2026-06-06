package chemdetector.actuator;

import chemdetector.datatype.Angle;

/**
 * Sink for the inter-controller signals emitted by the gas-analysis subsystem:
 *   turn ! a, stop, resume.
 *
 * In the formal model these are RoboChart events on the Shared interface
 * connecting the gas-analysis controller to the movement controller.
 * In Java they are delivered via this actuator, then forwarded into the
 * movement controller's step() method by the system harness.
 */
public final class GasAnalysisOutput {

    private Angle lastTurn;
    private boolean stopPending;
    private boolean resumePending;

    public void turn(Angle a) {
        this.lastTurn = a;
    }

    public void stop() {
        this.stopPending = true;
    }

    public void resume() {
        this.resumePending = true;
    }

    public Angle lastTurn() { return lastTurn; }
    public boolean stopPending() { return stopPending; }
    public boolean resumePending() { return resumePending; }

    public void clearPending() {
        this.stopPending = false;
        this.resumePending = false;
        this.lastTurn = null;
    }
}
