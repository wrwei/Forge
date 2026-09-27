package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events emitted by the SRanger controller to the differential-drive layer.
 */
public sealed interface OutputEvent {

    /** Combined motor command: linear velocity lv and angular velocity av. */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
