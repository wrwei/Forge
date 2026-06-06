package sranger.mode;

/**
 * SR-DM1: the three operating modes of the SRanger controller.
 *
 *  - Moving:  initial mode; robot drives forward at moveVel.
 *  - Turning: robot rotates in place at turnVel.
 *  - Final:   terminal mode entered on endTask; robot stops.
 */
public enum SRangerMode {
    Moving,
    Turning,
    Final
}
