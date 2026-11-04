package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/** Messages the program sends to the physical units. Pumps are numbered 1 to 4. */
public sealed interface OutputEvent {

    /** MODE(m): the program's current operating mode, sent on every cycle. */
    record Mode(ModeValue value) implements OutputEvent {}

    /** PROGRAM_READY: repeated in initialization until PHYSICAL_UNITS_READY arrives. */
    record ProgramReady() implements OutputEvent {}

    /** VALVE: request opening, and later closure, of the evacuation valve. */
    record Valve() implements OutputEvent {}

    /** OPEN_PUMP(n): activate pump n. */
    record OpenPump(@RoboChartType("nat") int pumpNumber) implements OutputEvent {}

    /** CLOSE_PUMP(n): stop pump n. */
    record ClosePump(@RoboChartType("nat") int pumpNumber) implements OutputEvent {}

    /** PUMP_FAILURE_DETECTION(n), repeated until acknowledged. */
    record PumpFailureDetection(@RoboChartType("nat") int pumpNumber) implements OutputEvent {}

    /** PUMP_CONTROL_FAILURE_DETECTION(n), repeated until acknowledged. */
    record PumpControlFailureDetection(@RoboChartType("nat") int pumpNumber)
            implements OutputEvent {}

    /** LEVEL_FAILURE_DETECTION, repeated until acknowledged. */
    record LevelFailureDetection() implements OutputEvent {}

    /** STEAM_FAILURE_DETECTION, repeated until acknowledged. */
    record SteamFailureDetection() implements OutputEvent {}

    /** PUMP_REPAIRED_ACKNOWLEDGEMENT(n). */
    record PumpRepairedAcknowledgement(@RoboChartType("nat") int pumpNumber)
            implements OutputEvent {}

    /** PUMP_CONTROL_REPAIRED_ACKNOWLEDGEMENT(n). */
    record PumpControlRepairedAcknowledgement(@RoboChartType("nat") int pumpNumber)
            implements OutputEvent {}

    /** LEVEL_REPAIRED_ACKNOWLEDGEMENT. */
    record LevelRepairedAcknowledgement() implements OutputEvent {}

    /** STEAM_REPAIRED_ACKNOWLEDGEMENT. */
    record SteamRepairedAcknowledgement() implements OutputEvent {}
}
