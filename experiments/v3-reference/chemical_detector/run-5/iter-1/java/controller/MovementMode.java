package chemical_detector.controller;

/**
 * Modes of the movement subsystem; the initial mode first.
 */
public enum MovementMode {
    /** Random-walk search between readings (initial). */
    Waiting,
    /** Moving with linear velocity lv in direction a. */
    Going,
    /** The chemical source has been confirmed; the robot stays halted (terminal). */
    Found,
    /** Steering away from an obstacle at the start of an evasion sequence. */
    Avoiding,
    /** Resuming motion after an avoidance manoeuvre. */
    TryingAgain,
    /** A second obstacle was hit during one evasion sequence; stuck detection decides. */
    AvoidingAgain,
    /** Recovery manoeuvre after stuck detection fired. */
    GettingOut
}
