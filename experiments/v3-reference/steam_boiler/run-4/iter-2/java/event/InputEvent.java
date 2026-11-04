package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.PumpControlReading;
import steam_boiler.domain.PumpReading;

/**
 * Messages the program receives from the physical units (SB-DM9), one record
 * per message, plus the per-cycle signal that the receive stage is complete
 * (SB-V3).
 */
public sealed interface InputEvent {

    /** STOP: operator request; three in a row force emergency stop. */
    record StopRequest() implements InputEvent {}

    /** STEAM_BOILER_WAITING: the plant is ready for the program to begin. */
    record SteamBoilerWaiting() implements InputEvent {}

    /** PHYSICAL_UNITS_READY: the plant's answer to PROGRAM_READY. */
    record PhysicalUnitsReady() implements InputEvent {}

    /** PUMP_STATE(n, b): the on/off state of pump n. */
    record PumpState(PumpReading value) implements InputEvent {}

    /** PUMP_CONTROL_STATE(n, b): whether the controller of pump n sees water flowing. */
    record PumpControlState(PumpControlReading value) implements InputEvent {}

    /** LEVEL(v): the water-level reading, in litres. */
    record Level(@RoboChartType("real") double value) implements InputEvent {}

    /** STEAM(v): the steam-rate reading, in litres per second. */
    record Steam(@RoboChartType("real") double value) implements InputEvent {}

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

    /** The cycle's transmission has been received in full; the program analyses it now. */
    record TransmissionReceived() implements InputEvent {}
}
