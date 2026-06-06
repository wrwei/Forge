package chemdetector.mode;

/**
 * Operating modes of the movement subsystem.
 */
public enum MvMode {
    Waiting,
    Going,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut,
    Found
}
