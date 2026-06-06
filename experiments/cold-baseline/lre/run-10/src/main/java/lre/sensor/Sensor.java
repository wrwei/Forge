package lre.sensor;

import java.util.List;

import lre.annotation.RoboChartType;

public final class Sensor {

    @RoboChartType("real")
    private double depth;

    @RoboChartType("real")
    private double ns_vel;

    @RoboChartType("real")
    private double ew_vel;

    @RoboChartType("real")
    private double rate_of_climb;

    private ObstacleRegister register;

    public Sensor() {
        this.depth = 0.0;
        this.ns_vel = 0.0;
        this.ew_vel = 0.0;
        this.rate_of_climb = 0.0;
        this.register = new ObstacleRegister();
    }

    public void update(@RoboChartType("real") double depth,
                       @RoboChartType("real") double nsVel,
                       @RoboChartType("real") double ewVel,
                       @RoboChartType("real") double rateOfClimb,
                       ObstacleRegister register) {
        this.depth = depth;
        this.ns_vel = nsVel;
        this.ew_vel = ewVel;
        this.rate_of_climb = rateOfClimb;
        this.register = register;
    }

    @RoboChartType("real")
    public double depth() { return depth; }

    @RoboChartType("real")
    public double ns_vel() { return ns_vel; }

    @RoboChartType("real")
    public double ew_vel() { return ew_vel; }

    @RoboChartType("real")
    public double rate_of_climb() { return rate_of_climb; }

    public ObstacleRegister register() { return register; }

    @RoboChartType("real")
    public double hdist(@RoboChartType("nat") int index) {
        if (index == -1 || !register.contains(index)) {
            return Double.MAX_VALUE;
        }
        Obstacle o = register.get(index);
        return Math.sqrt(o.ns_rel_dist() * o.ns_rel_dist() + o.ew_rel_dist() * o.ew_rel_dist());
    }

    @RoboChartType("real")
    public double vdist(@RoboChartType("nat") int index) {
        if (index == -1 || !register.contains(index)) {
            return Double.MAX_VALUE;
        }
        Obstacle o = register.get(index);
        return Math.abs(depth - o.obs_depth());
    }

    @RoboChartType("real")
    public double odist(@RoboChartType("nat") int index) {
        if (index == -1 || !register.contains(index)) {
            return Double.MAX_VALUE;
        }
        return Math.sqrt(hdist(index) * hdist(index) + vdist(index) * vdist(index));
    }

    @RoboChartType("real")
    public double nsRelDist(@RoboChartType("nat") int index) {
        if (index == -1 || !register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ns_rel_dist();
    }

    @RoboChartType("real")
    public double ewRelDist(@RoboChartType("nat") int index) {
        if (index == -1 || !register.contains(index)) {
            return 0.0;
        }
        return register.get(index).ew_rel_dist();
    }

    @RoboChartType("real")
    public double obsNsVel(@RoboChartType("nat") int index) {
        if (index == -1 || !register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ns_vel();
    }

    @RoboChartType("real")
    public double obsEwVel(@RoboChartType("nat") int index) {
        if (index == -1 || !register.contains(index)) {
            return 0.0;
        }
        return register.get(index).obs_ew_vel();
    }

    @RoboChartType("nat")
    public int closestStaticIndex() {
        List<Integer> ids = register.staticIndices();
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < ids.size(); i++) {
            int idx = ids.get(i);
            double d = odist(idx);
            if (d < bestDist) {
                bestDist = d;
                best = idx;
            }
        }
        return best;
    }

    @RoboChartType("nat")
    public int closestDynamicIndex() {
        List<Integer> ids = register.dynamicIndices();
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < ids.size(); i++) {
            int idx = ids.get(i);
            double d = odist(idx);
            if (d < bestDist) {
                bestDist = d;
                best = idx;
            }
        }
        return best;
    }
}
