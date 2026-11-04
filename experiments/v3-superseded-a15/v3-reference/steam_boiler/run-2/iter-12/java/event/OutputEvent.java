package steam_boiler.event;

import steam_boiler.domain.OperatingMode;
import steam_boiler.domain.PumpId;
import steam_boiler.domain.PumpSwitch;

/** The messages the program sends to the physical units, one record per message. */
public sealed interface OutputEvent {
    /** MODE(m): the program's current operating mode, sent on every cycle. */
    record ModeMessage(OperatingMode operatingMode) implements OutputEvent {}

    /** PROGRAM_READY: the start-up level is achieved. */
    record ProgramReady() implements OutputEvent {}

    /** VALVE: open, and later close, the evacuation valve. */
    record Valve() implements OutputEvent {}

    /**
     * A regulation order for all four pumps; the actuator sends it as OPEN_PUMP(n) or
     * CLOSE_PUMP(n) for every pump n.
     */
    record SwitchPumps(PumpSwitch setting) implements OutputEvent {}

    /** OPEN_PUMP(n). */
    record OpenPump(PumpId pump) implements OutputEvent {}

    /** CLOSE_PUMP(n). */
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
