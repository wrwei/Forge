package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.PumpControlReport;
import steam_boiler.domain.PumpStateReport;

/**
 * Messages the program receives from the physical units, one record per message, plus the
 * per-cycle trigger {@link TransmissionReceived} that starts the analysis stage of a cycle.
 * Pumps are numbered 1 to 4.
 */
public sealed interface InputEvent {

    /** The whole incoming transmission of the current cycle has been received; analyse it. */
    record TransmissionReceived() implements InputEvent {}

    /** STOP: operator request; three successive transmissions carrying it force emergency stop. */
    record StopSignal() implements InputEvent {}

    /** STEAM_BOILER_WAITING: in initialization, the plant is ready for the program to begin. */
    record SteamBoilerWaiting() implements InputEvent {}

    /** PHYSICAL_UNITS_READY: in initialization, the answer to PROGRAM_READY. */
    record PhysicalUnitsReady() implements InputEvent {}

    /** PUMP_STATE(n, b): the on/off state of pump n. */
    record PumpState(PumpStateReport report) implements InputEvent {}

    /** PUMP_CONTROL_STATE(n, b): whether the controller of pump n sees water flowing. */
    record PumpControlState(PumpControlReport report) implements InputEvent {}

    /** LEVEL(v): the water-level reading, in litres. */
    record Level(@RoboChartType("real") double litres) implements InputEvent {}

    /** STEAM(v): the steam-rate reading, in litres per second. */
    record Steam(@RoboChartType("real") double litresPerSecond) implements InputEvent {}

    /** PUMP_REPAIRED(n). */
    record PumpRepaired(@RoboChartType("nat") int pumpNumber) implements InputEvent {}

    /** PUMP_CONTROL_REPAIRED(n). */
    record PumpControlRepaired(@RoboChartType("nat") int pumpNumber) implements InputEvent {}

    /** LEVEL_REPAIRED. */
    record LevelRepaired() implements InputEvent {}

    /** STEAM_REPAIRED. */
    record SteamRepaired() implements InputEvent {}

    /** PUMP_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpFailureAcknowledgement(@RoboChartType("nat") int pumpNumber) implements InputEvent {}

    /** PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpControlFailureAcknowledgement(@RoboChartType("nat") int pumpNumber) implements InputEvent {}

    /** LEVEL_FAILURE_ACKNOWLEDGEMENT. */
    record LevelFailureAcknowledgement() implements InputEvent {}

    /** STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT. */
    record SteamOutcomeFailureAcknowledgement() implements InputEvent {}
}
