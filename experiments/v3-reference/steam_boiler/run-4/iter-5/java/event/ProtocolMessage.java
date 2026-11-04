package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * The failure/repair protocol messages the program sends (SB-DM8, SB-FR6,
 * SB-FR7), one record per message. They are produced by the unit bookkeeping
 * ({@code BoilerSensor.protocolMessages}) and transmitted with the
 * controller's commands.
 */
public sealed interface ProtocolMessage extends Message {

    /** PUMP_FAILURE_DETECTION(n). */
    record PumpFailureDetection(@RoboChartType("nat") int pump) implements ProtocolMessage {}

    /** PUMP_CONTROL_FAILURE_DETECTION(n). */
    record PumpControlFailureDetection(@RoboChartType("nat") int pump) implements ProtocolMessage {}

    /** LEVEL_FAILURE_DETECTION. */
    record LevelFailureDetection() implements ProtocolMessage {}

    /** STEAM_FAILURE_DETECTION. */
    record SteamFailureDetection() implements ProtocolMessage {}

    /** PUMP_REPAIRED_ACKNOWLEDGEMENT(n). */
    record PumpRepairedAcknowledgement(@RoboChartType("nat") int pump) implements ProtocolMessage {}

    /** PUMP_CONTROL_REPAIRED_ACKNOWLEDGEMENT(n). */
    record PumpControlRepairedAcknowledgement(@RoboChartType("nat") int pump) implements ProtocolMessage {}

    /** LEVEL_REPAIRED_ACKNOWLEDGEMENT. */
    record LevelRepairedAcknowledgement() implements ProtocolMessage {}

    /** STEAM_REPAIRED_ACKNOWLEDGEMENT. */
    record SteamRepairedAcknowledgement() implements ProtocolMessage {}
}
