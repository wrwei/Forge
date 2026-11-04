package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * Events received by the program. {@link Transmission} marks the arrival of one
 * cycle's incoming transmission (SB-V3) and is the event the controller steps on;
 * the other records are the messages a transmission can carry (SB-DM9).
 */
public sealed interface InputEvent {

    /** The incoming transmission of the current cycle has been received (SB-V3). */
    record Transmission() implements InputEvent {}

    /** STOP: an operator request. */
    record StopMessage() implements InputEvent {}

    /** STEAM_BOILER_WAITING: the plant is ready for the program to begin. */
    record SteamBoilerWaiting() implements InputEvent {}

    /** PHYSICAL_UNITS_READY: the units' answer to PROGRAM_READY. */
    record PhysicalUnitsReady() implements InputEvent {}

    /** PUMP_STATE(n, b): the on/off state of pump n. */
    record PumpState(PumpReading reading) implements InputEvent {}

    /** PUMP_CONTROL_STATE(n, b): whether the controller of pump n sees water flowing. */
    record PumpControlState(PumpControlReading reading) implements InputEvent {}

    /** LEVEL(v): the water-level reading, in litres. */
    record LevelMessage(@RoboChartType("real") double litres) implements InputEvent {}

    /** STEAM(v): the steam-rate reading, in litres per second. */
    record SteamMessage(@RoboChartType("real") double rate) implements InputEvent {}

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
