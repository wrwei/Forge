package sranger.event;

import sranger.annotation.RoboChartType;

/** Output events emitted by the SRanger controller. */
public sealed interface OutputEvent {
    /** Combined linear / angular velocity command to the differential drive. */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
