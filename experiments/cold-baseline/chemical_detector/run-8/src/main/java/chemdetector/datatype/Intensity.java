package chemdetector.datatype;

import chemdetector.annotation.RoboChartType;

public record Intensity(@RoboChartType("real") double value) {
    public static boolean goreq(Intensity a, Intensity b) {
        return a.value() >= b.value();
    }
}
