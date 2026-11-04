package sranger.controller;

/**
 * Operating modes of the SRanger controller; the initial mode comes first.
 */
public enum SRangerMode {
    /** Initial mode: the robot drives forward at moveVel. */
    MOVING,
    /** The robot rotates in place at turnVel for turnDuration seconds. */
    TURNING,
    /** Terminal mode entered on endTask: the robot is stopped. */
    FINAL
}
