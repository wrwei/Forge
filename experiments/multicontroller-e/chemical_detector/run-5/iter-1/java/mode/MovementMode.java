package chemical_detector.mode;

/** States of the movement subsystem (CD-MV-FR1..7); Waiting is initial. */
public enum MovementMode {
    Waiting,
    Going,
    Found,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut
}
