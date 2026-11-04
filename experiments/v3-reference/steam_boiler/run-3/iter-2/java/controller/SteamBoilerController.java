package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.domain.OperatingMode;
import steam_boiler.domain.ValvePosition;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * Mode controller of the steam-boiler control program (SB-V1, SB-V2).
 *
 * <p>{@link #step(InputEvent)} is the analysis stage of one five-second cycle (SB-V3):
 * it is called with {@link InputEvent.TransmissionReceived} once the cycle's incoming
 * messages have been received, decides the operating mode and issues the mode-level
 * messages: MODE on every cycle (SB-Beh24), PROGRAM_READY, VALVE and the pump orders.
 * In every mode the transitions are listed by severity, highest first (SB-FR8); the
 * last one keeps the mode when nothing else applies. Emergency stop is terminal
 * (SB-Beh22).
 */
public final class SteamBoilerController {

    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    private boolean readyAnnounced = false;
    private ValvePosition evacuationValve = ValvePosition.CLOSED;
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

    /** Analyses one cycle and moves to the mode the requirements prescribe. */
    public void step(InputEvent event) {
        // --- Named predicates: what the cycle's transmission and analysis report ---
        boolean stopOrTransmissionFault = sensor.stopOrTransmissionFailure();
        boolean plantWaiting = sensor.steamBoilerWaitingReceived();
        boolean unitsReady = sensor.physicalUnitsReadyReceived();
        boolean startupFault = sensor.startupCheckFailed();
        boolean operatingEmergency = sensor.operatingEmergency();
        boolean levelDefective = sensor.levelDeviceDefective();
        boolean otherUnitDefective = sensor.otherUnitDefective();
        boolean estimateUnreliable = sensor.estimateUnreliable();
        boolean levelLow = sensor.levelBelowNormal();
        boolean levelHigh = sensor.levelAboveNormal();
        boolean predictedLow = sensor.predictedLevelBelowNormal();
        boolean predictedHigh = sensor.predictedLevelAboveNormal();
        boolean valveOpen = evacuationValve == ValvePosition.OPEN;
        // Initialization: operator stop, transmission failure (including PHYSICAL_UNITS_READY
        // before PROGRAM_READY), or failed start-up checks (SB-Beh3, SB-Beh6, SB-Beh9, SB-Beh23).
        boolean initAbort = stopOrTransmissionFault || (unitsReady && !readyAnnounced) || (plantWaiting && startupFault);
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
            } else if (event instanceof InputEvent.TransmissionReceived && readyAnnounced) {
                // SB-Beh7: repeat PROGRAM_READY until PHYSICAL_UNITS_READY
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived && plantWaiting && levelHigh && !valveOpen) {
                // SB-Beh4: level above N2, open the evacuation valve (and stop filling)
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                evacuationValve = ValvePosition.OPEN;
            } else if (event instanceof InputEvent.TransmissionReceived && plantWaiting && levelLow && valveOpen) {
                // SB-Beh5: level below N1 after draining, close the valve and fill
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(1));
                evacuationValve = ValvePosition.CLOSED;
            } else if (event instanceof InputEvent.TransmissionReceived && plantWaiting && levelLow) {
                // SB-Beh5: level below N1, activate a pump
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.OpenPump(1));
            } else if (event instanceof InputEvent.TransmissionReceived && plantWaiting && !levelHigh && valveOpen) {
                // SB-Beh7: level between N1 and N2, close the valve, announce PROGRAM_READY
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ProgramReady());
                evacuationValve = ValvePosition.CLOSED;
                readyAnnounced = true;
            } else if (event instanceof InputEvent.TransmissionReceived && plantWaiting && !levelHigh) {
                // SB-Beh7: level between N1 and N2, stop filling, announce PROGRAM_READY
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ProgramReady());
                readyAnnounced = true;
            } else if (event instanceof InputEvent.TransmissionReceived) {
                // SB-Beh2: wait for STEAM_BOILER_WAITING; SB-Beh4: keep draining
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
            } else if (event instanceof InputEvent.TransmissionReceived && predictedHigh) {
                // SB-Beh10: level heading above N2, switch the pumps off
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && predictedLow) {
                // SB-Beh10: level heading below N1, switch the pumps on
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived) {
                // SB-Beh10: level staying between N1 and N2, keep the pumps as they are
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
            } else if (event instanceof InputEvent.TransmissionReceived && predictedHigh) {
                // SB-Beh15: regulation as in normal mode
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && predictedLow) {
                // SB-Beh15: regulation as in normal mode
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived) {
                // SB-Beh15: regulation as in normal mode, pumps kept
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
            } else if (event instanceof InputEvent.TransmissionReceived && predictedHigh) {
                // SB-Beh19: regulation on the estimated level
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && predictedLow) {
                // SB-Beh19: regulation on the estimated level
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived) {
                // SB-Beh19: regulation on the estimated level, pumps kept
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // SB-Beh22: terminal; the control task has ended and the program stops.
        }
    }
}
