package chemical_detector.mode;

/**
 * States of the movement subsystem; WAITING is initial.
 */
public enum MovementMode {
    WAITING,
    GOING,
    AVOIDING,
    TRYING_AGAIN,
    AVOIDING_AGAIN,
    GETTING_OUT,
    FOUND,
    HALTED
}
