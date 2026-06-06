package lre.event;

/**
 * Output events from the LRE to the autopilot controller (LRE-DM7).
 * advVel and advHdng are the only two outputs the LRE issues.
 */
public sealed interface OutputEvent {

    /** Advised velocity in m/s. */
    record advVel(double value) implements OutputEvent {
    }

    /** Advised heading in degrees. */
    record advHdng(double value) implements OutputEvent {
    }
}
