package lre.sensor;

import lre.annotation.RoboChartType;
import lre.data.Obstacle;
import lre.data.ObstacleRegister;

public final class Sensor {

    @RoboChartType("real")
    private double depth;

    @RoboChartType("real")
    private double nsVel;

    @RoboChartType("real")
    private double ewVel;

    @RoboChartType("real")
    private double rateOfClimb;

    private final ObstacleRegister obstacleRegister;

    public Sensor(ObstacleRegister obstacleRegister) {
        this.obstacleRegister = obstacleRegister;
    }

    public void update(@RoboChartType("real") double depth,
                       @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel,
                       @RoboChartType("real") double rateOfClimb) {
        this.depth = depth;
        this.nsVel = nsVel;
        this.ewVel = ewVel;
        this.rateOfClimb = rateOfClimb;
    }

    @RoboChartType("real")
    public double depth() {
        return depth;
    }

    @RoboChartType("real")
    public double nsVel() {
        return nsVel;
    }

    @RoboChartType("real")
    public double ewVel() {
        return ewVel;
    }

    @RoboChartType("real")
    public double rateOfClimb() {
        return rateOfClimb;
    }

    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        var opt = obstacleRegister.get(index);
        if (opt.isEmpty()) {
            return Double.MAX_VALUE;
        }
        Obstacle obs = opt.get();
        return Math.sqrt(obs.nsRelDist() * obs.nsRelDist() + obs.ewRelDist() * obs.ewRelDist());
    }

    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        var opt = obstacleRegister.get(index);
        if (opt.isEmpty()) {
            return Double.MAX_VALUE;
        }
        Obstacle obs = opt.get();
        return Math.abs(depth - obs.obsDepth());
    }

    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return Double.MAX_VALUE;
        }
        double h = hdist(index);
        double v = vdist(index);
        return Math.sqrt(h * h + v * v);
    }

    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        var opt = obstacleRegister.get(index);
        if (opt.isEmpty()) {
            return 0.0;
        }
        return opt.get().nsRelDist();
    }

    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        var opt = obstacleRegister.get(index);
        if (opt.isEmpty()) {
            return 0.0;
        }
        return opt.get().ewRelDist();
    }

    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        var opt = obstacleRegister.get(index);
        if (opt.isEmpty()) {
            return 0.0;
        }
        return opt.get().obsNsVel();
    }

    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (index == -1) {
            return 0.0;
        }
        var opt = obstacleRegister.get(index);
        if (opt.isEmpty()) {
            return 0.0;
        }
        return opt.get().obsEwVel();
    }

    @RoboChartType("nat")
    public int closestStaticIndex() {
        int closest = -1;
        double minDist = Double.MAX_VALUE;
        for (var entry : obstacleRegister.staticObstacles().entrySet()) {
            double dist = odist(entry.getKey());
            if (dist < minDist) {
                minDist = dist;
                closest = entry.getKey();
            }
        }
        return closest;
    }

    @RoboChartType("nat")
    public int closestDynamicIndex() {
        int closest = -1;
        double minDist = Double.MAX_VALUE;
        for (var entry : obstacleRegister.dynamicObstacles().entrySet()) {
            double dist = odist(entry.getKey());
            if (dist < minDist) {
                minDist = dist;
                closest = entry.getKey();
            }
        }
        return closest;
    }
}
