package syn04.sensor;

import syn04.annotation.RoboChartType;

public final class Sensor {

    @RoboChartType("real")
    private double reading1 = 0.0;

    @RoboChartType("real")
    private double reading2 = 0.0;

    @RoboChartType("real")
    private double reading3 = 0.0;

    public void updateReading1(@RoboChartType("real") double value) {
        this.reading1 = value;
    }

    public void updateReading2(@RoboChartType("real") double value) {
        this.reading2 = value;
    }

    public void updateReading3(@RoboChartType("real") double value) {
        this.reading3 = value;
    }

    @RoboChartType("real")
    public double reading1() {
        return reading1;
    }

    @RoboChartType("real")
    public double reading2() {
        return reading2;
    }

    @RoboChartType("real")
    public double reading3() {
        return reading3;
    }
}
