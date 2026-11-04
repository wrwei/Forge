package steam_boiler.sensor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.actuator.Transmitter;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.BoilerConstants;
import steam_boiler.domain.FlowObservation;
import steam_boiler.domain.PumpPosition;
import steam_boiler.domain.ValvePosition;
import steam_boiler.event.InputEvent;
import steam_boiler.mode.OperatingMode;

/**
 * The receive stage of a cycle. It takes every message of the cycle's incoming transmission at
 * once, checks the transmission, processes repair reports and failure acknowledgements, judges the
 * units against the failure-detection rules, maintains the water-level estimate, forecasts the
 * level at the next cycle under worst-case dynamics, and publishes the result to the
 * {@link PlantSensor}.
 */
public final class MessageReceiver {

    private final PlantSensor sensor;
    private final UnitStatusTable units;
    private final Transmitter transmitter;

    @RoboChartType("nat") private int successiveStops;
    private boolean plantStarted;
    private boolean previousLevelValid;
    @RoboChartType("real") private double previousLevel;
    private boolean previousSteamValid;
    @RoboChartType("real") private double previousSteam;
    @RoboChartType("real") private double previousSteamVolumeMax;
    @RoboChartType("real") private double previousSteamVolumeMin;
    private boolean intervalValid;
    @RoboChartType("real") private double levelLow;
    @RoboChartType("real") private double levelHigh;
    private final List<Boolean> previousReportedOn = flags(BoilerConstants.PUMP_COUNT);
    private final List<Boolean> previousPouring = flags(BoilerConstants.PUMP_COUNT);

    /** Creates the receive stage publishing to {@code sensor}. */
    public MessageReceiver(PlantSensor sensor, UnitStatusTable units, Transmitter transmitter) {
        this.sensor = sensor;
        this.units = units;
        this.transmitter = transmitter;
    }

    /** Receives all the messages of one cycle's incoming transmission, considered simultaneous. */
    public void receive(List<InputEvent> messages) {
        units.startCycle();
        int pumpCount = BoilerConstants.PUMP_COUNT;
        boolean stop = false;
        boolean waiting = false;
        boolean unitsReady = false;
        boolean aberrant = false;
        boolean levelSeen = false;
        boolean steamSeen = false;
        double level = sensor.levelReading();
        double steam = sensor.steamReading();
        List<Boolean> stateSeen = flags(pumpCount);
        List<Boolean> reportedOn = flags(pumpCount);
        List<Boolean> flowSeen = flags(pumpCount);
        List<Boolean> reportedFlow = flags(pumpCount);

        for (int i = 0; i < messages.size(); i++) {
            InputEvent message = messages.get(i);
            if (message instanceof InputEvent.StopRequest) {
                stop = true;
            } else if (message instanceof InputEvent.SteamBoilerWaiting) {
                waiting = true;
            } else if (message instanceof InputEvent.PhysicalUnitsReady) {
                unitsReady = true;
            } else if (message instanceof InputEvent.Level) {
                InputEvent.Level reading = (InputEvent.Level) message;
                if (levelSeen) {
                    aberrant = true;
                }
                levelSeen = true;
                level = reading.value();
            } else if (message instanceof InputEvent.Steam) {
                InputEvent.Steam reading = (InputEvent.Steam) message;
                if (steamSeen) {
                    aberrant = true;
                }
                steamSeen = true;
                steam = reading.value();
            } else if (message instanceof InputEvent.PumpState) {
                InputEvent.PumpState report = (InputEvent.PumpState) message;
                int pump = report.reading().pump();
                if (!units.isPumpNumber(pump) || stateSeen.get(pump - 1)) {
                    aberrant = true;
                } else {
                    stateSeen.set(pump - 1, true);
                    reportedOn.set(pump - 1, report.reading().position() == PumpPosition.PUMP_ON);
                }
            } else if (message instanceof InputEvent.PumpControlState) {
                InputEvent.PumpControlState report = (InputEvent.PumpControlState) message;
                int pump = report.reading().pump();
                if (!units.isPumpNumber(pump) || flowSeen.get(pump - 1)) {
                    aberrant = true;
                } else {
                    flowSeen.set(pump - 1, true);
                    reportedFlow.set(pump - 1, report.reading().flow() == FlowObservation.WATER_FLOWING);
                }
            } else if (message instanceof InputEvent.PumpRepaired) {
                InputEvent.PumpRepaired report = (InputEvent.PumpRepaired) message;
                if (units.isPumpNumber(report.pump())) {
                    units.pump(report.pump()).recordRepair();
                } else {
                    aberrant = true;
                }
            } else if (message instanceof InputEvent.PumpControlRepaired) {
                InputEvent.PumpControlRepaired report = (InputEvent.PumpControlRepaired) message;
                if (units.isPumpNumber(report.pump())) {
                    units.pumpControl(report.pump()).recordRepair();
                } else {
                    aberrant = true;
                }
            } else if (message instanceof InputEvent.LevelRepaired) {
                units.levelDevice().recordRepair();
                previousLevelValid = false;
            } else if (message instanceof InputEvent.SteamRepaired) {
                units.steamDevice().recordRepair();
                previousSteamValid = false;
            } else if (message instanceof InputEvent.PumpFailureAcknowledgement) {
                InputEvent.PumpFailureAcknowledgement ack = (InputEvent.PumpFailureAcknowledgement) message;
                if (units.isPumpNumber(ack.pump())) {
                    units.pump(ack.pump()).recordFailureAcknowledgement();
                } else {
                    aberrant = true;
                }
            } else if (message instanceof InputEvent.PumpControlFailureAcknowledgement) {
                InputEvent.PumpControlFailureAcknowledgement ack =
                        (InputEvent.PumpControlFailureAcknowledgement) message;
                if (units.isPumpNumber(ack.pump())) {
                    units.pumpControl(ack.pump()).recordFailureAcknowledgement();
                } else {
                    aberrant = true;
                }
            } else if (message instanceof InputEvent.LevelFailureAcknowledgement) {
                units.levelDevice().recordFailureAcknowledgement();
            } else if (message instanceof InputEvent.SteamOutcomeFailureAcknowledgement) {
                units.steamDevice().recordFailureAcknowledgement();
            }
        }

        // SB-FR5: the four kinds of data message must be present in every transmission; the
        // initialization messages are aberrant outside initialization, and PHYSICAL_UNITS_READY
        // is aberrant before PROGRAM_READY has been sent.
        boolean missing = !levelSeen || !steamSeen;
        for (int i = 0; i < pumpCount; i++) {
            if (!stateSeen.get(i) || !flowSeen.get(i)) {
                missing = true;
            }
        }
        boolean initializing = transmitter.lastMode() == OperatingMode.INITIALIZATION;
        if (!initializing && (waiting || unitsReady)) {
            aberrant = true;
        }
        if (unitsReady && !transmitter.programReadySent()) {
            aberrant = true;
        }

        // SB-V10: the length of the current run of transmissions carrying STOP.
        if (stop) {
            if (successiveStops < BoilerConstants.STOP_LIMIT) {
                successiveStops = successiveStops + 1;
            }
        } else {
            successiveStops = 0;
        }

        if (waiting) {
            plantStarted = true;
        }
        if (plantStarted) {
            judgeSteamDevice(steamSeen, steam);
            judgeLevelDevice(levelSeen, level);
            judgePumps(stateSeen, reportedOn, flowSeen, reportedFlow);
        }

        // SB-V7: throughput of the pumps reported on and seen flowing; the most they can pour.
        int pouring = 0;
        int running = 0;
        for (int i = 0; i < pumpCount; i++) {
            if (stateSeen.get(i) && reportedOn.get(i)) {
                running = running + 1;
                if (flowSeen.get(i) && reportedFlow.get(i)) {
                    pouring = pouring + 1;
                }
            }
        }
        double cycle = BoilerConstants.CYCLE_SECONDS;
        double throughput = BoilerConstants.PUMP_CAPACITY_P * pouring;
        double throughputMax = BoilerConstants.PUMP_CAPACITY_P * running;

        // SB-DM2: steam that can leave during the coming cycle, from the reading when the device
        // is sound, otherwise from the whole range 0..W.
        boolean steamTrusted = steamSeen && !units.steamDevice().isOutOfOrder();
        double rateHigh = BoilerConstants.STEAM_MAX_W;
        double rateLow = 0.0;
        if (steamTrusted) {
            rateHigh = steam;
            rateLow = steam;
        }
        double volumeMax = rateHigh * cycle + BoilerConstants.STEAM_RISE_U1 * cycle * cycle / 2;
        if (volumeMax > BoilerConstants.STEAM_MAX_W * cycle) {
            volumeMax = BoilerConstants.STEAM_MAX_W * cycle;
        }
        double volumeMin = rateLow * cycle - BoilerConstants.STEAM_FALL_U2 * cycle * cycle / 2;
        if (volumeMin < 0.0) {
            volumeMin = 0.0;
        }

        // SB-V8, SB-Beh19: the level interval. With a working level device it is the reading;
        // otherwise the previous interval advanced over the last cycle by the steam bounds and
        // the water known to be supplied (pumps reported on and seen pouring then, and still
        // ordered on afterwards). A pump ordered on in the last cycle supplied nothing yet.
        boolean levelTrusted = levelSeen && !units.levelDevice().isOutOfOrder();
        if (levelTrusted) {
            levelLow = level;
            levelHigh = level;
        } else if (intervalValid) {
            int knownSupply = 0;
            int possibleSupply = 0;
            for (int i = 0; i < pumpCount; i++) {
                boolean stillOn = transmitter.pumpOrderedOn(i + 1);
                if (stillOn && previousPouring.get(i)) {
                    knownSupply = knownSupply + 1;
                }
                if (stillOn && previousReportedOn.get(i)) {
                    possibleSupply = possibleSupply + 1;
                }
            }
            levelLow = levelLow + BoilerConstants.PUMP_CAPACITY_P * knownSupply * cycle - previousSteamVolumeMax;
            levelHigh = levelHigh + BoilerConstants.PUMP_CAPACITY_P * possibleSupply * cycle - previousSteamVolumeMin;
            if (transmitter.valvePosition() == ValvePosition.VALVE_OPEN) {
                levelLow = 0.0;
            }
        } else {
            levelLow = 0.0;
            levelHigh = BoilerConstants.CAPACITY_C;
        }
        intervalValid = true;

        // SB-Beh13, SB-Beh25: worst-case level at the next cycle. Low side: the pumps now pouring
        // are kept and the most steam leaves; high side: every pump reported on pours and the
        // least steam leaves; overflow is judged with all pumps closed, which takes effect at once.
        double lowest = levelLow + throughput * cycle - volumeMax;
        double highest = levelHigh + throughputMax * cycle - volumeMin;
        double highestClosed = levelHigh - volumeMin;
        boolean limitRisk = lowest < BoilerConstants.LIMIT_M1 || highestClosed > BoilerConstants.LIMIT_M2;

        previousLevelValid = levelTrusted;
        if (levelTrusted) {
            previousLevel = level;
        }
        previousSteamValid = steamTrusted;
        if (steamTrusted) {
            previousSteam = steam;
        }
        previousSteamVolumeMax = volumeMax;
        previousSteamVolumeMin = volumeMin;
        for (int i = 0; i < pumpCount; i++) {
            previousReportedOn.set(i, stateSeen.get(i) && reportedOn.get(i));
            previousPouring.set(i, stateSeen.get(i) && reportedOn.get(i) && flowSeen.get(i) && reportedFlow.get(i));
        }

        boolean steamDeviceDown = units.steamDevice().isOutOfOrder();
        boolean controlDown = units.anyPumpControlOutOfOrder();
        sensor.recordQuantities(level, steam, throughput, levelLow, levelHigh);
        sensor.recordMessages(successiveStops, plantStarted, unitsReady, missing || aberrant);
        sensor.recordUnits(
                units.levelDevice().isOutOfOrder(),
                steamDeviceDown || controlDown || units.anyPumpOutOfOrder(),
                steamDeviceDown || controlDown);
        sensor.recordLevelJudgements(
                steam != 0.0,
                level > BoilerConstants.NORMAL_N2,
                level < BoilerConstants.NORMAL_N1,
                limitRisk,
                lowest < BoilerConstants.NORMAL_N1,
                highest > BoilerConstants.NORMAL_N2);
    }

    /** SB-FR4: outside 0..W, or changed faster than U1 (increase) or U2 (decrease) allow. */
    private void judgeSteamDevice(boolean steamSeen, @RoboChartType("real") double steam) {
        UnitRecord device = units.steamDevice();
        if (!steamSeen || device.isOutOfOrder()) {
            return;
        }
        boolean outOfRange = steam < 0.0 || steam > BoilerConstants.STEAM_MAX_W;
        boolean tooFast = false;
        if (previousSteamValid) {
            double cycle = BoilerConstants.CYCLE_SECONDS;
            tooFast = steam > previousSteam + BoilerConstants.STEAM_RISE_U1 * cycle
                    || steam < previousSteam - BoilerConstants.STEAM_FALL_U2 * cycle;
        }
        if (outOfRange || tooFast) {
            device.recordFailure();
        }
    }

    /**
     * SB-FR3: outside 0..C, or a level the boiler could not have reached from the previous reading
     * even with every pump pouring and the least steam leaving (rise), or with no pump pouring and
     * the most steam leaving (fall). The fall bound is not applied while the valve was open.
     */
    private void judgeLevelDevice(boolean levelSeen, @RoboChartType("real") double level) {
        UnitRecord device = units.levelDevice();
        if (!levelSeen || device.isOutOfOrder()) {
            return;
        }
        boolean outOfRange = level < 0.0 || level > BoilerConstants.CAPACITY_C;
        boolean incompatible = false;
        if (previousLevelValid) {
            double highest = previousLevel
                    + BoilerConstants.PUMP_COUNT * BoilerConstants.PUMP_CAPACITY_P * BoilerConstants.CYCLE_SECONDS
                    - previousSteamVolumeMin;
            double lowest = previousLevel - previousSteamVolumeMax;
            boolean valveClosed = transmitter.valvePosition() == ValvePosition.VALVE_CLOSED;
            incompatible = level > highest || (valveClosed && level < lowest);
        }
        if (outOfRange || incompatible) {
            device.recordFailure();
        }
    }

    /**
     * SB-FR1: a pump whose reported state differs from the last order (it did not follow an order
     * by the next transmission, or changed spontaneously). SB-FR2: a controller whose flow report
     * differs from the order two or more transmissions after it, while the pump itself is known to
     * be working.
     */
    private void judgePumps(
            List<Boolean> stateSeen,
            List<Boolean> reportedOn,
            List<Boolean> flowSeen,
            List<Boolean> reportedFlow) {
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            int pump = i + 1;
            UnitRecord pumpRecord = units.pump(pump);
            boolean orderedOn = transmitter.pumpOrderedOn(pump);
            if (stateSeen.get(i) && !pumpRecord.isOutOfOrder() && reportedOn.get(i) != orderedOn) {
                pumpRecord.recordFailure();
            }
            boolean pumpWorking = stateSeen.get(i) && !pumpRecord.isOutOfOrder();
            UnitRecord controlRecord = units.pumpControl(pump);
            boolean settled = transmitter.transmissionsSinceOrder(pump) >= 2;
            if (flowSeen.get(i) && pumpWorking && settled && !controlRecord.isOutOfOrder()
                    && reportedFlow.get(i) != orderedOn) {
                controlRecord.recordFailure();
            }
        }
    }

    private static List<Boolean> flags(@RoboChartType("nat") int count) {
        List<Boolean> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(false);
        }
        return result;
    }
}
