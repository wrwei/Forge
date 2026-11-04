package steam_boiler.controller;

import steam_boiler.BoilerConstants;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.BoilerMode;
import steam_boiler.mode.OperatingMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * The mode logic of the steam-boiler control program. Once per cycle, after
 * the incoming transmission has been received and analysed, {@link #step}
 * chooses the operating mode and issues the mode message, the readiness and
 * valve messages and the pump orders of the cycle.
 */
public final class SteamBoilerController {

    private BoilerMode currentMode = BoilerMode.INITIALIZATION;
    /** STEAM_BOILER_WAITING has been received. */
    private boolean plantWaiting = false;
    /** PROGRAM_READY has been sent. */
    private boolean readyAnnounced = false;
    /** The evacuation valve has been requested open. */
    private boolean valveOpen = false;
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /** Creates the controller in initialization mode. */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode. */
    public BoilerMode currentMode() {
        return currentMode;
    }

    /** Handles one cycle; {@code event} is the cycle trigger. */
    public void step(InputEvent event) {
        // --- Conditions read from the analysed transmission ---
        boolean operatorStop = sensor.consecutiveStopCount() >= BoilerConstants.STOP_REPETITIONS;
        boolean transmissionLost = sensor.transmissionFailure();
        boolean plantSignalled = sensor.steamBoilerWaitingReceived();
        boolean unitsReadySignalled = sensor.physicalUnitsReadyReceived();
        boolean initMessageMisplaced = plantSignalled || unitsReadySignalled;
        boolean levelDeviceDown = sensor.levelDeviceFailed();
        boolean steamDeviceDown = sensor.steamDeviceFailed();
        boolean pumpDown = sensor.pumpFailed();
        boolean pumpControlDown = sensor.pumpControlFailed();
        boolean otherUnitDown = steamDeviceDown || pumpDown || pumpControlDown;
        boolean estimateUnreliable = steamDeviceDown || pumpControlDown;
        boolean steamNotZero = sensor.steamRate() != 0.0;
        boolean levelBelowN1 = sensor.waterLevel() < BoilerConstants.NORMAL_N1;
        boolean levelAboveN2 = sensor.waterLevel() > BoilerConstants.NORMAL_N2;
        boolean levelHighRegulation = levelAboveN2 && !levelBelowN1;
        boolean levelInBand = !levelBelowN1 && !levelAboveN2;
        boolean estimateBelowN1 =
                sensor.estimatedLevelLow() + sensor.estimatedLevelHigh() < 2.0 * BoilerConstants.NORMAL_N1;
        boolean estimateAboveN2 =
                sensor.estimatedLevelLow() + sensor.estimatedLevelHigh() > 2.0 * BoilerConstants.NORMAL_N2;
        boolean estimateHighRegulation = estimateAboveN2 && !estimateBelowN1;
        boolean estimateInBand = !estimateBelowN1 && !estimateAboveN2;
        boolean levelAtRisk = sensor.worstCaseLevelLow() <= BoilerConstants.LIMIT_M1
                || sensor.worstCaseLevelHigh() >= BoilerConstants.LIMIT_M2;

        // --- Initialization guards ---
        boolean plantActive = plantWaiting || plantSignalled;
        boolean unitsReadyTooEarly = unitsReadySignalled && !readyAnnounced;
        boolean initFault = transmissionLost || steamNotZero || levelDeviceDown || unitsReadyTooEarly;
        boolean initEmergency = operatorStop || (plantActive && initFault);
        boolean initIdle = !initEmergency && !plantActive;
        boolean initActive = !initEmergency && plantActive;
        boolean unitsReadyAccepted = initActive && unitsReadySignalled;
        boolean initAdjusting = initActive && !unitsReadySignalled;
        boolean startDraining = initAdjusting && levelHighRegulation && !valveOpen;
        boolean continueDraining = initAdjusting && levelHighRegulation && valveOpen;
        boolean stopDrainingAndFill = initAdjusting && levelBelowN1 && valveOpen;
        boolean continueFilling = initAdjusting && levelBelowN1 && !valveOpen;
        boolean stopDrainingAndAnnounce = initAdjusting && levelInBand && valveOpen;
        boolean announceReady = initAdjusting && levelInBand && !valveOpen;

        // --- Normal, degraded and rescue guards (emergency, then rescue, then degraded) ---
        boolean commonEmergency = operatorStop || transmissionLost || initMessageMisplaced || levelAtRisk;
        boolean regulatedEmergency = commonEmergency || (levelDeviceDown && estimateUnreliable);
        boolean enterRescue = !regulatedEmergency && levelDeviceDown;
        boolean regulatedSteady = !regulatedEmergency && !levelDeviceDown;
        boolean normalSteady = regulatedSteady && !otherUnitDown;
        boolean degradedSteady = regulatedSteady && otherUnitDown;
        boolean rescueEmergency = commonEmergency || estimateUnreliable;
        boolean levelRestored = !rescueEmergency && !levelDeviceDown;
        boolean rescueContinues = !rescueEmergency && levelDeviceDown;

        if (currentMode == BoilerMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionReceived && initEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && initIdle) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && unitsReadyAccepted && !otherUnitDown) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && unitsReadyAccepted && otherUnitDown) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && startDraining) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                valveOpen = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && continueDraining) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && stopDrainingAndFill) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                valveOpen = false;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && continueFilling) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && stopDrainingAndAnnounce) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                valveOpen = false;
                readyAnnounced = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived && announceReady) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                readyAnnounced = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.ProgramReady());
            }

        } else if (currentMode == BoilerMode.NORMAL) {
            if (event instanceof InputEvent.TransmissionReceived && regulatedEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && enterRescue) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && degradedSteady) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && normalSteady && levelBelowN1) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && normalSteady && levelHighRegulation) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && normalSteady && levelInBand) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL_MODE));
            }

        } else if (currentMode == BoilerMode.DEGRADED) {
            if (event instanceof InputEvent.TransmissionReceived && regulatedEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && enterRescue) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && normalSteady) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && degradedSteady && levelBelowN1) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && degradedSteady && levelHighRegulation) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && degradedSteady && levelInBand) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED_MODE));
            }

        } else if (currentMode == BoilerMode.RESCUE) {
            if (event instanceof InputEvent.TransmissionReceived && rescueEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && levelRestored && !otherUnitDown) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && levelRestored && otherUnitDown) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueContinues && estimateBelowN1) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueContinues && estimateHighRegulation) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueContinues && estimateInBand) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE_MODE));
            }

        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
            // Terminal: the control task has ended and the plant is left to the environment.
        }
    }
}
