package lre.event;

/**
 * Output events from the LRE to the autopilot controller (LRE-DM7).
 * These are the only two outputs the LRE issues.
 */
public sealed interface OutputEvent {

    /** Advised velocity, m/s. */
    record AdvVel(double value) implements OutputEvent {}

    /** Advised heading, degrees. */
    record AdvHdng(double value) implements OutputEvent {}
}
