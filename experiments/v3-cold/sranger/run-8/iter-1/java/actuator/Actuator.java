package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Differential-drive actuator: receives Move commands and records the last one issued.
 * Before any command it reports zero velocities.
 */
public final class Actuator {

    @RoboChartType("real") private double lastLinearVelocity = 0.0;
    @RoboChartType("real") private double lastAngularVelocity = 0.0;

    /**
     * Receives a Move command from the controller.
     *
     * @param command the command
     */
    public void apply(OutputEvent.Move command) {
        this.lastLinearVelocity = command.lv();
        this.lastAngularVelocity = command.av();
    }

    /**
     * Returns the linear velocity of the last Move command.
     *
     * @return linear velocity, m/s
     */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /**
     * Returns the angular velocity of the last Move command.
     *
     * @return angular velocity, rad/s
     */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
