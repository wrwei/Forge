package chemical_detector.mode;

/**
 * Modes of the movement subsystem; WAITING is the initial mode.
 */
public enum MovementMode {
    WAITING,
    GOING,
    FOUND,
    AVOIDING,
    TRYING_AGAIN,
    AVOIDING_AGAIN,
    GETTING_OUT
}
