package lre.sensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lre.annotation.RoboChartType;

/**
 * Stores a collection of obstacles indexed by natural-number identifiers
 * (LRE-DM3). Modelled as a partial function nat -&gt; Obstacle, internally
 * backed by a HashMap. Copy-on-write semantics are used for mutating
 * operations so the register can be treated as immutable downstream.
 */
public final class ObstacleRegister {

    private final Map<Integer, Obstacle> obstacles;

    public ObstacleRegister() {
        this.obstacles = new HashMap<>();
    }

    public ObstacleRegister(Map<Integer, Obstacle> initial) {
        this.obstacles = new HashMap<>(initial);
    }

    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        this.obstacles.put(index, obstacle);
    }

    public void remove(@RoboChartType("nat") int index) {
        this.obstacles.remove(index);
    }

    public void clear() {
        this.obstacles.clear();
    }

    public boolean contains(@RoboChartType("nat") int index) {
        return obstacles.containsKey(index);
    }

    /**
     * Returns the obstacle at the given index, or null if absent.
     */
    public Obstacle get(@RoboChartType("nat") int index) {
        return obstacles.get(index);
    }

    public List<Integer> indices() {
        return new ArrayList<>(obstacles.keySet());
    }

    public List<Integer> staticIndices() {
        List<Integer> out = new ArrayList<>();
        for (Integer idx : obstacles.keySet()) {
            Obstacle o = obstacles.get(idx);
            if (o.isStatic()) {
                out.add(idx);
            }
        }
        return out;
    }

    public List<Integer> dynamicIndices() {
        List<Integer> out = new ArrayList<>();
        for (Integer idx : obstacles.keySet()) {
            Obstacle o = obstacles.get(idx);
            if (o.isDynamic()) {
                out.add(idx);
            }
        }
        return out;
    }

    @RoboChartType("nat")
    public int size() {
        return obstacles.size();
    }
}
