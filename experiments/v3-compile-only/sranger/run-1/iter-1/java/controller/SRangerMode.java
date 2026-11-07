package sranger.controller;

/**
 * Operating modes of the SRanger controller (SR-DM1). Moving is the initial mode.
 */
public enum SRangerMode {
    /** Driving forward at the configured linear velocity (initial mode). */
    MOVING,
    /** Rotating in place at the configured angular velocity. */
    TURNING,
    /** Terminal mode entered on endTask; the robot is stopped. */
    FINAL
}
