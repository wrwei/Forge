package lre.event;

/**
 * LRE -> Autopilot output events (LRE-DM7).
 *
 * <p>The LRE issues ONLY these two output events. There are no other output
 * methods (e.g., no setVelocity). All velocity and heading outputs flow
 * through actuator.receive(new OutputEvent.AdvVel(...)) or
 * actuator.receive(new OutputEvent.AdvHdng(...)).
 *
 * <ul>
 *   <li>AdvVel  -- advised velocity in m/s.</li>
 *   <li>AdvHdng -- advised heading in degrees.</li>
 * </ul>
 */
public sealed interface OutputEvent {
    record AdvVel(double value) implements OutputEvent {}
    record AdvHdng(double value) implements OutputEvent {}
}
