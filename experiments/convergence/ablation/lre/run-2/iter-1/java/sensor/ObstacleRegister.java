package lre.sensor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lre.annotation.RoboChartType;

/**
 * An immutable collection of obstacles indexed by natural-number
 * identifiers, modelled as a partial function nat -&gt; Obstacle
 * (LRE-DM3). Copy-on-write updates preserve immutability.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    private ObstacleRegister(Map<Integer, Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    public static ObstacleRegister empty() {
        return new ObstacleRegister(new LinkedHashMap<Integer, Obstacle>());
    }

    public ObstacleRegister with(@RoboChartType("nat") int index, Obstacle obstacle) {
        Map<Integer, Obstacle> copy = new LinkedHashMap<Integer, Obstacle>(obstacles);
        copy.put(index, obstacle);
        return new ObstacleRegister(copy);
    }

    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    public Obstacle lookup(@RoboChartType("nat") int index) {
        return obstacles.get(index);
    }

    public List<Integer> indices() {
        List<Integer> out = new ArrayList<Integer>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            out.add(entry.getKey());
        }
        return out;
    }

    public List<Integer> staticIndices() {
        List<Integer> out = new ArrayList<Integer>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (entry.getValue().isStatic()) {
                out.add(entry.getKey());
            }
        }
        return out;
    }

    public List<Integer> dynamicIndices() {
        List<Integer> out = new ArrayList<Integer>();
        for (Map.Entry<Integer, Obstacle> entry : obstacles.entrySet()) {
            if (entry.getValue().isDynamic()) {
                out.add(entry.getKey());
            }
        }
        return out;
    }
}
