package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events emitted by the SRanger controller.
 * Move(lv, av) carries a target linear and angular velocity command.
 */
public sealed interface OutputEvent {
    record Move(@RoboChartType("real") double lv,
                @RoboChartType("real") double av) implements OutputEvent {}
}
