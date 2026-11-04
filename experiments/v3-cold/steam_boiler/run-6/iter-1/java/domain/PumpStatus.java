package steam_boiler.domain;

/**
 * The on/off state reported for one pump.
 *
 * @param pump       the pump reported on
 * @param switchedOn whether the pump is on
 */
public record PumpStatus(PumpId pump, boolean switchedOn) {
}
