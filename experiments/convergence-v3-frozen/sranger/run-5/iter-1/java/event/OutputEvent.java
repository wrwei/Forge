package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * The single output event of the SRanger controller (SR-DM4): a combined
 * linear / angular velocity command to the differential-drive layer.
 */
public sealed interface OutputEvent {

    record Move(@RoboChartType("real") double lv,
                @RoboChartType("real") double av) implements OutputEvent {}
}
