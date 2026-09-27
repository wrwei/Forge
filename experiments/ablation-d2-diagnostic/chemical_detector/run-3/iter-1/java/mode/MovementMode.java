package chemical_detector.mode;

/** Operating modes of the movement subsystem; Waiting is initial. */
public enum MovementMode {
    Waiting,
    Going,
    Found,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut
}
