package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events emitted by the SRanger controller (SR-DM4).
 * The only output event is Move(lv, av) — a combined linear/angular velocity command.
 */
public sealed interface OutputEvent {
    record Move(@RoboChartType("real") double lv,
                @RoboChartType("real") double av) implements OutputEvent {}
}
