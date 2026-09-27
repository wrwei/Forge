package chemical_detector.mode;

/** Operating modes of the movement subsystem; WAITING is initial. */
public enum MovementMode {
    WAITING,
    GOING,
    FOUND,
    AVOIDING,
    TRYING_AGAIN,
    AVOIDING_AGAIN,
    GETTING_OUT
}
