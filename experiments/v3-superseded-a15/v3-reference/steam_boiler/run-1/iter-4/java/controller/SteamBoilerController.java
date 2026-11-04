package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
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
        boolean startupEmergency = sensor.initialEmergency();
        boolean plantWaiting = sensor.plantWaitingSignalled();
        boolean unitsConfirmedReady = sensor.startConfirmed();
        boolean excessWater = sensor.drainNeeded();
        boolean lowWater = sensor.fillNeeded();
        boolean operatingEmergency = sensor.runningEmergency();
        boolean levelUnitFailed = sensor.levelDeviceFailed();
        boolean nonLevelUnitFailed = sensor.otherUnitFailed();
        boolean levelEstimateUnreliable = sensor.estimateUnreliable();

        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionReceived && startupEmergency) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !startupEmergency
                    && unitsConfirmedReady && !nonLevelUnitFailed) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
                actuator.closeValve();
                actuator.regulatePumps();
            } else if (event instanceof InputEvent.TransmissionReceived && !startupEmergency
                    && unitsConfirmedReady && nonLevelUnitFailed) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
                actuator.closeValve();
                actuator.regulatePumps();
            } else if (event instanceof InputEvent.TransmissionReceived && !startupEmergency
                    && !unitsConfirmedReady && !plantWaiting) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !startupEmergency
                    && !unitsConfirmedReady && plantWaiting && excessWater) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.openValve();
                actuator.stopPumps();
            } else if (event instanceof InputEvent.TransmissionReceived && !startupEmergency
                    && !unitsConfirmedReady && plantWaiting && !excessWater && lowWater) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.closeValve();
                actuator.runFillingPump();
            } else if (event instanceof InputEvent.TransmissionReceived && !startupEmergency
                    && !unitsConfirmedReady && plantWaiting && !excessWater && !lowWater) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.INITIALIZATION_MODE));
                actuator.closeValve();
                actuator.stopPumps();
                actuator.apply(new OutputEvent.ProgramReady());
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.TransmissionReceived && operatingEmergency) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingEmergency
                    && levelUnitFailed) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
                actuator.regulatePumps();
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingEmergency
                    && !levelUnitFailed && nonLevelUnitFailed) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
                actuator.regulatePumps();
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingEmergency
                    && !levelUnitFailed && !nonLevelUnitFailed) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
                actuator.regulatePumps();
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.TransmissionReceived && operatingEmergency) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingEmergency
                    && levelUnitFailed) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
                actuator.regulatePumps();
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingEmergency
                    && !levelUnitFailed && !nonLevelUnitFailed) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
                actuator.regulatePumps();
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingEmergency
                    && !levelUnitFailed && nonLevelUnitFailed) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
                actuator.regulatePumps();
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.TransmissionReceived && operatingEmergency) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingEmergency
                    && levelEstimateUnreliable) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingEmergency
                    && !levelEstimateUnreliable && levelUnitFailed) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.RESCUE_MODE));
                actuator.regulatePumps();
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingEmergency
                    && !levelEstimateUnreliable && !levelUnitFailed && !nonLevelUnitFailed) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.NORMAL_MODE));
                actuator.regulatePumps();
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingEmergency
                    && !levelEstimateUnreliable && !levelUnitFailed && nonLevelUnitFailed) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeValue.DEGRADED_MODE));
                actuator.regulatePumps();
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // Terminal mode: the control task has ended (SB-Beh22).
        }
    }
}
