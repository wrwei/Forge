package lre.sensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lre.annotation.RoboChartType;

/**
 * Stores obstacles indexed by natural-number identifier (LRE-DM3).
 * Modelled as a partial function nat -> Obstacle.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new HashMap<>();
    }

    public ObstacleRegister(Map<Integer, Obstacle> obstacles) {
        this.obstacles = new HashMap<>(obstacles);
    }

    public void put(@RoboChartType("nat") int index, Obstacle obs) {
        this.obstacles.put(index, obs);
    }

    public void remove(@RoboChartType("nat") int index) {
        this.obstacles.remove(index);
    }

    public boolean contains(@RoboChartType("nat") int index) {
        return this.obstacles.containsKey(index);
    }

    public Obstacle get(@RoboChartType("nat") int index) {
        return this.obstacles.get(index);
    }

    public List<Integer> indices() {
        return new ArrayList<>(this.obstacles.keySet());
    }

    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (Integer idx : this.obstacles.keySet()) {
            Obstacle o = this.obstacles.get(idx);
            if (o.isStatic()) {
                result.add(idx);
            }
        }
        return result;
    }

    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (Integer idx : this.obstacles.keySet()) {
            Obstacle o = this.obstacles.get(idx);
            if (o.isDynamic()) {
                result.add(idx);
            }
        }
        return result;
    }

    public int size() {
        return this.obstacles.size();
    }
}
