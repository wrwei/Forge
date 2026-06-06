package lre.event;

/**
 * LRE-DM7: LRE -> autopilot output events.
 * Only AdvVel and AdvHdng are permitted.
 */
public sealed interface OutputEvent {
    record AdvVel(double value) implements OutputEvent {}
    record AdvHdng(double value) implements OutputEvent {}
}
