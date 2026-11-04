package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.PumpControlStatus;
import steam_boiler.domain.PumpId;
import steam_boiler.domain.PumpStatus;

/**
 * Events received by the control program. {@link Cycle} marks the end of the
 * reception stage of a five-second cycle and triggers the controller; the
 * other records are the messages the physical units transmit, all of one
 * cycle's messages being considered received simultaneously.
 */
public sealed interface InputEvent {

    /** The incoming transmission of the current cycle has been received. */
    record Cycle() implements InputEvent {}

    /** STOP: an operator request to stop. */
    record OperatorStop() implements InputEvent {}

    /** STEAM_BOILER_WAITING: the plant is ready for the program to begin. */
    record SteamBoilerWaiting() implements InputEvent {}

    /** PHYSICAL_UNITS_READY: the units' answer to PROGRAM_READY. */
    record PhysicalUnitsReady() implements InputEvent {}

    /** PUMP_STATE(n, b): the on/off state of pump n. */
    record PumpState(PumpStatus status) implements InputEvent {}

    /** PUMP_CONTROL_STATE(n, b): whether the controller of pump n sees water flowing. */
    record PumpControlState(PumpControlStatus status) implements InputEvent {}

    /** LEVEL(v): the water-level reading q, in litres. */
    record Level(@RoboChartType("real") double quantity) implements InputEvent {}

    /** STEAM(v): the steam-rate reading v, in litres per second. */
    record Steam(@RoboChartType("real") double rate) implements InputEvent {}

    /** PUMP_REPAIRED(n). */
    record PumpRepaired(PumpId pump) implements InputEvent {}

    /** PUMP_CONTROL_REPAIRED(n). */
    record PumpControlRepaired(PumpId pump) implements InputEvent {}

    /** LEVEL_REPAIRED. */
    record LevelRepaired() implements InputEvent {}

    /** STEAM_REPAIRED. */
    record SteamRepaired() implements InputEvent {}

    /** PUMP_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpFailureAcknowledgement(PumpId pump) implements InputEvent {}

    /** PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpControlFailureAcknowledgement(PumpId pump) implements InputEvent {}

    /** LEVEL_FAILURE_ACKNOWLEDGEMENT. */
    record LevelFailureAcknowledgement() implements InputEvent {}

    /** STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT. */
    record SteamOutcomeFailureAcknowledgement() implements InputEvent {}
}
