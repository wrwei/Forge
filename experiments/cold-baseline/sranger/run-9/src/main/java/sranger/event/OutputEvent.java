package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events from the SRanger controller. The only output is Move(lv, av):
 * a combined linear/angular velocity command to the differential-drive layer.
 */
public sealed interface OutputEvent {
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
