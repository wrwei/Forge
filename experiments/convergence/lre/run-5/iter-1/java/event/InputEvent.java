package lre.event;

import lre.annotation.RoboChartType;

/**
 * Input events from the operator controller to the LRE (LRE-DM6).
 */
public sealed interface InputEvent {

    record reqVel(@RoboChartType("real") double value) implements InputEvent {
    }

    record reqHdng(@RoboChartType("real") double value) implements InputEvent {
    }

    record reqOCM() implements InputEvent {
    }

    record reqMOM() implements InputEvent {
    }

    record reqHCM() implements InputEvent {
    }

    record endTask() implements InputEvent {
    }
}
