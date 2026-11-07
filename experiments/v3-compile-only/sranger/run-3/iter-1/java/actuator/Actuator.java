package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Differential-drive actuator: receives Move commands and stores the last
 * one issued (SR-DM6).
 */
public final class Actuator {

    @RoboChartType("real") private double lastLinearVelocity = 0.0;
    @RoboChartType("real") private double lastAngularVelocity = 0.0;

    /** Receives an output command from the controller and records it. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move mv = (OutputEvent.Move) command;
            lastLinearVelocity = mv.lv();
            lastAngularVelocity = mv.av();
        }
    }

    /** Last commanded linear velocity (m/s); 0.0 before any command. */
    @RoboChartType("real") public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** Last commanded angular velocity (rad/s); 0.0 before any command. */
    @RoboChartType("real") public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
