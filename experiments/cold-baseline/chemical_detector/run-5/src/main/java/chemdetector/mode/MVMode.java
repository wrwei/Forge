package chemdetector.mode;

/**
 * Operating modes of the movement subsystem.
 * Waiting is the initial mode (CD-MV-Beh1). Final is the post-Found terminal mode.
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
