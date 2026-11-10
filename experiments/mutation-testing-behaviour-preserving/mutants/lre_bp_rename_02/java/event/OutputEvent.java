package lre.event;

import lre.annotation.RoboChartType;

/**
 * Output events from the LRE to the autopilot controller.
 *
 * <ul>
 *   <li>{@link AdvVel} — advised velocity (m/s).</li>
 *   <li>{@link AdvHdng} — advised heading (degrees).</li>
 * </ul>
 */
public sealed interface OutputEvent {
    record AdvVel(@RoboChartType("real") double value) implements OutputEvent {}
    record AdvHdng(@RoboChartType("real") double value) implements OutputEvent {}
}
