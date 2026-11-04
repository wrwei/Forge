package steam_boiler.sensor;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.SteamBoilerConstants;
import steam_boiler.event.OutputEvent;

/**
 * What the program knows about the plant, derived each cycle from the incoming
 * transmission and the orders it sent: the soundness of every unit, the
 * failure/repair message protocol, and the water level as a pair of bounds.
 */
public final class PlantSensor {
    private final UnitStatus levelDevice = new UnitStatus();
    private final UnitStatus steamDevice = new UnitStatus();
    private final PumpTracker[] pumps = new PumpTracker[SteamBoilerConstants.PUMP_COUNT];
    private final LevelEstimator estimator = new LevelEstimator();
    // p: the throughput of the pumps known to be pouring water.
    @RoboChartType("real") private double p = 0.0;
    @RoboChartType("nat") private int possiblyDeliveringPumps = 0;

    /** Creates the plant knowledge with every unit believed sound and every pump off. */
    public PlantSensor() {
        for (int n = 0; n < pumps.length; n++) {
            pumps[n] = new PumpTracker();
        }
    }

    /** Analyses the current transmission: acknowledgements, repairs, failure detection, level bounds. */
    public void analyse(TransmissionSensor in, BoilerActuator out) {
        // The previous cycle's orders, then this cycle's reports, acknowledgements and repairs.
        int elapsedKnown = 0;
        int elapsedPossible = 0;
        for (int n = 0; n < pumps.length; n++) {
            pumps[n].applyOrders(out.openOrdered(n), out.closeOrdered(n));
            pumps[n].receive(in, n);
            if (pumps[n].elapsedKnown()) {
                elapsedKnown = elapsedKnown + 1;
            }
            if (pumps[n].elapsedPossible()) {
                elapsedPossible = elapsedPossible + 1;
            }
        }
        if (in.levelFailureAckReceived()) {
            levelDevice.acknowledgeFailure();
        }
        if (in.steamFailureAckReceived()) {
            steamDevice.acknowledgeFailure();
        }
        if (in.levelRepairReported()) {
            levelDevice.repair();
        }
        if (in.steamRepairReported()) {
            steamDevice.repair();
        }
        estimator.advance(elapsedKnown, elapsedPossible, out.valveCommandedOpen());

        // Failure detection.
        if (levelDevice.sound() && !estimator.levelPlausible(in.levelReading())) {
            levelDevice.detectFailure();
        }
        if (steamDevice.sound() && !estimator.steamPlausible(in.steamReading())) {
            steamDevice.detectFailure();
        }
        for (PumpTracker pump : pumps) {
            pump.detect();
        }

        // What is now known of the pumps and the level.
        int known = 0;
        int possible = 0;
        for (PumpTracker pump : pumps) {
            pump.settle();
            if (pump.knownDelivering()) {
                known = known + 1;
            }
            if (pump.possiblyDelivering()) {
                possible = possible + 1;
            }
        }
        p = SteamBoilerConstants.PUMP_CAPACITY_P * known;
        possiblyDeliveringPumps = possible;
        estimator.settle(levelDevice.sound(), in.levelReading(), steamDevice.sound(), in.steamReading());
    }

    /** Adds the failure announcements and repair acknowledgements of this cycle to the outgoing transmission. */
    public void announce(BoilerActuator out) {
        if (levelDevice.announcementPending()) {
            out.apply(new OutputEvent.LevelFailureDetection());
        }
        if (steamDevice.announcementPending()) {
            out.apply(new OutputEvent.SteamFailureDetection());
        }
        for (int n = 0; n < pumps.length; n++) {
            if (pumps[n].pumpUnit().announcementPending()) {
                out.apply(new OutputEvent.PumpFailureDetection(n));
            }
            if (pumps[n].controllerUnit().announcementPending()) {
                out.apply(new OutputEvent.PumpControlFailureDetection(n));
            }
        }
        if (levelDevice.takeRepairAcknowledgement()) {
            out.apply(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (steamDevice.takeRepairAcknowledgement()) {
            out.apply(new OutputEvent.SteamRepairedAcknowledgement());
        }
        for (int n = 0; n < pumps.length; n++) {
            if (pumps[n].pumpUnit().takeRepairAcknowledgement()) {
                out.apply(new OutputEvent.PumpRepairedAcknowledgement(n));
            }
            if (pumps[n].controllerUnit().takeRepairAcknowledgement()) {
                out.apply(new OutputEvent.PumpControlRepairedAcknowledgement(n));
            }
        }
    }

    /** Whether the water-level measuring device is out of order. */
    public boolean levelDeviceDefective() {
        return levelDevice.outOfOrder();
    }

    /** Whether the steam measuring device is out of order. */
    public boolean steamDeviceDefective() {
        return steamDevice.outOfOrder();
    }

    /** Whether some pump is out of order. */
    public boolean pumpDefective() {
        for (PumpTracker pump : pumps) {
            if (pump.pumpUnit().outOfOrder()) {
                return true;
            }
        }
        return false;
    }

    /** Whether some pump controller is out of order. */
    public boolean pumpControllerDefective() {
        for (PumpTracker pump : pumps) {
            if (pump.controllerUnit().outOfOrder()) {
                return true;
            }
        }
        return false;
    }

    /** Lower bound on the current water level (litre). */
    @RoboChartType("real")
    public double levelLow() {
        return estimator.levelLow();
    }

    /** Upper bound on the current water level (litre). */
    @RoboChartType("real")
    public double levelHigh() {
        return estimator.levelHigh();
    }

    /** Lowest level reachable by the end of the next cycle under worst-case behaviour (litre). */
    @RoboChartType("real")
    public double predictedLevelMin() {
        return estimator.predictedMin();
    }

    /** Highest level reachable by the end of the next cycle under worst-case behaviour (litre). */
    @RoboChartType("real")
    public double predictedLevelMax() {
        return estimator.predictedMax(possiblyDeliveringPumps);
    }

    /** p: the current known throughput of the pumps taken together (litre/sec). */
    @RoboChartType("real")
    public double pumpThroughput() {
        return p;
    }
}
