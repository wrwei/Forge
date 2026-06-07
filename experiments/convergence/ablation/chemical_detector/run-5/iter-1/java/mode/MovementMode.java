package chemdetector.mode;

/**
 * Operating modes of the movement subsystem (CD-MV-FR1..7).
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
