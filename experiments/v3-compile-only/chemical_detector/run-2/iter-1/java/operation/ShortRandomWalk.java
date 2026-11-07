package chemical_detector.operation;

/**
 * The Vehicle's shortRandomWalk operation (CD-OP3): requests a random walk that
 * runs for a bounded period, used to get out when stuck.
 */
public final class ShortRandomWalk {

    private boolean boundedWalkRequested = false;

    /** Requests the bounded random walk. */
    public void compute() {
        this.boundedWalkRequested = true;
    }

    /** Whether the bounded random walk has been requested. */
    public boolean boundedWalkRequested() {
        return boundedWalkRequested;
    }
}
