package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.FlowReport;
import steam_boiler.domain.PumpReport;

/**
 * Inputs of the control program.
 *
 * <p>{@link TransmissionReceived} marks the end of the first stage of a cycle (SB-V3): the cycle's
 * incoming messages have all been received, simultaneously, and the program now analyses them. It
 * is the event the controller's {@code step} is called with, once per cycle.
 *
 * <p>Every other record is one message the physical units send (SB-DM9). A cycle's transmission is
 * the list of those messages; the sensor decodes it before the controller steps.
 */
public sealed interface InputEvent {

    /** The cycle's transmission has been received and is ready for analysis (SB-V3). */
    record TransmissionReceived() implements InputEvent {}

    /** STOP: operator request; three successive transmissions force emergency stop. */
    record StopRequest() implements InputEvent {}

    /** STEAM_BOILER_WAITING: in initialization, the plant is ready for the program to begin. */
    record SteamBoilerWaiting() implements InputEvent {}

    /** PHYSICAL_UNITS_READY: in initialization, the answer to PROGRAM_READY. */
    record PhysicalUnitsReady() implements InputEvent {}

    /** PUMP_STATE(n, b): the on/off state of pump n; required in every transmission. */
    record PumpState(PumpReport report) implements InputEvent {}

    /** PUMP_CONTROL_STATE(n, b): whether the controller of pump n sees water flowing. */
    record PumpControlState(FlowReport report) implements InputEvent {}

    /** LEVEL(v): the water-level reading (litre); required in every transmission. */
    record Level(@RoboChartType("real") double value) implements InputEvent {}

    /** STEAM(v): the steam-rate reading (litre/sec); required in every transmission. */
    record Steam(@RoboChartType("real") double value) implements InputEvent {}

    /** PUMP_REPAIRED(n): pump n announces its repair. */
    record PumpRepaired(@RoboChartType("nat") int pump) implements InputEvent {}

    /** PUMP_CONTROL_REPAIRED(n): the controller of pump n announces its repair. */
    record PumpControlRepaired(@RoboChartType("nat") int pump) implements InputEvent {}

    /** LEVEL_REPAIRED: the level measuring device announces its repair. */
    record LevelRepaired() implements InputEvent {}

    /** STEAM_REPAIRED: the steam measuring device announces its repair. */
    record SteamRepaired() implements InputEvent {}

    /** PUMP_FAILURE_ACKNOWLEDGEMENT(n): acknowledges PUMP_FAILURE_DETECTION(n). */
    record PumpFailureAcknowledgement(@RoboChartType("nat") int pump) implements InputEvent {}

    /** PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT(n): acknowledges PUMP_CONTROL_FAILURE_DETECTION(n). */
    record PumpControlFailureAcknowledgement(@RoboChartType("nat") int pump) implements InputEvent {}

    /** LEVEL_FAILURE_ACKNOWLEDGEMENT: acknowledges LEVEL_FAILURE_DETECTION. */
    record LevelFailureAcknowledgement() implements InputEvent {}

    /** STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT: acknowledges STEAM_FAILURE_DETECTION. */
    record SteamOutcomeFailureAcknowledgement() implements InputEvent {}
}
