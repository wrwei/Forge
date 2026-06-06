package sranger.mode;

/**
 * The three operating modes of the SRanger controller (SR-DM1).
 * Moving is the initial mode on power-up. Stopped is the terminal mode
 * entered when the operator issues endTask (SR-FR3); it is modelled as a
 * live absorbing state — the robot stays Stopped, re-issuing the stop
 * command — rather than a RoboChart Final pseudo-state, so the
 * Z-Machine deadlock_free proof can give it a bare-precondition operation.
 */
public enum SRangerMode {
    Moving,
    Turning,
    Stopped
}
