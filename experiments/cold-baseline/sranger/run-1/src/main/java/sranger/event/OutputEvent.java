package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * SR-DM4: the single output event type emitted by the SRanger controller.
 *
 * Move(lv, av) is used for forward motion (Move(moveVel, 0)), turning
 * (Move(0, turnVel)), and stopping (Move(0, 0)).
 */
public sealed interface OutputEvent {
    record Move(
            @RoboChartType("real") double lv,
            @RoboChartType("real") double av
    ) implements OutputEvent {}
}
