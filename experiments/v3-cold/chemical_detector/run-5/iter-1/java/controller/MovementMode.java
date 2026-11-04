package chemical_detector.controller;

/** States of the movement subsystem; the initial state first. */
public enum MovementMode {
    WAITING,
    GOING,
    FOUND,
    AVOIDING,
    TRYING_AGAIN,
    AVOIDING_AGAIN,
    GETTING_OUT
}
