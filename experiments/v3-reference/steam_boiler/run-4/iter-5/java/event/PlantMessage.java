package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.PumpControlReading;
import steam_boiler.domain.PumpReading;

/**
 * Messages the program receives from the physical units (SB-DM9), one record
 * per message. They are consumed by the receive stage
 * ({@code BoilerSensor.receive}); the controller reacts to the analysed
 * transmission, not to individual messages.
 */
public sealed interface PlantMessage {

    /** STOP: operator request; three in a row force emergency stop. */
    record StopRequest() implements PlantMessage {}

    /** STEAM_BOILER_WAITING: the plant is ready for the program to begin. */
    record SteamBoilerWaiting() implements PlantMessage {}

    /** PHYSICAL_UNITS_READY: the plant's answer to PROGRAM_READY. */
    record PhysicalUnitsReady() implements PlantMessage {}

    /** PUMP_STATE(n, b): the on/off state of pump n. */
    record PumpState(PumpReading value) implements PlantMessage {}

    /** PUMP_CONTROL_STATE(n, b): whether the controller of pump n sees water flowing. */
    record PumpControlState(PumpControlReading value) implements PlantMessage {}

    /** LEVEL(v): the water-level reading, in litres. */
    record Level(@RoboChartType("real") double value) implements PlantMessage {}

    /** STEAM(v): the steam-rate reading, in litres per second. */
    record Steam(@RoboChartType("real") double value) implements PlantMessage {}

    /** PUMP_REPAIRED(n). */
    record PumpRepaired(@RoboChartType("nat") int pump) implements PlantMessage {}

    /** PUMP_CONTROL_REPAIRED(n). */
    record PumpControlRepaired(@RoboChartType("nat") int pump) implements PlantMessage {}

    /** LEVEL_REPAIRED. */
    record LevelRepaired() implements PlantMessage {}

    /** STEAM_REPAIRED. */
    record SteamRepaired() implements PlantMessage {}

    /** PUMP_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpFailureAcknowledgement(@RoboChartType("nat") int pump) implements PlantMessage {}

    /** PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpControlFailureAcknowledgement(@RoboChartType("nat") int pump) implements PlantMessage {}

    /** LEVEL_FAILURE_ACKNOWLEDGEMENT. */
    record LevelFailureAcknowledgement() implements PlantMessage {}

    /** STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT. */
    record SteamOutcomeFailureAcknowledgement() implements PlantMessage {}
}
