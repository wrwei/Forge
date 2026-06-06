package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events emitted by the SRanger controller.
 * SR-DM4: Move(lv, av) is the only output event.
 */
public sealed interface OutputEvent {
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
