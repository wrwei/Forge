package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * SR-DM4: The SRanger controller emits a single output event type
 * Move(lv : real, av : real) — combined linear and angular velocity
 * command to the differential-drive layer.
 */
public sealed interface OutputEvent {
    record Move(@RoboChartType("real") double lv,
                @RoboChartType("real") double av) implements OutputEvent {}
}
