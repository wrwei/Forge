package steam_boiler.event;

/**
 * Inputs of the mode controller. The messages of a cycle are received together by the
 * plant monitor (SB-V3, SB-DM9); {@link TransmissionReceived} marks the end of that
 * reception stage and triggers the controller's analysis of the cycle.
 */
public sealed interface InputEvent {

    /** The incoming transmission of the current cycle has been received (SB-V3). */
    record TransmissionReceived() implements InputEvent {}
}
