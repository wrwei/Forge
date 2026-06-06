package lre.event;

/**
 * LRE -> autopilot output events. See LRE-DM7.
 */
public sealed interface OutputEvent {
    record AdvVel(double value) implements OutputEvent {}
    record AdvHdng(double value) implements OutputEvent {}
}
