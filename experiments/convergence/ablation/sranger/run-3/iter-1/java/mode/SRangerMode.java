package sranger.mode;

/**
 * Operating modes of the SRanger controller. Moving is the initial mode.
 * Stopped is the terminal mode entered on the endTask event; it stays
 * live (tick self-loop) rather than being a RoboChart Final state.
 */
public enum SRangerMode {
    Moving,
    Turning,
    Stopped
}
