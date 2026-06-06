package lre.event;

/**
 * Input events from the operator controller to the LRE (LRE-DM6).
 * Record names match the RoboChart event names verbatim.
 */
public sealed interface InputEvent {

    /** Requested velocity in m/s. */
    record reqVel(double value) implements InputEvent {
    }

    /** Requested heading in degrees. */
    record reqHdng(double value) implements InputEvent {
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
