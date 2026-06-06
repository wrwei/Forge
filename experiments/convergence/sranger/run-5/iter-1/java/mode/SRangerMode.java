package sranger.mode;

/**
 * Operating modes of the SRanger controller (SR-DM1).
 *
 * <p>Halted realises the specification's terminal "Final" mode (entered
 * on endTask, full stop on entry). It is deliberately not named
 * "Final": the theory-generated controller must not contain a RoboChart
 * Final state (see CLAUDE.md, "Interpreting Isabelle Results"), so the
 * terminal mode is an ordinary absorbing state instead.
 */
public enum SRangerMode {
    Moving,
    Turning,
    Halted
}
