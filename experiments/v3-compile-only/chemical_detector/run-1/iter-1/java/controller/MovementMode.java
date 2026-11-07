package chemical_detector.controller;

/** States of the movement subsystem; the initial state first. */
public enum MovementMode {
    Waiting,
    Going,
    Found,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut,
    Halted
}
