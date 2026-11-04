package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program's mode logic. It is stepped once per five-second
 * cycle, after the sensor has received and analysed the cycle's transmission, with
 * the {@link InputEvent.Transmission} event. Each step selects the next operating
 * mode, broadcasts it, records the program's current measures and issues the pump,
 * valve and PROGRAM_READY commands of the target mode.
 */
public final class SteamBoilerController {
    private BoilerMode currentMode = BoilerMode.INITIALIZATION;
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /** q: the water level most recently reported (litre). */
    @RoboChartType("real") private double waterLevel = 0.0;
    /** v: the steam rate most recently reported (litre/sec). */
    @RoboChartType("real") private double steamRate = 0.0;
    /** p: the throughput of the pumps taken together (litre/sec). */
    @RoboChartType("real") private double pumpRate = 0.0;
    /** Lower bound of the estimated water level (litre). */
    @RoboChartType("real") private double levelLowBound = 0.0;
    /** Upper bound of the estimated water level (litre). */
    @RoboChartType("real") private double levelHighBound = 0.0;

    /** Creates the controller over the program's sensor view and outgoing-message actuator. */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode. */
    public BoilerMode currentMode() {
        return currentMode;
    }

    /** Performs the analysis and transmission stages of one cycle. */
    public void step(InputEvent event) {
        // --- Named predicates ---
        boolean stopDemanded = sensor.consecutiveStops() >= BoilerConstants.STOP_LIMIT;
        boolean transmissionFailure = sensor.transmissionFailure();
        boolean plantWaiting = sensor.boilerWaitingReceived();
        boolean unitsReady = sensor.physicalUnitsReadyReceived();
        boolean readyAnnounced = sensor.programReadySent();
        boolean valveIsOpen = sensor.valveOpen();
        boolean steamNotZero = sensor.steamNonZero();
        boolean levelFailure = sensor.levelDeviceFailed();
        boolean steamFailure = sensor.steamDeviceFailed();
        boolean pumpFailure = sensor.anyPumpFailed();
        boolean pumpControlFailure = sensor.anyPumpControllerFailed();
        boolean otherFailure = steamFailure || pumpFailure || pumpControlFailure;
        boolean waterBelowNormal = sensor.knownLevelLow() < BoilerConstants.NORMAL_N1;
        boolean waterAboveNormal = sensor.knownLevelHigh() > BoilerConstants.NORMAL_N2 && !waterBelowNormal;
        boolean waterInBand = !waterBelowNormal && !waterAboveNormal;
        boolean levelRisk = sensor.worstCaseLevelLow() <= BoilerConstants.LIMIT_M1
                || sensor.worstCaseLevelHigh() >= BoilerConstants.LIMIT_M2;
        boolean initFault = plantWaiting && (transmissionFailure || steamNotZero || levelFailure);
        boolean initContinue = !stopDemanded && !initFault;
        boolean initAdjusting = initContinue && plantWaiting && !readyAnnounced;
        boolean initHandshake = initContinue && plantWaiting && readyAnnounced;
        boolean rescueUntenable = levelFailure && (steamFailure || pumpControlFailure);
        boolean regulatedFault = transmissionFailure || levelRisk || rescueUntenable;
        boolean regulatedContinue = !stopDemanded && !regulatedFault;
        boolean rescueFault = transmissionFailure || levelRisk || steamFailure || pumpControlFailure;
        boolean rescueContinue = !stopDemanded && !rescueFault;

        // --- Mode-nested transitions ---
        if (currentMode == BoilerMode.INITIALIZATION) {
            if (event instanceof InputEvent.Transmission && stopDemanded) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(BoilerMode.EMERGENCY_STOP));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && initFault) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(BoilerMode.EMERGENCY_STOP));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && initContinue && !plantWaiting) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(BoilerMode.INITIALIZATION));
            } else if (event instanceof InputEvent.Transmission && initHandshake && unitsReady && !otherFailure) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(BoilerMode.NORMAL));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && initHandshake && unitsReady && otherFailure) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(BoilerMode.DEGRADED));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && initHandshake && !unitsReady) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(BoilerMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ProgramReady());
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && initAdjusting && waterAboveNormal && !valveIsOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(BoilerMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && initAdjusting && waterAboveNormal && valveIsOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(BoilerMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && initAdjusting && waterBelowNormal && valveIsOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(BoilerMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && initAdjusting && waterBelowNormal && !valveIsOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(BoilerMode.INITIALIZATION));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && initAdjusting && waterInBand && valveIsOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(BoilerMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.ProgramReady());
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && initAdjusting && waterInBand && !valveIsOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(BoilerMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.ProgramReady());
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            }

        } else if (currentMode == BoilerMode.NORMAL) {
            if (event instanceof InputEvent.Transmission && stopDemanded) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(BoilerMode.EMERGENCY_STOP));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && regulatedFault) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(BoilerMode.EMERGENCY_STOP));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && regulatedContinue && levelFailure) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(BoilerMode.RESCUE));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && regulatedContinue && !levelFailure && otherFailure) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(BoilerMode.DEGRADED));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && regulatedContinue && !levelFailure && !otherFailure
                    && waterBelowNormal) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(BoilerMode.NORMAL));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && regulatedContinue && !levelFailure && !otherFailure
                    && waterAboveNormal) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(BoilerMode.NORMAL));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && regulatedContinue && !levelFailure && !otherFailure
                    && waterInBand) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(BoilerMode.NORMAL));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            }

        } else if (currentMode == BoilerMode.DEGRADED) {
            if (event instanceof InputEvent.Transmission && stopDemanded) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(BoilerMode.EMERGENCY_STOP));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && regulatedFault) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(BoilerMode.EMERGENCY_STOP));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && regulatedContinue && levelFailure) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(BoilerMode.RESCUE));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && regulatedContinue && !levelFailure && !otherFailure) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(BoilerMode.NORMAL));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && regulatedContinue && !levelFailure && otherFailure
                    && waterBelowNormal) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(BoilerMode.DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && regulatedContinue && !levelFailure && otherFailure
                    && waterAboveNormal) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(BoilerMode.DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && regulatedContinue && !levelFailure && otherFailure
                    && waterInBand) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(BoilerMode.DEGRADED));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            }

        } else if (currentMode == BoilerMode.RESCUE) {
            if (event instanceof InputEvent.Transmission && stopDemanded) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(BoilerMode.EMERGENCY_STOP));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && !stopDemanded && rescueFault) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(BoilerMode.EMERGENCY_STOP));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && rescueContinue && !levelFailure && !otherFailure) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(BoilerMode.NORMAL));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && rescueContinue && !levelFailure && otherFailure) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(BoilerMode.DEGRADED));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && rescueContinue && levelFailure && waterBelowNormal) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(BoilerMode.RESCUE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && rescueContinue && levelFailure && waterAboveNormal) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(BoilerMode.RESCUE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            } else if (event instanceof InputEvent.Transmission && rescueContinue && levelFailure && waterInBand) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(BoilerMode.RESCUE));
                waterLevel = sensor.levelReading();
                steamRate = sensor.steamReading();
                pumpRate = sensor.pumpThroughput();
                levelLowBound = sensor.levelEstimateLow();
                levelHighBound = sensor.levelEstimateHigh();
            }

        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
            // Terminal mode: the control task has ended; no transition leaves it.
        }
    }
}
