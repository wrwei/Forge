package steam_boiler.event;

/**
 * Events that drive the controller, one per cycle. Within one cycle all
 * incoming messages are received simultaneously (SB-V3); the unit monitor
 * analyses the cycle's transmission and delivers it to the controller as the
 * single event below that applies first, in this order of precedence.
 */
public sealed interface InputEvent {

    /**
     * A condition that demands emergency stop in the current mode (SB-FR8):
     * STOP on three successive transmissions (SB-Beh23) or a transmission
     * failure (SB-FR5), in every mode; in initialisation, once
     * STEAM_BOILER_WAITING has arrived, a level-device failure or a steam rate
     * other than zero (SB-Beh3, SB-Beh6); outside initialisation, a risk of
     * reaching M1 or M2 (SB-Beh13); in rescue, a failure of the steam device or
     * of a pump controller (SB-Beh21).
     */
    record EmergencyStopDemanded() implements InputEvent {}

    /** Initialisation: STEAM_BOILER_WAITING has arrived for the first time (SB-Beh2). */
    record SteamBoilerWaiting() implements InputEvent {}

    /** Initialisation: PHYSICAL_UNITS_READY has arrived in answer to PROGRAM_READY (SB-Beh8). */
    record PhysicalUnitsReady() implements InputEvent {}

    /** Any other transmission; its contents are read through the sensor service. */
    record Transmission() implements InputEvent {}
}
