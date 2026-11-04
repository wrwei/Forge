package steam_boiler.monitor;

/** The order the program sent to a pump in one transmission (SB-FR1, SB-FR2). */
enum PumpOrder {
    NO_ORDER,
    OPEN_ORDER,
    CLOSE_ORDER
}
