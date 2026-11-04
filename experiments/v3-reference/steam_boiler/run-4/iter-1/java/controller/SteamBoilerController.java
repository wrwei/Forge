package steam_boiler.controller;

import steam_boiler.BoilerConstants;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.domain.BoilerMode;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program's mode logic (SB-V1, SB-V2): one call of
 * {@link #step(InputEvent)} per cycle, after the receive stage. Every cycle
 * takes exactly one transition, and every transition sends MODE (SB-Beh24).
 * Within a mode the transitions are listed by precedence (SB-FR8): emergency
 * stop, then rescue, then degraded, then regulation.
 */
public final class SteamBoilerController {

    private BoilerMode currentMode = BoilerMode.INITIALIZATION;
    private boolean boilerWaiting = false;
    private boolean valveOpen = false;
    private boolean readyAnnounced = false;
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /** Creates the controller in initialization mode (SB-Beh1). */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode. */
    public BoilerMode currentMode() {
        return currentMode;
    }

    /** Analyses the cycle's transmission and decides the mode and the commands. */
    public void step(InputEvent event) {
        // --- Named predicates ---
        boolean operatorStop = sensor.consecutiveStops() >= BoilerConstants.STOP_LIMIT;
        boolean transmissionLost = sensor.transmissionFailure();
        boolean waitingReceived = sensor.waitingMessagePresent();
        boolean readyReceived = sensor.readyMessagePresent();
        boolean plantWaiting = boilerWaiting || waitingReceived;
        boolean readyAberrant = readyReceived && !readyAnnounced;
        boolean initMessageAberrant = waitingReceived || readyReceived;
        boolean steamNonZero = sensor.steamRate() != 0.0;
        boolean levelFailed = sensor.levelUnitFailed();
        boolean steamFailed = sensor.steamUnitFailed();
        boolean pumpFailed = sensor.failedPumpCount() > 0;
        boolean pumpControlFailed = sensor.failedPumpControlCount() > 0;
        boolean otherUnitFailed = steamFailed || pumpFailed || pumpControlFailed;
        boolean rescueImpossible = steamFailed || pumpControlFailed;
        boolean levelLossFatal = levelFailed && rescueImpossible;
        boolean levelBelowMinLimit = sensor.minimumLevelNextCycle() < BoilerConstants.MIN_LIMIT;
        boolean levelAboveMaxLimit = sensor.maximumLevelNextCycle() > BoilerConstants.MAX_LIMIT;
        boolean levelRisk = levelBelowMinLimit || levelAboveMaxLimit;
        boolean levelBelowNormal = sensor.waterLevel() < BoilerConstants.MIN_NORMAL;
        boolean levelAboveNormal = sensor.waterLevel() > BoilerConstants.MAX_NORMAL;
        boolean evacuationOpen = valveOpen;
        boolean startupSteamFault = plantWaiting && steamNonZero;
        boolean startupLevelFault = plantWaiting && levelFailed;
        boolean initAbort = operatorStop || transmissionLost || readyAberrant || startupSteamFault || startupLevelFault;
        boolean operatingAbort = operatorStop || transmissionLost || initMessageAberrant || levelRisk || levelLossFatal;
        boolean rescueAbort = operatingAbort || rescueImpossible;

        // --- One block per mode ---
        if (currentMode == BoilerMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionReceived && initAbort) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && !plantWaiting) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.INITIALIZATION));
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && plantWaiting
                    && readyReceived && !otherUnitFailed) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && plantWaiting
                    && readyReceived && otherUnitFailed) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && plantWaiting
                    && !readyReceived && levelAboveNormal && !evacuationOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                boilerWaiting = true;
                valveOpen = true;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && plantWaiting
                    && !readyReceived && levelAboveNormal && evacuationOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                boilerWaiting = true;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.INITIALIZATION));
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && plantWaiting
                    && !readyReceived && levelBelowNormal && evacuationOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                boilerWaiting = true;
                valveOpen = false;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && plantWaiting
                    && !readyReceived && levelBelowNormal && !evacuationOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                boilerWaiting = true;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.INITIALIZATION));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && plantWaiting
                    && !readyReceived && !levelBelowNormal && !levelAboveNormal && evacuationOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                boilerWaiting = true;
                valveOpen = false;
                readyAnnounced = true;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && plantWaiting
                    && !readyReceived && !levelBelowNormal && !levelAboveNormal && !evacuationOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                boilerWaiting = true;
                readyAnnounced = true;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.ProgramReady());
            }

        } else if (currentMode == BoilerMode.NORMAL) {
            if (event instanceof InputEvent.TransmissionReceived && operatingAbort) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingAbort && levelFailed) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.RESCUE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingAbort && !levelFailed
                    && otherUnitFailed) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingAbort && !levelFailed
                    && !otherUnitFailed && levelBelowNormal) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.NORMAL));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingAbort && !levelFailed
                    && !otherUnitFailed && levelAboveNormal) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.NORMAL));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingAbort && !levelFailed
                    && !otherUnitFailed && !levelBelowNormal && !levelAboveNormal) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.NORMAL));
            }

        } else if (currentMode == BoilerMode.DEGRADED) {
            if (event instanceof InputEvent.TransmissionReceived && operatingAbort) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingAbort && levelFailed) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.RESCUE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingAbort && !levelFailed
                    && !otherUnitFailed) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingAbort && !levelFailed
                    && otherUnitFailed && levelBelowNormal) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingAbort && !levelFailed
                    && otherUnitFailed && levelAboveNormal) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatingAbort && !levelFailed
                    && otherUnitFailed && !levelBelowNormal && !levelAboveNormal) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.DEGRADED));
            }

        } else if (currentMode == BoilerMode.RESCUE) {
            if (event instanceof InputEvent.TransmissionReceived && rescueAbort) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && !rescueAbort && !levelFailed
                    && !otherUnitFailed) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && !rescueAbort && !levelFailed
                    && otherUnitFailed) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && !rescueAbort && levelFailed
                    && levelBelowNormal) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.RESCUE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && !rescueAbort && levelFailed
                    && levelAboveNormal) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.RESCUE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && !rescueAbort && levelFailed
                    && !levelBelowNormal && !levelAboveNormal) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.RESCUE));
            }

        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
            // Terminal mode (SB-Beh22): the control task has ended and no transition leaves it.
        }
    }
}
