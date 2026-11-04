package lre.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import lre.annotation.RoboChartType;

/**
 * Obstacles indexed by natural numbers: a partial function from nat to {@link Obstacle}.
 * Index {@link #NO_OBSTACLE} (0) stands for "no obstacle" and is never registered, so
 * obstacles are registered at 1, 2, ...
 */
public final class ObstacleRegister {

    /** The index that means "no obstacle"; it is never registered. */
    @RoboChartType("nat") public static final int NO_OBSTACLE = 0;

    private final List<Integer> keys = new ArrayList<>();
    private final List<Obstacle> entries = new ArrayList<>();

    /** Registers (or replaces) the obstacle at an index of at least 1. */
    public void put(@RoboChartType("nat") int index, Obstacle obstacle) {
        if (index <= NO_OBSTACLE) {
            throw new IllegalArgumentException("obstacle index must be at least 1: " + index);
        }
        Objects.requireNonNull(obstacle, "obstacle");
        int position = positionOf(index);
        if (position < keys.size()) {
            entries.set(position, obstacle);
        } else {
            keys.add(index);
            entries.add(obstacle);
        }
    }

    /** Removes the obstacle at an index, if there is one. */
    public void remove(@RoboChartType("nat") int index) {
        int position = positionOf(index);
        if (position < keys.size()) {
            keys.remove(position);
            entries.remove(position);
        }
    }

    /** True when an obstacle is registered at the index. */
    public boolean contains(@RoboChartType("nat") int index) {
        return positionOf(index) < keys.size();
    }

    /** The obstacle at the index; the index must be registered. */
    public Obstacle get(@RoboChartType("nat") int index) {
        int position = positionOf(index);
        if (position >= keys.size()) {
            throw new NoSuchElementException("no obstacle at index " + index);
        }
        return entries.get(position);
    }

    /** The indices of all registered obstacles. */
    public List<Integer> indices() {
        return new ArrayList<>(keys);
    }

    /** The indices of the static obstacles. */
    public List<Integer> staticIndices() {
        List<Integer> result = new ArrayList<>();
        for (int position = 0; position < keys.size(); position++) {
            if (entries.get(position).isStatic()) {
                result.add(keys.get(position));
            }
        }
        return result;
    }

    /** The indices of the dynamic obstacles. */
    public List<Integer> dynamicIndices() {
        List<Integer> result = new ArrayList<>();
        for (int position = 0; position < keys.size(); position++) {
            if (!entries.get(position).isStatic()) {
                result.add(keys.get(position));
            }
        }
        return result;
    }

    /** Position of the index in {@code keys}, or {@code keys.size()} when it is not registered. */
    private int positionOf(int index) {
        for (int position = 0; position < keys.size(); position++) {
            if (keys.get(position) == index) {
                return position;
            }
        }
        return keys.size();
    }
}
