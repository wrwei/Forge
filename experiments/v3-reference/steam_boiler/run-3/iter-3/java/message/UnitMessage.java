package steam_boiler.message;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.PumpPosition;
import steam_boiler.domain.WaterFlow;

/**
 * A message the physical units send to the program over the transmission system
 * (SB-DM9). The messages of one cycle are received together by the plant monitor
 * (SB-V3); the mode controller sees their analysis, not the messages themselves.
 * Each message has a kind and, depending on the kind, a pump number n, a reading
 * (LEVEL, STEAM), a pump state (PUMP_STATE) or a flow observation (PUMP_CONTROL_STATE).
 */
public final class UnitMessage {

    private final MessageKind kind;
    @RoboChartType("nat") private final int pump;
    @RoboChartType("real") private final double reading;
    private final PumpPosition position;
    private final WaterFlow flow;

    private UnitMessage(MessageKind kind, @RoboChartType("nat") int pump, @RoboChartType("real") double reading,
            PumpPosition position, WaterFlow flow) {
        this.kind = kind;
        this.pump = pump;
        this.reading = reading;
        this.position = position;
        this.flow = flow;
    }

    private static UnitMessage signal(MessageKind kind) {
        return new UnitMessage(kind, 0, 0.0, PumpPosition.OFF, WaterFlow.NO_FLOW);
    }

    private static UnitMessage forPump(MessageKind kind, @RoboChartType("nat") int pump) {
        return new UnitMessage(kind, pump, 0.0, PumpPosition.OFF, WaterFlow.NO_FLOW);
    }

    /** STOP: operator request. */
    public static UnitMessage stopRequest() {
        return signal(MessageKind.STOP_REQUEST);
    }

    /** STEAM_BOILER_WAITING. */
    public static UnitMessage steamBoilerWaiting() {
        return signal(MessageKind.STEAM_BOILER_WAITING);
    }

    /** PHYSICAL_UNITS_READY. */
    public static UnitMessage physicalUnitsReady() {
        return signal(MessageKind.PHYSICAL_UNITS_READY);
    }

    /** PUMP_STATE(n, b): pump n is on or off (SB-DM3). */
    public static UnitMessage pumpState(@RoboChartType("nat") int pump, PumpPosition position) {
        return new UnitMessage(MessageKind.PUMP_STATE, pump, 0.0, position, WaterFlow.NO_FLOW);
    }

    /** PUMP_CONTROL_STATE(n, b): the controller of pump n sees water flowing or not (SB-DM4). */
    public static UnitMessage pumpControlState(@RoboChartType("nat") int pump, WaterFlow flow) {
        return new UnitMessage(MessageKind.PUMP_CONTROL_STATE, pump, 0.0, PumpPosition.OFF, flow);
    }

    /** LEVEL(v): water-level reading in litres (SB-DM5). */
    public static UnitMessage level(@RoboChartType("real") double litres) {
        return new UnitMessage(MessageKind.LEVEL_READING, 0, litres, PumpPosition.OFF, WaterFlow.NO_FLOW);
    }

    /** STEAM(v): steam-rate reading in litres per second (SB-DM6). */
    public static UnitMessage steam(@RoboChartType("real") double litresPerSecond) {
        return new UnitMessage(MessageKind.STEAM_READING, 0, litresPerSecond, PumpPosition.OFF, WaterFlow.NO_FLOW);
    }

    /** PUMP_REPAIRED(n). */
    public static UnitMessage pumpRepaired(@RoboChartType("nat") int pump) {
        return forPump(MessageKind.PUMP_REPAIRED, pump);
    }

    /** PUMP_CONTROL_REPAIRED(n). */
    public static UnitMessage pumpControlRepaired(@RoboChartType("nat") int pump) {
        return forPump(MessageKind.PUMP_CONTROL_REPAIRED, pump);
    }

    /** LEVEL_REPAIRED. */
    public static UnitMessage levelRepaired() {
        return signal(MessageKind.LEVEL_REPAIRED);
    }

    /** STEAM_REPAIRED. */
    public static UnitMessage steamRepaired() {
        return signal(MessageKind.STEAM_REPAIRED);
    }

    /** PUMP_FAILURE_ACKNOWLEDGEMENT(n). */
    public static UnitMessage pumpFailureAcknowledgement(@RoboChartType("nat") int pump) {
        return forPump(MessageKind.PUMP_FAILURE_ACKNOWLEDGEMENT, pump);
    }

    /** PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT(n). */
    public static UnitMessage pumpControlFailureAcknowledgement(@RoboChartType("nat") int pump) {
        return forPump(MessageKind.PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT, pump);
    }

    /** LEVEL_FAILURE_ACKNOWLEDGEMENT. */
    public static UnitMessage levelFailureAcknowledgement() {
        return signal(MessageKind.LEVEL_FAILURE_ACKNOWLEDGEMENT);
    }

    /** STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT. */
    public static UnitMessage steamOutcomeFailureAcknowledgement() {
        return signal(MessageKind.STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT);
    }

    /** The kind of message. */
    public MessageKind kind() {
        return kind;
    }

    /** The pump number n, for the per-pump kinds (0 otherwise). */
    @RoboChartType("nat")
    public int pump() {
        return pump;
    }

    /** The reading of LEVEL or STEAM (0 otherwise). */
    @RoboChartType("real")
    public double reading() {
        return reading;
    }

    /** The state reported by PUMP_STATE. */
    public PumpPosition position() {
        return position;
    }

    /** The observation reported by PUMP_CONTROL_STATE. */
    public WaterFlow flow() {
        return flow;
    }
}
