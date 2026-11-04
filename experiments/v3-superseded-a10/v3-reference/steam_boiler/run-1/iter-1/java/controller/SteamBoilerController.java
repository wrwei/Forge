package steam_boiler.controller;

import steam_boiler.SteamBoilerConstants;
import steam_boiler.actuator.SteamBoilerActuator;
import steam_boiler.actuator.ValvePosition;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.event.InputEvent;
import steam_boiler.event.ModeValue;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program's operating-mode state machine.
 *
 * <p>{@link #step} runs once per five-second cycle, after the sensor has analysed the cycle's
 * transmission. Every cycle ends with a MODE message carrying the mode the program is now in.
 * Emergency stop is terminal: the program's control task ends there.
 */
public final class SteamBoilerController {

    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    private ValvePosition evacuationValve = ValvePosition.VALVE_CLOSED;
    private boolean fillingPumpOn = false;
    @RoboChartType("nat") private int orderedPump = 0;
    private final BoilerSensor sensor;
    private final SteamBoilerActuator actuator;

    /** Creates the controller over the program's sensor and actuator. */
    public SteamBoilerController(BoilerSensor sensor, SteamBoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode. */
    public OperatingMode currentMode() {
        return currentMode;
    }

    /** Analyses one cycle's transmission and decides the cycle's outgoing commands. */
    public void step(InputEvent event) {
        // --- Quantities and judgements of the current transmission ---
        boolean stopLimitReached = sensor.successiveStopCount() >= SteamBoilerConstants.STOP_LIMIT;
        boolean transmissionFailed = sensor.transmissionFailure();
        boolean boilerWaiting = sensor.steamBoilerWaitingReceived();
        boolean unitsReady = sensor.physicalUnitsReadyReceived();
        boolean steamNonZero = sensor.steamReading() != 0.0;
        boolean levelFailed = sensor.levelDeviceOutOfOrder();
        boolean steamFailed = sensor.steamDeviceOutOfOrder();
        boolean pumpControlFailed = sensor.pumpControllerOutOfOrder();
        boolean otherUnitFailed = sensor.otherUnitOutOfOrder();
        boolean levelAboveN2 = sensor.levelReading() > SteamBoilerConstants.NORMAL_N2;
        boolean levelBelowN1 = sensor.levelReading() < SteamBoilerConstants.NORMAL_N1;
        boolean estimateAboveN2 = sensor.estimatedLevel() > SteamBoilerConstants.NORMAL_N2;
        boolean estimateBelowN1 = sensor.estimatedLevel() < SteamBoilerConstants.NORMAL_N1;
        boolean reachBelowM1 = sensor.minimumReachableLevel() <= SteamBoilerConstants.LIMIT_M1;
        boolean reachAboveM2 = sensor.maximumReachableLevel() >= SteamBoilerConstants.LIMIT_M2;
        boolean pumpOpenable = sensor.openablePumpCount() > 0;
        boolean pumpClosable = sensor.closablePumpCount() > 0;
        boolean valveOpen = evacuationValve == ValvePosition.VALVE_OPEN;

        // --- Causes of emergency stop ---
        boolean stopOrLinkFailure = stopLimitReached || transmissionFailed;
        boolean levelAtRisk = reachBelowM1 || reachAboveM2;
        boolean rescueUntrustworthy = steamFailed || pumpControlFailed;

        // --- Initialization: wait, check, adjust the level, hand over ---
        boolean initAbort = stopOrLinkFailure || (boilerWaiting && (steamNonZero || levelFailed));
        boolean initWaiting = !initAbort && !boilerWaiting;
        boolean initActive = !initAbort && boilerWaiting;
        boolean initLeave = initActive && unitsReady && !valveOpen && !fillingPumpOn;
        boolean initToNormal = initLeave && !otherUnitFailed;
        boolean initToDegraded = initLeave && otherUnitFailed;
        boolean initAdjust = initActive && !initLeave;
        boolean initDraining = initAdjust && valveOpen && levelAboveN2;
        boolean initCloseValve = initAdjust && valveOpen && !levelAboveN2;
        boolean initFilling = initAdjust && !valveOpen && fillingPumpOn && levelBelowN1;
        boolean initStopFilling = initAdjust && !valveOpen && fillingPumpOn && !levelBelowN1;
        boolean initIdle = initAdjust && !valveOpen && !fillingPumpOn;
        boolean initOpenValve = initIdle && levelAboveN2;
        boolean initStartFilling = initIdle && !levelAboveN2 && levelBelowN1 && pumpOpenable;
        boolean initNoPumpToStart = initIdle && !levelAboveN2 && levelBelowN1 && !pumpOpenable;
        boolean initLevelReady = initIdle && !levelAboveN2 && !levelBelowN1;

        // --- Normal and degraded: regulate on the measured level ---
        boolean regulatedAbort =
                stopOrLinkFailure || levelAtRisk || (levelFailed && rescueUntrustworthy);
        boolean regulatedToRescue = !regulatedAbort && levelFailed;
        boolean regulatedContinue = !regulatedAbort && !levelFailed;
        boolean regulatedAllSound = regulatedContinue && !otherUnitFailed;
        boolean regulatedSomeFailed = regulatedContinue && otherUnitFailed;
        boolean levelOpenPump = levelBelowN1 && pumpOpenable;
        boolean levelClosePump = levelAboveN2 && pumpClosable && !levelOpenPump;
        boolean levelHoldPumps = !levelOpenPump && !levelClosePump;

        // --- Rescue: regulate on the estimated level ---
        boolean rescueAbort = stopOrLinkFailure || levelAtRisk || rescueUntrustworthy;
        boolean rescueLevelBack = !rescueAbort && !levelFailed;
        boolean rescueToNormal = rescueLevelBack && !otherUnitFailed;
        boolean rescueToDegraded = rescueLevelBack && otherUnitFailed;
        boolean rescueContinue = !rescueAbort && levelFailed;
        boolean estimateOpenPump = estimateBelowN1 && pumpOpenable;
        boolean estimateClosePump = estimateAboveN2 && pumpClosable && !estimateOpenPump;
        boolean estimateHoldPumps = !estimateOpenPump && !estimateClosePump;

        // --- Two-level if-else: one block per mode, transitions in priority order ---
        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionReceived && initAbort) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && initWaiting) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_INITIALIZATION));
            } else if (event instanceof InputEvent.TransmissionReceived && initToNormal) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && initToDegraded) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && initDraining) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_INITIALIZATION));
            } else if (event instanceof InputEvent.TransmissionReceived && initCloseValve) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                evacuationValve = ValvePosition.VALVE_CLOSED;
            } else if (event instanceof InputEvent.TransmissionReceived && initFilling) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_INITIALIZATION));
            } else if (event instanceof InputEvent.TransmissionReceived && initStopFilling) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(orderedPump));
                fillingPumpOn = false;
            } else if (event instanceof InputEvent.TransmissionReceived && initOpenValve) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                evacuationValve = ValvePosition.VALVE_OPEN;
            } else if (event instanceof InputEvent.TransmissionReceived && initStartFilling) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_INITIALIZATION));
                orderedPump = sensor.pumpToOpen();
                actuator.apply(new OutputEvent.OpenPump(orderedPump));
                fillingPumpOn = true;
            } else if (event instanceof InputEvent.TransmissionReceived && initNoPumpToStart) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_INITIALIZATION));
            } else if (event instanceof InputEvent.TransmissionReceived && initLevelReady) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.ProgramReady());
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.TransmissionReceived && regulatedAbort) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && regulatedToRescue) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_RESCUE));
            } else if (event instanceof InputEvent.TransmissionReceived && regulatedSomeFailed) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && regulatedAllSound && levelOpenPump) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_NORMAL));
                orderedPump = sensor.pumpToOpen();
                actuator.apply(new OutputEvent.OpenPump(orderedPump));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && regulatedAllSound && levelClosePump) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_NORMAL));
                orderedPump = sensor.pumpToClose();
                actuator.apply(new OutputEvent.ClosePump(orderedPump));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && regulatedAllSound && levelHoldPumps) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_NORMAL));
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.TransmissionReceived && regulatedAbort) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && regulatedToRescue) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_RESCUE));
            } else if (event instanceof InputEvent.TransmissionReceived && regulatedAllSound) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && regulatedSomeFailed && levelOpenPump) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_DEGRADED));
                orderedPump = sensor.pumpToOpen();
                actuator.apply(new OutputEvent.OpenPump(orderedPump));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && regulatedSomeFailed && levelClosePump) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_DEGRADED));
                orderedPump = sensor.pumpToClose();
                actuator.apply(new OutputEvent.ClosePump(orderedPump));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && regulatedSomeFailed && levelHoldPumps) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_DEGRADED));
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.TransmissionReceived && rescueAbort) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueToNormal) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueToDegraded) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && rescueContinue && estimateOpenPump) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_RESCUE));
                orderedPump = sensor.pumpToOpen();
                actuator.apply(new OutputEvent.OpenPump(orderedPump));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && rescueContinue && estimateClosePump) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_RESCUE));
                orderedPump = sensor.pumpToClose();
                actuator.apply(new OutputEvent.ClosePump(orderedPump));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && rescueContinue && estimateHoldPumps) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(ModeValue.MODE_RESCUE));
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // Terminal: the control task has ended and the physical environment is in charge.
        }
    }
}
