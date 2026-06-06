package sranger.mode;

/**
 * Operating modes of the SRanger controller (SR-DM1). The controller starts in
 * {@link #Moving}.
 */
public enum SRangerMode {
    Moving,
    Turning,
    Final
}
