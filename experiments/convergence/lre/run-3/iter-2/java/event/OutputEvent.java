package lre.event;

/**
 * Output events from the LRE to the autopilot controller (LRE-DM7).
 * These are the ONLY two outputs the LRE issues.
 * Record names match the RoboChart event names verbatim.
 */
public sealed interface OutputEvent {

    /** Advised velocity in m/s. */
    record advVel(double value) implements OutputEvent {
    }

    /** Advised heading in degrees. */
    record advHdng(double value) implements OutputEvent {
    }
}
