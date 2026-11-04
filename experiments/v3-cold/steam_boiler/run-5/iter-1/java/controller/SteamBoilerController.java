package steam_boiler.controller;

import steam_boiler.BoilerConstants;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.domain.ModeIndicator;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * Mode logic of the steam-boiler control program. It is always in exactly one of five modes
 * and starts in initialization. {@link #step(InputEvent)} is the analysis stage of one cycle:
 * it is called with {@link InputEvent.TransmissionReceived} once the cycle's incoming
 * transmission has been received and analysed by the sensor, decides the mode, and issues
 * MODE(m) and the mode's commands (PROGRAM_READY, VALVE, OPEN_PUMP, CLOSE_PUMP).
 *
 * <p>Within each mode the transitions are listed in priority order and every guard conjoins
 * the negation of the guards above it, so that the most severe consequence prevails
 * (SB-FR8): emergency stop over rescue, rescue over degraded. A cycle that changes mode leaves
 * the pumps as they are; pump regulation changes at most one pump per cycle. Emergency stop
 * is terminal.
 */
public final class SteamBoilerController {

    private BoilerMode currentMode = BoilerMode.INITIALIZATION;
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

    /** Analysis stage of one control cycle. */
    public void step(InputEvent event) {
        // --- Unit status, transmission and operator stop (SB-V9, SB-V10, SB-FR1..SB-FR5) ---
        boolean operatorStopDemanded = sensor.successiveStops() >= BoilerConstants.STOP_LIMIT;
        boolean transmissionFailed = sensor.transmissionFailure();
        boolean levelFailed = sensor.levelDeviceFailed();
        boolean steamFailed = sensor.steamDeviceFailed();
        boolean pumpFailed = sensor.pumpFailurePresent();
        boolean pumpControlFailed = sensor.pumpControlFailurePresent();
        boolean otherUnitFailed = steamFailed || pumpFailed || pumpControlFailed;
        boolean allUnitsSound = !levelFailed && !otherUnitFailed;
        boolean estimateUnreliable = steamFailed || pumpControlFailed;

        // --- Worst-case level forecast for the coming cycle (SB-V4, SB-Beh13, SB-Beh25) ---
        boolean levelRiskLow = sensor.levelForecastMin() <= BoilerConstants.MIN_LIMIT_M1;
        boolean levelRiskHigh = sensor.levelForecastMax() >= BoilerConstants.MAX_LIMIT_M2;
        boolean emergencyCondition = operatorStopDemanded || transmissionFailed || levelRiskLow || levelRiskHigh;
        boolean regulatedEmergency = emergencyCondition || (levelFailed && estimateUnreliable);
        boolean rescueEmergency = emergencyCondition || estimateUnreliable;

        // --- Pump regulation towards [N1, N2] (SB-Beh10, SB-Beh15, SB-Beh19) ---
        boolean forecastBelowNormal = sensor.levelForecastMin() < BoilerConstants.MIN_NORMAL_N1;
        boolean forecastAboveNormal = sensor.levelForecastMax() > BoilerConstants.MAX_NORMAL_N2;
        boolean pumpCanOpen = sensor.openablePumpCount() > 0;
        boolean pumpCanClose = sensor.closablePumpCount() > 0;
        boolean closeOnePump = forecastAboveNormal && !forecastBelowNormal && pumpCanClose;
        boolean openOnePump = forecastBelowNormal && !forecastAboveNormal && pumpCanOpen;
        boolean holdPumps = !closeOnePump && !openOnePump;

        // --- Initialization (SB-Beh2..SB-Beh9) ---
        boolean plantWaiting = sensor.boilerWaitingReceived();
        boolean unitsReady = sensor.unitsReadyReceived();
        boolean steamNotZero = sensor.steamRate() != 0.0;
        boolean initEmergency = operatorStopDemanded || transmissionFailed || (plantWaiting && (steamNotZero || levelFailed));
        boolean initAdjusting = !initEmergency && plantWaiting && !unitsReady;
        boolean levelAboveNormal = sensor.waterLevel() > BoilerConstants.MAX_NORMAL_N2;
        boolean levelBelowNormal = sensor.waterLevel() < BoilerConstants.MIN_NORMAL_N1;
        boolean levelWithinNormal = !levelAboveNormal && !levelBelowNormal;
        boolean valveIsOpen = sensor.valveOpen();
        boolean drainStart = levelAboveNormal && !valveIsOpen;
        boolean drainStop = !levelAboveNormal && valveIsOpen;
        boolean fillStart = levelBelowNormal && !levelAboveNormal && !valveIsOpen && !pumpCanClose && pumpCanOpen;
        boolean fillStop = levelWithinNormal && !valveIsOpen && pumpCanClose;
        boolean readyToStart = levelWithinNormal && !valveIsOpen && !pumpCanClose;
        boolean valveOnly = drainStart || (drainStop && !levelWithinNormal);
        boolean levelSettling = !drainStart && !drainStop && !fillStart && !fillStop && !readyToStart;

        if (currentMode == BoilerMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionReceived && initEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !initEmergency && plantWaiting && unitsReady
                    && allUnitsSound) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.NORMAL_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !initEmergency && plantWaiting && unitsReady
                    && !allUnitsSound) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.DEGRADED_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !initEmergency && !plantWaiting) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.INITIALIZATION_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && initAdjusting && valveOnly) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
            } else if (event instanceof InputEvent.TransmissionReceived && initAdjusting && drainStop
                    && levelWithinNormal) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived && initAdjusting && fillStart) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.OpenPump(sensor.pumpToOpen()));
            } else if (event instanceof InputEvent.TransmissionReceived && initAdjusting && fillStop) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ClosePump(sensor.pumpToClose()));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived && initAdjusting && readyToStart) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived && initAdjusting && levelSettling) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.INITIALIZATION_MODE));
            }

        } else if (currentMode == BoilerMode.NORMAL) {
            if (event instanceof InputEvent.TransmissionReceived && regulatedEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !regulatedEmergency && levelFailed) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.RESCUE_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !regulatedEmergency && !levelFailed
                    && otherUnitFailed) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.DEGRADED_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !regulatedEmergency && allUnitsSound
                    && closeOnePump) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.NORMAL_MODE));
                actuator.apply(new OutputEvent.ClosePump(sensor.pumpToClose()));
            } else if (event instanceof InputEvent.TransmissionReceived && !regulatedEmergency && allUnitsSound
                    && openOnePump) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.NORMAL_MODE));
                actuator.apply(new OutputEvent.OpenPump(sensor.pumpToOpen()));
            } else if (event instanceof InputEvent.TransmissionReceived && !regulatedEmergency && allUnitsSound
                    && holdPumps) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.NORMAL_MODE));
            }

        } else if (currentMode == BoilerMode.DEGRADED) {
            if (event instanceof InputEvent.TransmissionReceived && regulatedEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !regulatedEmergency && levelFailed) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.RESCUE_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !regulatedEmergency && allUnitsSound) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.NORMAL_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !regulatedEmergency && !levelFailed
                    && otherUnitFailed && closeOnePump) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.DEGRADED_MODE));
                actuator.apply(new OutputEvent.ClosePump(sensor.pumpToClose()));
            } else if (event instanceof InputEvent.TransmissionReceived && !regulatedEmergency && !levelFailed
                    && otherUnitFailed && openOnePump) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.DEGRADED_MODE));
                actuator.apply(new OutputEvent.OpenPump(sensor.pumpToOpen()));
            } else if (event instanceof InputEvent.TransmissionReceived && !regulatedEmergency && !levelFailed
                    && otherUnitFailed && holdPumps) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.DEGRADED_MODE));
            }

        } else if (currentMode == BoilerMode.RESCUE) {
            if (event instanceof InputEvent.TransmissionReceived && rescueEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !rescueEmergency && allUnitsSound) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.NORMAL_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !rescueEmergency && !levelFailed
                    && otherUnitFailed) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.DEGRADED_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !rescueEmergency && levelFailed
                    && closeOnePump) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.RESCUE_MODE));
                actuator.apply(new OutputEvent.ClosePump(sensor.pumpToClose()));
            } else if (event instanceof InputEvent.TransmissionReceived && !rescueEmergency && levelFailed
                    && openOnePump) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.RESCUE_MODE));
                actuator.apply(new OutputEvent.OpenPump(sensor.pumpToOpen()));
            } else if (event instanceof InputEvent.TransmissionReceived && !rescueEmergency && levelFailed
                    && holdPumps) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeIndicator.RESCUE_MODE));
            }

        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
            // Terminal mode (SB-Beh22): the control task has ended and the program has stopped;
            // no transition leaves it and nothing more is transmitted.
        }
    }
}
