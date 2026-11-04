package steam_boiler.message;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.PumpPosition;
import steam_boiler.domain.WaterFlow;

/**
 * A message the physical units send to the program over the transmission system
 * (SB-DM9). The messages of one cycle are received together by the plant monitor
 * (SB-V3); the mode controller sees their analysis, not the messages themselves.
 */
public interface UnitMessage {

    /** STOP: operator request; three in successive transmissions demand emergency stop. */
    final class StopRequest implements UnitMessage {
    }

    /** STEAM_BOILER_WAITING: the plant is ready for the program to begin. */
    final class SteamBoilerWaiting implements UnitMessage {
    }

    /** PHYSICAL_UNITS_READY: the units' answer to PROGRAM_READY. */
    final class PhysicalUnitsReady implements UnitMessage {
    }

    /** PUMP_STATE(n, b): the on/off state of pump n (SB-DM3). */
    final class PumpState implements UnitMessage {
        @RoboChartType("nat") private final int pump;
        private final PumpPosition position;

        /** Pump {@code pump} reports {@code position}. */
        public PumpState(@RoboChartType("nat") int pump, PumpPosition position) {
            this.pump = pump;
            this.position = position;
        }

        /** The pump number n. */
        @RoboChartType("nat")
        public int pump() {
            return pump;
        }

        /** The reported state b. */
        public PumpPosition position() {
            return position;
        }
    }

    /** PUMP_CONTROL_STATE(n, b): whether the controller of pump n sees water flowing (SB-DM4). */
    final class PumpControlState implements UnitMessage {
        @RoboChartType("nat") private final int pump;
        private final WaterFlow flow;

        /** The controller of pump {@code pump} reports {@code flow}. */
        public PumpControlState(@RoboChartType("nat") int pump, WaterFlow flow) {
            this.pump = pump;
            this.flow = flow;
        }

        /** The pump number n. */
        @RoboChartType("nat")
        public int pump() {
            return pump;
        }

        /** The reported observation b. */
        public WaterFlow flow() {
            return flow;
        }
    }

    /** LEVEL(v): the water-level reading, in litres (SB-DM5). */
    final class Level implements UnitMessage {
        @RoboChartType("real") private final double litres;

        /** A level reading of {@code litres}. */
        public Level(@RoboChartType("real") double litres) {
            this.litres = litres;
        }

        /** The reading q. */
        @RoboChartType("real")
        public double litres() {
            return litres;
        }
    }

    /** STEAM(v): the steam-rate reading, in litres per second (SB-DM6). */
    final class Steam implements UnitMessage {
        @RoboChartType("real") private final double litresPerSecond;

        /** A steam-rate reading of {@code litresPerSecond}. */
        public Steam(@RoboChartType("real") double litresPerSecond) {
            this.litresPerSecond = litresPerSecond;
        }

        /** The reading v. */
        @RoboChartType("real")
        public double litresPerSecond() {
            return litresPerSecond;
        }
    }

    /** PUMP_REPAIRED(n). */
    final class PumpRepaired implements UnitMessage {
        @RoboChartType("nat") private final int pump;

        /** Pump {@code pump} has been repaired. */
        public PumpRepaired(@RoboChartType("nat") int pump) {
            this.pump = pump;
        }

        /** The pump number n. */
        @RoboChartType("nat")
        public int pump() {
            return pump;
        }
    }

    /** PUMP_CONTROL_REPAIRED(n). */
    final class PumpControlRepaired implements UnitMessage {
        @RoboChartType("nat") private final int pump;

        /** The controller of pump {@code pump} has been repaired. */
        public PumpControlRepaired(@RoboChartType("nat") int pump) {
            this.pump = pump;
        }

        /** The pump number n. */
        @RoboChartType("nat")
        public int pump() {
            return pump;
        }
    }

    /** LEVEL_REPAIRED. */
    final class LevelRepaired implements UnitMessage {
    }

    /** STEAM_REPAIRED. */
    final class SteamRepaired implements UnitMessage {
    }

    /** PUMP_FAILURE_ACKNOWLEDGEMENT(n). */
    final class PumpFailureAcknowledgement implements UnitMessage {
        @RoboChartType("nat") private final int pump;

        /** The failure detection of pump {@code pump} is acknowledged. */
        public PumpFailureAcknowledgement(@RoboChartType("nat") int pump) {
            this.pump = pump;
        }

        /** The pump number n. */
        @RoboChartType("nat")
        public int pump() {
            return pump;
        }
    }

    /** PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT(n). */
    final class PumpControlFailureAcknowledgement implements UnitMessage {
        @RoboChartType("nat") private final int pump;

        /** The failure detection of the controller of pump {@code pump} is acknowledged. */
        public PumpControlFailureAcknowledgement(@RoboChartType("nat") int pump) {
            this.pump = pump;
        }

        /** The pump number n. */
        @RoboChartType("nat")
        public int pump() {
            return pump;
        }
    }

    /** LEVEL_FAILURE_ACKNOWLEDGEMENT. */
    final class LevelFailureAcknowledgement implements UnitMessage {
    }

    /** STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT. */
    final class SteamOutcomeFailureAcknowledgement implements UnitMessage {
    }
}
