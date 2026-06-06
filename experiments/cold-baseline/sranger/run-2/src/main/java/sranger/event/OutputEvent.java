package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events emitted by the SRanger controller.
 * Requirement: SR-DM4.
 *
 * Move(lv, av) is the only output event variant; it is used for forward
 * motion (Move(moveVel, 0)), turning in place (Move(0, turnVel)), and
 * stopping (Move(0, 0)).
 */
public sealed interface OutputEvent {
    record Move(@RoboChartType("real") double lv,
                @RoboChartType("real") double av) implements OutputEvent {}
}
