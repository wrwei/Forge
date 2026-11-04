package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.domain.OperatingMode;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * Mode controller of the steam-boiler control program (SB-V1, SB-V2).
 *
 * <p>{@link #step(InputEvent)} is the mode decision of one five-second cycle (SB-V3):
 * it is called with {@link InputEvent.TransmissionReceived} once the cycle's incoming
 * messages have been received and analysed, chooses the operating mode, and sends
 * MODE(m) on every cycle (SB-Beh24). In every mode the transitions are listed by
 * severity, highest first (SB-FR8); the last one keeps the mode when nothing else
 * applies. Emergency stop is terminal (SB-Beh22). Regulation within a mode is done by
 * {@link steam_boiler.monitor.LevelRegulator}.
 */
public final class SteamBoilerController {

    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /** Creates the controller in initialization mode (SB-Beh1). */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode (SB-V2). */
    public OperatingMode currentMode() {
        return currentMode;
    }

    /** Decides the operating mode of one cycle, as the requirements prescribe. */
    public void step(InputEvent event) {
        // --- Named predicates: what the analysis of the cycle reports ---
        boolean initAbort = sensor.initialisationAbort();
        boolean unitsReady = sensor.physicalUnitsReadyReceived();
        boolean operatingEmergency = sensor.operatingEmergency();
        boolean levelDefective = sensor.levelDeviceDefective();
        boolean otherUnitDefective = sensor.otherUnitDefective();
        boolean estimateUnreliable = sensor.estimateUnreliable();
        // Normal and degraded: a lost level device cannot be replaced by an estimate (SB-FR8).
        boolean operatingAbort = operatingEmergency || (levelDefective && estimateUnreliable);
        // Rescue: the estimate is no longer trustworthy (SB-Beh21).
        boolean rescueAbort = operatingEmergency || estimateUnreliable;
        boolean allUnitsSound = !levelDefective && !otherUnitDefective;

        // --- Mode-nested transitions, highest priority first ---
        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionReceived && initAbort) {
                // SB-Beh23, SB-Beh9, SB-Beh3, SB-Beh6
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && unitsReady && otherUnitDefective) {
                // SB-Beh8: some unit other than the level device defective
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && unitsReady) {
                // SB-Beh8: every unit sound
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived) {
                // SB-Beh2, SB-Beh4, SB-Beh5, SB-Beh7: initialization continues
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.TransmissionReceived && operatingAbort) {
                // SB-Beh23, SB-Beh14, SB-Beh13, SB-FR8
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelDefective) {
                // SB-Beh11: level device failed
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
            } else if (event instanceof InputEvent.TransmissionReceived && otherUnitDefective) {
                // SB-Beh12: another unit failed
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived) {
                // SB-Beh10: normal operation continues
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.TransmissionReceived && operatingAbort) {
                // SB-Beh23, SB-Beh18, SB-FR8
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelDefective) {
                // SB-Beh17: level device failed
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
            } else if (event instanceof InputEvent.TransmissionReceived && !otherUnitDefective) {
                // SB-Beh16: every failed unit repaired
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived) {
                // SB-Beh15: degraded operation continues
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.TransmissionReceived && rescueAbort) {
                // SB-Beh23, SB-Beh21
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && allUnitsSound) {
                // SB-Beh20: level device repaired, no other unit defective
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && !levelDefective) {
                // SB-Beh20: level device repaired, another unit still defective
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived) {
                // SB-Beh19: rescue operation continues on the estimated level
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // SB-Beh22: terminal; the control task has ended and the program stops.
        }
    }
}
