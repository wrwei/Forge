package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events of the SRanger controller (SR-DM4). Move is the only one: it covers forward
 * motion, turning in place and stopping.
 */
public sealed interface OutputEvent {

    /** Combined motor command: linear velocity lv (m/s) and angular velocity av (rad/s). */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
