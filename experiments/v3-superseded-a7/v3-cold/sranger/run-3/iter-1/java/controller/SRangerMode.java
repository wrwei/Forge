package sranger.controller;

/**
 * Operating modes of the SRanger controller; the initial mode first.
 */
public enum SRangerMode {
    /** Driving forward at the configured linear velocity. */
    MOVING,
    /** Rotating in place at the configured angular velocity. */
    TURNING,
    /** Terminal mode after endTask; the robot is stopped. */
    FINAL
}
