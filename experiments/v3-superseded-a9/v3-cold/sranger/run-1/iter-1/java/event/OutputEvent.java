package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events of the SRanger controller. Move is the only one.
 */
public sealed interface OutputEvent {

    /**
     * Combined command to the differential-drive layer.
     *
     * @param lv linear velocity, m/s
     * @param av angular velocity, rad/s
     */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
