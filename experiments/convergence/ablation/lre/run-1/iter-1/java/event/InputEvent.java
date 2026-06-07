package lre.event;

import lre.annotation.RoboChartType;

/**
 * Input events from the operator controller to the LRE (LRE-DM6).
 * Record names match the event names in the requirements so the
 * extracted RoboChart event channels carry the specified names.
 */
public sealed interface InputEvent {

    /** Requested velocity in m/s. */
    record reqVel(@RoboChartType("real") double value) implements InputEvent {
    }

    /** Requested heading in degrees. */
    record reqHdng(@RoboChartType("real") double value) implements InputEvent {
    }

    /** Requests Operator Control Mode. */
    record reqOCM() implements InputEvent {
    }

    /** Requests Main Operating Mode. */
    record reqMOM() implements InputEvent {
    }

    /** Requests High Caution Mode. */
    record reqHCM() implements InputEvent {
    }

    /** Indicates end of the current task. */
    record endTask() implements InputEvent {
    }
}
