package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/** The differential-drive actuator: records the last motor command issued. */
public final class Actuator {

    // Stationary until the controller issues its first command.
    private OutputEvent.Move lastMove = new OutputEvent.Move(0.0, 0.0);

    /** Receives an output command from the controller. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) command;
            this.lastMove = move;
        }
    }

    /** The last Move command issued. */
    public OutputEvent.Move lastMove() {
        return lastMove;
    }

    /** The last commanded linear velocity (m/s). */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastMove.lv();
    }

    /** The last commanded angular velocity (rad/s). */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastMove.av();
    }
}
