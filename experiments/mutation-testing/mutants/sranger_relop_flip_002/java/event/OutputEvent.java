package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events emitted by the SRanger controller (SR-DM4).
 *
 * <p>The controller emits a single output event variant, {@link Move}, carrying a
 * combined linear-and-angular velocity command. Used uniformly for forward motion,
 * turning in place, and stopping.</p>
 */
public sealed interface OutputEvent {

    /**
     * Combined linear/angular velocity command for the differential-drive layer.
     *
     * @param lv target linear velocity (m/s)
     * @param av target angular velocity (rad/s)
     */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
