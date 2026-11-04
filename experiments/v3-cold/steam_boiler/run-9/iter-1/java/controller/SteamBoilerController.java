package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.event.InputEvent;
import steam_boiler.mode.BoilerMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program (SB-V1). Once per five-second cycle, after
 * the sensor stage has received and analysed the incoming transmission, the
 * transmission system calls {@link #step} with
 * {@link InputEvent.TransmissionComplete}; the controller decides the
 * operating mode (SB-V2) and issues the cycle's outgoing messages.
 */
public final class SteamBoilerController {

    private BoilerMode currentMode = BoilerMode.INITIALIZATION;
    /** STEAM_BOILER_WAITING has arrived (SB-Beh2). */
    private boolean plantWaiting = false;
    /** PROGRAM_READY has been announced (SB-Beh7). */
    private boolean readyAnnounced = false;
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /** Creates the controller in initialization mode (SB-Beh1). */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode. */
    public BoilerMode currentMode() {
        return currentMode;
    }

    /** One control cycle: analyse the received information and transmit. */
    public void step(InputEvent event) {
        // --- Named predicates ---
        boolean operatorStop = sensor.successiveStops() >= BoilerConstants.STOP_REPETITIONS;
        boolean transmissionFault = sensor.transmissionFailure();
        boolean waitingNow = sensor.steamBoilerWaitingReceived();
        boolean plantStarted = plantWaiting || waitingNow;
        boolean steamNotZero = sensor.steamReading() != 0.0;
        boolean levelFault = sensor.levelDeviceFailed();
        boolean steamFault = sensor.steamDeviceFailed();
        boolean pumpFault = sensor.anyPumpFailed();
        boolean pumpControlFault = sensor.anyPumpControllerFailed();
        boolean otherUnitFault = steamFault || pumpFault || pumpControlFault;
        boolean unitsReady = sensor.physicalUnitsReadyReceived();
        boolean handshakeDone = readyAnnounced && unitsReady;
        boolean initAbort = operatorStop
                || (plantStarted && (transmissionFault || steamNotZero || levelFault));
        boolean readingAboveNormal = sensor.levelReading() > BoilerConstants.MAX_NORMAL_N2;
        boolean readingBelowNormal = sensor.levelReading() < BoilerConstants.MIN_NORMAL_N1;
        boolean levelRiskLow = sensor.predictedLevelLow() < BoilerConstants.MIN_LIMIT_M1;
        boolean levelRiskHigh = sensor.predictedLevelHigh() > BoilerConstants.MAX_LIMIT_M2;
        boolean emergencyNeeded = operatorStop || transmissionFault || levelRiskLow || levelRiskHigh;
        boolean rescueEmergency = emergencyNeeded || steamFault || pumpControlFault;
        boolean waterBelowNormal = sensor.levelEstimate() < BoilerConstants.MIN_NORMAL_N1;
        boolean waterHeadingAboveNormal = sensor.predictedLevelHigh() > BoilerConstants.MAX_NORMAL_N2;

        // --- Mode-nested transitions ---
        if (currentMode == BoilerMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionComplete && initAbort) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.broadcastMode(BoilerMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionComplete && !initAbort && !plantStarted) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.broadcastMode(BoilerMode.INITIALIZATION);
            } else if (event instanceof InputEvent.TransmissionComplete && !initAbort && plantStarted
                    && handshakeDone && !otherUnitFault) {
                currentMode = BoilerMode.NORMAL;
                actuator.broadcastMode(BoilerMode.NORMAL);
                actuator.commandValveClosed();
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !initAbort && plantStarted
                    && handshakeDone && otherUnitFault) {
                currentMode = BoilerMode.DEGRADED;
                actuator.broadcastMode(BoilerMode.DEGRADED);
                actuator.commandValveClosed();
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !initAbort && plantStarted
                    && readyAnnounced && !unitsReady) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.broadcastMode(BoilerMode.INITIALIZATION);
                plantWaiting = true;
                actuator.announceProgramReady();
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !initAbort && plantStarted
                    && !readyAnnounced && readingAboveNormal) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.broadcastMode(BoilerMode.INITIALIZATION);
                plantWaiting = true;
                actuator.commandPumpOff(1);
                actuator.commandValveOpen();
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !initAbort && plantStarted
                    && !readyAnnounced && readingBelowNormal) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.broadcastMode(BoilerMode.INITIALIZATION);
                plantWaiting = true;
                actuator.commandValveClosed();
                actuator.commandPumpOn(1);
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !initAbort && plantStarted
                    && !readyAnnounced && !readingAboveNormal && !readingBelowNormal) {
                currentMode = BoilerMode.INITIALIZATION;
                actuator.broadcastMode(BoilerMode.INITIALIZATION);
                plantWaiting = true;
                readyAnnounced = true;
                actuator.commandValveClosed();
                actuator.commandPumpOff(1);
                actuator.announceProgramReady();
                actuator.reportUnitStatus();
            }

        } else if (currentMode == BoilerMode.NORMAL) {
            if (event instanceof InputEvent.TransmissionComplete && emergencyNeeded) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.broadcastMode(BoilerMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionComplete && !emergencyNeeded && levelFault) {
                currentMode = BoilerMode.RESCUE;
                actuator.broadcastMode(BoilerMode.RESCUE);
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !emergencyNeeded && !levelFault
                    && otherUnitFault) {
                currentMode = BoilerMode.DEGRADED;
                actuator.broadcastMode(BoilerMode.DEGRADED);
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !emergencyNeeded && !levelFault
                    && !otherUnitFault && waterBelowNormal && !waterHeadingAboveNormal) {
                currentMode = BoilerMode.NORMAL;
                actuator.broadcastMode(BoilerMode.NORMAL);
                actuator.commandPumpOn(1);
                actuator.commandPumpOn(2);
                actuator.commandPumpOn(3);
                actuator.commandPumpOn(4);
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !emergencyNeeded && !levelFault
                    && !otherUnitFault && waterHeadingAboveNormal) {
                currentMode = BoilerMode.NORMAL;
                actuator.broadcastMode(BoilerMode.NORMAL);
                actuator.commandPumpOff(1);
                actuator.commandPumpOff(2);
                actuator.commandPumpOff(3);
                actuator.commandPumpOff(4);
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !emergencyNeeded && !levelFault
                    && !otherUnitFault && !waterBelowNormal && !waterHeadingAboveNormal) {
                currentMode = BoilerMode.NORMAL;
                actuator.broadcastMode(BoilerMode.NORMAL);
                actuator.reportUnitStatus();
            }

        } else if (currentMode == BoilerMode.DEGRADED) {
            if (event instanceof InputEvent.TransmissionComplete && emergencyNeeded) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.broadcastMode(BoilerMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionComplete && !emergencyNeeded && levelFault) {
                currentMode = BoilerMode.RESCUE;
                actuator.broadcastMode(BoilerMode.RESCUE);
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !emergencyNeeded && !levelFault
                    && !otherUnitFault) {
                currentMode = BoilerMode.NORMAL;
                actuator.broadcastMode(BoilerMode.NORMAL);
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !emergencyNeeded && !levelFault
                    && otherUnitFault && waterBelowNormal && !waterHeadingAboveNormal) {
                currentMode = BoilerMode.DEGRADED;
                actuator.broadcastMode(BoilerMode.DEGRADED);
                actuator.commandPumpOn(1);
                actuator.commandPumpOn(2);
                actuator.commandPumpOn(3);
                actuator.commandPumpOn(4);
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !emergencyNeeded && !levelFault
                    && otherUnitFault && waterHeadingAboveNormal) {
                currentMode = BoilerMode.DEGRADED;
                actuator.broadcastMode(BoilerMode.DEGRADED);
                actuator.commandPumpOff(1);
                actuator.commandPumpOff(2);
                actuator.commandPumpOff(3);
                actuator.commandPumpOff(4);
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !emergencyNeeded && !levelFault
                    && otherUnitFault && !waterBelowNormal && !waterHeadingAboveNormal) {
                currentMode = BoilerMode.DEGRADED;
                actuator.broadcastMode(BoilerMode.DEGRADED);
                actuator.reportUnitStatus();
            }

        } else if (currentMode == BoilerMode.RESCUE) {
            if (event instanceof InputEvent.TransmissionComplete && rescueEmergency) {
                currentMode = BoilerMode.EMERGENCY_STOP;
                actuator.broadcastMode(BoilerMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionComplete && !rescueEmergency && !levelFault
                    && !otherUnitFault) {
                currentMode = BoilerMode.NORMAL;
                actuator.broadcastMode(BoilerMode.NORMAL);
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !rescueEmergency && !levelFault
                    && otherUnitFault) {
                currentMode = BoilerMode.DEGRADED;
                actuator.broadcastMode(BoilerMode.DEGRADED);
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !rescueEmergency && levelFault
                    && waterBelowNormal && !waterHeadingAboveNormal) {
                currentMode = BoilerMode.RESCUE;
                actuator.broadcastMode(BoilerMode.RESCUE);
                actuator.commandPumpOn(1);
                actuator.commandPumpOn(2);
                actuator.commandPumpOn(3);
                actuator.commandPumpOn(4);
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !rescueEmergency && levelFault
                    && waterHeadingAboveNormal) {
                currentMode = BoilerMode.RESCUE;
                actuator.broadcastMode(BoilerMode.RESCUE);
                actuator.commandPumpOff(1);
                actuator.commandPumpOff(2);
                actuator.commandPumpOff(3);
                actuator.commandPumpOff(4);
                actuator.reportUnitStatus();
            } else if (event instanceof InputEvent.TransmissionComplete && !rescueEmergency && levelFault
                    && !waterBelowNormal && !waterHeadingAboveNormal) {
                currentMode = BoilerMode.RESCUE;
                actuator.broadcastMode(BoilerMode.RESCUE);
                actuator.reportUnitStatus();
            }

        } else if (currentMode == BoilerMode.EMERGENCY_STOP) {
            // Terminal mode (SB-Beh22): the program has stopped; no transition leaves it.
        }
    }
}
