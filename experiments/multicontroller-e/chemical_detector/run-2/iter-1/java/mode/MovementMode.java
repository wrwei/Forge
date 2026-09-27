package chemical_detector.mode;

/** States of the movement subsystem; {@code Waiting} is initial (CD-MV-FR1..7). */
public enum MovementMode {
    Waiting,
    Going,
    Found,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut
}
