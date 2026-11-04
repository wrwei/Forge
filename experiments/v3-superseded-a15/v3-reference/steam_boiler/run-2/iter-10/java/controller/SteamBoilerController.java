package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.domain.OperatingMode;
import steam_boiler.domain.PumpSwitch;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program's mode logic. It is stepped once per cycle, after the sensor
 * has analysed the cycle's incoming messages, and issues the cycle's mode-dependent messages:
 * MODE(m) on every cycle, PROGRAM_READY, VALVE, OPEN_PUMP(n) and CLOSE_PUMP(n).
 */
public final class SteamBoilerController {
    private BoilerMode currentMode = BoilerMode.INITIALIZATION;
    private boolean plantWaiting = false;
    private boolean valveOpen = false;
    private boolean programReadySent = false;
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

    /** Performs the analysis stage of one cycle. */
    public void step(InputEvent event) {
        // Each predicate uses one operator (all && or all ||); a predicate of the other operator
        // is only ever referenced negated.

        // --- Initialization: no condition demanding emergency stop holds (SB-Beh3/6/9/23; the
        // sensor's emergencyDemanded covers the steam and level checks after STEAM_BOILER_WAITING) ---
        boolean readyAberrant = sensor.physicalUnitsReadyReceived() && !programReadySent;
        boolean boilerIdle = !plantWaiting && !sensor.steamBoilerWaitingReceived();
        boolean initCalm = !sensor.emergencyDemanded() && !readyAberrant;

        // --- Initialization phases ---
        boolean unitsAllSound = sensor.levelDeviceSound() && sensor.pumpsSound() && sensor.estimateTrustworthy();
        boolean unitsReadyAllSound = initCalm && programReadySent && sensor.physicalUnitsReadyReceived() && unitsAllSound;
        boolean unitsReadySomeDefective = initCalm && programReadySent && sensor.physicalUnitsReadyReceived() && !unitsAllSound;
        boolean unitsReadyAwaited = initCalm && programReadySent && !sensor.physicalUnitsReadyReceived();
        boolean initIdle = initCalm && !programReadySent && boilerIdle;
        boolean initAdjusting = initCalm && !programReadySent && !boilerIdle;

        // --- Normal, degraded and rescue: emergency dominates rescue, which dominates degraded ---
        boolean operatingEmergency = sensor.emergencyDemanded() || sensor.steamBoilerWaitingReceived() || sensor.physicalUnitsReadyReceived();
        boolean rescueEmergency = operatingEmergency || !sensor.estimateTrustworthy();
        boolean nonLevelUnitsSound = sensor.pumpsSound() && sensor.estimateTrustworthy();
        boolean levelFailureNoEmergency = !operatingEmergency && !sensor.levelDeviceSound();
        boolean unitFailureNoEmergency = !operatingEmergency && sensor.levelDeviceSound() && !nonLevelUnitsSound;
        boolean soundNoEmergency = !operatingEmergency && unitsAllSound;
        boolean levelRepairedSound = !rescueEmergency && unitsAllSound;
        boolean levelRepairedDegraded = !rescueEmergency && sensor.levelDeviceSound() && !nonLevelUnitsSound;
        boolean levelStillEstimated = !rescueEmergency && !sensor.levelDeviceSound();

        // --- Water level against the normal band [N1, N2] ---
        boolean levelAboveN2 = sensor.levelAboveNormalBand() && !sensor.levelBelowNormalBand();
        boolean levelInBand = !sensor.levelBelowNormalBand() && !sensor.levelAboveNormalBand();
        boolean levelBelowN1 = !levelAboveN2 && !levelInBand;

        if (currentMode == BoilerMode.INITIALIZATION) {
            if (event instanceof InputEvent.MessagesReceived && !initCalm) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && unitsReadyAllSound) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_NORMAL));
            } else if (event instanceof InputEvent.MessagesReceived && unitsReadySomeDefective) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_DEGRADED));
            } else if (event instanceof InputEvent.MessagesReceived && unitsReadyAwaited) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.MessagesReceived && initIdle) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_INITIALIZATION));
            } else if (event instanceof InputEvent.MessagesReceived && initAdjusting && levelAboveN2 && !valveOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                valveOpen = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
            } else if (event instanceof InputEvent.MessagesReceived && initAdjusting && levelAboveN2 && valveOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_INITIALIZATION));
            } else if (event instanceof InputEvent.MessagesReceived && initAdjusting && levelBelowN1 && valveOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                valveOpen = false;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_ON));
            } else if (event instanceof InputEvent.MessagesReceived && initAdjusting && levelBelowN1 && !valveOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_ON));
            } else if (event instanceof InputEvent.MessagesReceived && initAdjusting && levelInBand && valveOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                valveOpen = false;
                programReadySent = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_OFF));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.MessagesReceived && initAdjusting && levelInBand && !valveOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                programReadySent = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_OFF));
                actuator.apply(new OutputEvent.ProgramReady());
            }

        } else if (currentMode == BoilerMode.NORMAL) {
            if (event instanceof InputEvent.MessagesReceived && operatingEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && levelFailureNoEmergency) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_RESCUE));
            } else if (event instanceof InputEvent.MessagesReceived && unitFailureNoEmergency) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_DEGRADED));
            } else if (event instanceof InputEvent.MessagesReceived && soundNoEmergency && levelBelowN1) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_NORMAL));
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_ON));
            } else if (event instanceof InputEvent.MessagesReceived && soundNoEmergency && levelAboveN2) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_NORMAL));
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_OFF));
            } else if (event instanceof InputEvent.MessagesReceived && soundNoEmergency && levelInBand) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_NORMAL));
            }

        } else if (currentMode == BoilerMode.DEGRADED) {
            if (event instanceof InputEvent.MessagesReceived && operatingEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && levelFailureNoEmergency) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_RESCUE));
            } else if (event instanceof InputEvent.MessagesReceived && soundNoEmergency) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_NORMAL));
            } else if (event instanceof InputEvent.MessagesReceived && unitFailureNoEmergency && levelBelowN1) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_DEGRADED));
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_ON));
            } else if (event instanceof InputEvent.MessagesReceived && unitFailureNoEmergency && levelAboveN2) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_DEGRADED));
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_OFF));
            } else if (event instanceof InputEvent.MessagesReceived && unitFailureNoEmergency && levelInBand) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_DEGRADED));
            }

        } else if (currentMode == BoilerMode.RESCUE) {
            if (event instanceof InputEvent.MessagesReceived && rescueEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && levelRepairedSound) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_NORMAL));
            } else if (event instanceof InputEvent.MessagesReceived && levelRepairedDegraded) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_DEGRADED));
            } else if (event instanceof InputEvent.MessagesReceived && levelStillEstimated && levelBelowN1) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_RESCUE));
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_ON));
            } else if (event instanceof InputEvent.MessagesReceived && levelStillEstimated && levelAboveN2) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_RESCUE));
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_OFF));
            } else if (event instanceof InputEvent.MessagesReceived && levelStillEstimated && levelInBand) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_RESCUE));
            }

        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
            // Terminal: the control task has ended and the physical environment is responsible.
        }
    }
}
