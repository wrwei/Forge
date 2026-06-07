package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

import lre.annotation.RoboChartType;

/**
 * Immutable register of obstacles indexed by natural number identifiers,
 * modelled as a partial function nat -&gt; Obstacle. Supports lookup by
 * index, iteration over all obstacles, and filtering by static/dynamic
 * status.
 */
public final class ObstacleRegister {

    private final TreeMap<Integer, Obstacle> entries;

    public ObstacleRegister() {
        this.entries = new TreeMap<>();
    }

    private ObstacleRegister(TreeMap<Integer, Obstacle> entries) {
        this.entries = entries;
    }

    /** Returns a copy of this register with the obstacle stored at the given index. */
    public ObstacleRegister with(@RoboChartType("nat") int index, Obstacle obstacle) {
        TreeMap<Integer, Obstacle> copy = new TreeMap<>(this.entries);
        copy.put(Integer.valueOf(index), obstacle);
        return new ObstacleRegister(copy);
    }

    /** Returns a copy of this register without the obstacle at the given index. */
    public ObstacleRegister without(@RoboChartType("nat") int index) {
        TreeMap<Integer, Obstacle> copy = new TreeMap<>(this.entries);
        copy.remove(Integer.valueOf(index));
        return new ObstacleRegister(copy);
    }

    public boolean contains(@RoboChartType("nat") int index) {
        return entries.containsKey(Integer.valueOf(index));
    }

    public Optional<Obstacle> lookup(@RoboChartType("nat") int index) {
        return Optional.ofNullable(entries.get(Integer.valueOf(index)));
    }

    /** All obstacle indices in ascending order. */
    public List<Integer> indices() {
        List<Integer> out = new ArrayList<>();
        for (Integer key : entries.keySet()) {
            out.add(key);
        }
        return out;
    }

    /** Indices of static obstacles in ascending order. */
    public List<Integer> staticIndices() {
        List<Integer> out = new ArrayList<>();
        for (Integer key : entries.keySet()) {
            if (isStatic(entries.get(key))) {
                out.add(key);
            }
        }
        return out;
    }

    /** Indices of dynamic obstacles in ascending order. */
    public List<Integer> dynamicIndices() {
        List<Integer> out = new ArrayList<>();
        for (Integer key : entries.keySet()) {
            if (!isStatic(entries.get(key))) {
                out.add(key);
            }
        }
        return out;
    }

    private static boolean isStatic(Obstacle obstacle) {
        return obstacle.obsNsVel() == 0.0 && obstacle.obsEwVel() == 0.0;
    }
}
