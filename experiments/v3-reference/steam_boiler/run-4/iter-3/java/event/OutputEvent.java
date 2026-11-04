package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.BoilerMode;

/**
 * The commands the controller issues to the physical units (SB-DM8), one
 * record per message. The failure/repair protocol messages are in
 * {@link ProtocolMessage}.
 */
public sealed interface OutputEvent extends Message {

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
}
