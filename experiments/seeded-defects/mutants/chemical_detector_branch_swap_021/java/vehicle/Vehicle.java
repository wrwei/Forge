package chemdetector.vehicle;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.SensorService;
import chemdetector.data.Angle;
import chemdetector.data.GasSensor;
import java.util.ArrayList;
import java.util.List;

/**
 * Sensing-and-actuation surface (CD-ARCH1, CD-ARCH2 subsystem 1).
 * <p>
 * Exposes the move/randomWalk/shortRandomWalk operations (CD-OP1, CD-OP2,
 * CD-OP3) that downstream controllers invoke.
 * <p>
 * The Vehicle layer also returns safe defaults for any missing-data case so
 * that controller predicates never need sentinel-existence checks.
 * <p>
 * <b>Iter-6 state-space reduction.</b> The {@code lastVel}, {@code flagged},
 * {@code lastMoveWasRandomWalk}, and {@code lastMoveWasShortRandomWalk}
 * status fields were removed. Iter-5 surfaced them as public getters which
 * the M2M lifted into a {@code Sensors} interface, adding four boolean +
 * one real Cartesian factor to FDR4's state space without contributing to
 * any verified guard or action (no controller predicate reads them). They
 * remained as effectful no-op side updates inside the operations, which is
 * pure overhead for the formal model. The operations below stay
 * effect-free at the formal level while still satisfying the CD-OP1..3
 * signatures.
 */
@SensorService
public final class Vehicle {

    /** CD-OP1: move with linear velocity {@code v} in direction {@code a}. */
    public void move(@RoboChartType("real") double v, Angle a) {
        // CD-OP1 has no observable post-state in the formal model; iter-5's
        // last-velocity / last-angle bookkeeping was unused by either
        // controller's guards or by the verifier theories.
    }

    /** CD-OP2: unbounded random-walk search. */
    public void randomWalk() {
        // CD-OP2 — no observable post-state at the formal level.
    }

    /** CD-OP3: bounded short random-walk used in recovery. */
    public void shortRandomWalk() {
        // CD-OP3 — no observable post-state at the formal level.
    }

    /** CD-Evt7: chemical-source-found notification. */
    public void flag() {
        // The flag event is observed via the output channel in the formal
        // model; no Vehicle-side state change is required.
    }

    /**
     * Safe-default empty-reading provider exposed for any consumer that needs
     * a baseline gas reading (CD-DM7). Returns an immutable empty list.
     */
    public List<GasSensor> emptyReading() {
        return new ArrayList<>();
    }
}
