package sranger.mode;

/**
 * Operating modes of the SRanger controller.
 *
 * The initial mode on power-up is {@link #Moving}.
 */
public enum SRangerMode {
    Moving,
    Turning,
    Final
}
