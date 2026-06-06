package lre.event;

import lre.annotation.RoboChartType;

/**
 * Operator -&gt; LRE input events (LRE-DM6).
 * <p>
 * {@code ReqVel} and {@code ReqHdng} carry a real payload; the remaining
 * four are signal events with no payload.
 */
public sealed interface InputEvent {

    record ReqVel(@RoboChartType("real") double value) implements InputEvent {}

    record ReqHdng(@RoboChartType("real") double value) implements InputEvent {}

    record ReqOCM() implements InputEvent {}

    record ReqMOM() implements InputEvent {}

    record ReqHCM() implements InputEvent {}

    record EndTask() implements InputEvent {}
}
