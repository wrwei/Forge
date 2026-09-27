package sranger.mode;

/** The three operating modes of the SRanger controller (SR-DM1). */
public enum SRangerMode {
    /** Initial mode: the robot drives forward at the configured linear velocity. */
    MOVING,
    /** The robot rotates in place at the configured angular velocity. */
    TURNING,
    /** Terminal mode entered on endTask; the robot is stopped. */
    FINAL
}
