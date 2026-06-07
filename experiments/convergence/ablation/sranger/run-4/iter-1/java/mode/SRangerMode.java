package sranger.mode;

/**
 * Operating modes of the SRanger controller. Moving is the initial mode.
 * Halted is the absorbing shutdown mode entered on endTask.
 */
public enum SRangerMode {
    Moving,
    Turning,
    Halted
}
