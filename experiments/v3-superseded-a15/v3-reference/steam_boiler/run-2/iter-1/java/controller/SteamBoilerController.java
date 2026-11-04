package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.domain.OperatingMode;
import steam_boiler.domain.PumpId;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program's mode logic. It is stepped once per cycle, after the sensor
 * has analysed the cycle's incoming messages, and issues the cycle's mode-dependent messages:
 * MODE(m) on every cycle, PROGRAM_READY, VALVE, OPEN_PUMP(n) and CLOSE_PUMP(n).
 */
public final class SteamBoilerController {
    private BoilerMode currentMode = BoilerMode.INITIALIZATION;
    private boolean plantWaiting = false;
    private boolean valveOpen = false;
    private boolean programReadySent = false;
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

    /** Performs the analysis stage of one cycle. */
    public void step(InputEvent event) {
        // --- Operator stop and transmission ---
        boolean stopRequested = sensor.successiveStops() >= BoilerConstants.STOP_REPETITIONS;
        boolean transmissionFailed = sensor.transmissionFailure();
        boolean waitingReceived = sensor.steamBoilerWaitingReceived();
        boolean unitsReady = sensor.physicalUnitsReadyReceived();
        boolean readyAberrant = unitsReady && !programReadySent;
        boolean initTransmissionFailed = transmissionFailed || readyAberrant;
        boolean operatingTransmissionFailed = transmissionFailed || waitingReceived || unitsReady;

        // --- Health of the physical units ---
        boolean levelDeviceDefective = !sensor.levelDeviceSound();
        boolean otherUnitsSound = sensor.otherUnitsSound();
        boolean allUnitsSound = otherUnitsSound && !levelDeviceDefective;
        boolean estimateUnreliable = !sensor.steamDeviceSound() || !sensor.pumpControllersSound();

        // --- Water level ---
        boolean levelRisk = sensor.lowestReachableLevel() < BoilerConstants.LIMIT_M1
                || sensor.highestReachableLevel() > BoilerConstants.LIMIT_M2;
        boolean levelBelowN1 = sensor.waterLevel() < BoilerConstants.NORMAL_N1;
        boolean levelAboveN2 = sensor.waterLevel() > BoilerConstants.NORMAL_N2;
        boolean levelInBand = !levelBelowN1 && !levelAboveN2;

        // --- Initialization ---
        boolean boilerWaiting = plantWaiting || waitingReceived;
        boolean steamNonZero = sensor.steamRate() != 0.0;
        boolean initSteamDefective = boilerWaiting && steamNonZero;
        boolean initLevelDefective = boilerWaiting && levelDeviceDefective;
        boolean initEmergency = stopRequested || initTransmissionFailed || initSteamDefective
                || initLevelDefective;
        boolean initReadyPhase = !initEmergency && programReadySent;
        boolean initIdle = !initEmergency && !programReadySent && !boilerWaiting;
        boolean initAdjusting = !initEmergency && !programReadySent && boilerWaiting;

        // --- Normal, degraded and rescue: emergency dominates rescue, which dominates degraded ---
        boolean operatingEmergency = stopRequested || operatingTransmissionFailed || levelRisk;
        boolean rescueEmergency = operatingEmergency || estimateUnreliable;
        boolean levelFailureNoEmergency = !operatingEmergency && levelDeviceDefective;
        boolean unitFailureNoEmergency = !operatingEmergency && !levelDeviceDefective && !otherUnitsSound;
        boolean soundNoEmergency = !operatingEmergency && allUnitsSound;
        boolean levelRepairedSound = !rescueEmergency && allUnitsSound;
        boolean levelRepairedDegraded = !rescueEmergency && !levelDeviceDefective && !otherUnitsSound;
        boolean levelStillEstimated = !rescueEmergency && levelDeviceDefective;

        if (currentMode == BoilerMode.INITIALIZATION) {
            if (event instanceof InputEvent.MessagesReceived && stopRequested) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && initTransmissionFailed) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && initSteamDefective) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && initLevelDefective) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && initReadyPhase && unitsReady
                    && allUnitsSound) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.MessagesReceived && initReadyPhase && unitsReady
                    && !allUnitsSound) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.MessagesReceived && initReadyPhase && !unitsReady) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.MessagesReceived && initIdle) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
            } else if (event instanceof InputEvent.MessagesReceived && initAdjusting && levelAboveN2
                    && !valveOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                valveOpen = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
            } else if (event instanceof InputEvent.MessagesReceived && initAdjusting && levelAboveN2
                    && valveOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
            } else if (event instanceof InputEvent.MessagesReceived && initAdjusting && levelBelowN1
                    && valveOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                valveOpen = false;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP1));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP2));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP3));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP4));
            } else if (event instanceof InputEvent.MessagesReceived && initAdjusting && levelBelowN1
                    && !valveOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP1));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP2));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP3));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP4));
            } else if (event instanceof InputEvent.MessagesReceived && initAdjusting && levelInBand
                    && valveOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                valveOpen = false;
                programReadySent = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP1));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP2));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP3));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP4));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.MessagesReceived && initAdjusting && levelInBand
                    && !valveOpen) {
                currentMode = BoilerMode.INITIALIZATION;
                plantWaiting = true;
                programReadySent = true;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP1));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP2));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP3));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP4));
                actuator.apply(new OutputEvent.ProgramReady());
            }

        } else if (currentMode == BoilerMode.NORMAL) {
            if (event instanceof InputEvent.MessagesReceived && stopRequested) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && operatingTransmissionFailed) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && levelRisk) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && levelFailureNoEmergency) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
            } else if (event instanceof InputEvent.MessagesReceived && unitFailureNoEmergency) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.MessagesReceived && soundNoEmergency && levelBelowN1) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP1));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP2));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP3));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP4));
            } else if (event instanceof InputEvent.MessagesReceived && soundNoEmergency && levelAboveN2) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP1));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP2));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP3));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP4));
            } else if (event instanceof InputEvent.MessagesReceived && soundNoEmergency && levelInBand) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            }

        } else if (currentMode == BoilerMode.DEGRADED) {
            if (event instanceof InputEvent.MessagesReceived && stopRequested) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && operatingTransmissionFailed) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && levelRisk) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && levelFailureNoEmergency) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
            } else if (event instanceof InputEvent.MessagesReceived && soundNoEmergency) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.MessagesReceived && unitFailureNoEmergency
                    && levelBelowN1) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP1));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP2));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP3));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP4));
            } else if (event instanceof InputEvent.MessagesReceived && unitFailureNoEmergency
                    && levelAboveN2) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP1));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP2));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP3));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP4));
            } else if (event instanceof InputEvent.MessagesReceived && unitFailureNoEmergency
                    && levelInBand) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            }

        } else if (currentMode == BoilerMode.RESCUE) {
            if (event instanceof InputEvent.MessagesReceived && stopRequested) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && operatingTransmissionFailed) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && estimateUnreliable) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && levelRisk) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.EMERGENCY_STOP));
            } else if (event instanceof InputEvent.MessagesReceived && levelRepairedSound) {
                currentMode = BoilerMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.NORMAL));
            } else if (event instanceof InputEvent.MessagesReceived && levelRepairedDegraded) {
                currentMode = BoilerMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.DEGRADED));
            } else if (event instanceof InputEvent.MessagesReceived && levelStillEstimated
                    && levelBelowN1) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP1));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP2));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP3));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP4));
            } else if (event instanceof InputEvent.MessagesReceived && levelStillEstimated
                    && levelAboveN2) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP1));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP2));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP3));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP4));
            } else if (event instanceof InputEvent.MessagesReceived && levelStillEstimated
                    && levelInBand) {
                currentMode = BoilerMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(OperatingMode.RESCUE));
            }

        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
            // Terminal: the control task has ended and the physical environment is responsible.
        }
    }
}
