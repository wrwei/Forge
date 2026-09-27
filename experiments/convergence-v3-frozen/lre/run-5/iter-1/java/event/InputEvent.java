package lre.event;

import lre.annotation.RoboChartType;

/** Inputs the operator controller sends to the LRE. */
public sealed interface InputEvent {

    record ReqVel(@RoboChartType("real") double value) implements InputEvent {
    }

    record ReqHdng(@RoboChartType("real") double value) implements InputEvent {
    }

    record ReqOCM() implements InputEvent {
    }

    record ReqMOM() implements InputEvent {
    }

    record ReqHCM() implements InputEvent {
    }

    record EndTask() implements InputEvent {
    }
}
