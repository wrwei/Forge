package chemical_detector.operation;

/**
 * The Vehicle's randomWalk operation (CD-OP2): requests an unbounded random-walk
 * search, which continues until a move command supersedes it.
 */
public final class RandomWalk {

    private boolean unboundedWalkRequested = false;

    /** Requests the unbounded random walk. */
    public void compute() {
        this.unboundedWalkRequested = true;
    }

    /** Whether the unbounded random walk has been requested. */
    public boolean unboundedWalkRequested() {
        return unboundedWalkRequested;
    }
}
