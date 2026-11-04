package chemical_detector.controller;

/** Modes of the movement subsystem; the initial mode first. */
public enum MovementMode {
    WAITING,
    GOING,
    FOUND,
    AVOIDING,
    TRYING_AGAIN,
    AVOIDING_AGAIN,
    GETTING_OUT,
    HALTED
}
