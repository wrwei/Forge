package steam_boiler.controller;

import steam_boiler.BoilerConstants;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.domain.OperatingMode;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program's mode logic (SB-V1, SB-V2). {@link #step} runs once per cycle,
 * in the analysis stage, triggered by {@link InputEvent.CycleStart} once the cycle's messages have
 * been received (SB-V3). Every branch sends MODE(m) for the mode it enters (SB-Beh24).
 */
public final class SteamBoilerController {

    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /**
     * Creates the controller in initialization mode (SB-Beh1).
     *
     * @param sensor the analysed information of each cycle
     * @param actuator the outgoing messages of each cycle
     */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** @return the current operating mode. */
    public OperatingMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one cycle's analysis and decision.
     *
     * @param event the cycle trigger
     */
    public void step(InputEvent event) {
        // --- conditions read from the analysed information ---
        boolean stopLimitReached = sensor.successiveStops() >= BoilerConstants.STOP_REPETITIONS;
        boolean transmissionLost = sensor.transmissionFailure();
        boolean plantWaiting = sensor.waitingSignalled();
        boolean steamNotZero = sensor.steamRate() != BoilerConstants.STEAM_AT_REST;
        boolean levelUnitDown = sensor.levelDeviceFailed();
        boolean steamUnitDown = sensor.steamDeviceFailed();
        boolean controlUnitDown = sensor.pumpControlFailed();
        boolean everyUnitSound = sensor.allUnitsSound();
        boolean unitsReadyArrived = sensor.unitsReadyReceived();
        boolean valveIsOpen = sensor.valveOpen();
        boolean levelAboveN2 = sensor.waterLevel() > BoilerConstants.LEVEL_N2;
        boolean levelBelowN1 = sensor.waterLevel() < BoilerConstants.LEVEL_N1;
        boolean estimateAboveN2 = sensor.estimatedLevel() > BoilerConstants.LEVEL_N2;
        boolean estimateBelowN1 = sensor.estimatedLevel() < BoilerConstants.LEVEL_N1;
        boolean riskBelowM1 = sensor.predictedMinimumLevel() <= BoilerConstants.LEVEL_M1;
        boolean riskAboveM2 = sensor.predictedMaximumLevel() >= BoilerConstants.LEVEL_M2;

        // --- derived conditions ---
        boolean levelAtRisk = riskBelowM1 || riskAboveM2;
        boolean levelInBand = !levelAboveN2 && !levelBelowN1;
        boolean estimateInBand = !estimateAboveN2 && !estimateBelowN1;
        boolean rescueUnsupported = steamUnitDown || controlUnitDown;

        // emergency causes in order of precedence (SB-FR8)
        boolean transmissionStop = transmissionLost && !stopLimitReached;
        boolean riskStop = levelAtRisk && !transmissionLost && !stopLimitReached;
        boolean noEmergency = !stopLimitReached && !transmissionLost && !levelAtRisk;

        // initialization
        boolean initTransmissionStop = plantWaiting && transmissionStop;
        boolean initSteamStop = plantWaiting && steamNotZero && !transmissionLost
                && !stopLimitReached;
        boolean initLevelStop = plantWaiting && levelUnitDown && !steamNotZero
                && !transmissionLost && !stopLimitReached;
        boolean initRunning = plantWaiting && !stopLimitReached && !transmissionLost
                && !steamNotZero && !levelUnitDown;
        boolean initAdjusting = initRunning && !unitsReadyArrived;
        boolean initIdle = !plantWaiting && !stopLimitReached;

        // normal and degraded
        boolean levelLossBlocked = noEmergency && levelUnitDown && rescueUnsupported;
        boolean levelLossRescue = noEmergency && levelUnitDown && !rescueUnsupported;
        boolean otherUnitDown = noEmergency && !levelUnitDown && !everyUnitSound;
        boolean everyUnitRunning = noEmergency && !levelUnitDown && everyUnitSound;

        // rescue
        boolean rescueBlocked = noEmergency && rescueUnsupported;
        boolean levelBackAllSound = noEmergency && !rescueUnsupported && !levelUnitDown
                && everyUnitSound;
        boolean levelBackSomeDown = noEmergency && !rescueUnsupported && !levelUnitDown
                && !everyUnitSound;
        boolean rescueContinues = noEmergency && !rescueUnsupported && levelUnitDown;

        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.CycleStart && stopLimitReached) {
                // SB-Beh23: STOP on three successive transmissions
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && initTransmissionStop) {
                // SB-Beh9: transmission failure during initialization
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && initSteamStop) {
                // SB-Beh3: steam is escaping although the plant is waiting
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && initLevelStop) {
                // SB-Beh6: level measuring device failure during initialization
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && initRunning && unitsReadyArrived
                    && everyUnitSound) {
                // SB-Beh8: every unit sound
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.CycleStart && initRunning && unitsReadyArrived
                    && !everyUnitSound) {
                // SB-Beh8: some unit defective
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.CycleStart && initAdjusting && levelAboveN2
                    && !valveIsOpen) {
                // SB-Beh4: excessive level, open the evacuation valve
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(0));
                actuator.apply(new OutputEvent.Valve());
            } else if (event instanceof InputEvent.CycleStart && initAdjusting && levelAboveN2
                    && valveIsOpen) {
                // SB-Beh4: keep draining
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.INITIALIZATION));
            } else if (event instanceof InputEvent.CycleStart && initAdjusting && levelBelowN1
                    && !levelAboveN2 && valveIsOpen) {
                // SB-Beh4, SB-Beh5: drained too far, close the valve and fill
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(0));
            } else if (event instanceof InputEvent.CycleStart && initAdjusting && levelBelowN1
                    && !levelAboveN2 && !valveIsOpen) {
                // SB-Beh5: low level, fill with a pump
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.OpenPump(0));
            } else if (event instanceof InputEvent.CycleStart && initAdjusting && levelInBand
                    && valveIsOpen) {
                // SB-Beh7: level between N1 and N2, close the valve, announce readiness
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(0));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.CycleStart && initAdjusting && levelInBand
                    && !valveIsOpen) {
                // SB-Beh7: level between N1 and N2, announce readiness
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(0));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.CycleStart && initIdle) {
                // SB-Beh2: wait for STEAM_BOILER_WAITING
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.INITIALIZATION));
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.CycleStart && stopLimitReached) {
                // SB-Beh23
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && transmissionStop) {
                // SB-Beh14
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && riskStop) {
                // SB-Beh13
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && levelLossBlocked) {
                // SB-FR8: level device lost while rescue cannot run
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && levelLossRescue) {
                // SB-Beh11
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.RESCUE));
            } else if (event instanceof InputEvent.CycleStart && otherUnitDown) {
                // SB-Beh12
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.CycleStart && everyUnitRunning && levelBelowN1
                    && !levelAboveN2) {
                // SB-Beh10: level below N1, switch the pumps on
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.OpenPump(0));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
            } else if (event instanceof InputEvent.CycleStart && everyUnitRunning
                    && levelAboveN2) {
                // SB-Beh10: level above N2, switch the pumps off
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.ClosePump(0));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
            } else if (event instanceof InputEvent.CycleStart && everyUnitRunning
                    && levelInBand) {
                // SB-Beh10: level between N1 and N2
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.NORMAL));
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.CycleStart && stopLimitReached) {
                // SB-Beh23
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && transmissionStop) {
                // SB-Beh18: transmission failure
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && riskStop) {
                // SB-Beh18: level risks M1 or M2
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && levelLossBlocked) {
                // SB-FR8: level device lost while rescue cannot run
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && levelLossRescue) {
                // SB-Beh17
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.RESCUE));
            } else if (event instanceof InputEvent.CycleStart && everyUnitRunning) {
                // SB-Beh16: every failed unit repaired
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.CycleStart && otherUnitDown && levelBelowN1
                    && !levelAboveN2) {
                // SB-Beh15: regulate as in normal mode, level below N1
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(0));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
            } else if (event instanceof InputEvent.CycleStart && otherUnitDown && levelAboveN2) {
                // SB-Beh15: level above N2
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(0));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
            } else if (event instanceof InputEvent.CycleStart && otherUnitDown && levelInBand) {
                // SB-Beh15: level between N1 and N2
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.DEGRADED));
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.CycleStart && stopLimitReached) {
                // SB-Beh23
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && transmissionStop) {
                // SB-Beh21: transmission failure
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && riskStop) {
                // SB-Beh21: estimated level risks M1 or M2
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && rescueBlocked) {
                // SB-Beh21: steam device or pump controller failed
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.CycleStart && levelBackAllSound) {
                // SB-Beh20: level device repaired, no other unit defective
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.CycleStart && levelBackSomeDown) {
                // SB-Beh20: level device repaired, another unit defective
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.CycleStart && rescueContinues
                    && estimateBelowN1 && !estimateAboveN2) {
                // SB-Beh19: estimated level below N1
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.OpenPump(0));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
            } else if (event instanceof InputEvent.CycleStart && rescueContinues
                    && estimateAboveN2) {
                // SB-Beh19: estimated level above N2
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.ClosePump(0));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
            } else if (event instanceof InputEvent.CycleStart && rescueContinues
                    && estimateInBand) {
                // SB-Beh19: estimated level between N1 and N2
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeReport(OperatingMode.RESCUE));
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // SB-Beh22: terminal mode; the program has stopped and no transition leaves it.
        }
    }
}
