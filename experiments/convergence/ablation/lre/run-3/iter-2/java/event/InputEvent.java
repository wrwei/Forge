package lre.event;

import lre.annotation.RoboChartType;

/**
 * Input events received by the LRE from the operator controller.
 */
public sealed interface InputEvent {

    /** Operator requests a velocity, m/s. */
    record reqVel(@RoboChartType("real") double value) implements InputEvent {}

    /** Operator requests a heading, degrees. */
    record reqHdng(@RoboChartType("real") double value) implements InputEvent {}

    /** Operator requests Operator Control Mode. */
    record reqOCM() implements InputEvent {}

    /** Operator requests Main Operating Mode. */
    record reqMOM() implements InputEvent {}

    /** Operator requests High Caution Mode. */
    record reqHCM() implements InputEvent {}

    /** Operator indicates end of the current task. */
    record endTask() implements InputEvent {}
}
