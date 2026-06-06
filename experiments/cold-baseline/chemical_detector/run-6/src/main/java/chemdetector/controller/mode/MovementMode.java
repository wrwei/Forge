package chemdetector.controller.mode;

/**
 * Modes of the movement subsystem (CD-MV-FR1..FR7 plus the final state j1).
 */
public enum MovementMode {
    Waiting,
    Going,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut,
    Found,
    J1
}
