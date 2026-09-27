package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * The single output event of the SRanger controller (SR-DM4).
 */
public sealed interface OutputEvent {

    /** Combined differential-drive command: linear and angular velocity. */
    record Move(@RoboChartType("real") double lv,
                @RoboChartType("real") double av) implements OutputEvent {}
}
