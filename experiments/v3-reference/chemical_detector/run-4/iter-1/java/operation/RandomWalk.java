package chemical_detector.operation;

/**
 * The Vehicle's randomWalk operation (CD-OP2): an unbounded random-walk search.
 * Records that an unbounded walk was requested.
 */
public final class RandomWalk {

    private boolean unboundedWalk = false;

    public RandomWalk() {
    }

    /** Starts an unbounded random walk. */
    public void compute() {
        this.unboundedWalk = true;
    }

    /** Whether an unbounded walk has been requested. */
    public boolean unboundedWalk() {
        return unboundedWalk;
    }
}
