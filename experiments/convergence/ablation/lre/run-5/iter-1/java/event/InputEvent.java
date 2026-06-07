package lre.event;

import lre.annotation.RoboChartType;

/**
 * Input events from the operator controller to the LRE.
 */
public sealed interface InputEvent {

    /** Requested velocity in m/s. */
    record reqVel(@RoboChartType("real") double value) implements InputEvent {}

    /** Requested heading in degrees. */
    record reqHdng(@RoboChartType("real") double value) implements InputEvent {}

    /** Request to enter Operator Control Mode. */
    record reqOCM() implements InputEvent {}

    /** Request to enter Main Operating Mode. */
    record reqMOM() implements InputEvent {}

    /** Request to enter High Caution Mode. */
    record reqHCM() implements InputEvent {}

    /** Indicates end of the current task. */
    record endTask() implements InputEvent {}
}
