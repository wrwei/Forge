package chemdetector.actuator;

import chemdetector.datamodel.Angle;

/**
 * Inter-subsystem signal surface: the gas-analysis subsystem emits the
 * turn, stop, and resume events the movement subsystem consumes.
 */
public final class SignalBus {

    private Angle lastTurn = Angle.Front;
    private boolean stopRequested = false;
    private boolean resumeRequested = false;

    /** Commands the robot to face the carried direction next. */
    public void turn(Angle direction) {
        this.lastTurn = direction;
    }

    /** Chemical source confirmed: the movement subsystem must halt. */
    public void stop() {
        this.stopRequested = true;
    }

    /** No-gas reading analysed: the movement subsystem resumes searching. */
    public void resume() {
        this.resumeRequested = true;
    }

    public Angle lastTurn() {
        return lastTurn;
    }

    public boolean stopRequested() {
        return stopRequested;
    }

    public boolean resumeRequested() {
        return resumeRequested;
    }
}
