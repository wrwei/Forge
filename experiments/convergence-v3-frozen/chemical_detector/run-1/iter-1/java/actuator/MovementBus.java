package chemical_detector.actuator;

import chemical_detector.data.Angle;

/**
 * The only channel between the two reasoning subsystems (CD-ARCH2): the
 * gas-analysis subsystem writes turn / stop / resume here and the movement
 * subsystem consumes them as events.
 */
public final class MovementBus {

    private Angle lastTurn = Angle.Front;

    private boolean stopped;

    private boolean resumed;

    /** Command the movement subsystem to face {@code a} (CD-Evt4). */
    public void turn(Angle a) {
        this.lastTurn = a;
        this.stopped = false;
    }

    /** Command the movement subsystem to halt (CD-Evt5). */
    public void stop() {
        this.stopped = true;
    }

    /** Command the movement subsystem to carry on searching (CD-Evt6). */
    public void resume() {
        this.resumed = true;
    }
}
