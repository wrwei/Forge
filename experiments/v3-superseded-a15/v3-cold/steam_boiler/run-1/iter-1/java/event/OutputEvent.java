package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.OperatingMode;

/** Messages the program sends to the physical units (SB-DM8). */
public sealed interface OutputEvent {
    /** MODE(m): the current operating mode, sent on every cycle. */
    record ModeMessage(OperatingMode broadcastMode) implements OutputEvent {}

    /** PROGRAM_READY: the start-up level has been achieved. */
    record ProgramReady() implements OutputEvent {}

    /** VALVE: request opening, or later closure, of the evacuation valve. */
    record ValveCommand() implements OutputEvent {}

    /** OPEN_PUMP(n). */
    record OpenPump(@RoboChartType("nat") int openedPump) implements OutputEvent {}

    /** CLOSE_PUMP(n). */
    record ClosePump(@RoboChartType("nat") int closedPump) implements OutputEvent {}

    /** PUMP_FAILURE_DETECTION(n). */
    record PumpFailureDetection(@RoboChartType("nat") int failedPump) implements OutputEvent {}

    /** PUMP_CONTROL_FAILURE_DETECTION(n). */
    record PumpControlFailureDetection(@RoboChartType("nat") int failedController) implements OutputEvent {}

    /** LEVEL_FAILURE_DETECTION. */
    record LevelFailureDetection() implements OutputEvent {}

    /** STEAM_FAILURE_DETECTION. */
    record SteamFailureDetection() implements OutputEvent {}

    /** PUMP_REPAIRED_ACKNOWLEDGEMENT(n). */
    record PumpRepairedAcknowledgement(@RoboChartType("nat") int restoredPump) implements OutputEvent {}

    /** PUMP_CONTROL_REPAIRED_ACKNOWLEDGEMENT(n). */
    record PumpControlRepairedAcknowledgement(@RoboChartType("nat") int restoredController)
            implements OutputEvent {}

    /** LEVEL_REPAIRED_ACKNOWLEDGEMENT. */
    record LevelRepairedAcknowledgement() implements OutputEvent {}

    /** STEAM_REPAIRED_ACKNOWLEDGEMENT. */
    record SteamRepairedAcknowledgement() implements OutputEvent {}
}
