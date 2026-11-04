package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.domain.ModeSignal;
import steam_boiler.domain.OperatingMode;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.sensor.BoilerSensor;

/**
 * Operating-mode controller of the steam-boiler control program.
 *
 * <p>{@link #step} is called once per five-second cycle, after the sensor has received the cycle's
 * transmission (SB-V3). It chooses the operating mode, the most severe consequence prevailing when
 * several failure conditions hold together (SB-FR8), and issues the mode-level messages: MODE on
 * every cycle (SB-Beh24), and PROGRAM_READY, VALVE, OPEN_PUMP and CLOSE_PUMP for regulation.
 * Within each mode the transitions are mutually exclusive and cover every case; emergency stop is
 * terminal (SB-Beh22).
 */
public final class SteamBoilerController {

    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    private boolean valveOpened = false;
    private boolean programReadySent = false;
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /** Creates the controller in initialization mode (SB-Beh1). */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode (SB-V2). */
    public OperatingMode currentMode() {
        return currentMode;
    }

    /** Analyses the cycle's information and emits the cycle's mode-level messages. */
    public void step(InputEvent event) {
        // --- Named boolean predicates ---
        // Operator stop and transmission system (SB-Beh23, SB-FR5)
        boolean stopRequestedThrice = sensor.consecutiveStops() >= BoilerConstants.STOP_LIMIT;
        boolean linkFailed = sensor.transmissionFailure();
        boolean waitingMessageSeen = sensor.steamBoilerWaitingReceived();
        boolean unitsReadyMessageSeen = sensor.physicalUnitsReadyReceived();
        boolean boilerIsWaiting = sensor.plantWaiting();
        // Unit status (SB-V9)
        boolean levelUnitDefective = sensor.levelDeviceOutOfOrder();
        boolean steamUnitDefective = sensor.steamDeviceOutOfOrder();
        boolean pumpUnitDefective = sensor.pumpUnitOutOfOrder();
        boolean pumpControlDefective = sensor.pumpControlUnitOutOfOrder();
        boolean otherUnitDefective = steamUnitDefective || pumpUnitDefective || pumpControlDefective;
        boolean allUnitsSound = !levelUnitDefective && !otherUnitDefective;
        boolean onlyOtherUnitDefective = !levelUnitDefective && otherUnitDefective;
        boolean estimateUnreliable = steamUnitDefective || pumpControlDefective;
        // Steam and water level (SB-V4 to SB-V8)
        boolean steamRateNonZero = sensor.steamReading() != 0.0;
        boolean levelAboveNormal = sensor.levelEstimate() > BoilerConstants.NORMAL_N2;
        boolean levelBelowNormal = sensor.levelEstimate() < BoilerConstants.NORMAL_N1 && !levelAboveNormal;
        boolean levelWithinNormal = !levelAboveNormal && !levelBelowNormal;
        boolean levelRiskLow = sensor.predictedLevelMin() <= BoilerConstants.LIMIT_M1;
        boolean levelRiskHigh = sensor.predictedLevelMax() >= BoilerConstants.LIMIT_M2;
        boolean levelRisk = levelRiskLow || levelRiskHigh;
        // Initialization progress (SB-DM7, SB-Beh7)
        boolean valveIsOpen = valveOpened;
        boolean readyAnnounced = programReadySent;
        // Initialization guards (SB-Beh2 to SB-Beh9)
        boolean unitsReadyPremature = unitsReadyMessageSeen && !readyAnnounced;
        boolean initLinkFailed = linkFailed || unitsReadyPremature;
        boolean steamCheckFailed = boilerIsWaiting && steamRateNonZero;
        boolean levelCheckFailed = boilerIsWaiting && levelUnitDefective;
        boolean initAbort = stopRequestedThrice || initLinkFailed || steamCheckFailed || levelCheckFailed;
        boolean initUnitsReady = !initAbort && unitsReadyMessageSeen;
        boolean initAwaitingUnits = !initAbort && !unitsReadyMessageSeen && readyAnnounced;
        boolean initAdjustingLevel = !initAbort && !readyAnnounced && boilerIsWaiting;
        boolean initAwaitingPlant = !initAbort && !readyAnnounced && !boilerIsWaiting;
        // Guards of the running modes (SB-Beh11 to SB-Beh21, SB-FR8)
        boolean runLinkFailed = linkFailed || waitingMessageSeen || unitsReadyMessageSeen;
        boolean rescueUnusable = levelUnitDefective && estimateUnreliable;
        boolean measuredAbort = stopRequestedThrice || runLinkFailed || levelRisk || rescueUnusable;
        boolean rescueAbort = stopRequestedThrice || runLinkFailed || levelRisk || estimateUnreliable;

        // --- One block per operating mode ---
        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionReceived && stopRequestedThrice) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && initLinkFailed) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && steamCheckFailed) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelCheckFailed) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && initUnitsReady && allUnitsSound) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && initUnitsReady && !allUnitsSound) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived && initAwaitingUnits) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_INITIALIZATION));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived
                    && initAdjustingLevel && levelAboveNormal && !valveIsOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_INITIALIZATION));
                valveOpened = true;
                actuator.apply(new OutputEvent.Valve());
            } else if (event instanceof InputEvent.TransmissionReceived
                    && initAdjustingLevel && levelAboveNormal && valveIsOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_INITIALIZATION));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && initAdjustingLevel && levelBelowNormal && valveIsOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_INITIALIZATION));
                valveOpened = false;
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(sensor.fillPump()));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && initAdjustingLevel && levelBelowNormal && !valveIsOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_INITIALIZATION));
                actuator.apply(new OutputEvent.OpenPump(sensor.fillPump()));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && initAdjustingLevel && levelWithinNormal && valveIsOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_INITIALIZATION));
                valveOpened = false;
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                programReadySent = true;
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived
                    && initAdjustingLevel && levelWithinNormal && !valveIsOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
                programReadySent = true;
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.TransmissionReceived && initAwaitingPlant) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_INITIALIZATION));
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.TransmissionReceived && stopRequestedThrice) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && runLinkFailed) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelRisk) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueUnusable) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && !measuredAbort && levelUnitDefective) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_RESCUE));
            } else if (event instanceof InputEvent.TransmissionReceived && !measuredAbort && onlyOtherUnitDefective) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && !measuredAbort && allUnitsSound && levelBelowNormal) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_NORMAL));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && !measuredAbort && allUnitsSound && levelAboveNormal) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_NORMAL));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && !measuredAbort && allUnitsSound && levelWithinNormal) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_NORMAL));
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.TransmissionReceived && stopRequestedThrice) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && runLinkFailed) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelRisk) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && rescueUnusable) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && !measuredAbort && levelUnitDefective) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_RESCUE));
            } else if (event instanceof InputEvent.TransmissionReceived && !measuredAbort && allUnitsSound) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && !measuredAbort && onlyOtherUnitDefective && levelBelowNormal) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && !measuredAbort && onlyOtherUnitDefective && levelAboveNormal) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && !measuredAbort && onlyOtherUnitDefective && levelWithinNormal) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_DEGRADED));
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.TransmissionReceived && stopRequestedThrice) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && runLinkFailed) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && levelRisk) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && estimateUnreliable) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.TransmissionReceived && !rescueAbort && allUnitsSound) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_NORMAL));
            } else if (event instanceof InputEvent.TransmissionReceived && !rescueAbort && onlyOtherUnitDefective) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_DEGRADED));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && !rescueAbort && levelUnitDefective && levelBelowNormal) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_RESCUE));
                actuator.apply(new OutputEvent.OpenPump(1));
                actuator.apply(new OutputEvent.OpenPump(2));
                actuator.apply(new OutputEvent.OpenPump(3));
                actuator.apply(new OutputEvent.OpenPump(4));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && !rescueAbort && levelUnitDefective && levelAboveNormal) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_RESCUE));
                actuator.apply(new OutputEvent.ClosePump(1));
                actuator.apply(new OutputEvent.ClosePump(2));
                actuator.apply(new OutputEvent.ClosePump(3));
                actuator.apply(new OutputEvent.ClosePump(4));
            } else if (event instanceof InputEvent.TransmissionReceived
                    && !rescueAbort && levelUnitDefective && levelWithinNormal) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.ModeMessage(ModeSignal.SIGNAL_RESCUE));
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // Terminal: the control task has ended and the physical environment is responsible.
        }
    }
}
