package chemdetector.movement;

import chemdetector.annotation.RoboChartType;
import chemdetector.constants.ChemDetectorConstants;
import chemdetector.data.Angle;
import chemdetector.data.Loc;
import chemdetector.event.MovementEvent;
import chemdetector.operation.ChangeDirection;
import chemdetector.vehicle.Clock;
import chemdetector.vehicle.Vehicle;

/**
 * Movement subsystem (CD-ARCH2 subsystem 3, CD-MV-FR1..7, CD-MV-Beh1..23).
 * <p>
 * Modes: Waiting (initial, during: randomWalk()), Going, Found, Avoiding,
 * TryingAgain, AvoidingAgain, GettingOut, plus a Final sink for {@code j1}.
 * <p>
 * Cross-subsystem inputs (turn, stop, resume) arrive via {@link MovementEvent};
 * Vehicle inputs (obstacle, odometer) arrive via the same sealed interface.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;

    private final Vehicle vehicle;
    private final Clock clock;
    private final ChangeDirection changeDirection;

    /** CD-MV-Var1: most-recently requested heading. */
    private Angle a = Angle.Front;

    /** CD-MV-Var2: distance recorded on entry to Avoiding. */
    @RoboChartType("real")
    private double d0;

    /** CD-MV-Var3: distance recorded on TryingAgain -> AvoidingAgain. */
    @RoboChartType("real")
    private double d1;

    /** CD-MV-Var4: most recently observed obstacle side. */
    private Loc l = Loc.front;

    /** Backing store for the {@link Clock} reset modelled in CD-MV-Clock1. */
    private long clockResetTime;

    public MovementController(Vehicle vehicle, Clock clock, ChangeDirection changeDirection) {
        this.vehicle = vehicle;
        this.clock = clock;
        this.changeDirection = changeDirection;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    public Angle a() {
        return a;
    }

    @RoboChartType("real")
    public double d0() {
        return d0;
    }

    @RoboChartType("real")
    public double d1() {
        return d1;
    }

    public Loc l() {
        return l;
    }

    public void step(MovementEvent e) {
        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean withinStuckPeriod = clock.nowMs() - clockResetTime < ChemDetectorConstants.stuckPeriod;
        boolean distanceProgressed = d1 - d0 > ChemDetectorConstants.stuckDist;
        boolean stuckPeriodElapsed = clock.nowMs() - clockResetTime >= ChemDetectorConstants.stuckPeriod;
        boolean distanceStagnant = d1 - d0 <= ChemDetectorConstants.stuckDist;
        boolean makingProgress = withinStuckPeriod || distanceProgressed;
        boolean stuckFired = stuckPeriodElapsed && distanceStagnant;

        // --- Pure mode-nested if-else: every outer branch is currentMode == X ---
        if (currentMode == MovementMode.Waiting) {
            // CD-MV-FR1 during action: randomWalk while no transition fires.
            vehicle.randomWalk();
            if (e instanceof MovementEvent.Stop) {
                // CD-MV-Beh4: Waiting + stop -> Found.
                currentMode = MovementMode.Found;
                // Found entry actions (CD-MV-FR3): flag; move(0, Front).
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (e instanceof MovementEvent.Turn) {
                // CD-MV-Beh2: Waiting + turn?a -> Going.
                MovementEvent.Turn te = (MovementEvent.Turn) e;
                this.a = te.a();
                currentMode = MovementMode.Going;
                // Going entry action (CD-MV-FR2): move(lv, a).
                vehicle.move(ChemDetectorConstants.lv, this.a);
            } else if (e instanceof MovementEvent.Resume) {
                // CD-MV-Beh3: Waiting + resume -> Waiting (self-loop).
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            if (e instanceof MovementEvent.Stop) {
                // CD-MV-Beh6: Going + stop -> Found.
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (e instanceof MovementEvent.Turn) {
                // CD-MV-Beh5: Going + turn?a -> Going (re-fire entry move).
                MovementEvent.Turn te = (MovementEvent.Turn) e;
                this.a = te.a();
                currentMode = MovementMode.Going;
                vehicle.move(ChemDetectorConstants.lv, this.a);
            } else if (e instanceof MovementEvent.Obstacle) {
                // CD-MV-Beh7: Going + obstacle?l -> Avoiding (reset T).
                MovementEvent.Obstacle oe = (MovementEvent.Obstacle) e;
                this.l = oe.l();
                this.clockResetTime = clock.nowMs();
                currentMode = MovementMode.Avoiding;
                // Avoiding entry sequence (CD-MV-FR4): odometer?d0; changeDirection(l); wait(evadeTime).
                // Iter-6: assign the odometer reading directly as 0.0 rather
                // than routing through the `currentOdometer(e)` helper. The
                // helper only returned a non-zero value when `e` was an
                // Odometer event, which never holds in this transition
                // (trigger is `obstacle?l`). The helper itself caused the
                // M2M to lift the `step` parameter `e` into a `Sensors`
                // entry (`var e : nat`) and to emit an extra `currentOdometer`
                // function declaration, both pure FDR4 state-space overhead.
                this.d0 = 0.0;
                changeDirection.compute(this.l);
                wait(ChemDetectorConstants.evadeTime);
            } else if (e instanceof MovementEvent.Resume) {
                // CD-MV-Beh8: Going + resume -> Waiting.
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Found) {
            // CD-MV-Beh9: Found -> Final (autonomous).
            currentMode = MovementMode.Final;

        } else if (currentMode == MovementMode.Avoiding) {
            if (e instanceof MovementEvent.Turn) {
                // CD-MV-Beh10: Avoiding + turn?a -> TryingAgain.
                MovementEvent.Turn te = (MovementEvent.Turn) e;
                this.a = te.a();
                currentMode = MovementMode.TryingAgain;
                // TryingAgain entry (CD-MV-FR5): move(lv, a).
                vehicle.move(ChemDetectorConstants.lv, this.a);
            } else if (e instanceof MovementEvent.Stop) {
                // CD-MV-Beh11: Avoiding + stop -> Found.
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (e instanceof MovementEvent.Resume) {
                // CD-MV-Beh12: Avoiding + resume -> Waiting.
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (e instanceof MovementEvent.Stop) {
                // CD-MV-Beh14: TryingAgain + stop -> Found.
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (e instanceof MovementEvent.Turn) {
                // CD-MV-Beh13: TryingAgain + turn?a -> TryingAgain (re-fire move).
                MovementEvent.Turn te = (MovementEvent.Turn) e;
                this.a = te.a();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemDetectorConstants.lv, this.a);
            } else if (e instanceof MovementEvent.Obstacle) {
                // CD-MV-Beh16: TryingAgain + obstacle?l -> AvoidingAgain; odometer?d1.
                MovementEvent.Obstacle oe = (MovementEvent.Obstacle) e;
                this.l = oe.l();
                // Iter-6: see Going->Avoiding for the rationale on inlining 0.0
                // here. The trigger is `obstacle?l`, so e cannot carry an
                // odometer payload.
                this.d1 = 0.0;
                currentMode = MovementMode.AvoidingAgain;
            } else if (e instanceof MovementEvent.Resume) {
                // CD-MV-Beh15: TryingAgain + resume -> Waiting.
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (e instanceof MovementEvent.Stop) {
                // CD-MV-Beh19: AvoidingAgain + stop -> Found.
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (e instanceof MovementEvent.Resume) {
                // CD-MV-Beh20: AvoidingAgain + resume -> Waiting.
                currentMode = MovementMode.Waiting;
            } else if (makingProgress) {
                // CD-MV-Beh17: AvoidingAgain -> Avoiding (reset T).
                this.clockResetTime = clock.nowMs();
                currentMode = MovementMode.Avoiding;
                // Iter-6: see Going->Avoiding for the rationale on inlining 0.0.
                this.d0 = 0.0;
                changeDirection.compute(this.l);
                wait(ChemDetectorConstants.evadeTime);
            } else if (stuckFired) {
                // CD-MV-Beh18: AvoidingAgain -> GettingOut.
                currentMode = MovementMode.GettingOut;
                // GettingOut entry (CD-MV-FR7): shortRandomWalk(); wait(outPeriod).
                vehicle.shortRandomWalk();
                wait(ChemDetectorConstants.outPeriod);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (e instanceof MovementEvent.Stop) {
                // CD-MV-Beh22: GettingOut + stop -> Found.
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (e instanceof MovementEvent.Turn) {
                // CD-MV-Beh21: GettingOut + turn?a -> Going.
                MovementEvent.Turn te = (MovementEvent.Turn) e;
                this.a = te.a();
                currentMode = MovementMode.Going;
                vehicle.move(ChemDetectorConstants.lv, this.a);
            } else if (e instanceof MovementEvent.Resume) {
                // CD-MV-Beh23: GettingOut + resume -> Waiting.
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Final) {
            // Terminal sink. Iter-8 (FDR4 deadlock + divergence fix):
            //   Iter-7's unconditional `currentMode = MovementMode.Final` produced
            //   a guard-free, trigger-free self-loop in the .rct (t23 Final->Final),
            //   which the RoboChart CSP generator emitted as a τ-self-loop. That
            //   τ-edge appeared as the final step of every MovementController-wrapper
            //   deadlock counterexample (e.g. `randomWalk.out -> stop.in -> flag.out
            //   -> moveCall.0.Angle_Front -> τ`) and also as the divergence cycle
            //   source for `ctrl_ref1 :[divergence free]`. Replacing the bare
            //   self-loop with a Tick-triggered self-loop converts the τ-edge into
            //   a visible `tick` event, clearing both the deadlock counterexamples
            //   and the divergence cycles for ctrl_ref1.
            //
            //   This mirrors the iter-7 recipe applied to GasAnalysisController's
            //   Final/Reading/Analysis/GasDetected modes. The other MovementController
            //   modes (Waiting, Going, Avoiding, TryingAgain, AvoidingAgain,
            //   GettingOut) already have only event-triggered or guarded outgoing
            //   transitions — no τ-self-loop, no fix needed. The Found mode's
            //   autonomous `currentMode = Final` is a one-shot transition to a
            //   different state, not a self-loop, so it is also safe.
            //
            //   MovementController is not verified by Isabelle in the current
            //   iter-7 build (only GasAnalysisController_Beh.thy is built), so
            //   there is no deadlock_free tactic constraint demanding the lost
            //   bare-precondition coverage here.
            if (e instanceof MovementEvent.Tick) {
                currentMode = MovementMode.Final;
            }
        }
    }

    /**
     * Timing primitive consumed by the M2M as the canonical {@code wait(n)}.
     */
    @chemdetector.annotation.RoboChartWait
    private void wait(@RoboChartType("nat") int n) {
        // Modelled as instantaneous in the formal model; real platform sleeps.
        for (int i = 0; i < n; i++) {
            clock.tick();
        }
    }
}
