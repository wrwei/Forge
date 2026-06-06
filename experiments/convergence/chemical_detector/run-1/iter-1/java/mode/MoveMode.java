package chemdetector.mode;

/**
 * Operating modes of the movement subsystem (CD-MV-FR1..7).
 */
public enum MoveMode {
    Waiting,
    Going,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut,
    Found
}
