package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events emitted by the SRanger controller.
 */
public sealed interface OutputEvent {

    /** Combined differential-drive command: linear velocity lv and angular velocity av. */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
