package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.event.InputEvent;
import steam_boiler.event.ModeValue;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.PlantSensor;
import steam_boiler.sensor.TransmissionSensor;

/**
 * The steam-boiler control program's mode logic, stepped once per cycle after
 * the incoming transmission has been received and analysed.
 */
public final class SteamBoilerController {
    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    // PROGRAM_READY has been sent and is repeated until PHYSICAL_UNITS_READY arrives.
    private boolean readyAnnounced = false;
    // The evacuation valve has been opened by VALVE and not yet closed.
    private boolean valveOpen = false;
    private final TransmissionSensor receiver;
    private final PlantSensor plant;
    private final BoilerActuator actuator;

    /** Creates the controller in initialization mode. */
    public SteamBoilerController(TransmissionSensor receiver, PlantSensor plant, BoilerActuator actuator) {
        this.receiver = receiver;
        this.plant = plant;
        this.actuator = actuator;
    }

    /** The current operating mode. */
    public OperatingMode currentMode() {
        return currentMode;
    }

    /** Performs the analysis and transmission stages of one cycle. */
    public void step(InputEvent event) {
        // --- Incoming transmission ---
        boolean stopLimitReached = receiver.successiveStops() >= SteamBoilerConstants.STOP_LIMIT;
        boolean plantNotWaiting = !receiver.boilerWaitingSeen();
        boolean dataMessagesFaulty = !receiver.transmissionIntact();
        boolean waitingMessagePresent = receiver.boilerWaitingReceived();
        boolean unitsReadyPresent = receiver.unitsReadyReceived();
        boolean unitsReadyPremature = unitsReadyPresent && !readyAnnounced;
        boolean unitsReadyAnswered = unitsReadyPresent && readyAnnounced;
        boolean initTransmissionFailure = dataMessagesFaulty || unitsReadyPremature;
        boolean transmissionFailure = dataMessagesFaulty || waitingMessagePresent || unitsReadyPresent;
        boolean steamRateNonZero = receiver.steamReading() != 0.0;

        // --- Unit status ---
        boolean levelUnitFailed = plant.levelDeviceDefective();
        boolean steamUnitFailed = plant.steamDeviceDefective();
        boolean pumpUnitFailed = plant.pumpDefective();
        boolean pumpControlUnitFailed = plant.pumpControllerDefective();
        boolean otherUnitFailed = steamUnitFailed || pumpUnitFailed || pumpControlUnitFailed;
        boolean allUnitsSound = !levelUnitFailed && !otherUnitFailed;
        boolean estimateUnreliable = steamUnitFailed || pumpControlUnitFailed;

        // --- Water level ---
        boolean levelMayReachLimit = plant.predictedLevelMin() <= SteamBoilerConstants.LIMIT_M1
                || plant.predictedLevelMax() >= SteamBoilerConstants.LIMIT_M2;
        boolean levelBelowNormal = plant.levelLow() < SteamBoilerConstants.NORMAL_N1;
        boolean levelAboveNormal = plant.levelHigh() > SteamBoilerConstants.NORMAL_N2;
        boolean levelWithinNormal = !levelBelowNormal && !levelAboveNormal;
        boolean pumpsToOpen = levelBelowNormal && !levelAboveNormal;
        boolean pumpsToClose = levelAboveNormal && !levelBelowNormal;
        boolean pumpsToHold = !pumpsToOpen && !pumpsToClose;

        // --- Initialization phases ---
        boolean initProceeding = !stopLimitReached && !plantNotWaiting;
        boolean initChecksPassed = initProceeding && !initTransmissionFailure && !steamRateNonZero
                && !levelUnitFailed;
        boolean readyRepeating = initChecksPassed && readyAnnounced && !unitsReadyPresent;
        boolean initLevelAdjusting = initChecksPassed && !readyAnnounced;
        boolean drainStart = initLevelAdjusting && levelAboveNormal && !valveOpen;
        boolean drainContinue = initLevelAdjusting && levelAboveNormal && valveOpen;
        boolean fillWithValveOpen = initLevelAdjusting && pumpsToOpen && valveOpen;
        boolean fillWithValveClosed = initLevelAdjusting && pumpsToOpen && !valveOpen;
        boolean readyWithValveOpen = initLevelAdjusting && levelWithinNormal && valveOpen;
        boolean readyWithValveClosed = initLevelAdjusting && levelWithinNormal && !valveOpen;

        // --- Operating modes: no condition demanding emergency stop ---
        boolean noEmergency = !stopLimitReached && !transmissionFailure && !levelMayReachLimit;

        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.Transmission && stopLimitReached) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && !stopLimitReached && plantNotWaiting) {
                // Waiting for STEAM_BOILER_WAITING: nothing else happens.
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.INITIALIZATION));
            } else if (event instanceof InputEvent.Transmission && initProceeding && initTransmissionFailure) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && initProceeding && !initTransmissionFailure
                    && steamRateNonZero) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && initProceeding && !initTransmissionFailure
                    && !steamRateNonZero && levelUnitFailed) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && initChecksPassed && unitsReadyAnswered
                    && allUnitsSound) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeValue.NORMAL));
            } else if (event instanceof InputEvent.Transmission && initChecksPassed && unitsReadyAnswered
                    && !allUnitsSound) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeValue.DEGRADED));
            } else if (event instanceof InputEvent.Transmission && readyRepeating) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.INITIALIZATION));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.Transmission && drainStart) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                valveOpen = true;
            } else if (event instanceof InputEvent.Transmission && drainContinue) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.INITIALIZATION));
            } else if (event instanceof InputEvent.Transmission && fillWithValveOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                valveOpen = false;
                actuator.apply(new OutputEvent.OpenPump(0));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
            } else if (event instanceof InputEvent.Transmission && fillWithValveClosed) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.INITIALIZATION));
                actuator.apply(new OutputEvent.OpenPump(0));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
            } else if (event instanceof InputEvent.Transmission && readyWithValveOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                valveOpen = false;
                actuator.apply(new OutputEvent.ClosePump(0));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ProgramReady());
                readyAnnounced = true;
            } else if (event instanceof InputEvent.Transmission && readyWithValveClosed) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(0));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ProgramReady());
                readyAnnounced = true;
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.Transmission && stopLimitReached) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && !stopLimitReached && transmissionFailure) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && !stopLimitReached && !transmissionFailure
                    && levelMayReachLimit) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && noEmergency && levelUnitFailed
                    && estimateUnreliable) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && noEmergency && levelUnitFailed
                    && !estimateUnreliable) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(ModeValue.RESCUE));
            } else if (event instanceof InputEvent.Transmission && noEmergency && !levelUnitFailed
                    && otherUnitFailed) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeValue.DEGRADED));
            } else if (event instanceof InputEvent.Transmission && noEmergency && allUnitsSound && pumpsToOpen) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeValue.NORMAL));
                actuator.apply(new OutputEvent.OpenPump(0));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
            } else if (event instanceof InputEvent.Transmission && noEmergency && allUnitsSound && pumpsToClose) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeValue.NORMAL));
                actuator.apply(new OutputEvent.ClosePump(0));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
            } else if (event instanceof InputEvent.Transmission && noEmergency && allUnitsSound && pumpsToHold) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeValue.NORMAL));
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.Transmission && stopLimitReached) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && !stopLimitReached && transmissionFailure) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && !stopLimitReached && !transmissionFailure
                    && levelMayReachLimit) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && noEmergency && levelUnitFailed
                    && estimateUnreliable) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && noEmergency && levelUnitFailed
                    && !estimateUnreliable) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(ModeValue.RESCUE));
            } else if (event instanceof InputEvent.Transmission && noEmergency && allUnitsSound) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeValue.NORMAL));
            } else if (event instanceof InputEvent.Transmission && noEmergency && !levelUnitFailed
                    && otherUnitFailed && pumpsToOpen) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeValue.DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(0));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
            } else if (event instanceof InputEvent.Transmission && noEmergency && !levelUnitFailed
                    && otherUnitFailed && pumpsToClose) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeValue.DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(0));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
            } else if (event instanceof InputEvent.Transmission && noEmergency && !levelUnitFailed
                    && otherUnitFailed && pumpsToHold) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeValue.DEGRADED));
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.Transmission && stopLimitReached) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && !stopLimitReached && transmissionFailure) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && !stopLimitReached && !transmissionFailure
                    && levelMayReachLimit) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && noEmergency && estimateUnreliable) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && noEmergency && !estimateUnreliable
                    && allUnitsSound) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeValue.NORMAL));
            } else if (event instanceof InputEvent.Transmission && noEmergency && !estimateUnreliable
                    && !levelUnitFailed && otherUnitFailed) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeValue.DEGRADED));
            } else if (event instanceof InputEvent.Transmission && noEmergency && !estimateUnreliable
                    && levelUnitFailed && pumpsToOpen) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(ModeValue.RESCUE));
                actuator.apply(new OutputEvent.OpenPump(0));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
            } else if (event instanceof InputEvent.Transmission && noEmergency && !estimateUnreliable
                    && levelUnitFailed && pumpsToClose) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(ModeValue.RESCUE));
                actuator.apply(new OutputEvent.ClosePump(0));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
            } else if (event instanceof InputEvent.Transmission && noEmergency && !estimateUnreliable
                    && levelUnitFailed && pumpsToHold) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(ModeValue.RESCUE));
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // Terminal: control has ended; only the mode is still reported on each cycle.
            if (event instanceof InputEvent.Transmission) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.EMERGENCY_STOP));
            }
        }
    }
}
