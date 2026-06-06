package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events emitted by the SRanger controller to the differential-drive
 * layer (SR-DM4). The controller emits a single output event type:
 * {@link Move} — a combined linear/angular velocity command. The same event
 * expresses forward motion (Move(moveVel, 0)), turning in place
 * (Move(0, turnVel)), and stopping (Move(0, 0)).
 */
public sealed interface OutputEvent {
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av)
            implements OutputEvent {}
}
