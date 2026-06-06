package chemdetector.event;

import chemdetector.data.Angle;

/**
 * Inter-controller communication channel. The gas-analysis
 * controller calls these methods as transition actions to emit
 * the shared events turn (CD-Evt4), stop (CD-Evt5) and resume
 * (CD-Evt6). The movement controller consumes them via
 * MVInputEvent.Turn / Stop / Resume.
 */
public final class SharedEventBus {

    private Angle lastTurn;

    private boolean stopSignalled = false;

    private boolean resumeSignalled = false;

    /** CD-Evt4: emit turn with payload a. */
    public void turn(Angle a) {
        this.lastTurn = a;
    }

    /** CD-Evt5: emit stop signal. */
    public void stop() {
        this.stopSignalled = true;
    }

    /** CD-Evt6: emit resume signal. */
    public void resume() {
        this.resumeSignalled = true;
    }

    public Angle lastTurn() {
        return lastTurn;
    }

    public boolean stopSignalled() {
        return stopSignalled;
    }

    public boolean resumeSignalled() {
        return resumeSignalled;
    }
}
