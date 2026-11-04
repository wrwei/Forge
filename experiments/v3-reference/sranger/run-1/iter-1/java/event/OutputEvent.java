package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events the SRanger controller issues to the differential-drive layer.
 * Move is the only one.
 */
public sealed interface OutputEvent {

    /**
     * Combined motor command: linear velocity {@code lv} (m/s) and angular
     * velocity {@code av} (rad/s).
     */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
