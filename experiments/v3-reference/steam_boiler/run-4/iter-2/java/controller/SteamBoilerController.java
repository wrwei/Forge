package steam_boiler.controller;

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
 * stop, then rescue, then degraded, then regulation; each guard conjoins the
 * negations of the guards above it, so the guards of a mode are mutually
 * exclusive and together cover every case.
 */
public final class SteamBoilerController {

    private BoilerMode currentMode = BoilerMode.INITIALIZATION;
    private boolean valveOpen = false;
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
        boolean initAbort = sensor.initialisationEmergency();
        boolean operatingAbort = sensor.operationEmergency();
        boolean rescueAbort = sensor.rescueEmergency();
        boolean plantWaiting = sensor.steamBoilerWaitingReceived();
        boolean unitsReady = sensor.physicalUnitsReadyReceived();
        boolean levelFailed = sensor.levelUnitFailed();
        boolean otherUnitFailed = sensor.nonLevelUnitFailed();
        boolean levelBelowNormal = sensor.levelUnderNormalBand();
        boolean levelAboveNormal = sensor.levelOverNormalBand();
        boolean evacuationOpen = valveOpen;

        // --- One block per mode ---
        if (currentMode == BoilerMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionReceived && initAbort) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && !plantWaiting) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.INITIALIZATION));
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && plantWaiting
                    && unitsReady && !otherUnitFailed) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && plantWaiting
                    && unitsReady && otherUnitFailed) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && plantWaiting
                    && !unitsReady && levelAboveNormal && !evacuationOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                valveOpen = true;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && plantWaiting
                    && !unitsReady && levelAboveNormal && evacuationOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.INITIALIZATION));
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && plantWaiting
                    && !unitsReady && !levelAboveNormal && evacuationOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                valveOpen = false;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && plantWaiting
                    && !unitsReady && levelBelowNormal && !levelAboveNormal && !evacuationOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(BoilerMode.INITIALIZATION));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && !initAbort && plantWaiting
                    && !unitsReady && !levelBelowNormal && !levelAboveNormal && !evacuationOpen) {
                currentMode = BoilerMode.INITIALIZATION;
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
                    && !otherUnitFailed && levelBelowNormal && !levelAboveNormal) {
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
                    && otherUnitFailed && levelBelowNormal && !levelAboveNormal) {
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
                    && levelBelowNormal && !levelAboveNormal) {
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
