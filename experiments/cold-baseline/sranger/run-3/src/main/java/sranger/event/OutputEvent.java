package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events for the SRanger controller (SR-DM4).
 * Move(lv, av) is the only output, used for forward motion,
 * turning in place, and stopping.
 */
public sealed interface OutputEvent {
    record Move(@RoboChartType("real") double lv,
                @RoboChartType("real") double av) implements OutputEvent {}
}
