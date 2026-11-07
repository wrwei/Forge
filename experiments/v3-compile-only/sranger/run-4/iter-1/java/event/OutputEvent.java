package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events of the SRanger controller (SR-DM4). Move is the only output:
 * a combined linear / angular velocity command to the differential-drive layer.
 */
public sealed interface OutputEvent {

    /**
     * Motor command.
     *
     * @param lv target linear velocity (m/s)
     * @param av target angular velocity (rad/s)
     */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
