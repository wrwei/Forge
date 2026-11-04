package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.ModeSignal;

/** Messages the control program sends to the physical units (SB-DM8). */
public sealed interface OutputEvent {

    /** MODE(m): the program's current operating mode, sent on every cycle. */
    record ModeMessage(ModeSignal value) implements OutputEvent {}

    /** PROGRAM_READY: sent in initialization, once the start-up level is reached. */
    record ProgramReady() implements OutputEvent {}

    /** VALVE: requests opening, and later closure, of the evacuation valve. */
    record Valve() implements OutputEvent {}

    /** OPEN_PUMP(n): activate pump n. */
    record OpenPump(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** CLOSE_PUMP(n): stop pump n. */
    record ClosePump(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** PUMP_FAILURE_DETECTION(n): announces a detected failure of pump n. */
    record PumpFailureDetection(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** PUMP_CONTROL_FAILURE_DETECTION(n): announces a detected failure of the controller of pump n. */
    record PumpControlFailureDetection(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** LEVEL_FAILURE_DETECTION: announces a detected failure of the level measuring device. */
    record LevelFailureDetection() implements OutputEvent {}

    /** STEAM_FAILURE_DETECTION: announces a detected failure of the steam measuring device. */
    record SteamFailureDetection() implements OutputEvent {}

    /** PUMP_REPAIRED_ACKNOWLEDGEMENT(n): acknowledges PUMP_REPAIRED(n). */
    record PumpRepairedAcknowledgement(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** PUMP_CONTROL_REPAIRED_ACKNOWLEDGEMENT(n): acknowledges PUMP_CONTROL_REPAIRED(n). */
    record PumpControlRepairedAcknowledgement(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** LEVEL_REPAIRED_ACKNOWLEDGEMENT: acknowledges LEVEL_REPAIRED. */
    record LevelRepairedAcknowledgement() implements OutputEvent {}

    /** STEAM_REPAIRED_ACKNOWLEDGEMENT: acknowledges STEAM_REPAIRED. */
    record SteamRepairedAcknowledgement() implements OutputEvent {}
}
