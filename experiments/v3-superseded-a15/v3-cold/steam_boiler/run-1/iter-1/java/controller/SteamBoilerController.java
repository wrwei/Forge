package steam_boiler.controller;

import steam_boiler.BoilerConstants;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * Mode logic of the steam-boiler control program (SB-V1, SB-V2). Once per
 * five-second cycle, after the receive stage, {@link #step(InputEvent)}
 * chooses the operating mode and the MODE, VALVE, PROGRAM_READY and pump
 * orders to transmit.
 */
public final class SteamBoilerController {
    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    private boolean boilerWaitingSeen = false;
    private boolean programReadySent = false;
    private boolean valveOpen = false;
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /** Creates the controller in initialization mode (SB-Beh1). */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode. */
    public OperatingMode currentMode() {
        return currentMode;
    }

    /** Analysis stage of one cycle; {@code event} marks the end of the receive stage. */
    public void step(InputEvent event) {
        // --- Judgements of the current transmission ---
        boolean stopDemanded = sensor.consecutiveStopCount() >= BoilerConstants.STOP_LIMIT;
        boolean transmissionFailed = sensor.transmissionFailureDetected();
        boolean levelDeviceFailed = sensor.levelFailureDetected();
        boolean otherUnitFailed = sensor.steamFailureDetected() || sensor.pumpFailureDetected()
                || sensor.pumpControlFailureDetected();
        boolean everyUnitSound = sensor.allUnitsSound();
        boolean estimateUnreliable = sensor.steamDeviceOutOfOrder() || sensor.anyPumpControllerOutOfOrder();
        boolean levelRiskOfLimit = sensor.predictedLevelLow() <= BoilerConstants.M1
                || sensor.predictedLevelHigh() >= BoilerConstants.M2;
        boolean levelBelowNormal = sensor.levelMidpoint() < BoilerConstants.N1;
        boolean levelAboveNormal = sensor.levelMidpoint() > BoilerConstants.N2;
        boolean levelWithinNormal = !levelBelowNormal && !levelAboveNormal;
        boolean pumpStartable = sensor.startablePumpAvailable();
        boolean pumpStoppable = sensor.stoppablePumpAvailable();
        boolean supplyTooLow = sensor.pumpsRunning() < sensor.pumpsWanted() && pumpStartable;
        boolean supplyTooHigh = sensor.pumpsRunning() > sensor.pumpsWanted() && pumpStoppable;
        boolean supplyBalanced = !supplyTooLow && !supplyTooHigh;

        // --- Initialization (SB-Beh2 to SB-Beh9) ---
        boolean plantWaiting = boilerWaitingSeen || sensor.boilerWaitingReceived();
        boolean steamNotZero = sensor.steamRateNonZero();
        boolean unitsReadyAnswered = sensor.unitsReadyReceived();
        boolean initIdle = !stopDemanded && !plantWaiting;
        boolean initStarted = !stopDemanded && plantWaiting;
        boolean initTransmissionFault = initStarted && transmissionFailed;
        boolean initLevelFault = initStarted && !transmissionFailed && levelDeviceFailed;
        boolean initSteamFault = initStarted && !transmissionFailed && !levelDeviceFailed && steamNotZero;
        boolean initHealthy = initStarted && !transmissionFailed && !levelDeviceFailed && !steamNotZero;
        boolean initReadyForNormal = initHealthy && programReadySent && unitsReadyAnswered && everyUnitSound;
        boolean initReadyForDegraded = initHealthy && programReadySent && unitsReadyAnswered && !everyUnitSound;
        boolean initAwaitingAnswer = initHealthy && programReadySent && !unitsReadyAnswered;
        boolean initAdjusting = initHealthy && !programReadySent;
        boolean initHighStopFill = initAdjusting && levelAboveNormal && pumpStoppable;
        boolean initHighOpenValve = initAdjusting && levelAboveNormal && !pumpStoppable && !valveOpen;
        boolean initHighDraining = initAdjusting && levelAboveNormal && !pumpStoppable && valveOpen;
        boolean initLowCloseValve = initAdjusting && levelBelowNormal && valveOpen;
        boolean initLowStartFill = initAdjusting && levelBelowNormal && !valveOpen && !pumpStoppable
                && pumpStartable;
        boolean initLowFilling = initAdjusting && levelBelowNormal && !valveOpen
                && (pumpStoppable || !pumpStartable);
        boolean initReadyCloseValve = initAdjusting && levelWithinNormal && valveOpen;
        boolean initReadyStopFill = initAdjusting && levelWithinNormal && !valveOpen && pumpStoppable;
        boolean initReady = initAdjusting && levelWithinNormal && !valveOpen && !pumpStoppable;

        // --- Normal, degraded and rescue (SB-Beh10 to SB-Beh21, SB-FR8) ---
        boolean hazardDetected = transmissionFailed || levelRiskOfLimit;
        boolean hazardStop = !stopDemanded && hazardDetected;
        boolean noEscalation = !stopDemanded && !hazardDetected;
        boolean levelLossUnrecoverable = noEscalation && levelDeviceFailed && estimateUnreliable;
        boolean levelLossRecoverable = noEscalation && levelDeviceFailed && !estimateUnreliable;
        boolean levelTrusted = noEscalation && !levelDeviceFailed;
        boolean normalToDegraded = levelTrusted && otherUnitFailed;
        boolean normalRegulating = levelTrusted && !otherUnitFailed;
        boolean degradedToNormal = levelTrusted && everyUnitSound;
        boolean degradedRegulating = levelTrusted && !everyUnitSound;
        boolean rescueAbandoned = noEscalation && estimateUnreliable;
        boolean rescueSustained = noEscalation && !estimateUnreliable;
        boolean levelRestored = sensor.levelRepairReported() && !levelDeviceFailed;
        boolean rescueToNormal = rescueSustained && levelRestored && everyUnitSound;
        boolean rescueToDegraded = rescueSustained && levelRestored && !everyUnitSound;
        boolean rescueRegulating = rescueSustained && !levelRestored;

        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionReceived && stopDemanded) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && initIdle) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
            } else if (event instanceof InputEvent.TransmissionReceived && initTransmissionFault) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && initLevelFault) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && initSteamFault) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && initReadyForNormal) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && initReadyForDegraded) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && initAwaitingAnswer) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived && initHighStopFill) {
                currentMode = OperatingMode.INITIALIZATION;
                boilerWaitingSeen = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(sensor.pumpToClose()));
            } else if (event instanceof InputEvent.TransmissionReceived && initHighOpenValve) {
                currentMode = OperatingMode.INITIALIZATION;
                boilerWaitingSeen = true;
                valveOpen = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ValveCommand());
            } else if (event instanceof InputEvent.TransmissionReceived && initHighDraining) {
                currentMode = OperatingMode.INITIALIZATION;
                boilerWaitingSeen = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
            } else if (event instanceof InputEvent.TransmissionReceived && initLowCloseValve) {
                currentMode = OperatingMode.INITIALIZATION;
                boilerWaitingSeen = true;
                valveOpen = false;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ValveCommand());
            } else if (event instanceof InputEvent.TransmissionReceived && initLowStartFill) {
                currentMode = OperatingMode.INITIALIZATION;
                boilerWaitingSeen = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.OpenPump(sensor.pumpToOpen()));
            } else if (event instanceof InputEvent.TransmissionReceived && initLowFilling) {
                currentMode = OperatingMode.INITIALIZATION;
                boilerWaitingSeen = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
            } else if (event instanceof InputEvent.TransmissionReceived && initReadyCloseValve) {
                currentMode = OperatingMode.INITIALIZATION;
                boilerWaitingSeen = true;
                valveOpen = false;
                programReadySent = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ValveCommand());
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived && initReadyStopFill) {
                currentMode = OperatingMode.INITIALIZATION;
                boilerWaitingSeen = true;
                programReadySent = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(sensor.pumpToClose()));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived && initReady) {
                currentMode = OperatingMode.INITIALIZATION;
                boilerWaitingSeen = true;
                programReadySent = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ProgramReady());
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.TransmissionReceived && stopDemanded) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && hazardStop) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelLossUnrecoverable) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelLossRecoverable) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
            } else if (event instanceof InputEvent.TransmissionReceived && normalToDegraded) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && normalRegulating && supplyTooLow) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.OpenPump(sensor.pumpToOpen()));
            } else if (event instanceof InputEvent.TransmissionReceived && normalRegulating && supplyTooHigh) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.ClosePump(sensor.pumpToClose()));
            } else if (event instanceof InputEvent.TransmissionReceived && normalRegulating && supplyBalanced) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.TransmissionReceived && stopDemanded) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && hazardStop) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelLossUnrecoverable) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelLossRecoverable) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
            } else if (event instanceof InputEvent.TransmissionReceived && degradedToNormal) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && degradedRegulating && supplyTooLow) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(sensor.pumpToOpen()));
            } else if (event instanceof InputEvent.TransmissionReceived && degradedRegulating && supplyTooHigh) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(sensor.pumpToClose()));
            } else if (event instanceof InputEvent.TransmissionReceived && degradedRegulating && supplyBalanced) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.TransmissionReceived && stopDemanded) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && hazardStop) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueAbandoned) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueToNormal) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueToDegraded) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueRegulating && supplyTooLow) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.OpenPump(sensor.pumpToOpen()));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueRegulating && supplyTooHigh) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.ClosePump(sensor.pumpToClose()));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueRegulating && supplyBalanced) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // Terminal (SB-Beh22): the control task has ended; the plant is the
            // physical environment's responsibility.
        }
    }
}
