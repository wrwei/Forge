package sranger.controller;

/**
 * Operating modes of the SRanger controller; the initial mode is first.
 */
public enum SRangerMode {
    /** Initial mode: the robot drives forward. */
    MOVING,
    /** The robot rotates in place for the turn duration. */
    TURNING,
    /** Terminal mode entered on endTask: the robot is stopped. */
    FINAL
}
