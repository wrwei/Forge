package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.BoilerMode;

/**
 * Messages the program sends to the physical units (SB-DM8), one record per
 * message.
 */
public sealed interface OutputEvent {

    /** MODE(m): the program's current operating mode, sent on every cycle. */
    record ModeMessage(BoilerMode value) implements OutputEvent {}

    /** PROGRAM_READY: the start-up level has been reached. */
    record ProgramReady() implements OutputEvent {}

    /** VALVE: open the evacuation valve, or close it if it is open. */
    record Valve() implements OutputEvent {}

    /** OPEN_PUMP(n): activate pump n. */
    record OpenPump(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** CLOSE_PUMP(n): stop pump n. */
    record ClosePump(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** PUMP_FAILURE_DETECTION(n). */
    record PumpFailureDetection(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** PUMP_CONTROL_FAILURE_DETECTION(n). */
    record PumpControlFailureDetection(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** LEVEL_FAILURE_DETECTION. */
    record LevelFailureDetection() implements OutputEvent {}

    /** STEAM_FAILURE_DETECTION. */
    record SteamFailureDetection() implements OutputEvent {}

    /** PUMP_REPAIRED_ACKNOWLEDGEMENT(n). */
    record PumpRepairedAcknowledgement(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** PUMP_CONTROL_REPAIRED_ACKNOWLEDGEMENT(n). */
    record PumpControlRepairedAcknowledgement(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** LEVEL_REPAIRED_ACKNOWLEDGEMENT. */
    record LevelRepairedAcknowledgement() implements OutputEvent {}

    /** STEAM_REPAIRED_ACKNOWLEDGEMENT. */
    record SteamRepairedAcknowledgement() implements OutputEvent {}
}
