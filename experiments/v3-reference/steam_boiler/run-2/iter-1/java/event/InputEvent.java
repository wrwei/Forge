package steam_boiler.event;

/**
 * Events that drive the controller. Within one cycle all incoming messages are
 * received simultaneously (SB-V3); the controller is stepped once per cycle by
 * the arrival of that cycle's transmission, whose contents it reads through
 * the sensor service.
 */
public sealed interface InputEvent {

    /** The incoming transmission of the current cycle has been received. */
    record Transmission() implements InputEvent {}
}
