package sranger.controller;

/**
 * Operating modes of the SRanger controller; the initial mode comes first.
 */
public enum SRangerMode {
    /** Initial mode: drive forward at the configured linear velocity. */
    MOVING,
    /** Rotate in place at the configured angular velocity. */
    TURNING,
    /** Terminal mode entered on endTask: full stop. */
    FINAL
}
