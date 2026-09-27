package chemical_detector.event;

import java.util.List;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;

/**
 * Events consumed by the controllers' {@code step} methods.
 * <ul>
 *   <li>{@link Gas} and {@link Obstacle} come from the Vehicle.</li>
 *   <li>{@link Turn}, {@link Stop} and {@link Resume} are emitted by the
 *       gas-analysis subsystem and consumed by the movement subsystem.</li>
 *   <li>{@link Tick} is the periodic control-cycle tick, used to keep the
 *       terminal modes of both subsystems live.</li>
 * </ul>
 */
public sealed interface InputEvent {

    /** A multi-sensor gas reading; list position encodes the sensing direction. */
    record Gas(List<GasSensor> gs) implements InputEvent {
    }

    /** An obstacle has been detected on side {@code l}. */
    record Obstacle(Loc l) implements InputEvent {
    }

    /** Face and move in direction {@code a}. */
    record Turn(Angle a) implements InputEvent {
    }

    /** The chemical source has been confirmed. */
    record Stop() implements InputEvent {
    }

    /** The last reading showed no gas; return to searching. */
    record Resume() implements InputEvent {
    }

    /** Periodic control-cycle tick. */
    record Tick() implements InputEvent {
    }
}
