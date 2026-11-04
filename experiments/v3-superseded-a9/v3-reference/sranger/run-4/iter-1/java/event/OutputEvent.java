package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events of the SRanger controller. Move is the only command sent to
 * the differential-drive layer.
 */
public sealed interface OutputEvent {

    /**
     * Combined motor command.
     *
     * @param lv target linear velocity (m/s)
     * @param av target angular velocity (rad/s)
     */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
