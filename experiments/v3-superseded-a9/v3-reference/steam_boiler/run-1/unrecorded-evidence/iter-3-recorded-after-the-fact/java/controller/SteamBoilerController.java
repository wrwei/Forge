package steam_boiler.controller;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.domain.ValvePosition;
import steam_boiler.event.InputEvent;
import steam_boiler.mode.OperatingMode;
import steam_boiler.sensor.PlantSensor;

/**
 * The steam-boiler control program's mode logic. {@link #step} is called once per five-second
 * cycle, after the receive stage, with {@link InputEvent.TransmissionReceived}; it analyses the
 * cycle's information and commands the cycle's outgoing messages.
 *
 * <p>Within a mode the branches are the leaves of a decision tree: each guard is the conjunction
 * of the tests on its path, so the branches are mutually exclusive, and the last branch of each
 * mode, which tests the event only, takes the one remaining case. The tree tests the conditions in
 * the precedence of SB-FR8: operator stop and emergency stop first, then rescue, then degraded,
 * then regulation within the mode.
 */
public final class SteamBoilerController {

    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    private boolean readyAnnounced = false;
    private final PlantSensor sensor;
    private final BoilerActuator actuator;

    /** Creates the controller in initialization mode. */
    public SteamBoilerController(PlantSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current operating mode. */
    public OperatingMode currentMode() {
        return currentMode;
    }

    /** Runs the analysis and transmission stages of one cycle. */
    public void step(InputEvent event) {
        boolean stopLimitReached = sensor.successiveStops() >= BoilerConstants.STOP_LIMIT;
        boolean transmissionFault = sensor.transmissionFailed();
        boolean plantWaiting = sensor.steamBoilerWaitingSeen();
        boolean unitsReady = sensor.physicalUnitsReadyReceived();
        boolean steamNonZero = sensor.steamRateNonZero();
        boolean levelFailed = sensor.levelDeviceOutOfOrder();
        boolean otherFailed = sensor.otherUnitOutOfOrder();
        boolean estimateUnreliable = sensor.rescueEstimateUnreliable();
        boolean levelAboveN2 = sensor.levelAboveNormal();
        boolean levelBelowN1 = sensor.levelBelowNormal();
        boolean limitRisk = sensor.levelLimitRisk();
        boolean headingBelowN1 = sensor.levelHeadingLow();
        boolean headingAboveN2 = sensor.levelHeadingHigh();
        boolean programReadySent = readyAnnounced;

        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.TransmissionReceived && stopLimitReached) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && transmissionFault) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && plantWaiting && steamNonZero) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && plantWaiting && !steamNonZero && levelFailed) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && plantWaiting && !steamNonZero && !levelFailed && !programReadySent && levelAboveN2) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.sendMode(OperatingMode.INITIALIZATION);
                actuator.setValve(ValvePosition.VALVE_OPEN);
                actuator.stopAllPumps();
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && plantWaiting && !steamNonZero && !levelFailed && !programReadySent && !levelAboveN2
                    && levelBelowN1) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.sendMode(OperatingMode.INITIALIZATION);
                actuator.setValve(ValvePosition.VALVE_CLOSED);
                actuator.startFillPump();
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && plantWaiting && !steamNonZero && !levelFailed && !programReadySent && !levelAboveN2
                    && !levelBelowN1) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.sendMode(OperatingMode.INITIALIZATION);
                readyAnnounced = true;
                actuator.setValve(ValvePosition.VALVE_CLOSED);
                actuator.stopAllPumps();
                actuator.sendProgramReady();
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && plantWaiting && !steamNonZero && !levelFailed && programReadySent && unitsReady
                    && !otherFailed) {
                currentMode = OperatingMode.NORMAL;
                actuator.sendMode(OperatingMode.NORMAL);
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && plantWaiting && !steamNonZero && !levelFailed && programReadySent && unitsReady
                    && otherFailed) {
                currentMode = OperatingMode.DEGRADED;
                actuator.sendMode(OperatingMode.DEGRADED);
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && plantWaiting && !steamNonZero && !levelFailed && programReadySent && !unitsReady) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.sendMode(OperatingMode.INITIALIZATION);
                actuator.setValve(ValvePosition.VALVE_CLOSED);
                actuator.stopAllPumps();
                actuator.sendProgramReady();
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived) {
                // Remaining case: STEAM_BOILER_WAITING has not arrived yet (SB-Beh2).
                currentMode = OperatingMode.INITIALIZATION;
                actuator.sendMode(OperatingMode.INITIALIZATION);
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.TransmissionReceived && stopLimitReached) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && transmissionFault) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && limitRisk) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && !limitRisk && levelFailed && estimateUnreliable) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && !limitRisk && levelFailed && !estimateUnreliable) {
                currentMode = OperatingMode.RESCUE;
                actuator.sendMode(OperatingMode.RESCUE);
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && !limitRisk && !levelFailed && otherFailed) {
                currentMode = OperatingMode.DEGRADED;
                actuator.sendMode(OperatingMode.DEGRADED);
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && !limitRisk && !levelFailed && !otherFailed && headingBelowN1) {
                currentMode = OperatingMode.NORMAL;
                actuator.sendMode(OperatingMode.NORMAL);
                actuator.startAllPumps();
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && !limitRisk && !levelFailed && !otherFailed && !headingBelowN1 && headingAboveN2) {
                currentMode = OperatingMode.NORMAL;
                actuator.sendMode(OperatingMode.NORMAL);
                actuator.stopAllPumps();
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived) {
                // Remaining case: the level stays within its regulation band; pumps are left as they are.
                currentMode = OperatingMode.NORMAL;
                actuator.sendMode(OperatingMode.NORMAL);
                actuator.sendUnitMessages();
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.TransmissionReceived && stopLimitReached) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && transmissionFault) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && limitRisk) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && !limitRisk && levelFailed && estimateUnreliable) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && !limitRisk && levelFailed && !estimateUnreliable) {
                currentMode = OperatingMode.RESCUE;
                actuator.sendMode(OperatingMode.RESCUE);
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && !limitRisk && !levelFailed && !otherFailed) {
                currentMode = OperatingMode.NORMAL;
                actuator.sendMode(OperatingMode.NORMAL);
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && !limitRisk && !levelFailed && otherFailed && headingBelowN1) {
                currentMode = OperatingMode.DEGRADED;
                actuator.sendMode(OperatingMode.DEGRADED);
                actuator.startAllPumps();
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && !limitRisk && !levelFailed && otherFailed && !headingBelowN1 && headingAboveN2) {
                currentMode = OperatingMode.DEGRADED;
                actuator.sendMode(OperatingMode.DEGRADED);
                actuator.stopAllPumps();
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived) {
                // Remaining case: the level stays within its regulation band; pumps are left as they are.
                currentMode = OperatingMode.DEGRADED;
                actuator.sendMode(OperatingMode.DEGRADED);
                actuator.sendUnitMessages();
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.TransmissionReceived && stopLimitReached) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && transmissionFault) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && estimateUnreliable) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && !estimateUnreliable && limitRisk) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && !estimateUnreliable && !limitRisk && !levelFailed && !otherFailed) {
                currentMode = OperatingMode.NORMAL;
                actuator.sendMode(OperatingMode.NORMAL);
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && !estimateUnreliable && !limitRisk && !levelFailed && otherFailed) {
                currentMode = OperatingMode.DEGRADED;
                actuator.sendMode(OperatingMode.DEGRADED);
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && !estimateUnreliable && !limitRisk && levelFailed && headingBelowN1) {
                currentMode = OperatingMode.RESCUE;
                actuator.sendMode(OperatingMode.RESCUE);
                actuator.startAllPumps();
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived && !stopLimitReached && !transmissionFault
                    && !estimateUnreliable && !limitRisk && levelFailed && !headingBelowN1 && headingAboveN2) {
                currentMode = OperatingMode.RESCUE;
                actuator.sendMode(OperatingMode.RESCUE);
                actuator.stopAllPumps();
                actuator.sendUnitMessages();
            } else if (event instanceof InputEvent.TransmissionReceived) {
                // Remaining case: the level stays within its regulation band; pumps are left as they are.
                currentMode = OperatingMode.RESCUE;
                actuator.sendMode(OperatingMode.RESCUE);
                actuator.sendUnitMessages();
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // Terminal (SB-Beh22): emergency stop is never left; a further cycle only repeats MODE.
            if (event instanceof InputEvent.TransmissionReceived) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.sendMode(OperatingMode.EMERGENCY_STOP);
            }
        }
    }
}
