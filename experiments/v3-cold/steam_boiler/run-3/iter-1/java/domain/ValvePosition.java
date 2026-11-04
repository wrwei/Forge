package steam_boiler.domain;

/**
 * The position of the boiler's evacuation valve, used only during
 * initialization to lower an excessive starting level.
 */
public enum ValvePosition {
    VALVE_CLOSED,
    VALVE_OPEN
}
