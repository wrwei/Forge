package steam_boiler;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.event.InputEvent;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.ModeValue;
import steam_boiler.mode.OperatingMode;
import steam_boiler.monitor.UnitMonitor;
import steam_boiler.sensor.BoilerSensor;

/**
 * The steam-boiler control program (SB-V1): wires the unit monitor, the sensor
 * service, the controller and the actuator, and runs the five-second cycle of
 * receive, analyse, transmit (SB-V3).
 *
 * <p>Per cycle the message transmission system delivers every incoming message
 * through {@link #monitor()}'s receive methods, then calls {@link #runCycle()},
 * then collects the outgoing transmission from {@link #actuator()}.
 */
public final class SteamBoilerProgram {

    private final BoilerSensor sensor = new BoilerSensor();
    private final BoilerActuator actuator = new BoilerActuator();
    private final UnitMonitor monitor = new UnitMonitor(sensor);
    private final SteamBoilerController controller = new SteamBoilerController(sensor);

    /** Where incoming messages are delivered. */
    public UnitMonitor monitor() {
        return monitor;
    }

    /** Where the outgoing transmission is collected. */
    public BoilerActuator actuator() {
        return actuator;
    }

    /** The mode controller. */
    public SteamBoilerController controller() {
        return controller;
    }

    /**
     * Analyses the transmission received in this cycle and emits the outgoing
     * one: MODE(m) (SB-Beh24); in initialisation the controller's valve, pump
     * and PROGRAM_READY decisions (SB-Beh4, SB-Beh5, SB-Beh7); in the normal,
     * degraded and rescue modes the regulation of the pumps (SB-Beh10,
     * SB-Beh15, SB-Beh19); and the failure/repair protocol (SB-FR6, SB-FR7).
     * Once in emergency stop the control task has ended (SB-Beh22): nothing is
     * analysed, and MODE(emergency_stop) is the only message sent.
     */
    public void runCycle() {
        actuator.beginTransmission();
        if (controller.currentMode() == OperatingMode.EMERGENCY_STOP) {
            controller.step(new InputEvent.Transmission());
            actuator.apply(new OutputEvent.ModeMessage(ModeValue.EMERGENCY_STOP_MODE));
        } else {
            InputEvent event = monitor.analyse();
            controller.step(event);
            OperatingMode mode = controller.currentMode();
            actuator.apply(new OutputEvent.ModeMessage(modeValue(mode)));
            if (mode == OperatingMode.INITIALIZATION) {
                monitor.sendValveOrder(actuator, controller.valveOpen());
                monitor.sendPumpOrders(actuator, controller.pumpsOpen());
                if (controller.readyAnnounced()) {
                    actuator.apply(new OutputEvent.ProgramReady());
                }
            } else if (mode == OperatingMode.NORMAL || mode == OperatingMode.DEGRADED
                    || mode == OperatingMode.RESCUE) {
                monitor.regulatePumps(actuator);
            }
            monitor.sendProtocolMessages(actuator);
        }
        monitor.endCycle(actuator.sentThisCycle());
    }

    /** The MODE(m) value of an operating mode (SB-DM10). */
    private static ModeValue modeValue(OperatingMode mode) {
        if (mode == OperatingMode.NORMAL) {
            return ModeValue.NORMAL_MODE;
        }
        if (mode == OperatingMode.DEGRADED) {
            return ModeValue.DEGRADED_MODE;
        }
        if (mode == OperatingMode.RESCUE) {
            return ModeValue.RESCUE_MODE;
        }
        if (mode == OperatingMode.EMERGENCY_STOP) {
            return ModeValue.EMERGENCY_STOP_MODE;
        }
        return ModeValue.INITIALIZATION_MODE;
    }
}
