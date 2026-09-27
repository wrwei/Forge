package lre.event;

import lre.annotation.RoboChartType;

/**
 * Input events the operator controller sends to the LRE (LRE-DM6).
 * {@code reqVel} and {@code reqHdng} carry a payload; the remainder are signals.
 */
public sealed interface InputEvent {

    /** Requested velocity, m/s. */
    record reqVel(@RoboChartType("real") double value) implements InputEvent {
    }

    /** Requested heading, degrees. */
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

    /** End of the current task. */
    record endTask() implements InputEvent {
    }
}
