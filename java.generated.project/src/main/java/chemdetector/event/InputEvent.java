package chemdetector.event;

import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Loc;
import java.util.List;

/**
 * Events consumed (as transition triggers) by the two subsystem state
 * machines.
 *
 * <ul>
 *   <li>{@code Gas} — boundary input to the gas-analysis subsystem (CD-Evt1).</li>
 *   <li>{@code Obstacle} — boundary input to the movement subsystem (CD-Evt2).</li>
 *   <li>{@code Turn}/{@code Stop}/{@code Resume} — inter-controller events
 *       emitted by gas-analysis and consumed by movement
 *       (CD-Evt4/CD-Evt5/CD-Evt6).</li>
 * </ul>
 */
public sealed interface InputEvent {

    /** gas ? gs — a multi-sensor reading (CD-Evt1). */
    record Gas(List<GasSensor> reading) implements InputEvent {
    }

    /** obstacle ? l — side of a detected obstacle (CD-Evt2). */
    record Obstacle(Loc loc) implements InputEvent {
    }

    /** turn ? a — direction to face next (CD-Evt4). */
    record Turn(Angle angle) implements InputEvent {
    }

    /** stop — chemical source confirmed (CD-Evt5). */
    record Stop() implements InputEvent {
    }

    /** resume — continue searching after a no-gas reading (CD-Evt6). */
    record Resume() implements InputEvent {
    }

    /**
     * tick — heartbeat consumed only by the terminal (Final) state to
     * keep it live. Tooling workaround: the EGL theory template does not
     * mark RoboChart Final nodes as Z-Machine {@code End} states, so a
     * truly absorbing Final state would fail the {@code deadlock_free}
     * proof. A visible-event self-loop keeps Final deadlock-free without
     * introducing divergence (the event is external, not internal/tau).
     */
    record Tick() implements InputEvent {
    }
}
