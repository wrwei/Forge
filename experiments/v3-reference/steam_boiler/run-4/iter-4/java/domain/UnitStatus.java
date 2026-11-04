package steam_boiler.domain;

/**
 * What the program believes about one physical unit (SB-V9): whether it is out
 * of order, and whether the failure detection announced for it has been
 * acknowledged. A sound unit has nothing pending, so its detection counts as
 * acknowledged.
 *
 * @param outOfOrder            true once a failure has been detected, until the unit reports its repair
 * @param detectionAcknowledged true once the unit has acknowledged the failure detection
 */
public record UnitStatus(boolean outOfOrder, boolean detectionAcknowledged) {
}
