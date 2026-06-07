package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events emitted by the SRanger controller. Move is the only
 * output: a combined linear / angular velocity command to the
 * differential-drive layer.
 */
public sealed interface OutputEvent {

    /** Motor command: target linear velocity (m/s) and angular velocity (rad/s). */
    record Move(@RoboChartType("real") double lv,
                @RoboChartType("real") double av) implements OutputEvent {
    }
}
