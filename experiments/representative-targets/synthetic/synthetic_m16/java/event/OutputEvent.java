package syn16.event;

import syn16.annotation.RoboChartType;

public sealed interface OutputEvent {

    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
