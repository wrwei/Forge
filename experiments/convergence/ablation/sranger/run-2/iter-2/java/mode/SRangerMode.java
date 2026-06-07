package sranger.mode;

/**
 * Operating modes of the SRanger controller. Moving is the initial mode.
 * Halted is the absorbing terminal mode entered on endTask (the robot is
 * stopped and ignores all further sensor events).
 */
public enum SRangerMode {
    Moving,
    Turning,
    Halted
}
