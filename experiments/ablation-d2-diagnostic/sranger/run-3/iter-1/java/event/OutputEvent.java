package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events emitted by the SRanger controller.
 */
public sealed interface OutputEvent {
    /** Combined motor command: linear velocity lv (m/s) and angular velocity av (rad/s). */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
