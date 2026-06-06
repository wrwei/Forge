package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events emitted by the SRanger controller (SR-DM4). The controller emits
 * a single output event type, {@link Move}, a combined linear / angular velocity
 * command to the differential-drive layer.
 */
public sealed interface OutputEvent {

    /** A combined linear ({@code lv}) and angular ({@code av}) velocity command. */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {
    }
}
