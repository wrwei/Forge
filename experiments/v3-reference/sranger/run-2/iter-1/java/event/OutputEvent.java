package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events of the SRanger controller: a single combined motor command.
 */
public sealed interface OutputEvent {

    /** Target linear velocity (m/s) and angular velocity (rad/s) for the differential drive. */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
