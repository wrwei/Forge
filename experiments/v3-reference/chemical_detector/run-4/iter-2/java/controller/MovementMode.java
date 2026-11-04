package chemical_detector.controller;

/**
 * States of the movement subsystem (CD-MV-FR1..7); Waiting is initial.
 */
public enum MovementMode {
    Waiting,
    Going,
    Found,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut
}
