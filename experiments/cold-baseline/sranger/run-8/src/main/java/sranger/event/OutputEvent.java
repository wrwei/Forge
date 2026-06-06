package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events emitted by the SRanger controller.
 *
 * The controller emits a single output event type, {@link Move},
 * representing a combined linear-and-angular velocity command to
 * the differential-drive layer.
 */
public sealed interface OutputEvent {
    record Move(@RoboChartType("real") double lv,
                @RoboChartType("real") double av) implements OutputEvent {}
}
