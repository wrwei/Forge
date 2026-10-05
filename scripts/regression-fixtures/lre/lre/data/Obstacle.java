package lre.data;

import lre.annotation.RoboChartType;

public record Obstacle(
    @RoboChartType("real") double nsRelDist,
    @RoboChartType("real") double ewRelDist,
    @RoboChartType("real") double obsDepth,
    @RoboChartType("real") double obsNsVel,
    @RoboChartType("real") double obsEwVel,
    @RoboChartType("real") double obsRoc
) {
    public boolean isStatic() {
        return obsNsVel == 0.0 && obsEwVel == 0.0;
    }

    public boolean isDynamic() {
        return !isStatic();
    }
}
