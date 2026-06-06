package sranger.mode;

/**
 * Operating modes of the SRanger controller (SR-DM1).
 *
 * MOVING is the initial mode. HALTED realises the specification's
 * terminal "Final" mode; it is named HALTED because the theory-generated
 * controller must not contain a state named Final (see CLAUDE.md,
 * "Interpreting Isabelle Results").
 */
public enum SRangerMode {
    MOVING,
    TURNING,
    HALTED
}
