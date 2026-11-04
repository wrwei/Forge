package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.BoilerMode;
import steam_boiler.mode.OperatingMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program's mode logic (SB-V1, SB-V2). {@link #step(InputEvent)} is
 * called once per five-second cycle, with {@link InputEvent.TransmissionReceived}, after the
 * sensor has received and analysed the cycle's incoming transmission (SB-V3). It chooses the
 * next operating mode, regulates the water level with the pumps, drives the start-up
 * handshake and sends MODE(m) on every cycle (SB-Beh24).
 *
 * <p>Within each mode the branches are mutually exclusive and together cover every case. When
 * several failure conditions hold at once the most severe consequence wins: emergency stop,
 * then rescue, then degraded (SB-FR8).
 */
public final class SteamBoilerController {

    private BoilerMode currentMode = BoilerMode.INITIALIZATION;
    /** PROGRAM_READY has been announced during initialization (SB-Beh7). */
    private boolean readyAnnounced = false;
    /** The evacuation valve has been ordered open by VALVE and not yet closed (SB-DM7, SB-Beh4). */
    private boolean valveOpen = false;

    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /** Creates the controller in initialization mode (SB-Beh1). */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode (SB-V2). */
    public BoilerMode currentMode() {
        return currentMode;
    }

    /** Performs the analysis and transmission stages of one cycle (SB-V3). */
    public void step(InputEvent event) {
        // --- Judgements of this cycle, read from the sensor ---
        boolean operatorStop = sensor.stopDemanded();                       // SB-Beh23
        boolean transmissionLost = sensor.transmissionFailed();             // SB-FR5
        boolean levelRisk = sensor.levelLimitRisk();                        // SB-V4, SB-Beh13
        boolean levelFailed = sensor.levelDeviceOutOfOrder();               // SB-FR3
        boolean estimationUnitFailed = sensor.steamOrPumpControllerOutOfOrder(); // SB-Beh21
        boolean otherUnitFailed = sensor.nonLevelUnitOutOfOrder();          // SB-Beh12
        boolean belowNormal = sensor.levelBelowNormal();                    // level < N1
        boolean aboveNormal = sensor.levelAboveNormal();                    // level > N2
        boolean plantWaiting = sensor.steamBoilerWaitingReceived();         // SB-Beh2
        boolean unitsReady = sensor.physicalUnitsReadyReceived();           // SB-Beh8
        boolean steamNonZero = sensor.steamRateNonZero();                   // SB-Beh3

        // --- Conjunctions of the judgements above ---
        boolean messagesClear = !operatorStop && !transmissionLost;
        boolean regulationClear = !operatorStop && !transmissionLost && !levelRisk;
        boolean startupHealthy = !operatorStop && !transmissionLost && plantWaiting && !steamNonZero
                && !levelFailed;
        boolean withinNormal = !belowNormal && !aboveNormal;

        if (currentMode == BoilerMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionReceived && operatorStop) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStop && transmissionLost) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && messagesClear && plantWaiting
                    && steamNonZero) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && messagesClear && plantWaiting
                    && !steamNonZero && levelFailed) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && messagesClear && !plantWaiting) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && startupHealthy && unitsReady
                    && !otherUnitFailed) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && startupHealthy && unitsReady
                    && otherUnitFailed) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && startupHealthy && !unitsReady
                    && readyAnnounced) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived && startupHealthy && !unitsReady
                    && !readyAnnounced && aboveNormal && !valveOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                valveOpen = true;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
            } else if (event instanceof InputEvent.TransmissionReceived && startupHealthy && !unitsReady
                    && !readyAnnounced && aboveNormal && valveOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && startupHealthy && !unitsReady
                    && !readyAnnounced && !aboveNormal && valveOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                valveOpen = false;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
            } else if (event instanceof InputEvent.TransmissionReceived && startupHealthy && !unitsReady
                    && !readyAnnounced && !aboveNormal && !valveOpen && belowNormal) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
            } else if (event instanceof InputEvent.TransmissionReceived && startupHealthy && !unitsReady
                    && !readyAnnounced && !aboveNormal && !valveOpen && !belowNormal) {
                currentMode = BoilerMode.INITIALIZATION;
                readyAnnounced = true;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ProgramReady());
            }

        } else if (currentMode == BoilerMode.NORMAL) {
            if (event instanceof InputEvent.TransmissionReceived && operatorStop) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStop && transmissionLost) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && messagesClear && levelRisk) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && levelFailed
                    && estimationUnitFailed) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && levelFailed
                    && !estimationUnitFailed) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && !levelFailed
                    && otherUnitFailed) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && !levelFailed
                    && !otherUnitFailed && belowNormal) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && !levelFailed
                    && !otherUnitFailed && !belowNormal && aboveNormal) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && !levelFailed
                    && !otherUnitFailed && withinNormal) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            }

        } else if (currentMode == BoilerMode.DEGRADED) {
            if (event instanceof InputEvent.TransmissionReceived && operatorStop) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStop && transmissionLost) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && messagesClear && levelRisk) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && levelFailed
                    && estimationUnitFailed) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && levelFailed
                    && !estimationUnitFailed) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && !levelFailed
                    && !otherUnitFailed) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && !levelFailed
                    && otherUnitFailed && belowNormal) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && !levelFailed
                    && otherUnitFailed && !belowNormal && aboveNormal) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && !levelFailed
                    && otherUnitFailed && withinNormal) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            }

        } else if (currentMode == BoilerMode.RESCUE) {
            if (event instanceof InputEvent.TransmissionReceived && operatorStop) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && !operatorStop && transmissionLost) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && messagesClear && levelRisk) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && estimationUnitFailed) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && !estimationUnitFailed
                    && !levelFailed && !otherUnitFailed) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && !estimationUnitFailed
                    && !levelFailed && otherUnitFailed) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && !estimationUnitFailed
                    && levelFailed && belowNormal) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && !estimationUnitFailed
                    && levelFailed && !belowNormal && aboveNormal) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived && regulationClear && !estimationUnitFailed
                    && levelFailed && withinNormal) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            }

        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
            // Terminal (SB-Beh22): no control action and no way out; the mode is still
            // reported on every cycle (SB-Beh24).
            if (event instanceof InputEvent.TransmissionReceived) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            }
        }
    }
}
