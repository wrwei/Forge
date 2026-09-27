package lre.event;

import lre.annotation.RoboChartType;

/** Events the operator controller sends to the LRE (LRE-DM6). */
public sealed interface InputEvent {

    /** Requested velocity in m/s. */
    record ReqVel(@RoboChartType("real") double value) implements InputEvent {
    }

    /** Requested heading in degrees. */
    record ReqHdng(@RoboChartType("real") double value) implements InputEvent {
    }

    /** Request Operator Control Mode. */
    record ReqOCM() implements InputEvent {
    }

    /** Request Main Operating Mode. */
    record ReqMOM() implements InputEvent {
    }

    /** Request High Caution Mode. */
    record ReqHCM() implements InputEvent {
    }

    /** End of the current task. */
    record EndTask() implements InputEvent {
    }
}
