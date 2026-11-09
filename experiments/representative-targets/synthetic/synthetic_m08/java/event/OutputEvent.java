package syn08.event;

import syn08.annotation.RoboChartType;

public sealed interface OutputEvent {

    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
