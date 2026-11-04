package steam_boiler.data;

/**
 * What the program believes about one physical unit (SB-V9): sound, or out of order with the
 * failure detection not yet acknowledged, or out of order with the detection acknowledged.
 */
public enum UnitCondition {
    SOUND,
    FAILED_UNACKNOWLEDGED,
    FAILED_ACKNOWLEDGED
}
