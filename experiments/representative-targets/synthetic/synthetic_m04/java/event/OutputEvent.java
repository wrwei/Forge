package syn04.event;

import syn04.annotation.RoboChartType;

public sealed interface OutputEvent {

    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
