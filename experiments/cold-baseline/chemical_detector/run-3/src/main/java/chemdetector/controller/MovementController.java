package chemdetector.controller;

import chemdetector.Clock;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.data.Angle;
import chemdetector.data.Loc;
import chemdetector.event.MVInputEvent;
import chemdetector.sensor.Vehicle;

/**
 * Movement controller (CD-ARCH2 subsystem 3). Performs the
 * physical search behaviour: random-walk in Waiting, directed
 * motion in Going / TryingAgain, obstacle avoidance in
 * Avoiding / AvoidingAgain, and recovery in GettingOut.
 *
 * State machine per CD-MV-FR1..7 and CD-MV-Beh1..23.
 */
public final class MovementController {

    private MVMode currentMode = MVMode.Waiting;

    private final Vehicle vehicle;

    @chemdetector.annotation.Clock
    private final Clock clock;

    /** CD-MV-Var1: most-recent commanded direction. */
    private Angle a = Angle.Front;

    /** CD-MV-Var2: distance recorded on entry to Avoiding. */
    @RoboChartType("real")
    private double d0 = 0.0;

    /** CD-MV-Var3: distance recorded on entry to AvoidingAgain. */
    @RoboChartType("real")
    private double d1 = 0.0;

    /** CD-MV-Var4: side at which the latest obstacle was found. */
    private Loc l = Loc.front;

    /** CD-MV-Clock1: timestamp of the most recent clock T reset. */
    @RoboChartType("nat")
    private long tickT = 0L;

    public MovementController(Vehicle vehicle, Clock clock) {
        this.vehicle = vehicle;
        this.clock = clock;
        // Waiting during action — random-walk while idle.
        this.vehicle.randomWalk();
    }

    public MVMode currentMode() {
        return currentMode;
    }

    public void step(MVInputEvent event) {
        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean withinStuckWindow = clock.nowMs() - this.tickT < Constants.STUCK_PERIOD;
        boolean madeProgress = this.d1 - this.d0 > Constants.STUCK_DIST;
        boolean stuckTimeoutElapsed = clock.nowMs() - this.tickT >= Constants.STUCK_PERIOD;
        boolean noProgress = this.d1 - this.d0 <= Constants.STUCK_DIST;
        boolean stillProgressing = withinStuckWindow || madeProgress;
        boolean stuck = stuckTimeoutElapsed && noProgress;

        // --- Pure mode-nested if-else: every outer branch is currentMode == X ---
        if (currentMode == MVMode.Waiting) {
            // CD-MV-Beh4: Waiting -> Found on stop
            if (event instanceof MVInputEvent.Stop) {
                currentMode = MVMode.Found;
                // Found entry: send flag and halt.
                vehicle.sendFlag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh2: Waiting -> Going on turn ? a
            else if (event instanceof MVInputEvent.Turn) {
                MVInputEvent.Turn te = (MVInputEvent.Turn) event;
                this.a = te.a();
                currentMode = MVMode.Going;
                // Going entry: move(lv, a).
                vehicle.move(Constants.LV, this.a);
            }
            // CD-MV-Beh3: Waiting -> Waiting on resume (self-loop, no-op)
            else if (event instanceof MVInputEvent.Resume) {
                currentMode = MVMode.Waiting;
            }

        } else if (currentMode == MVMode.Going) {
            // CD-MV-Beh6: Going -> Found on stop
            if (event instanceof MVInputEvent.Stop) {
                currentMode = MVMode.Found;
                vehicle.sendFlag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh5: Going -> Going on turn ? a (re-fires move)
            else if (event instanceof MVInputEvent.Turn) {
                MVInputEvent.Turn te = (MVInputEvent.Turn) event;
                this.a = te.a();
                currentMode = MVMode.Going;
                vehicle.move(Constants.LV, this.a);
            }
            // CD-MV-Beh7: Going -> Avoiding on obstacle ? l; action: reset clock T
            else if (event instanceof MVInputEvent.Obstacle) {
                MVInputEvent.Obstacle oe = (MVInputEvent.Obstacle) event;
                this.l = oe.l();
                this.tickT = clock.nowMs();
                currentMode = MVMode.Avoiding;
                // Avoiding entry actions: odometer ? d0; changeDirection(l); wait(evadeTime).
                this.d0 = vehicle.odometer();
                vehicle.changeDirection(this.l);
                vehicle.pause(Constants.EVADE_TIME);
            }
            // CD-MV-Beh8: Going -> Waiting on resume
            else if (event instanceof MVInputEvent.Resume) {
                currentMode = MVMode.Waiting;
                vehicle.randomWalk();
            }

        } else if (currentMode == MVMode.Found) {
            // CD-MV-Beh9: Found -> Final (autonomous, bare precondition)
            currentMode = MVMode.Final;

        } else if (currentMode == MVMode.Avoiding) {
            // CD-MV-Beh11: Avoiding -> Found on stop
            if (event instanceof MVInputEvent.Stop) {
                currentMode = MVMode.Found;
                vehicle.sendFlag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh10: Avoiding -> TryingAgain on turn ? a
            else if (event instanceof MVInputEvent.Turn) {
                MVInputEvent.Turn te = (MVInputEvent.Turn) event;
                this.a = te.a();
                currentMode = MVMode.TryingAgain;
                // TryingAgain entry: move(lv, a).
                vehicle.move(Constants.LV, this.a);
            }
            // CD-MV-Beh12: Avoiding -> Waiting on resume
            else if (event instanceof MVInputEvent.Resume) {
                currentMode = MVMode.Waiting;
                vehicle.randomWalk();
            }

        } else if (currentMode == MVMode.TryingAgain) {
            // CD-MV-Beh14: TryingAgain -> Found on stop
            if (event instanceof MVInputEvent.Stop) {
                currentMode = MVMode.Found;
                vehicle.sendFlag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh13: TryingAgain -> TryingAgain on turn ? a
            else if (event instanceof MVInputEvent.Turn) {
                MVInputEvent.Turn te = (MVInputEvent.Turn) event;
                this.a = te.a();
                currentMode = MVMode.TryingAgain;
                vehicle.move(Constants.LV, this.a);
            }
            // CD-MV-Beh16: TryingAgain -> AvoidingAgain on obstacle ? l;
            //   action: odometer ? d1
            else if (event instanceof MVInputEvent.Obstacle) {
                MVInputEvent.Obstacle oe = (MVInputEvent.Obstacle) event;
                this.l = oe.l();
                this.d1 = vehicle.odometer();
                currentMode = MVMode.AvoidingAgain;
            }
            // CD-MV-Beh15: TryingAgain -> Waiting on resume
            else if (event instanceof MVInputEvent.Resume) {
                currentMode = MVMode.Waiting;
                vehicle.randomWalk();
            }

        } else if (currentMode == MVMode.AvoidingAgain) {
            // CD-MV-Beh19: AvoidingAgain -> Found on stop
            if (event instanceof MVInputEvent.Stop) {
                currentMode = MVMode.Found;
                vehicle.sendFlag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh20: AvoidingAgain -> Waiting on resume
            else if (event instanceof MVInputEvent.Resume) {
                currentMode = MVMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh18: AvoidingAgain -> GettingOut when stuck
            //   (since(T) >= stuckPeriod AND d1 - d0 <= stuckDist)
            else if (stuck) {
                currentMode = MVMode.GettingOut;
                // GettingOut entry: shortRandomWalk(); wait(outPeriod).
                vehicle.shortRandomWalk();
                vehicle.pause(Constants.OUT_PERIOD);
            }
            // CD-MV-Beh17: AvoidingAgain -> Avoiding when still progressing
            //   (since(T) < stuckPeriod OR d1 - d0 > stuckDist); action: reset T
            else if (stillProgressing) {
                this.tickT = clock.nowMs();
                currentMode = MVMode.Avoiding;
                // Avoiding entry actions re-execute: odometer ? d0; changeDirection(l); wait(evadeTime).
                this.d0 = vehicle.odometer();
                vehicle.changeDirection(this.l);
                vehicle.pause(Constants.EVADE_TIME);
            }

        } else if (currentMode == MVMode.GettingOut) {
            // CD-MV-Beh22: GettingOut -> Found on stop
            if (event instanceof MVInputEvent.Stop) {
                currentMode = MVMode.Found;
                vehicle.sendFlag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh21: GettingOut -> Going on turn ? a
            else if (event instanceof MVInputEvent.Turn) {
                MVInputEvent.Turn te = (MVInputEvent.Turn) event;
                this.a = te.a();
                currentMode = MVMode.Going;
                vehicle.move(Constants.LV, this.a);
            }
            // CD-MV-Beh23: GettingOut -> Waiting on resume
            else if (event instanceof MVInputEvent.Resume) {
                currentMode = MVMode.Waiting;
                vehicle.randomWalk();
            }
        }
    }

    /** Test/inspection accessors. */
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
}
