package steam_boiler.controller;

import steam_boiler.BoilerConstants;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.actuator.ValvePosition;
import steam_boiler.event.InputEvent;
import steam_boiler.event.ModeValue;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * Mode logic of the steam-boiler control program (SB-V1, SB-V2). The program
 * is always in exactly one of five modes and starts in initialization
 * (SB-Beh1). Its analysis stage runs once per cycle, when the receive stage
 * has completed ({@link InputEvent.TransmissionReceived}, SB-V3); every cycle
 * outside emergency stop sends MODE(m) (SB-Beh24). Emergency stop is terminal
 * (SB-Beh22). When several conditions hold in one cycle, emergency stop
 * prevails over rescue, and rescue over degraded (SB-FR8).
 */
public final class SteamBoilerController {

    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /** Creates the controller in initialization mode. */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode. */
    public OperatingMode currentMode() {
        return currentMode;
    }

    /** Analysis stage of one cycle: decides the mode and the messages to send. */
    public void step(InputEvent event) {
        boolean operatorStopRequested = sensor.stopRequestedThrice();
        boolean transmissionLost = sensor.transmissionFailure();
        boolean waitingSignalNow = sensor.steamBoilerWaitingReceived();
        boolean plantWaiting = sensor.plantWaitingSignalled();
        boolean unitsReady = sensor.physicalUnitsReadyReceived();
        boolean readyAnnounced = sensor.programReadyAnnounced();
        boolean steamNotZero = sensor.steamRate() != 0.0;
        boolean levelUnitFailed = sensor.levelDeviceFailed();
        boolean steamUnitFailed = sensor.steamDeviceFailed();
        boolean pumpControlUnitFailed = sensor.pumpControlFailed();
        boolean nonLevelUnitFailed = sensor.otherUnitFailed();
        boolean levelAboveN2 = sensor.waterLevel() > BoilerConstants.NORMAL_N2;
        boolean levelBelowN1 = sensor.waterLevel() < BoilerConstants.NORMAL_N1;
        boolean levelNearLimit = sensor.levelRisk();
        boolean startMessageOutOfPlace = waitingSignalNow || unitsReady;
        boolean linkFailed = transmissionLost || startMessageOutOfPlace;
        boolean startupFault = transmissionLost || steamNotZero || levelUnitFailed;
        boolean initialChecksPassed = plantWaiting && !startupFault;
        boolean runningChecksPassed = !linkFailed && !levelNearLimit;
        boolean estimateUnreliable = steamUnitFailed || pumpControlUnitFailed;

        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionReceived && operatorStopRequested) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && !plantWaiting) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && plantWaiting && transmissionLost) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && plantWaiting && !transmissionLost && steamNotZero) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && plantWaiting && !transmissionLost && !steamNotZero && levelUnitFailed) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && initialChecksPassed && unitsReady && !nonLevelUnitFailed) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
                actuator.setValve(ValvePosition.VALVE_CLOSED);
                actuator.regulatePumps(sensor.pumpsRequired());
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && initialChecksPassed && unitsReady && nonLevelUnitFailed) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
                actuator.setValve(ValvePosition.VALVE_CLOSED);
                actuator.regulatePumps(sensor.pumpsRequired());
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && initialChecksPassed && !unitsReady && readyAnnounced) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && initialChecksPassed && !unitsReady && !readyAnnounced && levelAboveN2) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.setValve(ValvePosition.VALVE_OPEN);
                actuator.regulatePumps(0);
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && initialChecksPassed && !unitsReady && !readyAnnounced && !levelAboveN2
                    && levelBelowN1) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.setValve(ValvePosition.VALVE_CLOSED);
                actuator.regulatePumps(1);
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && initialChecksPassed && !unitsReady && !readyAnnounced && !levelAboveN2
                    && !levelBelowN1) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.setValve(ValvePosition.VALVE_CLOSED);
                actuator.regulatePumps(0);
                actuator.apply(new OutputEvent.ProgramReady());
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.TransmissionReceived && operatorStopRequested) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && linkFailed) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && !linkFailed && levelNearLimit) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && runningChecksPassed && levelUnitFailed) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
                actuator.regulatePumps(sensor.pumpsRequired());
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && runningChecksPassed && !levelUnitFailed && nonLevelUnitFailed) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
                actuator.regulatePumps(sensor.pumpsRequired());
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && runningChecksPassed && !levelUnitFailed && !nonLevelUnitFailed) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
                actuator.regulatePumps(sensor.pumpsRequired());
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.TransmissionReceived && operatorStopRequested) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && linkFailed) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && !linkFailed && levelNearLimit) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && runningChecksPassed && levelUnitFailed) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
                actuator.regulatePumps(sensor.pumpsRequired());
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && runningChecksPassed && !levelUnitFailed && !nonLevelUnitFailed) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
                actuator.regulatePumps(sensor.pumpsRequired());
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && runningChecksPassed && !levelUnitFailed && nonLevelUnitFailed) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
                actuator.regulatePumps(sensor.pumpsRequired());
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.TransmissionReceived && operatorStopRequested) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && linkFailed) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && !linkFailed && levelNearLimit) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && runningChecksPassed && estimateUnreliable) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && runningChecksPassed && !estimateUnreliable && levelUnitFailed) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
                actuator.regulatePumps(sensor.pumpsRequired());
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && runningChecksPassed && !estimateUnreliable && !levelUnitFailed
                    && !nonLevelUnitFailed) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
                actuator.regulatePumps(sensor.pumpsRequired());
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStopRequested
                    && runningChecksPassed && !estimateUnreliable && !levelUnitFailed
                    && nonLevelUnitFailed) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
                actuator.regulatePumps(sensor.pumpsRequired());
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // Terminal mode: the control task has ended (SB-Beh22).
        }
    }
}
