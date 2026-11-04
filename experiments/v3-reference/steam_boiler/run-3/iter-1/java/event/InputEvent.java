package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.PumpControlReport;
import steam_boiler.domain.PumpStateReport;

/**
 * Inputs of the steam-boiler control program.
 *
 * <p>All records except {@link TransmissionReceived} are the messages the physical
 * units send over the transmission system (SB-DM9). The messages of one cycle are
 * received together (SB-V3); {@link TransmissionReceived} marks the end of that
 * reception stage and triggers the controller's analysis of the cycle.
 */
public sealed interface InputEvent {

    /** The incoming transmission of the current cycle has been received (SB-V3). */
    record TransmissionReceived() implements InputEvent {}

    /** STOP: operator request; three in successive transmissions demand emergency stop. */
    record StopRequest() implements InputEvent {}

    /** STEAM_BOILER_WAITING: the plant is ready for the program to begin. */
    record SteamBoilerWaiting() implements InputEvent {}

    /** PHYSICAL_UNITS_READY: the units' answer to PROGRAM_READY. */
    record PhysicalUnitsReady() implements InputEvent {}

    /** PUMP_STATE(n, b): the on/off state of pump n. */
    record PumpState(PumpStateReport report) implements InputEvent {}

    /** PUMP_CONTROL_STATE(n, b): whether the controller of pump n sees water flowing. */
    record PumpControlState(PumpControlReport report) implements InputEvent {}

    /** LEVEL(v): the water-level reading, in litres (SB-DM5). */
    record Level(@RoboChartType("real") double litres) implements InputEvent {}

    /** STEAM(v): the steam-rate reading, in litres per second (SB-DM6). */
    record Steam(@RoboChartType("real") double litresPerSecond) implements InputEvent {}

    /** PUMP_REPAIRED(n). */
    record PumpRepaired(@RoboChartType("nat") int pump) implements InputEvent {}

    /** PUMP_CONTROL_REPAIRED(n). */
    record PumpControlRepaired(@RoboChartType("nat") int pump) implements InputEvent {}

    /** LEVEL_REPAIRED. */
    record LevelRepaired() implements InputEvent {}

    /** STEAM_REPAIRED. */
    record SteamRepaired() implements InputEvent {}

    /** PUMP_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpFailureAcknowledgement(@RoboChartType("nat") int pump) implements InputEvent {}

    /** PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpControlFailureAcknowledgement(@RoboChartType("nat") int pump) implements InputEvent {}

    /** LEVEL_FAILURE_ACKNOWLEDGEMENT. */
    record LevelFailureAcknowledgement() implements InputEvent {}

    /** STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT. */
    record SteamOutcomeFailureAcknowledgement() implements InputEvent {}
}
