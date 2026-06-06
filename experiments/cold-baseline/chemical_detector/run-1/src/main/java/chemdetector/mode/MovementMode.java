package chemdetector.mode;

/**
 * Modes for the movement state machine (CD-MV-FR1..7 + Final).
 */
public enum MovementMode {
    Waiting,
    Going,
    Found,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut,
    Final
}
