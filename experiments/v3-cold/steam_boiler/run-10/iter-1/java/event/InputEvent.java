package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.FlowReport;
import steam_boiler.domain.PumpReport;

/**
 * Messages the program receives from the physical units (SB-DM9), plus {@link CycleStart}, which
 * closes the receive stage of a cycle and starts the analysis stage (SB-V3).
 */
public sealed interface InputEvent {

    /** The incoming messages of the current cycle have all been received; analyse them (SB-V3). */
    record CycleStart() implements InputEvent {}

    /** STOP: an operator request (SB-DM9, SB-Beh23). */
    record StopRequest() implements InputEvent {}

    /** STEAM_BOILER_WAITING: the plant is ready for the program to begin (SB-DM9, SB-Beh2). */
    record SteamBoilerWaiting() implements InputEvent {}

    /** PHYSICAL_UNITS_READY: the answer to PROGRAM_READY (SB-DM9, SB-Beh8). */
    record PhysicalUnitsReady() implements InputEvent {}

    /** PUMP_STATE(n, b): the on/off state of pump n (SB-DM9). */
    record PumpState(PumpReport report) implements InputEvent {}

    /** PUMP_CONTROL_STATE(n, b): whether the controller of pump n sees water flowing (SB-DM9). */
    record PumpControlState(FlowReport report) implements InputEvent {}

    /** LEVEL(v): the water-level reading q, in litres (SB-DM5, SB-DM9). */
    record Level(@RoboChartType("real") double litres) implements InputEvent {}

    /** STEAM(v): the steam-rate reading v, in litres per second (SB-DM6, SB-DM9). */
    record Steam(@RoboChartType("real") double litresPerSecond) implements InputEvent {}

    /** PUMP_REPAIRED(n) (SB-DM9, SB-FR7). */
    record PumpRepaired(@RoboChartType("nat") int pumpNumber) implements InputEvent {}

    /** PUMP_CONTROL_REPAIRED(n) (SB-DM9, SB-FR7). */
    record PumpControlRepaired(@RoboChartType("nat") int pumpNumber) implements InputEvent {}

    /** LEVEL_REPAIRED (SB-DM9, SB-FR7). */
    record LevelRepaired() implements InputEvent {}

    /** STEAM_REPAIRED (SB-DM9, SB-FR7). */
    record SteamRepaired() implements InputEvent {}

    /** PUMP_FAILURE_ACKNOWLEDGEMENT(n) (SB-DM9, SB-FR6). */
    record PumpFailureAcknowledgement(@RoboChartType("nat") int pumpNumber) implements InputEvent {}

    /** PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT(n) (SB-DM9, SB-FR6). */
    record PumpControlFailureAcknowledgement(@RoboChartType("nat") int pumpNumber)
            implements InputEvent {}

    /** LEVEL_FAILURE_ACKNOWLEDGEMENT (SB-DM9, SB-FR6). */
    record LevelFailureAcknowledgement() implements InputEvent {}

    /** STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT (SB-DM9, SB-FR6). */
    record SteamOutcomeFailureAcknowledgement() implements InputEvent {}
}
