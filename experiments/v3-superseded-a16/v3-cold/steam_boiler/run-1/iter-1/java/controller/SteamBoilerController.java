package steam_boiler.controller;

import steam_boiler.SteamBoilerConstants;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.BoilerMode;
import steam_boiler.mode.OperatingMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * The mode logic of the steam-boiler control program. {@link #step} is called once per
 * cycle with {@link InputEvent.Transmission}, after the sensor has received and analysed
 * the cycle's messages; every transition sends MODE(m) and the orders of the cycle.
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

    /** The current state. */
    public BoilerMode currentMode() {
        return currentMode;
    }

    /** Analyses one cycle's transmission and decides the mode and the orders of the cycle. */
    public void step(InputEvent event) {
        // --- Quantities of the cycle, as analysed by the sensor ---
        boolean operatorStop = sensor.successiveStops() >= SteamBoilerConstants.STOP_REPETITIONS;
        boolean transmissionFault = sensor.transmissionFailed();
        boolean plantWaiting = sensor.steamBoilerWaitingReceived();
        boolean unitsReady = sensor.physicalUnitsReadyReceived();
        boolean levelDeviceFaulty = sensor.levelDeviceFailed();
        boolean steamDeviceFaulty = sensor.steamDeviceFailed();
        boolean pumpFaulty = sensor.anyPumpFailed();
        boolean pumpControllerFaulty = sensor.anyPumpControllerFailed();
        boolean steamNotZero = sensor.steamRate() != 0.0;
        boolean levelAboveN2 = sensor.waterLevel() > SteamBoilerConstants.NORMAL_N2;
        boolean levelBelowN1 = sensor.waterLevel() < SteamBoilerConstants.NORMAL_N1;
        boolean estimateAboveN2 =
                sensor.levelLowerBound() + sensor.levelUpperBound() > 2.0 * SteamBoilerConstants.NORMAL_N2;
        boolean estimateBelowN1 =
                sensor.levelLowerBound() + sensor.levelUpperBound() < 2.0 * SteamBoilerConstants.NORMAL_N1;
        boolean levelRiskBelowM1 = sensor.lowestLevelNextCycle() < SteamBoilerConstants.LIMIT_M1;
        boolean levelRiskAboveM2 = sensor.highestLevelNextCycle() > SteamBoilerConstants.LIMIT_M2;

        boolean levelAtRisk = levelRiskBelowM1 || levelRiskAboveM2;
        boolean otherUnitFaulty = steamDeviceFaulty || pumpFaulty || pumpControllerFaulty;
        boolean startupFault = steamNotZero || levelDeviceFaulty;

        // --- Initialization before PROGRAM_READY (PHYSICAL_UNITS_READY would be aberrant) ---
        boolean initTransmissionFault = transmissionFault || unitsReady;
        boolean initEmergency = !operatorStop && initTransmissionFault;
        boolean initProceeds = !operatorStop && !initTransmissionFault;
        boolean initUnitFault = initProceeds && startupFault;
        boolean initLevelHigh = initProceeds && !startupFault && levelAboveN2;
        boolean initLevelLow = initProceeds && !startupFault && !levelAboveN2 && levelBelowN1;
        boolean initLevelNormal = initProceeds && !startupFault && !levelAboveN2 && !levelBelowN1;

        // --- Initialization after PROGRAM_READY ---
        boolean readyEmergency = !operatorStop && transmissionFault;
        boolean readyProceeds = !operatorStop && !transmissionFault;
        boolean readyUnitFault = readyProceeds && startupFault;
        boolean unitsReadyAllSound = readyProceeds && !startupFault && unitsReady && !otherUnitFaulty;
        boolean unitsReadySomeFaulty = readyProceeds && !startupFault && unitsReady && otherUnitFaulty;
        boolean unitsNotYetReady = readyProceeds && !startupFault && !unitsReady;

        // --- Normal and degraded ---
        boolean operationFault = transmissionFault || plantWaiting || unitsReady || levelAtRisk;
        boolean operationEmergency = !operatorStop && operationFault;
        boolean operationProceeds = !operatorStop && !operationFault;
        boolean levelDeviceLost = operationProceeds && levelDeviceFaulty;
        boolean unitsFaulty = operationProceeds && !levelDeviceFaulty && otherUnitFaulty;
        boolean unitsSound = operationProceeds && !levelDeviceFaulty && !otherUnitFaulty;

        // --- Rescue ---
        boolean rescueFault = operationFault || steamDeviceFaulty || pumpControllerFaulty;
        boolean rescueEmergency = !operatorStop && rescueFault;
        boolean rescueProceeds = !operatorStop && !rescueFault;
        boolean levelRepairedAllSound = rescueProceeds && !levelDeviceFaulty && !otherUnitFaulty;
        boolean levelRepairedSomeFaulty = rescueProceeds && !levelDeviceFaulty && otherUnitFaulty;
        boolean levelStillFaulty = rescueProceeds && levelDeviceFaulty;

        if (currentMode == BoilerMode.INITIALIZATION) {
            if (event instanceof InputEvent.Transmission && operatorStop) {
                // SB-Beh23
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && initEmergency) {
                // SB-Beh9
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && initProceeds && !plantWaiting) {
                // SB-Beh2: nothing happens before STEAM_BOILER_WAITING
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
            } else if (event instanceof InputEvent.Transmission && plantWaiting && initUnitFault) {
                // SB-Beh3, SB-Beh6
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && plantWaiting && initLevelHigh) {
                // SB-Beh4: open the evacuation valve
                currentMode = BoilerMode.INITIALIZATION_DRAINING;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
            } else if (event instanceof InputEvent.Transmission && plantWaiting && initLevelLow) {
                // SB-Beh5: activate the pumps
                currentMode = BoilerMode.INITIALIZATION_FILLING;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && plantWaiting && initLevelNormal) {
                // SB-Beh7
                currentMode = BoilerMode.INITIALIZATION_READY;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ProgramReady());
            }

        } else if (currentMode == BoilerMode.INITIALIZATION_FILLING) {
            if (event instanceof InputEvent.Transmission && operatorStop) {
                // SB-Beh23
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && initEmergency) {
                // SB-Beh9
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && initUnitFault) {
                // SB-Beh3, SB-Beh6
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && initLevelHigh) {
                // SB-Beh4: stop filling, open the evacuation valve
                currentMode = BoilerMode.INITIALIZATION_DRAINING;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.Valve());
            } else if (event instanceof InputEvent.Transmission && initLevelLow) {
                // SB-Beh5: keep filling
                currentMode = BoilerMode.INITIALIZATION_FILLING;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
            } else if (event instanceof InputEvent.Transmission && initLevelNormal) {
                // SB-Beh7: stop filling, announce PROGRAM_READY
                currentMode = BoilerMode.INITIALIZATION_READY;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.ProgramReady());
            }

        } else if (currentMode == BoilerMode.INITIALIZATION_DRAINING) {
            if (event instanceof InputEvent.Transmission && operatorStop) {
                // SB-Beh23
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && initEmergency) {
                // SB-Beh9
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && initUnitFault) {
                // SB-Beh3, SB-Beh6
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && initLevelHigh) {
                // SB-Beh4: keep draining
                currentMode = BoilerMode.INITIALIZATION_DRAINING;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
            } else if (event instanceof InputEvent.Transmission && initLevelLow) {
                // SB-Beh5: close the valve, activate the pumps
                currentMode = BoilerMode.INITIALIZATION_FILLING;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && initLevelNormal) {
                // SB-Beh7: close the valve, announce PROGRAM_READY
                currentMode = BoilerMode.INITIALIZATION_READY;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ProgramReady());
            }

        } else if (currentMode == BoilerMode.INITIALIZATION_READY) {
            if (event instanceof InputEvent.Transmission && operatorStop) {
                // SB-Beh23
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && readyEmergency) {
                // SB-Beh9
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && readyUnitFault) {
                // SB-Beh3, SB-Beh6
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && unitsReadyAllSound) {
                // SB-Beh8
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && unitsReadySomeFaulty) {
                // SB-Beh8
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && unitsNotYetReady) {
                // SB-Beh7: repeat PROGRAM_READY
                currentMode = BoilerMode.INITIALIZATION_READY;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION_MODE));
                actuator.apply(new OutputEvent.ProgramReady());
            }

        } else if (currentMode == BoilerMode.NORMAL) {
            if (event instanceof InputEvent.Transmission && operatorStop) {
                // SB-Beh23
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && operationEmergency) {
                // SB-Beh13, SB-Beh14
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && levelDeviceLost) {
                // SB-Beh11
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && unitsFaulty) {
                // SB-Beh12
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && unitsSound && levelBelowN1) {
                // SB-Beh10: level below N1, switch the pumps on
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && unitsSound && !levelBelowN1 && levelAboveN2) {
                // SB-Beh10: level above N2, switch the pumps off
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && unitsSound && !levelBelowN1 && !levelAboveN2) {
                // SB-Beh10: level between N1 and N2
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            }

        } else if (currentMode == BoilerMode.DEGRADED) {
            if (event instanceof InputEvent.Transmission && operatorStop) {
                // SB-Beh23
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && operationEmergency) {
                // SB-Beh18
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && levelDeviceLost) {
                // SB-Beh17
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            } else if (event instanceof InputEvent.Transmission && unitsSound) {
                // SB-Beh16
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && unitsFaulty && levelBelowN1) {
                // SB-Beh15: level below N1, switch the pumps on
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && unitsFaulty && !levelBelowN1 && levelAboveN2) {
                // SB-Beh15: level above N2, switch the pumps off
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && unitsFaulty && !levelBelowN1 && !levelAboveN2) {
                // SB-Beh15: level between N1 and N2
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            }

        } else if (currentMode == BoilerMode.RESCUE) {
            if (event instanceof InputEvent.Transmission && operatorStop) {
                // SB-Beh23
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && rescueEmergency) {
                // SB-Beh21
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            } else if (event instanceof InputEvent.Transmission && levelRepairedAllSound) {
                // SB-Beh20
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL_MODE));
            } else if (event instanceof InputEvent.Transmission && levelRepairedSomeFaulty) {
                // SB-Beh20
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED_MODE));
            } else if (event instanceof InputEvent.Transmission && levelStillFaulty && estimateBelowN1) {
                // SB-Beh19: estimated level below N1, switch the pumps on
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && levelStillFaulty && !estimateBelowN1 && estimateAboveN2) {
                // SB-Beh19: estimated level above N2, switch the pumps off
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && levelStillFaulty && !estimateBelowN1 && !estimateAboveN2) {
                // SB-Beh19: estimated level between N1 and N2
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE_MODE));
            }

        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
            if (event instanceof InputEvent.Transmission) {
                // SB-Beh22, SB-Beh24: terminal; the control task has ended, only the mode is reported
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP_MODE));
            }
        }
    }
}
