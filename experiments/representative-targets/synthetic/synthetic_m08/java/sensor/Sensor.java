package syn08.sensor;

import syn08.annotation.RoboChartType;

public final class Sensor {

    @RoboChartType("real")
    private double reading1 = 0.0;

    @RoboChartType("real")
    private double reading2 = 0.0;

    @RoboChartType("real")
    private double reading3 = 0.0;

    @RoboChartType("real")
    private double reading4 = 0.0;

    @RoboChartType("real")
    private double reading5 = 0.0;

    @RoboChartType("real")
    private double reading6 = 0.0;

    @RoboChartType("real")
    private double reading7 = 0.0;

    public void updateReading1(@RoboChartType("real") double value) {
        this.reading1 = value;
    }

    public void updateReading2(@RoboChartType("real") double value) {
        this.reading2 = value;
    }

    public void updateReading3(@RoboChartType("real") double value) {
        this.reading3 = value;
    }

    public void updateReading4(@RoboChartType("real") double value) {
        this.reading4 = value;
    }

    public void updateReading5(@RoboChartType("real") double value) {
        this.reading5 = value;
    }

    public void updateReading6(@RoboChartType("real") double value) {
        this.reading6 = value;
    }

    public void updateReading7(@RoboChartType("real") double value) {
        this.reading7 = value;
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

    @RoboChartType("real")
    public double reading4() {
        return reading4;
    }

    @RoboChartType("real")
    public double reading5() {
        return reading5;
    }

    @RoboChartType("real")
    public double reading6() {
        return reading6;
    }

    @RoboChartType("real")
    public double reading7() {
        return reading7;
    }
}
