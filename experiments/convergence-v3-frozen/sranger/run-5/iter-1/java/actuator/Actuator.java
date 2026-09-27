package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Differential-drive output interface (SR-DM6). Receives Move(lv, av)
 * commands from the controller and retains the last one for inspection.
 */
public final class Actuator {

    private OutputEvent.Move lastCommand = new OutputEvent.Move(0.0, 0.0);

    /** Issue a combined linear / angular velocity command (SR-DM4). */
    public void move(@RoboChartType("real") double lv, @RoboChartType("real") double av) {
        this.lastCommand = new OutputEvent.Move(lv, av);
    }

    /** The last Move command issued. */
    public OutputEvent.Move lastCommand() {
        return lastCommand;
    }

    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastCommand.lv();
    }

    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastCommand.av();
    }
}
