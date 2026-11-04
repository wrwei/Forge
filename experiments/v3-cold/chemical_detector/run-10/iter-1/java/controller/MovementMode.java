package chemical_detector.controller;

/** States of the movement subsystem; Waiting is the initial state. */
public enum MovementMode {
    Waiting,
    Going,
    Found,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut
}
