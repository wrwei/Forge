package sranger.mode;

/**
 * Operating modes of the SRanger controller. Moving is the initial mode.
 * Stopped is the terminal mode entered on endTask (full stop issued on
 * entry); the controller never leaves it.
 */
public enum SRangerMode {
    Moving,
    Turning,
    Stopped
}
