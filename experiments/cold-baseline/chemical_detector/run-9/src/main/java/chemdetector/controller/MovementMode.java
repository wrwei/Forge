package chemdetector.controller;

/**
 * Modes of the movement subsystem (CD-MV-FR1..7 + final).
 */
public enum MovementMode {
    Waiting,
    Going,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut,
    Found,
    Final
}
