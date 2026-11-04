package steam_boiler.data;

/** The on/off state of a pump, as reported by PUMP_STATE(n, b) and ordered by OPEN_PUMP / CLOSE_PUMP (SB-DM3). */
public enum PumpPosition {
    PUMP_OFF,
    PUMP_ON
}
