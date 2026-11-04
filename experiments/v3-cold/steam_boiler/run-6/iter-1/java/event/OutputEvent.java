package steam_boiler.event;

import steam_boiler.domain.PumpId;
import steam_boiler.mode.ModeCode;

/**
 * Messages the control program sends to the physical units; all of one
 * cycle's messages are emitted simultaneously.
 */
public sealed interface OutputEvent {

    /** MODE(m): the program's current operating mode, sent on every cycle. */
    record Mode(ModeCode code) implements OutputEvent {}

    /** PROGRAM_READY: the start-up level is achieved. */
    record ProgramReady() implements OutputEvent {}

    /** VALVE: open, or later close, the evacuation valve. */
    record Valve() implements OutputEvent {}

    /** OPEN_PUMP(n): activate pump n. */
    record OpenPump(PumpId pump) implements OutputEvent {}

    /** CLOSE_PUMP(n): stop pump n. */
    record ClosePump(PumpId pump) implements OutputEvent {}

    /** PUMP_FAILURE_DETECTION(n). */
    record PumpFailureDetection(PumpId pump) implements OutputEvent {}

    /** PUMP_CONTROL_FAILURE_DETECTION(n). */
    record PumpControlFailureDetection(PumpId pump) implements OutputEvent {}

    /** LEVEL_FAILURE_DETECTION. */
    record LevelFailureDetection() implements OutputEvent {}

    /** STEAM_FAILURE_DETECTION. */
    record SteamFailureDetection() implements OutputEvent {}

    /** PUMP_REPAIRED_ACKNOWLEDGEMENT(n). */
    record PumpRepairedAcknowledgement(PumpId pump) implements OutputEvent {}

    /** PUMP_CONTROL_REPAIRED_ACKNOWLEDGEMENT(n). */
    record PumpControlRepairedAcknowledgement(PumpId pump) implements OutputEvent {}

    /** LEVEL_REPAIRED_ACKNOWLEDGEMENT. */
    record LevelRepairedAcknowledgement() implements OutputEvent {}

    /** STEAM_REPAIRED_ACKNOWLEDGEMENT. */
    record SteamRepairedAcknowledgement() implements OutputEvent {}
}
