package lre.event;

import lre.annotation.RoboChartType;

/**
 * Events from the operator controller to the LRE (LRE-DM6).
 */
public sealed interface InputEvent {

    /** Requested velocity (m/s). */
    record reqVel(@RoboChartType("real") double value) implements InputEvent {
    }

    /** Requested heading (degrees). */
    record reqHdng(@RoboChartType("real") double value) implements InputEvent {
    }

    /** Request for Operator Control Mode. */
    record reqOCM() implements InputEvent {
    }

    /** Request for Main Operating Mode. */
    record reqMOM() implements InputEvent {
    }

    /** Request for High Caution Mode. */
    record reqHCM() implements InputEvent {
    }

    /** End of the current task. */
    record endTask() implements InputEvent {
    }
}
