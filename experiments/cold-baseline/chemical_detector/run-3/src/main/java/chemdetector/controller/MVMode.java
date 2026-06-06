package chemdetector.controller;

/**
 * Modes of the movement controller. Maps to RoboChart states
 * Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain,
 * GettingOut, plus the final state Final.
 */
public enum MVMode {
    Waiting,
    Going,
    Found,
    Avoiding,
    TryingAgain,
    AvoidingAgain,
    GettingOut,
    Final
}
