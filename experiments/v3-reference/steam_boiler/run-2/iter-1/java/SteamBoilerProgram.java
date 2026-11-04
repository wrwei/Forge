package steam_boiler;

import steam_boiler.actuator.BoilerActuator;
import steam_boiler.controller.SteamBoilerController;
import steam_boiler.event.InputEvent;
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
    private final SteamBoilerController controller = new SteamBoilerController(sensor, actuator);

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
     * one. Once in emergency stop the program has stopped (SB-Beh22): nothing
     * is analysed or sent.
     */
    public void runCycle() {
        actuator.beginTransmission();
        if (controller.currentMode() == OperatingMode.EMERGENCY_STOP) {
            monitor.endCycle(actuator.sentThisCycle());
            return;
        }
        monitor.analyse();
        controller.step(new InputEvent.Transmission());
        monitor.sendProtocolMessages(actuator);
        monitor.endCycle(actuator.sentThisCycle());
    }
}
