package steam_boiler.domain;

/**
 * The observation reported by the controller of one pump: water flows from
 * the pump into the boiler, or it does not.
 *
 * @param pump         the pump whose controller reports
 * @param waterFlowing whether water is circulating from that pump
 */
public record PumpControlStatus(PumpId pump, boolean waterFlowing) {
}
