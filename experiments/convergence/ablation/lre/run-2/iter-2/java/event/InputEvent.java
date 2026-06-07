package lre.event;

import lre.annotation.RoboChartType;

/**
 * Input events from the operator controller to the LRE (LRE-DM6).
 */
public sealed interface InputEvent {

    /** Requested velocity in m/s. */
    record reqVel(@RoboChartType("real") double value) implements InputEvent {
    }

    /** Requested heading in degrees. */
    record reqHdng(@RoboChartType("real") double value) implements InputEvent {
    }

    /** Request Operator Control Mode. */
    record reqOCM() implements InputEvent {
    }

    /** Request Main Operating Mode. */
    record reqMOM() implements InputEvent {
    }

    /** Request High Caution Mode. */
    record reqHCM() implements InputEvent {
    }

    /** End of current task. */
    record endTask() implements InputEvent {
    }
}
