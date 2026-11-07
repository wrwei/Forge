package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events of the SRanger controller (SR-DM4). The controller emits a
 * single output event, {@code Move}.
 */
public sealed interface OutputEvent {

    /**
     * Combined motor command to the differential-drive layer.
     *
     * @param lv target linear velocity (m/s)
     * @param av target angular velocity (rad/s)
     */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
