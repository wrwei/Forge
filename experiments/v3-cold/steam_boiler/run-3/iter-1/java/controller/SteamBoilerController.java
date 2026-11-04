package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.domain.OperatingMode;
import steam_boiler.domain.ValvePosition;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * Mode controller of the steam-boiler control program. Once per five-second
 * cycle, after the incoming transmission has been analysed, {@link #step} is
 * called with {@link InputEvent.TransmissionReceived}; it chooses the operating
 * mode for the cycle and issues MODE(m) together with the mode's valve, pump
 * and PROGRAM_READY messages. In each mode the transitions are listed by
 * priority, and each guard excludes the guards above it: emergency stop before
 * rescue, rescue before degraded.
 */
public final class SteamBoilerController {

    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    private boolean plantWaiting = false;
    private ValvePosition evacuationValve = ValvePosition.VALVE_CLOSED;
    private boolean readyAnnounced = false;

    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /** Creates the controller, in initialization mode, over its sensor service and actuator. */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode. */
    public OperatingMode currentMode() {
        return currentMode;
    }

    /**
     * Performs the analysis and decision stage of one cycle.
     *
     * @param event {@link InputEvent.TransmissionReceived} once per cycle; other events are ignored
     */
    public void step(InputEvent event) {
        // Messages of the current transmission
        boolean boilerWaitingReceived = sensor.steamBoilerWaitingPresent();
        boolean unitsReadyReceived = sensor.physicalUnitsReadyPresent();
        boolean steamNotZero = sensor.steamReading() != 0.0;
        boolean levelReadingAboveN2 = sensor.levelReading() > SteamBoilerConstants.NORMAL_MAX_N2;
        boolean levelReadingBelowN1 = sensor.levelReading() < SteamBoilerConstants.NORMAL_MIN_N1;

        // Operator stop and detected failures
        boolean stopLimitReached =
                sensor.consecutiveStopCount() >= SteamBoilerConstants.STOP_TRANSMISSIONS_FOR_EMERGENCY;
        boolean transmissionFailed = sensor.transmissionFailure();
        boolean levelDeviceFailed = sensor.levelDeviceOutOfOrder();
        boolean steamDeviceFailed = sensor.steamDeviceOutOfOrder();
        boolean pumpControllerFailed = sensor.pumpControllerOutOfOrder();
        boolean otherUnitFailed = sensor.nonLevelUnitOutOfOrder();

        // Initialization: wait for the plant, check the devices, bring the level to [N1, N2], handshake
        boolean waitingOver = plantWaiting || boilerWaitingReceived;
        boolean valveOpen = evacuationValve == ValvePosition.VALVE_OPEN;
        boolean unitsReadyPremature = unitsReadyReceived && !readyAnnounced;
        boolean initAborted = transmissionFailed || unitsReadyPremature;
        boolean initAbortedOnly = !stopLimitReached && initAborted;
        boolean initWaiting = !stopLimitReached && !initAborted && !waitingOver;
        boolean initProceeding = !stopLimitReached && !initAborted && waitingOver;
        boolean initSteamFault = initProceeding && steamNotZero;
        boolean initLevelFault = initProceeding && !steamNotZero && levelDeviceFailed;
        boolean initChecksPassed = initProceeding && !steamNotZero && !levelDeviceFailed;
        boolean awaitingUnitsReady = initChecksPassed && readyAnnounced;
        boolean adjustingLevel = initChecksPassed && !readyAnnounced;
        boolean levelReadingOnlyBelowN1 = !levelReadingAboveN2 && levelReadingBelowN1;
        boolean levelReadingWithinNormal = !levelReadingAboveN2 && !levelReadingBelowN1;

        // Worst-case level at the end of the next cycle, with the pumps closed now (at
        // once) or left as they are (a pump opened now pours only after five seconds)
        boolean riskBelowM1PumpsClosed =
                sensor.worstCaseLevelLowPumpsClosed() < SteamBoilerConstants.LIMIT_MIN_M1;
        boolean riskBelowM1PumpsKept =
                sensor.worstCaseLevelLowPumpsKept() < SteamBoilerConstants.LIMIT_MIN_M1;
        boolean riskAboveM2PumpsClosed =
                sensor.worstCaseLevelHighPumpsClosed() > SteamBoilerConstants.LIMIT_MAX_M2;
        boolean riskAboveM2PumpsKept =
                sensor.worstCaseLevelHighPumpsKept() > SteamBoilerConstants.LIMIT_MAX_M2;

        // Regulation: close the pumps above N2 or when keeping them risks M2; otherwise
        // open them below N1, and leave them as they are within [N1, N2]
        boolean estimateBelowN1 = sensor.levelEstimate() < SteamBoilerConstants.NORMAL_MIN_N1;
        boolean estimateAboveN2 = sensor.levelEstimate() > SteamBoilerConstants.NORMAL_MAX_N2;
        boolean pumpsToClose = estimateAboveN2 || riskAboveM2PumpsKept;
        boolean pumpsToOpen = !pumpsToClose && estimateBelowN1;
        boolean pumpsToHold = !pumpsToClose && !estimateBelowN1;

        // Risk of reaching M1 or M2 even with this cycle's pump decision
        boolean levelRisk = (pumpsToClose && (riskBelowM1PumpsClosed || riskAboveM2PumpsClosed))
                || (!pumpsToClose && riskBelowM1PumpsKept);

        // Emergency stop outside initialization; the initialization messages are aberrant there
        boolean communicationFailed = transmissionFailed || boilerWaitingReceived || unitsReadyReceived;
        boolean communicationFailedOnly = !stopLimitReached && communicationFailed;
        boolean levelRiskOnly = !stopLimitReached && !communicationFailed && levelRisk;
        boolean operatingEmergency = stopLimitReached || communicationFailed || levelRisk;
        boolean levelFailureNoEmergency = !operatingEmergency && levelDeviceFailed;
        boolean otherFailureNoEmergency = !operatingEmergency && !levelDeviceFailed && otherUnitFailed;
        boolean allSoundNoEmergency = !operatingEmergency && !levelDeviceFailed && !otherUnitFailed;

        // Rescue mode adds the steam device and the pump controllers to the emergency conditions
        boolean rescueSteamFault = !stopLimitReached && !communicationFailed && steamDeviceFailed;
        boolean rescueControllerFault =
                !stopLimitReached && !communicationFailed && !steamDeviceFailed && pumpControllerFailed;
        boolean rescueLevelRisk = !stopLimitReached && !communicationFailed && !steamDeviceFailed
                && !pumpControllerFailed && levelRisk;
        boolean rescueEmergency = operatingEmergency || steamDeviceFailed || pumpControllerFailed;
        boolean rescueContinues = !rescueEmergency && levelDeviceFailed;
        boolean levelRepairedOtherFailure = !rescueEmergency && !levelDeviceFailed && otherUnitFailed;
        boolean levelRepairedAllSound = !rescueEmergency && !levelDeviceFailed && !otherUnitFailed;

        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionReceived && stopLimitReached) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && initAbortedOnly) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && initWaiting) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
            } else if (event instanceof InputEvent.TransmissionReceived && initSteamFault) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && initLevelFault) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && awaitingUnitsReady
                    && unitsReadyReceived && !otherUnitFailed) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && awaitingUnitsReady
                    && unitsReadyReceived && otherUnitFailed) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && awaitingUnitsReady
                    && !unitsReadyReceived) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived && adjustingLevel
                    && levelReadingAboveN2 && !valveOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                evacuationValve = ValvePosition.VALVE_OPEN;
                plantWaiting = true;
            } else if (event instanceof InputEvent.TransmissionReceived && adjustingLevel
                    && levelReadingAboveN2 && valveOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
                plantWaiting = true;
            } else if (event instanceof InputEvent.TransmissionReceived && adjustingLevel
                    && levelReadingOnlyBelowN1 && valveOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(1));
                evacuationValve = ValvePosition.VALVE_CLOSED;
                plantWaiting = true;
            } else if (event instanceof InputEvent.TransmissionReceived && adjustingLevel
                    && levelReadingOnlyBelowN1 && !valveOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.OpenPump(1));
                plantWaiting = true;
            } else if (event instanceof InputEvent.TransmissionReceived && adjustingLevel
                    && levelReadingWithinNormal && valveOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ProgramReady());
                evacuationValve = ValvePosition.VALVE_CLOSED;
                readyAnnounced = true;
                plantWaiting = true;
            } else if (event instanceof InputEvent.TransmissionReceived && adjustingLevel
                    && levelReadingWithinNormal && !valveOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ProgramReady());
                readyAnnounced = true;
                plantWaiting = true;
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.TransmissionReceived && stopLimitReached) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && communicationFailedOnly) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelRiskOnly) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelFailureNoEmergency
                    && pumpsToOpen) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && levelFailureNoEmergency
                    && pumpsToClose) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && levelFailureNoEmergency
                    && pumpsToHold) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
            } else if (event instanceof InputEvent.TransmissionReceived && otherFailureNoEmergency
                    && pumpsToOpen) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && otherFailureNoEmergency
                    && pumpsToClose) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && otherFailureNoEmergency
                    && pumpsToHold) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && allSoundNoEmergency
                    && pumpsToOpen) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && allSoundNoEmergency
                    && pumpsToClose) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && allSoundNoEmergency
                    && pumpsToHold) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.TransmissionReceived && stopLimitReached) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && communicationFailedOnly) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelRiskOnly) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelFailureNoEmergency
                    && pumpsToOpen) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && levelFailureNoEmergency
                    && pumpsToClose) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && levelFailureNoEmergency
                    && pumpsToHold) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
            } else if (event instanceof InputEvent.TransmissionReceived && allSoundNoEmergency
                    && pumpsToOpen) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && allSoundNoEmergency
                    && pumpsToClose) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && allSoundNoEmergency
                    && pumpsToHold) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && otherFailureNoEmergency
                    && pumpsToOpen) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && otherFailureNoEmergency
                    && pumpsToClose) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && otherFailureNoEmergency
                    && pumpsToHold) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.TransmissionReceived && stopLimitReached) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && communicationFailedOnly) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueSteamFault) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueControllerFault) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueLevelRisk) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelRepairedAllSound
                    && pumpsToOpen) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && levelRepairedAllSound
                    && pumpsToClose) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && levelRepairedAllSound
                    && pumpsToHold) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && levelRepairedOtherFailure
                    && pumpsToOpen) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && levelRepairedOtherFailure
                    && pumpsToClose) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && levelRepairedOtherFailure
                    && pumpsToHold) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueContinues
                    && pumpsToOpen) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueContinues
                    && pumpsToClose) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueContinues
                    && pumpsToHold) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // Terminal: the program has stopped and the physical environment is in charge.
        }
    }
}
