package syn12.event;

import syn12.annotation.RoboChartType;

public sealed interface OutputEvent {

    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
