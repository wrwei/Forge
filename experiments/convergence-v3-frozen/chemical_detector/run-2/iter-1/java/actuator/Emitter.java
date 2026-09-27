package chemical_detector.actuator;

import chemical_detector.data.Angle;
import chemical_detector.event.SystemEvent;

/** Outbound half of the system event bus. Records what each subsystem emitted. */
public final class Emitter {

    private Angle lastTurn = Angle.Front;

    private boolean stopped = false;

    private boolean resumed = false;

    private boolean flagged = false;

    /** Emit a data-carrying event. */
    public void apply(SystemEvent signal) {
        if (signal instanceof SystemEvent.Turn) {
            SystemEvent.Turn t = (SystemEvent.Turn) signal;
            this.lastTurn = t.a();
        }
    }

    /** Emit the stop signal: the chemical source has been confirmed. */
    public void stop() {
        this.stopped = true;
    }

    /** Emit the resume signal: the last reading showed no target chemical. */
    public void resume() {
        this.resumed = true;
    }

    /** Emit the flag signal to the Vehicle: the source has been located. */
    public void flag() {
        this.flagged = true;
    }

    public Angle lastTurn() {
        return lastTurn;
    }

    public boolean stopped() {
        return stopped;
    }

    public boolean resumed() {
        return resumed;
    }

    public boolean flagged() {
        return flagged;
    }
}
