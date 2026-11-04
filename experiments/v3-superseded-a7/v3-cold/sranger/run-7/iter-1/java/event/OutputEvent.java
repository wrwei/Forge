package sranger.event;

import sranger.annotation.RoboChartType;

/**
 * Output events emitted by the SRanger controller. Move is the only one.
 */
public sealed interface OutputEvent {

    /**
     * Combined differential-drive command.
     *
     * @param lv target linear velocity, m/s
     * @param av target angular velocity, rad/s
     */
    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
