package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Receives the controller's Move commands and stores the last one (SR-DM6).
 */
public final class Actuator {

    @RoboChartType("real") private double lastLinearVelocity = 0.0;
    @RoboChartType("real") private double lastAngularVelocity = 0.0;

    /** Receives an output command from the controller and records it. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.Move) {
            OutputEvent.Move command = (OutputEvent.Move) output;
            this.lastLinearVelocity = command.lv();
            this.lastAngularVelocity = command.av();
        }
    }

    /** Linear velocity of the last Move command (m/s); 0 before any command. */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** Angular velocity of the last Move command (rad/s); 0 before any command. */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
