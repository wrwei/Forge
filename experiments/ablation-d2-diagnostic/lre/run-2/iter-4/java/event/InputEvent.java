package lre.event;

import lre.annotation.RoboChartType;

/** Events the operator controller sends to the LRE (LRE-DM6). */
public sealed interface InputEvent {

    /** Requested velocity, in m/s. */
    record reqVel(@RoboChartType("real") double value) implements InputEvent {}

    /** Requested heading, in degrees. */
    record reqHdng(@RoboChartType("real") double value) implements InputEvent {}

    /** Requests Operator Control Mode. */
    record reqOCM() implements InputEvent {}

    /** Requests Main Operating Mode. */
    record reqMOM() implements InputEvent {}

    /** Requests High Caution Mode. */
    record reqHCM() implements InputEvent {}

    /** Indicates the end of the current task. */
    record endTask() implements InputEvent {}
}
