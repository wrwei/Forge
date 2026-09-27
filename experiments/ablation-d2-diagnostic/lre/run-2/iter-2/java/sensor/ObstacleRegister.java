package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * A partial function from natural-number identifiers to obstacles (LRE-DM3).
 * The register is immutable; {@link #with} returns an updated copy.
 */
public final class ObstacleRegister {

    private static final Obstacle ABSENT = new Obstacle(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

    private final int[] indices;
    private final Obstacle[] obstacles;

    /** An empty register. */
    public ObstacleRegister() {
        this.indices = new int[0];
        this.obstacles = new Obstacle[0];
    }

    private ObstacleRegister(int[] indices, Obstacle[] obstacles) {
        this.indices = indices;
        this.obstacles = obstacles;
    }

    /** A copy of this register in which {@code index} maps to {@code obstacle}. */
    public ObstacleRegister with(@RoboChartType("nat") int index, Obstacle obstacle) {
        var position = positionOf(index);
        if (position >= 0) {
            var newObstacles = new Obstacle[obstacles.length];
            for (var i = 0; i < obstacles.length; i++) {
                newObstacles[i] = obstacles[i];
            }
            newObstacles[position] = obstacle;
            return new ObstacleRegister(indices, newObstacles);
        }
        var newIndices = new int[indices.length + 1];
        var newObstacles = new Obstacle[obstacles.length + 1];
        for (var i = 0; i < indices.length; i++) {
            newIndices[i] = indices[i];
            newObstacles[i] = obstacles[i];
        }
        newIndices[indices.length] = index;
        newObstacles[obstacles.length] = obstacle;
        return new ObstacleRegister(newIndices, newObstacles);
    }

    /** The number of obstacles in the register. */
    @RoboChartType("nat")
    public int size() {
        return indices.length;
    }

    /** The identifier stored at the given iteration position. */
    @RoboChartType("nat")
    public int indexAt(@RoboChartType("nat") int position) {
        return indices[position];
    }

    /** The obstacle stored at the given iteration position. */
    public Obstacle obstacleAt(@RoboChartType("nat") int position) {
        return obstacles[position];
    }

    /** True when the register maps the given identifier. */
    public boolean contains(int index) {
        return positionOf(index) >= 0;
    }

    /**
     * The obstacle mapped to the given identifier, or an all-zero obstacle
     * when the identifier is not in the domain of the register.
     */
    public Obstacle lookup(int index) {
        var position = positionOf(index);
        if (position < 0) {
            return ABSENT;
        }
        return obstacles[position];
    }

    /** True when the identifier is mapped and the obstacle it maps to is static. */
    public boolean isStaticAt(int index) {
        var position = positionOf(index);
        if (position < 0) {
            return false;
        }
        return obstacles[position].isStatic();
    }

    /** True when the identifier is mapped and the obstacle it maps to is dynamic. */
    public boolean isDynamicAt(int index) {
        var position = positionOf(index);
        if (position < 0) {
            return false;
        }
        return !obstacles[position].isStatic();
    }

    private int positionOf(int index) {
        for (var i = 0; i < indices.length; i++) {
            if (indices[i] == index) {
                return i;
            }
        }
        return -1;
    }
}
