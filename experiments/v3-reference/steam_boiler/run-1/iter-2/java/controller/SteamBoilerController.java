package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.BoilerMode;
import steam_boiler.mode.OperatingMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program's mode logic. {@link #step} is called once
 * per cycle with the cycle's {@link InputEvent.Transmission}, after the
 * transmission's messages have been analysed into the sensor; it chooses the
 * mode for the cycle and issues the cycle's MODE, valve, pump and
 * PROGRAM_READY messages.
 */
public final class SteamBoilerController {

    private BoilerMode currentMode = BoilerMode.INITIALISATION;
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;
    private boolean valveOpen = false;
    private boolean programReadySent = false;

    /** Creates the controller in initialisation mode. */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode. */
    public BoilerMode currentMode() {
        return currentMode;
    }

    /**
     * Performs the analysis stage of one cycle. In normal, degraded and
     * rescue mode every cycle that does not stop the program also regulates
     * the pumps: all four open below N1, all four closed above N2.
     */
    public void step(InputEvent event) {
        // What the cycle's transmission established.
        boolean shutdownDemanded = sensor.shutdownDemanded();
        boolean plantWaiting = sensor.plantWaiting();
        boolean unitsReady = sensor.unitsReady();
        boolean startUpFault = sensor.startUpFault();
        boolean levelBelowN1 = sensor.levelBelowNormal();
        boolean levelAboveN2 = sensor.levelAboveNormal();
        boolean levelInNormalBand = !levelBelowN1 && !levelAboveN2;
        boolean levelAtRisk = sensor.levelAtRisk();
        boolean levelFailure = sensor.levelFailure();
        boolean unitFailure = sensor.unitFailure();
        boolean estimateUnreliable = sensor.estimateUnreliable();

        // Initialisation.
        boolean initialisationAbort = shutdownDemanded || (plantWaiting && startUpFault);
        boolean adjustingLevel = !shutdownDemanded && plantWaiting && !startUpFault && !programReadySent;
        boolean awaitingUnits = !shutdownDemanded && plantWaiting && !startUpFault && programReadySent;

        // Normal and degraded modes.
        boolean operationSafe = !shutdownDemanded && !levelAtRisk;
        boolean operatingEmergency = !operationSafe || (levelFailure && estimateUnreliable);
        boolean enterRescue = operationSafe && levelFailure && !estimateUnreliable;
        boolean continueDegraded = operationSafe && !levelFailure && unitFailure;
        boolean continueNormal = operationSafe && !levelFailure && !unitFailure;

        // Rescue mode.
        boolean rescueEmergency = !operationSafe || estimateUnreliable;
        boolean leaveRescueNormal = operationSafe && !estimateUnreliable && !levelFailure && !unitFailure;
        boolean leaveRescueDegraded = operationSafe && !estimateUnreliable && !levelFailure && unitFailure;
        boolean stayInRescue = operationSafe && !estimateUnreliable && levelFailure;

        if (currentMode == BoilerMode.INITIALISATION) {
            if (event instanceof InputEvent.Transmission && initialisationAbort) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !shutdownDemanded && !plantWaiting) {
                currentMode = BoilerMode.INITIALISATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && adjustingLevel && levelAboveN2 && !valveOpen) {
                currentMode = BoilerMode.INITIALISATION;
                valveOpen = true;
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && adjustingLevel && levelAboveN2 && valveOpen) {
                currentMode = BoilerMode.INITIALISATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && adjustingLevel && levelBelowN1 && valveOpen) {
                currentMode = BoilerMode.INITIALISATION;
                valveOpen = false;
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && adjustingLevel && levelBelowN1 && !valveOpen) {
                currentMode = BoilerMode.INITIALISATION;
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && adjustingLevel && levelInNormalBand && valveOpen) {
                currentMode = BoilerMode.INITIALISATION;
                valveOpen = false;
                programReadySent = true;
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ProgramReady());
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && adjustingLevel && levelInNormalBand && !valveOpen) {
                currentMode = BoilerMode.INITIALISATION;
                programReadySent = true;
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ProgramReady());
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && awaitingUnits && !unitsReady) {
                currentMode = BoilerMode.INITIALISATION;
                actuator.apply(new OutputEvent.ProgramReady());
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && awaitingUnits && unitsReady && !unitFailure) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && awaitingUnits && unitsReady && unitFailure) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            }

        } else if (currentMode == BoilerMode.NORMAL) {
            if (event instanceof InputEvent.Transmission && operatingEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && enterRescue && levelBelowN1) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && enterRescue && levelAboveN2) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && enterRescue && levelInNormalBand) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && continueDegraded && levelBelowN1) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && continueDegraded && levelAboveN2) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && continueDegraded && levelInNormalBand) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && continueNormal && levelBelowN1) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && continueNormal && levelAboveN2) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && continueNormal && levelInNormalBand) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            }

        } else if (currentMode == BoilerMode.DEGRADED) {
            if (event instanceof InputEvent.Transmission && operatingEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && enterRescue && levelBelowN1) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && enterRescue && levelAboveN2) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && enterRescue && levelInNormalBand) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && continueDegraded && levelBelowN1) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && continueDegraded && levelAboveN2) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && continueDegraded && levelInNormalBand) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && continueNormal && levelBelowN1) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && continueNormal && levelAboveN2) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && continueNormal && levelInNormalBand) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            }

        } else if (currentMode == BoilerMode.RESCUE) {
            if (event instanceof InputEvent.Transmission && rescueEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && leaveRescueNormal && levelBelowN1) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && leaveRescueNormal && levelAboveN2) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && leaveRescueNormal && levelInNormalBand) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && leaveRescueDegraded && levelBelowN1) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && leaveRescueDegraded && levelAboveN2) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && leaveRescueDegraded && levelInNormalBand) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && stayInRescue && levelBelowN1) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && stayInRescue && levelAboveN2) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && stayInRescue && levelInNormalBand) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            }

        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
            // Terminal: the program has stopped and the physical environment
            // is responsible for the plant.
        }
    }
}
