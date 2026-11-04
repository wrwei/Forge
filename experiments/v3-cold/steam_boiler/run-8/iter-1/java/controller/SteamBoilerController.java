package steam_boiler.controller;

import steam_boiler.BoilerConstants;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * Mode logic of the steam-boiler control program (SB-V1, SB-V2, SB-V4). Once
 * per five-second cycle, after the incoming transmission has been received and
 * analysed, {@link #step} is called with {@link InputEvent.Transmission}; it
 * chooses the next operating mode, regulates the level with the pumps, and
 * emits MODE(m) together with the mode's other messages (SB-V3, SB-Beh24).
 *
 * <p>Regulation (SB-Beh10, SB-Beh15, SB-Beh19): below N1 all four pumps are
 * ordered on, above N2 all four are ordered off, between N1 and N2 the pumps
 * keep their state. The limit risk (SB-Beh13) is judged on the worst-case
 * level one cycle ahead, given that decision: pumps ordered off stop at once,
 * pumps ordered on pour nothing during the next cycle (SB-Beh25).
 */
public final class SteamBoilerController {

    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /**
     * Creates the controller in initialization mode (SB-Beh1).
     *
     * @param sensor   the analysed view of the physical units
     * @param actuator the outgoing message stage
     */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /**
     * The current operating mode (SB-V2).
     *
     * @return the mode
     */
    public OperatingMode currentMode() {
        return currentMode;
    }

    /**
     * Analysis of one cycle: called once per cycle with the end-of-transmission
     * event, after the sensor has analysed the cycle's messages.
     *
     * @param event the cycle's event; transitions fire on {@link InputEvent.Transmission}
     */
    public void step(InputEvent event) {
        // ---- Guard predicates ----
        // SB-Beh23, SB-V10: STOP on three successive transmissions.
        boolean stopCountAtLimit = sensor.successiveStopCount() >= BoilerConstants.STOP_REPETITIONS;
        // SB-FR5: aberrant or missing message in this transmission.
        boolean transmissionFailed = sensor.transmissionFailure();
        // SB-Beh2: STEAM_BOILER_WAITING has arrived.
        boolean plantWaiting = sensor.steamBoilerWaitingReceived();
        // SB-Beh8: PHYSICAL_UNITS_READY is in this transmission.
        boolean unitsReady = sensor.physicalUnitsReadyReceived();
        // SB-Beh3: the reported steam rate is not zero.
        boolean steamRateNonZero = sensor.steamRate() != 0.0;
        // SB-V9, SB-FR1 to SB-FR4: units currently out of order.
        boolean levelDeviceFailed = sensor.levelDeviceOutOfOrder();
        boolean steamDeviceFailed = sensor.steamDeviceOutOfOrder();
        boolean pumpFailed = sensor.pumpOutOfOrder();
        boolean pumpControlFailed = sensor.pumpControlOutOfOrder();
        // SB-Beh4: the evacuation valve is open.
        boolean valveIsOpen = sensor.valveOpen();
        // SB-Beh7: some pump reports itself on.
        boolean pumpsRunning = sensor.openPumpCount() > 0;
        // SB-Beh4, SB-Beh5, SB-Beh10: the level against the normal band [N1, N2].
        boolean levelBelowN1 = sensor.waterLevel() < BoilerConstants.MIN_NORMAL_LEVEL;
        boolean levelAboveN2 = sensor.waterLevel() > BoilerConstants.MAX_NORMAL_LEVEL;
        boolean levelBelowBand = levelBelowN1 && !levelAboveN2;
        boolean levelWithinBand = !levelBelowN1 && !levelAboveN2;
        // SB-V4, SB-Beh13, SB-Beh25: worst-case level one cycle ahead against M1 and M2.
        boolean lowLimitRisk = sensor.levelLowerBound() - sensor.maximumSteamOutflow()
                <= BoilerConstants.MIN_LIMIT_LEVEL;
        boolean highLimitRiskIfClosed = sensor.levelUpperBound() - sensor.minimumSteamOutflow()
                >= BoilerConstants.MAX_LIMIT_LEVEL;
        boolean highLimitRiskIfHeld = sensor.levelUpperBound() + sensor.maximumPumpInflow()
                - sensor.minimumSteamOutflow() >= BoilerConstants.MAX_LIMIT_LEVEL;
        boolean levelLimitRisk = lowLimitRisk
                || (levelAboveN2 && highLimitRiskIfClosed)
                || (!levelAboveN2 && highLimitRiskIfHeld);
        boolean allUnitsSound = !levelDeviceFailed && !steamDeviceFailed && !pumpFailed && !pumpControlFailed;
        // SB-Beh19, SB-Beh21: the estimate needs the steam device and the pump controllers.
        boolean estimateUnreliable = steamDeviceFailed || pumpControlFailed;
        // SB-Beh3, SB-Beh6, SB-Beh9: start-up failures once the plant is waiting.
        boolean initialisationAborted = plantWaiting
                && (transmissionFailed || steamRateNonZero || levelDeviceFailed);
        boolean initialisationProceeding = !stopCountAtLimit && !initialisationAborted && plantWaiting;
        // SB-FR8, SB-Beh13, SB-Beh14, SB-Beh18, SB-Beh23: emergency in normal or degraded mode.
        boolean emergencyInRegulation = stopCountAtLimit || transmissionFailed || levelLimitRisk
                || (levelDeviceFailed && estimateUnreliable);
        boolean levelFaultWhileRegulating = !emergencyInRegulation && levelDeviceFailed;
        boolean otherFaultWhileRegulating = !emergencyInRegulation && !levelDeviceFailed && !allUnitsSound;
        boolean noFaultWhileRegulating = !emergencyInRegulation && allUnitsSound;
        // SB-FR8, SB-Beh21, SB-Beh23: emergency in rescue mode.
        boolean emergencyInRescue = stopCountAtLimit || transmissionFailed || levelLimitRisk
                || estimateUnreliable;
        boolean levelRepairedAllSound = !emergencyInRescue && allUnitsSound;
        boolean levelRepairedPumpFault = !emergencyInRescue && !levelDeviceFailed && !allUnitsSound;
        boolean levelStillFaulty = !emergencyInRescue && levelDeviceFailed;

        // ---- Mode-nested transitions ----
        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.Transmission && stopCountAtLimit) {
                // SB-Beh23
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && !stopCountAtLimit && initialisationAborted) {
                // SB-Beh3, SB-Beh6, SB-Beh9
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && initialisationProceeding && unitsReady
                    && allUnitsSound && valveIsOpen) {
                // SB-Beh8: all units sound
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.Valve());
            } else if (event instanceof InputEvent.Transmission && initialisationProceeding && unitsReady
                    && allUnitsSound && !valveIsOpen) {
                // SB-Beh8: all units sound
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.Transmission && initialisationProceeding && unitsReady
                    && !allUnitsSound && valveIsOpen) {
                // SB-Beh8: some unit defective
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.Valve());
            } else if (event instanceof InputEvent.Transmission && initialisationProceeding && unitsReady
                    && !allUnitsSound && !valveIsOpen) {
                // SB-Beh8: some unit defective
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.Transmission && initialisationProceeding && !unitsReady
                    && levelAboveN2 && !valveIsOpen) {
                // SB-Beh4: open the valve to drain excess water
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && initialisationProceeding && !unitsReady
                    && levelAboveN2 && valveIsOpen) {
                // SB-Beh4: keep draining
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
            } else if (event instanceof InputEvent.Transmission && initialisationProceeding && !unitsReady
                    && levelBelowBand && valveIsOpen) {
                // SB-Beh4, SB-Beh5: close the valve, fill with the pumps
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && initialisationProceeding && !unitsReady
                    && levelBelowBand && !valveIsOpen) {
                // SB-Beh5: fill with the pumps
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && initialisationProceeding && !unitsReady
                    && levelWithinBand && valveIsOpen) {
                // SB-Beh4, SB-Beh7: level no longer excessive; close the valve, announce readiness
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.Transmission && initialisationProceeding && !unitsReady
                    && levelWithinBand && !valveIsOpen && pumpsRunning) {
                // SB-Beh5, SB-Beh7: level reached; stop filling, announce readiness
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.Transmission && initialisationProceeding && !unitsReady
                    && levelWithinBand && !valveIsOpen && !pumpsRunning) {
                // SB-Beh7: repeat PROGRAM_READY until PHYSICAL_UNITS_READY
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.Transmission && !stopCountAtLimit && !plantWaiting) {
                // SB-Beh2: wait for STEAM_BOILER_WAITING
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(OperatingMode.INITIALIZATION));
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.Transmission && emergencyInRegulation) {
                // SB-Beh13, SB-Beh14, SB-Beh23, SB-FR8
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && levelFaultWhileRegulating && levelAboveN2) {
                // SB-Beh11
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && levelFaultWhileRegulating && levelBelowBand) {
                // SB-Beh11
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && levelFaultWhileRegulating && levelWithinBand) {
                // SB-Beh11
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
            } else if (event instanceof InputEvent.Transmission && otherFaultWhileRegulating && levelAboveN2) {
                // SB-Beh12
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && otherFaultWhileRegulating && levelBelowBand) {
                // SB-Beh12
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && otherFaultWhileRegulating && levelWithinBand) {
                // SB-Beh12
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.Transmission && noFaultWhileRegulating && levelAboveN2) {
                // SB-Beh10: level above N2, switch the pumps off
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && noFaultWhileRegulating && levelBelowBand) {
                // SB-Beh10: level below N1, switch the pumps on
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && noFaultWhileRegulating && levelWithinBand) {
                // SB-Beh10: level within [N1, N2], pumps unchanged
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.Transmission && emergencyInRegulation) {
                // SB-Beh18, SB-Beh23, SB-FR8
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && levelFaultWhileRegulating && levelAboveN2) {
                // SB-Beh17
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && levelFaultWhileRegulating && levelBelowBand) {
                // SB-Beh17
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && levelFaultWhileRegulating && levelWithinBand) {
                // SB-Beh17
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
            } else if (event instanceof InputEvent.Transmission && noFaultWhileRegulating && levelAboveN2) {
                // SB-Beh16: every failed unit repaired
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && noFaultWhileRegulating && levelBelowBand) {
                // SB-Beh16
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && noFaultWhileRegulating && levelWithinBand) {
                // SB-Beh16
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.Transmission && otherFaultWhileRegulating && levelAboveN2) {
                // SB-Beh15: regulate as in normal mode
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && otherFaultWhileRegulating && levelBelowBand) {
                // SB-Beh15
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && otherFaultWhileRegulating && levelWithinBand) {
                // SB-Beh15
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.Transmission && emergencyInRescue) {
                // SB-Beh21, SB-Beh23, SB-FR8
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Transmission && levelRepairedAllSound && levelAboveN2) {
                // SB-Beh20: level device repaired, no other unit defective
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && levelRepairedAllSound && levelBelowBand) {
                // SB-Beh20
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && levelRepairedAllSound && levelWithinBand) {
                // SB-Beh20
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.Transmission && levelRepairedPumpFault && levelAboveN2) {
                // SB-Beh20: level device repaired, some other unit defective
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && levelRepairedPumpFault && levelBelowBand) {
                // SB-Beh20
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && levelRepairedPumpFault && levelWithinBand) {
                // SB-Beh20
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.Transmission && levelStillFaulty && levelAboveN2) {
                // SB-Beh19: regulate on the estimated level
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.Transmission && levelStillFaulty && levelBelowBand) {
                // SB-Beh19
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.Transmission && levelStillFaulty && levelWithinBand) {
                // SB-Beh19
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(OperatingMode.RESCUE));
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // SB-Beh22: terminal mode; the program has stopped and no transition leaves it.
        }
    }
}
