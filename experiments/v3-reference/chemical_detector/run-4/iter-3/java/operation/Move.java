package chemical_detector.operation;

/**
 * The Vehicle's move operation (CD-OP1): move at a linear velocity in a body-relative
 * direction until superseded; zero velocity facing Front halts. The velocity and
 * direction of the current command are those recorded by the MovementActuator.
 */
public final class Move {

    private boolean moveIssued = false;

    public Move() {
    }

    /** Invokes move with the currently commanded velocity and direction. */
    public void compute() {
        this.moveIssued = true;
    }

    /** Whether move has been invoked. */
    public boolean moveIssued() {
        return moveIssued;
    }
}
