package syn20.event;

import syn20.annotation.RoboChartType;

public sealed interface OutputEvent {

    record Move(@RoboChartType("real") double lv, @RoboChartType("real") double av) implements OutputEvent {}
}
