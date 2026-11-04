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
    private boolean plantWaiting = false;
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

    /** Performs the analysis stage of one cycle. */
    public void step(InputEvent event) {
        // Causes of emergency stop common to every mode.
        boolean stopLimitReached = sensor.stopCount() >= BoilerConstants.STOP_LIMIT;
        boolean transmissionFailure = sensor.transmissionFailed();
        boolean waitingMessage = sensor.waitingReceived();
        boolean unitsReadyMessage = sensor.unitsReadyReceived();
        boolean initMessageAberrant = waitingMessage || unitsReadyMessage;

        // Units believed out of order.
        boolean levelFailure = sensor.levelFailure();
        boolean steamFailure = sensor.steamFailure();
        boolean pumpFailure = sensor.pumpFailure();
        boolean pumpControlFailure = sensor.pumpControlFailure();
        boolean unitFailure = steamFailure || pumpFailure || pumpControlFailure;
        boolean rescueImpossible = steamFailure || pumpControlFailure;
        boolean rescueBlocked = levelFailure && rescueImpossible;

        // The measured level and the estimated level against N1 and N2.
        boolean levelBelowN1 = sensor.levelReading() < BoilerConstants.NORMAL_N1;
        boolean levelAboveN2 = sensor.levelReading() > BoilerConstants.NORMAL_N2;
        boolean levelInNormalBand = !levelBelowN1 && !levelAboveN2;
        boolean estimateBelowN1 = sensor.estimatedLevel() < BoilerConstants.NORMAL_N1;
        boolean estimateAboveN2 = sensor.estimatedLevel() > BoilerConstants.NORMAL_N2;
        boolean estimateInNormalBand = !estimateBelowN1 && !estimateAboveN2;

        // Worst-case risk of reaching M1 or M2 before the next cycle.
        boolean minimumBelowM1 = sensor.predictedMinimum() <= BoilerConstants.LIMIT_M1;
        boolean maximumAboveM2 = sensor.predictedMaximum() >= BoilerConstants.LIMIT_M2;
        boolean drainedMaximumAboveM2 = sensor.predictedMaximumWithoutPumps() >= BoilerConstants.LIMIT_M2;

        // Initialisation.
        boolean plantActive = plantWaiting || waitingMessage;
        boolean unexpectedUnitsReady = unitsReadyMessage && !programReadySent;
        boolean steamNotZero = sensor.steamReading() != 0.0;
        boolean initialisationAbort = stopLimitReached || transmissionFailure || unexpectedUnitsReady
                || (plantActive && (steamNotZero || levelFailure));
        boolean adjustingLevel = !initialisationAbort && plantActive && !programReadySent;
        boolean awaitingUnits = !initialisationAbort && plantActive && programReadySent;

        // Normal, degraded and rescue modes. A branch that closes the pumps
        // stops their supply at once; every other branch leaves the running
        // pumps on for the coming cycle.
        boolean operationAbort = stopLimitReached || transmissionFailure || initMessageAberrant || minimumBelowM1;
        boolean normalRegulates = !levelFailure && !unitFailure;
        boolean normalClosesPumps = normalRegulates && levelAboveN2;
        boolean normalEmergency = operationAbort || rescueBlocked
                || (normalClosesPumps && drainedMaximumAboveM2) || (!normalClosesPumps && maximumAboveM2);
        boolean degradedRegulates = !levelFailure && unitFailure;
        boolean degradedClosesPumps = degradedRegulates && levelAboveN2;
        boolean degradedEmergency = operationAbort || rescueBlocked
                || (degradedClosesPumps && drainedMaximumAboveM2) || (!degradedClosesPumps && maximumAboveM2);
        boolean rescueClosesPumps = levelFailure && estimateAboveN2;
        boolean rescueEmergency = operationAbort || rescueImpossible
                || (rescueClosesPumps && drainedMaximumAboveM2) || (!rescueClosesPumps && maximumAboveM2);

        if (currentMode == BoilerMode.INITIALISATION) {
            if (event instanceof InputEvent.Transmission && initialisationAbort) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !initialisationAbort && !plantActive) {
                currentMode = BoilerMode.INITIALISATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && adjustingLevel && levelAboveN2 && !valveOpen) {
                currentMode = BoilerMode.INITIALISATION;
                plantWaiting = true;
                valveOpen = true;
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && adjustingLevel && levelAboveN2 && valveOpen) {
                currentMode = BoilerMode.INITIALISATION;
                plantWaiting = true;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && adjustingLevel && levelBelowN1 && valveOpen) {
                currentMode = BoilerMode.INITIALISATION;
                plantWaiting = true;
                valveOpen = false;
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && adjustingLevel && levelBelowN1 && !valveOpen) {
                currentMode = BoilerMode.INITIALISATION;
                plantWaiting = true;
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && adjustingLevel && levelInNormalBand && valveOpen) {
                currentMode = BoilerMode.INITIALISATION;
                plantWaiting = true;
                valveOpen = false;
                programReadySent = true;
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ProgramReady());
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && adjustingLevel && levelInNormalBand && !valveOpen) {
                currentMode = BoilerMode.INITIALISATION;
                plantWaiting = true;
                programReadySent = true;
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ProgramReady());
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && awaitingUnits && !unitsReadyMessage) {
                currentMode = BoilerMode.INITIALISATION;
                actuator.apply(new OutputEvent.ProgramReady());
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALISATION_MODE));
            } else if (event instanceof InputEvent.Transmission && awaitingUnits && unitsReadyMessage && !unitFailure) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && awaitingUnits && unitsReadyMessage && unitFailure) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            }

        } else if (currentMode == BoilerMode.NORMAL) {
            if (event instanceof InputEvent.Transmission && normalEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !normalEmergency && levelFailure) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && !normalEmergency && !levelFailure && unitFailure) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && !normalEmergency && normalRegulates && levelBelowN1) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && !normalEmergency && normalRegulates && levelAboveN2) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && !normalEmergency && normalRegulates
                    && levelInNormalBand) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            }

        } else if (currentMode == BoilerMode.DEGRADED) {
            if (event instanceof InputEvent.Transmission && degradedEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !degradedEmergency && levelFailure) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && !degradedEmergency && !levelFailure && !unitFailure) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && !degradedEmergency && degradedRegulates
                    && levelBelowN1) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && !degradedEmergency && degradedRegulates
                    && levelAboveN2) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && !degradedEmergency && degradedRegulates
                    && levelInNormalBand) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            }

        } else if (currentMode == BoilerMode.RESCUE) {
            if (event instanceof InputEvent.Transmission && rescueEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && !rescueEmergency && !levelFailure && !unitFailure) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && !rescueEmergency && !levelFailure && unitFailure) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && !rescueEmergency && levelFailure
                    && estimateBelowN1) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && !rescueEmergency && levelFailure
                    && estimateAboveN2) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && !rescueEmergency && levelFailure
                    && estimateInNormalBand) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            }

        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
            // Terminal: the program has stopped and the physical environment
            // is responsible for the plant.
        }
    }
}
