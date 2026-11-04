package chemical_detector.operation;

/**
 * The changeDirection operation (CD-OP4): steer away from an obstacle at the linear
 * velocity lv. An obstacle on the left turns the robot Right, on the right turns it
 * Left, in front turns it Back. The MovementActuator derives the evasive heading
 * from the obstacle side before invoking it.
 */
public final class ChangeDirection {

    private boolean evasionIssued = false;

    public ChangeDirection() {
    }

    /** Invokes the evasive move with the currently commanded heading. */
    public void compute() {
        this.evasionIssued = true;
    }

    /** Whether an evasive move has been invoked. */
    public boolean evasionIssued() {
        return evasionIssued;
    }
}
