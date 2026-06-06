package chemdetector.mode;

/**
 * Operating modes of the movement subsystem (CD-MV-FR1..CD-MV-FR7).
 * Waiting is the initial mode.
 */
public enum MovementMode {
    Waiting,
    Going,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut,
    Found
}
