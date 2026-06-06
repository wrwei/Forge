package chemdetector.movement.mode;

/**
 * States of the movement subsystem. CD-MV-FR1..7 plus the final sink
 * state j1.
 */
public enum MVMode {
    Waiting,
    Going,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut,
    Found,
    Final
}
