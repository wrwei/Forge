package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * Everything the program receives from the transmission system: one record
 * per input message of SB-DM9, plus {@link Transmission}, which marks the end
 * of the receive stage of a cycle (SB-V3). All messages of one cycle are
 * considered received simultaneously; the controller is stepped once per cycle
 * with {@link Transmission}.
 */
public sealed interface InputEvent {

    /** The whole transmission of the current cycle has been received (SB-V3). */
    record Transmission() implements InputEvent {
    }

    /** STOP: operator request (SB-DM9, SB-Beh23). */
    record StopRequest() implements InputEvent {
    }

    /** STEAM_BOILER_WAITING: the plant is ready for the program to begin (SB-Beh2). */
    record SteamBoilerWaiting() implements InputEvent {
    }

    /** PHYSICAL_UNITS_READY: answer to PROGRAM_READY (SB-Beh7, SB-Beh8). */
    record PhysicalUnitsReady() implements InputEvent {
    }

    /**
     * PUMP_STATE(n, b): on/off state of pump n.
     *
     * @param reading the pump number and its state
     */
    record PumpState(PumpReading reading) implements InputEvent {
    }

    /**
     * PUMP_CONTROL_STATE(n, b): whether the controller of pump n sees water flowing.
     *
     * @param reading the pump number and the observed flow
     */
    record PumpControlState(FlowReading reading) implements InputEvent {
    }

    /**
     * LEVEL(v): water-level reading (litre).
     *
     * @param measuredLevel the reported quantity of water
     */
    record Level(@RoboChartType("real") double measuredLevel) implements InputEvent {
    }

    /**
     * STEAM(v): steam-rate reading (litre/sec).
     *
     * @param measuredSteam the reported quantity of steam
     */
    record Steam(@RoboChartType("real") double measuredSteam) implements InputEvent {
    }

    /**
     * PUMP_REPAIRED(n).
     *
     * @param pumpNumber the repaired pump, 1 to 4
     */
    record PumpRepaired(@RoboChartType("nat") int pumpNumber) implements InputEvent {
    }

    /**
     * PUMP_CONTROL_REPAIRED(n).
     *
     * @param pumpNumber the pump whose controller was repaired, 1 to 4
     */
    record PumpControlRepaired(@RoboChartType("nat") int pumpNumber) implements InputEvent {
    }

    /** LEVEL_REPAIRED. */
    record LevelRepaired() implements InputEvent {
    }

    /** STEAM_REPAIRED. */
    record SteamRepaired() implements InputEvent {
    }

    /**
     * PUMP_FAILURE_ACKNOWLEDGEMENT(n).
     *
     * @param pumpNumber the acknowledged pump, 1 to 4
     */
    record PumpFailureAcknowledgement(@RoboChartType("nat") int pumpNumber) implements InputEvent {
    }

    /**
     * PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT(n).
     *
     * @param pumpNumber the pump whose controller failure is acknowledged, 1 to 4
     */
    record PumpControlFailureAcknowledgement(@RoboChartType("nat") int pumpNumber) implements InputEvent {
    }

    /** LEVEL_FAILURE_ACKNOWLEDGEMENT. */
    record LevelFailureAcknowledgement() implements InputEvent {
    }

    /** STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT. */
    record SteamOutcomeFailureAcknowledgement() implements InputEvent {
    }
}
