package chemdetector.controller;

/** Operating modes of the movement subsystem (CD-MV-FR1..CD-MV-FR7). */
public enum MovementMode {
    Waiting,
    Going,
    Found,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut
}
