package chemical_detector.mode;

/**
 * Modes of the movement subsystem (CD-MV-FR1..7). {@code Waiting} is initial.
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
