package lre.event;

/** Events the operator controller sends to the Last Response Engine. */
public sealed interface InputEvent {

    /** Requests a velocity, in m/s. */
    record reqVel(double value) implements InputEvent {
    }

    /** Requests a heading, in degrees. */
    record reqHdng(double value) implements InputEvent {
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

    /** Indicates the end of the current task. */
    record endTask() implements InputEvent {
    }
}
