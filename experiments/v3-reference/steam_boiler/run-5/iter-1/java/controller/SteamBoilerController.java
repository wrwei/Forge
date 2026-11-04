package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.data.ValvePosition;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.BoilerMode;
import steam_boiler.mode.OperatingMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program's mode logic (SB-V1, SB-V2). {@link #step(InputEvent)} is
 * called once per five-second cycle, with {@link InputEvent.TransmissionReceived}, after the
 * sensor has received and analysed the cycle's incoming transmission (SB-V3). It chooses the
 * next operating mode, regulates the water level with the pumps, drives the start-up
 * handshake and sends MODE(m) on every cycle (SB-Beh24). When several failure conditions
 * hold at once the most severe consequence wins: emergency stop, then rescue, then degraded
 * (SB-FR8).
 */
public final class SteamBoilerController {

    private BoilerMode currentMode = BoilerMode.INITIALIZATION;
    /** STEAM_BOILER_WAITING has arrived during initialization (SB-Beh2). */
    private boolean startupBegun = false;
    /** PROGRAM_READY has been announced during initialization (SB-Beh7). */
    private boolean readyAnnounced = false;
    /** Position of the evacuation valve, toggled with VALVE during initialization (SB-DM7, SB-Beh4). */
    private ValvePosition valvePosition = ValvePosition.VALVE_CLOSED;

    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /** Creates the controller in initialization mode (SB-Beh1). */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode (SB-V2). */
    public BoilerMode currentMode() {
        return currentMode;
    }

    /** Performs the analysis and transmission stages of one cycle (SB-V3). */
    public void step(InputEvent event) {
        // --- Operator and transmission (SB-Beh23, SB-FR5, SB-V10) ---
        boolean operatorStop = sensor.successiveStopCount() >= SteamBoilerConstants.STOP_REPEAT_LIMIT;
        boolean transmissionFailure = sensor.transmissionFailed();
        boolean boilerWaitingNow = sensor.steamBoilerWaitingReceived();
        boolean unitsReadyNow = sensor.physicalUnitsReadyReceived();
        boolean startupMessageAberrant = boilerWaitingNow || unitsReadyNow;

        // --- Unit status (SB-V9) ---
        boolean levelOutOfOrder = sensor.levelDeviceOutOfOrder();
        boolean steamOutOfOrder = sensor.steamDeviceOutOfOrder();
        boolean pumpOutOfOrder = sensor.anyPumpOutOfOrder();
        boolean pumpControllerOutOfOrder = sensor.anyPumpControllerOutOfOrder();
        boolean otherUnitOutOfOrder = steamOutOfOrder || pumpOutOfOrder || pumpControllerOutOfOrder;
        boolean allUnitsSound = !levelOutOfOrder && !otherUnitOutOfOrder;
        boolean rescueUntenable = steamOutOfOrder || pumpControllerOutOfOrder;

        // --- Water level (SB-V4, SB-Beh10, SB-Beh13, SB-Beh19) ---
        boolean levelBelowNormal = sensor.levelEstimate() < SteamBoilerConstants.MIN_NORMAL;
        boolean levelAboveNormal = sensor.levelEstimate() > SteamBoilerConstants.MAX_NORMAL;
        boolean levelWithinNormal = !levelBelowNormal && !levelAboveNormal;
        boolean levelRiskLow = sensor.minimumLevelForecast() <= SteamBoilerConstants.MIN_LIMIT;
        boolean levelRiskHigh = sensor.maximumLevelForecast() >= SteamBoilerConstants.MAX_LIMIT;
        boolean levelRisk = levelRiskLow || levelRiskHigh;
        boolean steamNotZero = sensor.steamRate() != 0.0;

        // --- Initialization (SB-Beh2 to SB-Beh9) ---
        boolean startupActive = startupBegun || boilerWaitingNow;
        boolean unitsReadyPremature = unitsReadyNow && !readyAnnounced;
        boolean valveIsOpen = valvePosition == ValvePosition.VALVE_OPEN;
        boolean startupUnitFault = startupActive && (steamNotZero || levelOutOfOrder);
        boolean initEmergency = operatorStop || transmissionFailure || unitsReadyPremature || startupUnitFault;
        boolean awaitingPlant = !initEmergency && !startupActive;
        boolean initProceeding = !initEmergency && startupActive;
        boolean unitsReadyAllSound = initProceeding && unitsReadyNow && allUnitsSound;
        boolean unitsReadySomeFailed = initProceeding && unitsReadyNow && !allUnitsSound;
        boolean awaitingUnitsReady = initProceeding && !unitsReadyNow && readyAnnounced;
        boolean initAdjusting = initProceeding && !unitsReadyNow && !readyAnnounced;

        // --- Normal, degraded and rescue (SB-Beh11 to SB-Beh21, SB-FR8) ---
        boolean commonEmergency = operatorStop || transmissionFailure || startupMessageAberrant || levelRisk;
        boolean regulatedModeEmergency = commonEmergency || (levelOutOfOrder && rescueUntenable);
        boolean rescueModeEmergency = commonEmergency || rescueUntenable;
        boolean levelLossToRescue = !regulatedModeEmergency && levelOutOfOrder;
        boolean normalOperation = !regulatedModeEmergency && allUnitsSound;
        boolean degradedOperation = !regulatedModeEmergency && !levelOutOfOrder && otherUnitOutOfOrder;
        boolean levelRestoredAllSound = !rescueModeEmergency && allUnitsSound;
        boolean levelRestoredSomeFailed = !rescueModeEmergency && !levelOutOfOrder && otherUnitOutOfOrder;
        boolean rescueOperation = !rescueModeEmergency && levelOutOfOrder;

        if (currentMode == BoilerMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionReceived && initEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && awaitingPlant) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && unitsReadyAllSound) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && unitsReadySomeFailed) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && awaitingUnitsReady) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived && initAdjusting && levelAboveNormal
                    && !valveIsOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                startupBegun = true;
                valvePosition = ValvePosition.VALVE_OPEN;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
            } else if (event instanceof InputEvent.TransmissionReceived && initAdjusting && levelAboveNormal
                    && valveIsOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                startupBegun = true;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && initAdjusting && levelBelowNormal
                    && valveIsOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                startupBegun = true;
                valvePosition = ValvePosition.VALVE_CLOSED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(1));
            } else if (event instanceof InputEvent.TransmissionReceived && initAdjusting && levelBelowNormal
                    && !valveIsOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                startupBegun = true;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
            } else if (event instanceof InputEvent.TransmissionReceived && initAdjusting && levelWithinNormal
                    && valveIsOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                startupBegun = true;
                readyAnnounced = true;
                valvePosition = ValvePosition.VALVE_CLOSED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived && initAdjusting && levelWithinNormal
                    && !valveIsOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                startupBegun = true;
                readyAnnounced = true;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ProgramReady());
            }

        } else if (currentMode == BoilerMode.NORMAL) {
            if (event instanceof InputEvent.TransmissionReceived && regulatedModeEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && levelLossToRescue) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && degradedOperation) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && normalOperation && levelBelowNormal) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && normalOperation && levelAboveNormal) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && normalOperation && levelWithinNormal) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            }

        } else if (currentMode == BoilerMode.DEGRADED) {
            if (event instanceof InputEvent.TransmissionReceived && regulatedModeEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && levelLossToRescue) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && normalOperation) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && degradedOperation && levelBelowNormal) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && degradedOperation && levelAboveNormal) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && degradedOperation && levelWithinNormal) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            }

        } else if (currentMode == BoilerMode.RESCUE) {
            if (event instanceof InputEvent.TransmissionReceived && rescueModeEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && levelRestoredAllSound) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && levelRestoredSomeFailed) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueOperation && levelBelowNormal) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueOperation && levelAboveNormal) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueOperation && levelWithinNormal) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            }

        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
            // Terminal (SB-Beh22): the control task has ended; the physical environment is
            // responsible for the plant from here on.
        }
    }
}
