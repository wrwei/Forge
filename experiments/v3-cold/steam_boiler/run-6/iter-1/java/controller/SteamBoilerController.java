package steam_boiler.controller;

import steam_boiler.BoilerConstants;
import steam_boiler.actuator.BoilerActuator;
import steam_boiler.domain.PumpId;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.ModeCode;
import steam_boiler.mode.OperatingMode;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program's mode logic. Once per cycle, after the
 * sensor has received and analysed the incoming messages, {@link #step} takes
 * exactly one transition of the current mode and issues the mode's outgoing
 * messages: MODE(m) on every cycle, and PROGRAM_READY, VALVE, OPEN_PUMP and
 * CLOSE_PUMP as the mode requires. Within a mode the transitions' guards are
 * mutually exclusive and, together, cover every case; the most severe
 * consequence comes first. Emergency stop is terminal.
 */
public final class SteamBoilerController {

    private OperatingMode currentMode = OperatingMode.INITIALIZATION;
    private final BoilerSensor sensor;
    private final BoilerActuator actuator;

    /**
     * Creates the controller in initialization mode.
     *
     * @param sensor   the analysed information of the current cycle
     * @param actuator where outgoing messages are issued
     */
    public SteamBoilerController(BoilerSensor sensor, BoilerActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** @return the current operating mode */
    public OperatingMode currentMode() {
        return currentMode;
    }

    /**
     * Takes the current cycle's transition.
     *
     * @param event {@link InputEvent.Cycle} once the cycle's messages have been analysed
     */
    public void step(InputEvent event) {
        // --- Named boolean predicates ---
        boolean stopRequested = sensor.successiveStops() >= BoilerConstants.STOP_REPETITIONS;
        boolean transmissionLost = sensor.transmissionFailure();
        boolean plantWaiting = sensor.steamBoilerWaitingSeen();
        boolean waitingMessageNow = sensor.steamBoilerWaitingReceived();
        boolean unitsReady = sensor.physicalUnitsReadyReceived();
        boolean readyAnnounced = sensor.programReadyAnnounced();
        boolean steamNotZero = sensor.steamReading() != 0.0;
        boolean levelDeviceDown = sensor.levelDeviceFailed();
        boolean steamDeviceDown = sensor.steamDeviceFailed();
        boolean pumpControlDown = sensor.pumpControlFailed();
        boolean pumpDown = sensor.pumpFailed();
        boolean drainOpen = sensor.valveOpen();
        boolean levelAboveNormal = sensor.levelEstimate() > BoilerConstants.MAX_NORMAL;
        boolean levelBelowNormal = sensor.levelEstimate() < BoilerConstants.MIN_NORMAL;
        boolean levelNearLimit = sensor.worstCaseMinimumLevel() <= BoilerConstants.MIN_LIMIT
                || sensor.worstCaseMaximumLevel() >= BoilerConstants.MAX_LIMIT;

        boolean levelInRange = !levelAboveNormal && !levelBelowNormal;
        boolean otherUnitDown = steamDeviceDown || pumpControlDown || pumpDown;
        boolean anyUnitDown = levelDeviceDown || otherUnitDown;
        boolean forcedStop = stopRequested || transmissionLost;
        boolean initMessageOutOfMode = waitingMessageNow || unitsReady;

        boolean initAborted = forcedStop
                || (unitsReady && !readyAnnounced)
                || (plantWaiting && (steamNotZero || levelDeviceDown));
        boolean initUnitsReady = !initAborted && unitsReady;
        boolean initIdle = !initAborted && !unitsReady && !plantWaiting;
        boolean initHandshake = !initAborted && !unitsReady && plantWaiting && readyAnnounced;
        boolean initPreparing = !initAborted && !unitsReady && plantWaiting && !readyAnnounced;

        boolean operatingAborted = forcedStop || initMessageOutOfMode || levelNearLimit
                || (levelDeviceDown && (steamDeviceDown || pumpControlDown));
        boolean regulatingNormally = !operatingAborted && !anyUnitDown;
        boolean regulatingDegraded = !operatingAborted && !levelDeviceDown && otherUnitDown;

        boolean rescueAborted = forcedStop || initMessageOutOfMode || levelNearLimit
                || steamDeviceDown || pumpControlDown;
        boolean regulatingRescue = !rescueAborted && levelDeviceDown;

        // --- Two-level if-else: one block per mode ---
        if (currentMode == OperatingMode.INITIALIZATION) {
            if (event instanceof InputEvent.Cycle && initAborted) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Cycle && initUnitsReady && !anyUnitDown) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_NORMAL));
            } else if (event instanceof InputEvent.Cycle && initUnitsReady && anyUnitDown) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_DEGRADED));
            } else if (event instanceof InputEvent.Cycle && initIdle) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_INITIALIZATION));
            } else if (event instanceof InputEvent.Cycle && initHandshake) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.Cycle && initPreparing && levelAboveNormal && !drainOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
            } else if (event instanceof InputEvent.Cycle && initPreparing && levelAboveNormal && drainOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_INITIALIZATION));
            } else if (event instanceof InputEvent.Cycle && initPreparing && levelBelowNormal && drainOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP_1));
            } else if (event instanceof InputEvent.Cycle && initPreparing && levelBelowNormal && !drainOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP_1));
            } else if (event instanceof InputEvent.Cycle && initPreparing && levelInRange && drainOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.Valve());
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP_1));
                actuator.apply(new OutputEvent.ProgramReady());
            } else if (event instanceof InputEvent.Cycle && initPreparing && levelInRange && !drainOpen) {
                currentMode = OperatingMode.INITIALIZATION;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_INITIALIZATION));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP_1));
                actuator.apply(new OutputEvent.ProgramReady());
            }

        } else if (currentMode == OperatingMode.NORMAL) {
            if (event instanceof InputEvent.Cycle && operatingAborted) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Cycle && !operatingAborted && levelDeviceDown) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_RESCUE));
            } else if (event instanceof InputEvent.Cycle && regulatingDegraded) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_DEGRADED));
            } else if (event instanceof InputEvent.Cycle && regulatingNormally && levelBelowNormal) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_NORMAL));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP_1));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP_2));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP_3));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP_4));
            } else if (event instanceof InputEvent.Cycle && regulatingNormally && levelAboveNormal) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_NORMAL));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP_1));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP_2));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP_3));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP_4));
            } else if (event instanceof InputEvent.Cycle && regulatingNormally && levelInRange) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_NORMAL));
            }

        } else if (currentMode == OperatingMode.DEGRADED) {
            if (event instanceof InputEvent.Cycle && operatingAborted) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Cycle && !operatingAborted && levelDeviceDown) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_RESCUE));
            } else if (event instanceof InputEvent.Cycle && regulatingNormally) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_NORMAL));
            } else if (event instanceof InputEvent.Cycle && regulatingDegraded && levelBelowNormal) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_DEGRADED));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP_1));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP_2));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP_3));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP_4));
            } else if (event instanceof InputEvent.Cycle && regulatingDegraded && levelAboveNormal) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_DEGRADED));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP_1));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP_2));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP_3));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP_4));
            } else if (event instanceof InputEvent.Cycle && regulatingDegraded && levelInRange) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_DEGRADED));
            }

        } else if (currentMode == OperatingMode.RESCUE) {
            if (event instanceof InputEvent.Cycle && rescueAborted) {
                currentMode = OperatingMode.EMERGENCY_STOP;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_EMERGENCY_STOP));
            } else if (event instanceof InputEvent.Cycle && !rescueAborted && !anyUnitDown) {
                currentMode = OperatingMode.NORMAL;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_NORMAL));
            } else if (event instanceof InputEvent.Cycle && !rescueAborted && !levelDeviceDown && otherUnitDown) {
                currentMode = OperatingMode.DEGRADED;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_DEGRADED));
            } else if (event instanceof InputEvent.Cycle && regulatingRescue && levelBelowNormal) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_RESCUE));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP_1));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP_2));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP_3));
                actuator.apply(new OutputEvent.OpenPump(PumpId.PUMP_4));
            } else if (event instanceof InputEvent.Cycle && regulatingRescue && levelAboveNormal) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_RESCUE));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP_1));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP_2));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP_3));
                actuator.apply(new OutputEvent.ClosePump(PumpId.PUMP_4));
            } else if (event instanceof InputEvent.Cycle && regulatingRescue && levelInRange) {
                currentMode = OperatingMode.RESCUE;
                actuator.apply(new OutputEvent.Mode(ModeCode.MODE_RESCUE));
            }

        } else if (currentMode == OperatingMode.EMERGENCY_STOP) {
            // Terminal: the control task has ended and the program stops.
        }
    }
}
