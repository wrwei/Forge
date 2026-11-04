package steam_boiler.event;

/**
 * Events that drive the controller, one per cycle. Within one cycle all
 * incoming messages are received simultaneously (SB-V3); the unit monitor
 * analyses the cycle's transmission and delivers it to the controller as the
 * single event below that applies first, in this order of precedence.
 */
public sealed interface InputEvent {

    /** STOP has arrived on three successive transmissions (SB-V10, SB-Beh23). */
    record StopConfirmed() implements InputEvent {}

    /** The transmission system has failed: a message is missing or aberrant (SB-FR5). */
    record TransmissionFault() implements InputEvent {}

    /** Outside initialisation: the level risks reaching M1 or M2 before the next transmission (SB-V4, SB-Beh13). */
    record LevelRisk() implements InputEvent {}

    /**
     * Initialisation, once STEAM_BOILER_WAITING has arrived: the level device
     * has failed, or the steam rate is not zero and the steam device is judged
     * defective (SB-Beh3, SB-Beh6).
     */
    record StartUpFailure() implements InputEvent {}

    /**
     * Rescue: the steam device or a pump controller has failed, so the level
     * estimate is no longer trustworthy (SB-Beh19, SB-Beh21).
     */
    record EstimateUnreliable() implements InputEvent {}

    /** Initialisation: STEAM_BOILER_WAITING has arrived for the first time (SB-Beh2). */
    record SteamBoilerWaiting() implements InputEvent {}

    /** Initialisation: PHYSICAL_UNITS_READY has arrived in answer to PROGRAM_READY (SB-Beh8). */
    record PhysicalUnitsReady() implements InputEvent {}

    /** Any other transmission; its contents are read through the sensor service. */
    record Transmission() implements InputEvent {}
}
