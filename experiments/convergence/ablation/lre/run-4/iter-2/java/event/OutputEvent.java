package lre.event;

/**
 * Output events issued by the LRE to the autopilot controller (LRE-DM7).
 * These are the only two outputs the LRE issues.
 */
public sealed interface OutputEvent {

    /** LRE advises a velocity (m/s). */
    record advVel(double value) implements OutputEvent {}

    /** LRE advises a heading (degrees). */
    record advHdng(double value) implements OutputEvent {}
}
