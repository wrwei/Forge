package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Receives Move commands from the controller and stores the last one
 * issued (SR-DM6).
 */
public final class Actuator {

    @RoboChartType("real") private double lastLinearVelocity = 0.0;
    @RoboChartType("real") private double lastAngularVelocity = 0.0;

    /** Receives an output command from the controller and records it. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move moveCommand = (OutputEvent.Move) command;
            this.lastLinearVelocity = moveCommand.lv();
            this.lastAngularVelocity = moveCommand.av();
        }
    }

    /** Linear velocity of the last issued Move (m/s). */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** Angular velocity of the last issued Move (rad/s). */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
