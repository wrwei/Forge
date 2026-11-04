package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.OperatingMode;

/** Messages the program sends to the physical units (SB-DM8). */
public sealed interface OutputEvent {

    /** MODE(m): the program's current operating mode, sent on every cycle (SB-Beh24). */
    record ModeReport(OperatingMode operatingMode) implements OutputEvent {}

    /** PROGRAM_READY (SB-Beh7). */
    record ProgramReady() implements OutputEvent {}

    /** VALVE: toggles the evacuation valve, first to open it, then to close it (SB-DM7, SB-Beh4). */
    record Valve() implements OutputEvent {}

    /** OPEN_PUMP(n): activate pump n. */
    record OpenPump(@RoboChartType("nat") int pumpNumber) implements OutputEvent {}

    /** CLOSE_PUMP(n): stop pump n. */
    record ClosePump(@RoboChartType("nat") int pumpNumber) implements OutputEvent {}

    /** PUMP_FAILURE_DETECTION(n) (SB-FR6). */
    record PumpFailureDetection(@RoboChartType("nat") int pumpNumber) implements OutputEvent {}

    /** PUMP_CONTROL_FAILURE_DETECTION(n) (SB-FR6). */
    record PumpControlFailureDetection(@RoboChartType("nat") int pumpNumber)
            implements OutputEvent {}

    /** LEVEL_FAILURE_DETECTION (SB-FR6). */
    record LevelFailureDetection() implements OutputEvent {}

    /** STEAM_FAILURE_DETECTION (SB-FR6). */
    record SteamFailureDetection() implements OutputEvent {}

    /** PUMP_REPAIRED_ACKNOWLEDGEMENT(n) (SB-FR7). */
    record PumpRepairedAcknowledgement(@RoboChartType("nat") int pumpNumber)
            implements OutputEvent {}

    /** PUMP_CONTROL_REPAIRED_ACKNOWLEDGEMENT(n) (SB-FR7). */
    record PumpControlRepairedAcknowledgement(@RoboChartType("nat") int pumpNumber)
            implements OutputEvent {}

    /** LEVEL_REPAIRED_ACKNOWLEDGEMENT (SB-FR7). */
    record LevelRepairedAcknowledgement() implements OutputEvent {}

    /** STEAM_REPAIRED_ACKNOWLEDGEMENT (SB-FR7). */
    record SteamRepairedAcknowledgement() implements OutputEvent {}
}
