package sranger.mode;

/**
 * Operating modes for the SRanger controller (SR-DM1).
 * Moving is the initial mode on power-up.
 */
public enum SRangerMode {
    Moving,
    Turning,
    Final
}
