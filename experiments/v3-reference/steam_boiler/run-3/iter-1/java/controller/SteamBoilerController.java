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
 * In every mode the transitions are listed by severity (SB-FR8), and their guards
 * partition the possible situations, so exactly one applies in each cycle.
 * Emergency stop is terminal (SB-Beh22).
 */
public final class SteamBoilerController {

    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    private boolean boilerWaiting = false;
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
        boolean stopDemanded = sensor.successiveStops() >= BoilerConstants.STOP_REPETITIONS;
        boolean transmissionFault = sensor.transmissionFailure();
        boolean waitingMessage = sensor.steamBoilerWaitingReceived();
        boolean unitsReadyMessage = sensor.physicalUnitsReadyReceived();
        boolean levelDefective = sensor.levelDeviceDefective();
        boolean steamDefective = sensor.steamDeviceDefective();
        boolean pumpDefective = sensor.pumpDefective();
        boolean pumpControlDefective = sensor.pumpControllerDefective();
        boolean steamZero = sensor.steamRateZero();
        boolean levelLow = sensor.levelBelowNormal();
        boolean levelHigh = sensor.levelAboveNormal();
        boolean predictedLow = sensor.predictedLevelBelowNormal();
        boolean predictedHigh = sensor.predictedLevelAboveNormal();
        boolean limitRisk = sensor.levelLimitRisk();
        boolean valveOpen = evacuationValve == ValvePosition.OPEN;
        boolean otherUnitDefective = steamDefective || pumpDefective || pumpControlDefective;
        boolean levelRaise = levelLow && !levelHigh;
        boolean levelInBand = !levelLow && !levelHigh;
        boolean pumpsOn = predictedLow && !predictedHigh;
        boolean pumpsOff = predictedHigh;
        boolean pumpsKept = !predictedLow && !predictedHigh;

        // --- Initialization (SB-Beh2 to SB-Beh9, SB-Beh23) ---
        boolean initAbort = stopDemanded || transmissionFault || (unitsReadyMessage && !readyAnnounced);
        boolean plantWaiting = boilerWaiting || waitingMessage;
        boolean initIdle = !initAbort && !plantWaiting;
        boolean initSteamNotZero = !initAbort && plantWaiting && !steamZero;
        boolean initLevelFailure = !initAbort && plantWaiting && steamZero && levelDefective;
        boolean initRunning = !initAbort && plantWaiting && steamZero && !levelDefective;
        boolean unitsReadyAllSound = initRunning && unitsReadyMessage && !otherUnitDefective;
        boolean unitsReadySomeDefective = initRunning && unitsReadyMessage && otherUnitDefective;
        boolean readyRepeat = initRunning && !unitsReadyMessage && readyAnnounced;
        boolean levelAdjust = initRunning && !unitsReadyMessage && !readyAnnounced;

        // --- Normal, degraded and rescue (SB-Beh10 to SB-Beh21, SB-Beh23, SB-FR8) ---
        boolean operatingAbort = stopDemanded || transmissionFault || waitingMessage || unitsReadyMessage || limitRisk;
        boolean estimateUnreliable = steamDefective || pumpControlDefective;
        boolean levelLostNoEstimate = !operatingAbort && levelDefective && estimateUnreliable;
        boolean levelLostWithEstimate = !operatingAbort && levelDefective && !estimateUnreliable;
        boolean allUnitsSound = !operatingAbort && !levelDefective && !otherUnitDefective;
        boolean otherUnitsOnlyDefective = !operatingAbort && !levelDefective && otherUnitDefective;
        boolean estimateLost = !operatingAbort && estimateUnreliable;
        boolean levelBackSomeDefective = otherUnitsOnlyDefective && !estimateUnreliable;
        boolean rescueContinues = !operatingAbort && !estimateUnreliable && levelDefective;

        // --- Mode-nested transitions ---
        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionReceived && initAbort) {
                // SB-Beh23 (operator stop), SB-Beh9 (transmission failure)
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && initSteamNotZero) {
                // SB-Beh3: steam leaving the boiler is not zero
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && initLevelFailure) {
                // SB-Beh6: level measuring device failed during initialization
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && initIdle) {
                // SB-Beh2: wait for STEAM_BOILER_WAITING
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
            } else if (event instanceof InputEvent.TransmissionReceived && unitsReadyAllSound) {
                // SB-Beh8: every unit sound
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && unitsReadySomeDefective) {
                // SB-Beh8: some unit other than the level device defective
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && readyRepeat) {
                // SB-Beh7: repeat PROGRAM_READY until PHYSICAL_UNITS_READY
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ProgramReady());
                boilerWaiting = true;
            } else if (event instanceof InputEvent.TransmissionReceived && levelAdjust && levelHigh && !valveOpen) {
                // SB-Beh4: level above N2, open the evacuation valve (and stop filling)
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                evacuationValve = ValvePosition.OPEN;
                boilerWaiting = true;
            } else if (event instanceof InputEvent.TransmissionReceived && levelAdjust && levelHigh && valveOpen) {
                // SB-Beh4: keep draining
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                boilerWaiting = true;
            } else if (event instanceof InputEvent.TransmissionReceived && levelAdjust && levelRaise && valveOpen) {
                // SB-Beh5: level below N1 after draining, close the valve and fill
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(1));
                evacuationValve = ValvePosition.CLOSED;
                boilerWaiting = true;
            } else if (event instanceof InputEvent.TransmissionReceived && levelAdjust && levelRaise && !valveOpen) {
                // SB-Beh5: level below N1, activate a pump
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.OpenPump(1));
                boilerWaiting = true;
            } else if (event instanceof InputEvent.TransmissionReceived && levelAdjust && levelInBand && valveOpen) {
                // SB-Beh7: level between N1 and N2, close the valve, announce PROGRAM_READY
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ProgramReady());
                evacuationValve = ValvePosition.CLOSED;
                readyAnnounced = true;
                boilerWaiting = true;
            } else if (event instanceof InputEvent.TransmissionReceived && levelAdjust && levelInBand && !valveOpen) {
                // SB-Beh7: level between N1 and N2, stop filling, announce PROGRAM_READY
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ProgramReady());
                readyAnnounced = true;
                boilerWaiting = true;
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.TransmissionReceived && operatingAbort) {
                // SB-Beh23, SB-Beh14, SB-Beh13: operator stop, transmission failure, limit risk
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelLostNoEstimate) {
                // SB-FR8: level device failed while no reliable estimate is possible
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelLostWithEstimate) {
                // SB-Beh11: level device failed
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
            } else if (event instanceof InputEvent.TransmissionReceived && otherUnitsOnlyDefective) {
                // SB-Beh12: another unit failed
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && allUnitsSound && pumpsOff) {
                // SB-Beh10: level heading above N2, switch the pumps off
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && allUnitsSound && pumpsOn) {
                // SB-Beh10: level heading below N1, switch the pumps on
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && allUnitsSound && pumpsKept) {
                // SB-Beh10: level staying between N1 and N2, keep the pumps as they are
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.TransmissionReceived && operatingAbort) {
                // SB-Beh23, SB-Beh18: operator stop, transmission failure, limit risk
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelLostNoEstimate) {
                // SB-FR8: level device failed while no reliable estimate is possible
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelLostWithEstimate) {
                // SB-Beh17: level device failed
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
            } else if (event instanceof InputEvent.TransmissionReceived && allUnitsSound) {
                // SB-Beh16: every failed unit repaired
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && otherUnitsOnlyDefective && pumpsOff) {
                // SB-Beh15: regulation as in normal mode
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && otherUnitsOnlyDefective && pumpsOn) {
                // SB-Beh15: regulation as in normal mode
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && otherUnitsOnlyDefective && pumpsKept) {
                // SB-Beh15: regulation as in normal mode
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.TransmissionReceived && operatingAbort) {
                // SB-Beh23, SB-Beh21: operator stop, transmission failure, estimated limit risk
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && estimateLost) {
                // SB-Beh21: steam device or pump controller failed
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && allUnitsSound) {
                // SB-Beh20: level device repaired, no other unit defective
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && levelBackSomeDefective) {
                // SB-Beh20: level device repaired, another unit still defective
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueContinues && pumpsOff) {
                // SB-Beh19: regulation on the estimated level
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueContinues && pumpsOn) {
                // SB-Beh19: regulation on the estimated level
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueContinues && pumpsKept) {
                // SB-Beh19: regulation on the estimated level
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // SB-Beh22: terminal; the control task has ended and the program stops.
        }
    }
}
