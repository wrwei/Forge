package chemical_detector.controller;

/** States of the movement subsystem; Waiting is initial (CD-MV-FR1..7). */
public enum MovementMode {
    Waiting,
    Going,
    Found,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut
}
