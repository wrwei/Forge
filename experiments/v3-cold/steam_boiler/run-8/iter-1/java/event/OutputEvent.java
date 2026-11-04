package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.OperatingMode;

/**
 * Everything the program sends to the physical units: one record per output
 * message of SB-DM8. All messages of one cycle are emitted simultaneously
 * (SB-V3).
 */
public sealed interface OutputEvent {

    /**
     * MODE(m): the current operating mode, sent on every cycle (SB-Beh24).
     *
     * @param mode the mode the program is in
     */
    record Mode(OperatingMode mode) implements OutputEvent {
    }

    /** PROGRAM_READY: start-up level achieved (SB-Beh7). */
    record ProgramReady() implements OutputEvent {
    }

    /** VALVE: request to open, and later to close, the evacuation valve (SB-Beh4). */
    record Valve() implements OutputEvent {
    }

    /**
     * OPEN_PUMP(n): activate pump n.
     *
     * @param pumpNumber the pump to switch on, 1 to 4
     */
    record OpenPump(@RoboChartType("nat") int pumpNumber) implements OutputEvent {
    }

    /**
     * CLOSE_PUMP(n): stop pump n.
     *
     * @param pumpNumber the pump to switch off, 1 to 4
     */
    record ClosePump(@RoboChartType("nat") int pumpNumber) implements OutputEvent {
    }

    /**
     * PUMP_FAILURE_DETECTION(n) (SB-FR6).
     *
     * @param pumpNumber the failed pump, 1 to 4
     */
    record PumpFailureDetection(@RoboChartType("nat") int pumpNumber) implements OutputEvent {
    }

    /**
     * PUMP_CONTROL_FAILURE_DETECTION(n) (SB-FR6).
     *
     * @param pumpNumber the pump whose controller failed, 1 to 4
     */
    record PumpControlFailureDetection(@RoboChartType("nat") int pumpNumber) implements OutputEvent {
    }

    /** LEVEL_FAILURE_DETECTION (SB-FR6). */
    record LevelFailureDetection() implements OutputEvent {
    }

    /** STEAM_FAILURE_DETECTION (SB-FR6). */
    record SteamFailureDetection() implements OutputEvent {
    }

    /**
     * PUMP_REPAIRED_ACKNOWLEDGEMENT(n) (SB-FR7).
     *
     * @param pumpNumber the repaired pump, 1 to 4
     */
    record PumpRepairedAcknowledgement(@RoboChartType("nat") int pumpNumber) implements OutputEvent {
    }

    /**
     * PUMP_CONTROL_REPAIRED_ACKNOWLEDGEMENT(n) (SB-FR7).
     *
     * @param pumpNumber the pump whose controller was repaired, 1 to 4
     */
    record PumpControlRepairedAcknowledgement(@RoboChartType("nat") int pumpNumber) implements OutputEvent {
    }

    /** LEVEL_REPAIRED_ACKNOWLEDGEMENT (SB-FR7). */
    record LevelRepairedAcknowledgement() implements OutputEvent {
    }

    /** STEAM_REPAIRED_ACKNOWLEDGEMENT (SB-FR7). */
    record SteamRepairedAcknowledgement() implements OutputEvent {
    }
}
