package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events of the SRanger controller (SR-DM4). Move is the only one.
 */
public sealed interface OutputEvent {

    /**
     * Combined command to the differential-drive layer: linear velocity
     * {@code lv} (m/s) and angular velocity {@code av} (rad/s).
     */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
