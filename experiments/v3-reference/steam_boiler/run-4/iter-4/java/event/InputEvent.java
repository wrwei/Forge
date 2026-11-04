package steam_boiler.event;

/**
 * The controller's input event (SB-V3): the cycle's transmission has been
 * received in full (the messages themselves, {@link PlantMessage}, go to the
 * receive stage), so the program analyses it now.
 */
public sealed interface InputEvent {

    /** The cycle's transmission has been received in full; the program analyses it now. */
    record TransmissionReceived() implements InputEvent {}
}
