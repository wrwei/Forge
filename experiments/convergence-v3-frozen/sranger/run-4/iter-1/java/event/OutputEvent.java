package sranger.event;

import sranger.annotation.RoboChartType;

/** Output events emitted by the SRanger controller (SR-DM4). */
public sealed interface OutputEvent {

    /** Combined linear / angular velocity command to the differential-drive layer. */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av)
            implements OutputEvent {}
}
