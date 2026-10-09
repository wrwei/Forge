package sranger.mode;

/**
 * Operating modes of the SRanger controller (SR-DM1).
 *
 * <p>Three modes: {@link #Moving} (initial, drives forward), {@link #Turning}
 * (rotates in place), and {@link #Final} (terminal, full stop on endTask).</p>
 */
public enum SRangerMode {
    Moving,
    Turning,
    Final
}
