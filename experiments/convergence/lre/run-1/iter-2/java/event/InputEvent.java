package lre.event;

import lre.annotation.RoboChartType;

/**
 * Input events from the operator controller to the LRE (LRE-DM6).
 * Record names deliberately match the RoboChart event names.
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

    /** End of current task. */
    record endTask() implements InputEvent {
    }

    /**
     * Autonomous evaluation tick: carries no operator request. The
     * caller sends tick each control cycle so guard-only transitions
     * are evaluated on an event of their own, keeping their formal
     * contracts independent of the operator events.
     */
    record tick() implements InputEvent {
    }
}
