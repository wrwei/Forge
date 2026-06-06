package sranger.mode;

/**
 * Operating modes of the SRanger controller (SR-DM1). The controller starts in
 * {@link #Moving}.
 *
 * <p>The terminal mode SR-DM1 names "Final" is modelled here as {@code Stopped}.
 * The Z-Machine {@code deadlock_free} proof cannot discharge a controller whose
 * RoboChart state is named {@code Final} (the forked theory generator never
 * designates it as a terminal, so the proof's residual goal lacks a
 * {@code st = Final} disjunct and hangs). {@code Stopped} is an ordinary mode
 * with a single {@code tick} self-loop, which keeps it deadlock-free while
 * preserving the terminal stop semantics (the robot remains stopped and
 * re-issues Move(0,0) each cycle, never leaving the mode).
 */
public enum SRangerMode {
    Moving,
    Turning,
    Stopped
}
