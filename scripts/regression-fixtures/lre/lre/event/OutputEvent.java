package lre.event;

import lre.annotation.RoboChartType;

public sealed interface OutputEvent {
    record AdvVel(@RoboChartType("real") double value) implements OutputEvent {}
    record AdvHdng(@RoboChartType("real") double value) implements OutputEvent {}
}
