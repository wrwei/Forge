package chemical_detector.operation;

/**
 * The Vehicle's shortRandomWalk operation (CD-OP3): a random walk that runs for a
 * bounded period. Records that a bounded walk was requested.
 */
public final class ShortRandomWalk {

    private boolean boundedWalk = false;

    public ShortRandomWalk() {
    }

    /** Starts a bounded random walk. */
    public void compute() {
        this.boundedWalk = true;
    }

    /** Whether a bounded walk has been requested. */
    public boolean boundedWalk() {
        return boundedWalk;
    }
}
