package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.domain.OperatingMode;
import steam_boiler.domain.PumpSwitch;
import steam_boiler.domain.ValveSetting;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program's mode logic. It is stepped once per cycle, after the sensor
 * has analysed the cycle's incoming messages, and issues the cycle's mode-dependent messages:
 * MODE(m) on every cycle, PROGRAM_READY, the evacuation-valve order and the pump orders.
 */
public final class SteamBoilerController {
    private BoilerMode currentMode = BoilerMode.INITIALIZATION;
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
        // is only ever referenced negated. In each mode the last branch is taken when none of the
        // others is (its predicate conjoins their negations); for the regulation branches that is
        // the level lying within [N1, N2].

        // --- Water level against the normal band [N1, N2] ---
        boolean levelAboveN2 = sensor.levelAboveNormalBand() && !sensor.levelBelowNormalBand();

        // --- Initialization (SB-Beh2..9); the sensor's emergencyDemanded covers STOP, transmission
        // failure and, after STEAM_BOILER_WAITING, a non-zero steam rate or a failed level device ---
        boolean readyAberrant = sensor.physicalUnitsReadyReceived() && !programReadySent;
        boolean initCalm = !sensor.emergencyDemanded() && !readyAberrant;
        boolean unitsAllSound = sensor.levelDeviceSound() && sensor.otherUnitsSound();
        boolean unitsReadyAllSound = initCalm && programReadySent && sensor.physicalUnitsReadyReceived() && unitsAllSound;
        boolean unitsReadySomeDefective = initCalm && programReadySent && sensor.physicalUnitsReadyReceived() && !unitsAllSound;
        boolean unitsReadyAwaited = initCalm && programReadySent && !sensor.physicalUnitsReadyReceived();
        boolean initIdle = initCalm && !programReadySent && !sensor.boilerWaitingSeen();
        boolean adjustLow = initCalm && !programReadySent && sensor.boilerWaitingSeen() && sensor.levelBelowNormalBand();
        boolean adjustHigh = initCalm && !programReadySent && sensor.boilerWaitingSeen() && levelAboveN2;
        boolean initOtherwise = initCalm && !unitsReadyAllSound && !unitsReadySomeDefective && !unitsReadyAwaited && !initIdle && !adjustHigh && !adjustLow;

        // --- Normal, degraded and rescue: emergency dominates rescue, which dominates degraded ---
        boolean operatingEmergency = sensor.emergencyDemanded() || sensor.physicalUnitsReadyReceived();
        boolean levelFailureNoEmergency = !operatingEmergency && !sensor.levelDeviceSound();
        boolean unitFailureNoEmergency = !operatingEmergency && sensor.levelDeviceSound() && !sensor.otherUnitsSound();
        boolean soundNoEmergency = !operatingEmergency && sensor.levelDeviceSound() && sensor.otherUnitsSound();
        boolean soundLow = soundNoEmergency && sensor.levelBelowNormalBand();
        boolean soundHigh = soundNoEmergency && levelAboveN2;
        boolean normalOtherwise = !operatingEmergency && !levelFailureNoEmergency && !unitFailureNoEmergency && !soundLow && !soundHigh;
        boolean degradedLow = unitFailureNoEmergency && sensor.levelBelowNormalBand();
        boolean degradedHigh = unitFailureNoEmergency && levelAboveN2;
        boolean degradedOtherwise = !operatingEmergency && !levelFailureNoEmergency && !soundNoEmergency && !degradedLow && !degradedHigh;
        boolean estimatedLow = levelFailureNoEmergency && sensor.levelBelowNormalBand();
        boolean estimatedHigh = levelFailureNoEmergency && levelAboveN2;
        boolean rescueOtherwise = !operatingEmergency && !soundNoEmergency && !unitFailureNoEmergency && !estimatedLow && !estimatedHigh;

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
            } else if (event instanceof InputEvent.MessagesReceived && adjustHigh) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.DrainValve(ValveSetting.VALVE_OPEN));
            } else if (event instanceof InputEvent.MessagesReceived && adjustLow) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.DrainValve(ValveSetting.VALVE_CLOSED));
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_ON));
            } else if (event instanceof InputEvent.MessagesReceived && initOtherwise) {
                currentMode = BoilerMode.INITIALIZATION;
                programReadySent = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.DrainValve(ValveSetting.VALVE_CLOSED));
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
            } else if (event instanceof InputEvent.MessagesReceived && soundLow) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_NORMAL));
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_ON));
            } else if (event instanceof InputEvent.MessagesReceived && soundHigh) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_NORMAL));
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_OFF));
            } else if (event instanceof InputEvent.MessagesReceived && normalOtherwise) {
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
            } else if (event instanceof InputEvent.MessagesReceived && degradedLow) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_DEGRADED));
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_ON));
            } else if (event instanceof InputEvent.MessagesReceived && degradedHigh) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_DEGRADED));
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_OFF));
            } else if (event instanceof InputEvent.MessagesReceived && degradedOtherwise) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_DEGRADED));
            }

        } else if (currentMode == BoilerMode.RESCUE) {
            // In rescue, emergencyDemanded also covers an estimate that can no longer be trusted.
            if (event instanceof InputEvent.MessagesReceived && operatingEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && soundNoEmergency) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_NORMAL));
            } else if (event instanceof InputEvent.MessagesReceived && unitFailureNoEmergency) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_DEGRADED));
            } else if (event instanceof InputEvent.MessagesReceived && estimatedLow) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_RESCUE));
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_ON));
            } else if (event instanceof InputEvent.MessagesReceived && estimatedHigh) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_RESCUE));
                actuator.apply(new OutputEvent.SwitchPumps(PumpSwitch.ALL_OFF));
            } else if (event instanceof InputEvent.MessagesReceived && rescueOtherwise) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_RESCUE));
            }

        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
            // The control task has ended and the physical environment is responsible; the program
            // only keeps reporting its mode on every cycle (SB-Beh22, SB-Beh24).
            if (event instanceof InputEvent.MessagesReceived) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.MODE_EMERGENCY_STOP));
            }
        }
    }
}
