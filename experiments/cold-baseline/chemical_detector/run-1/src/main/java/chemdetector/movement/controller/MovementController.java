package chemdetector.movement.controller;

import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.DetectorConstants;
import chemdetector.datatype.Angle;
import chemdetector.datatype.Loc;
import chemdetector.event.InputEvent;
import chemdetector.mode.MovementMode;
import chemdetector.operation.ChangeDirection;
import chemdetector.sensor.Clock;

/**
 * Movement subsystem state machine (CD-MV-FR1..7, CD-MV-Beh1..23).
 *
 * Modes: Waiting (initial), Going, Found, Avoiding, TryingAgain,
 *        AvoidingAgain, GettingOut, Final.
 *
 * Inputs (boundary):   obstacle (CD-Evt2), odometer (CD-Evt3)
 * Inputs (shared):     turn (CD-Evt4), stop (CD-Evt5), resume (CD-Evt6)
 * Outputs (boundary):  flag (CD-Evt7) via Vehicle.flag()
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;

    private final Vehicle vehicle;
    private final ChangeDirection changeDirection;
    private final Clock clock;

    /** CD-MV-Var1: most recent commanded angle. */
    private Angle a = Angle.Front;

    /** CD-MV-Var2: odometer at first obstacle of current evasion sequence. */
    @RoboChartType("real")
    private double d0;

    /** CD-MV-Var3: odometer at second obstacle of current evasion sequence. */
    @RoboChartType("real")
    private double d1;

    /** CD-MV-Var4: most recent obstacle side. */
    private Loc l = Loc.front;

    /** Cached latest odometer reading. */
    @RoboChartType("real")
    private double odometerValue;

    /** CD-MV-Clock1: clock T for stuck detection. Records nowMs() at reset. */
    private long T;

    public MovementController(Vehicle vehicle, ChangeDirection changeDirection, Clock clock) {
        this.vehicle = vehicle;
        this.changeDirection = changeDirection;
        this.clock = clock;
        this.T = clock.nowMs();
        // Entry action of the initial state Waiting: during randomWalk().
        vehicle.randomWalk();
    }

    public MovementMode currentMode() { return currentMode; }
    public Angle a() { return a; }
    @RoboChartType("real") public double d0() { return d0; }
    @RoboChartType("real") public double d1() { return d1; }
    public Loc l() { return l; }

    public void step(InputEvent event) {
        // Named boolean predicates declared BEFORE the if-else chain.
        boolean withinStuckPeriod = clock.nowMs() - T < DetectorConstants.stuckPeriod;
        boolean atOrPastStuckPeriod = clock.nowMs() - T >= DetectorConstants.stuckPeriod;
        boolean progressedFarEnough = d1 - d0 > DetectorConstants.stuckDist;
        boolean progressedTooLittle = d1 - d0 <= DetectorConstants.stuckDist;
        boolean makingProgress = withinStuckPeriod || progressedFarEnough;
        boolean stuck = atOrPastStuckPeriod && progressedTooLittle;

        if (currentMode == MovementMode.Waiting) {
            // CD-MV-Beh4: Waiting -> Found on stop.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh2: Waiting -> Going on turn?a.
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.payload();
                currentMode = MovementMode.Going;
                vehicle.move(DetectorConstants.lv, a);
            }
            // CD-MV-Beh3: Waiting -> Waiting on resume (self-loop).
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // Odometer reading: cache the latest value.
            else if (event instanceof InputEvent.Odometer) {
                InputEvent.Odometer oe = (InputEvent.Odometer) event;
                odometerValue = oe.payload();
            }

        } else if (currentMode == MovementMode.Going) {
            // CD-MV-Beh6: Going -> Found on stop.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh7: Going -> Avoiding on obstacle?l. Action: reset T.
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obe = (InputEvent.Obstacle) event;
                l = obe.payload();
                T = clock.nowMs();
                currentMode = MovementMode.Avoiding;
                // Entry action of Avoiding: odometer?d0; changeDirection(l); wait(evadeTime).
                d0 = odometerValue;
                changeDirection.compute(l);
                vehicle.pause(DetectorConstants.evadeTime);
            }
            // CD-MV-Beh5: Going -> Going on turn?a (self-loop, re-fire move).
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.payload();
                currentMode = MovementMode.Going;
                vehicle.move(DetectorConstants.lv, a);
            }
            // CD-MV-Beh8: Going -> Waiting on resume.
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            else if (event instanceof InputEvent.Odometer) {
                InputEvent.Odometer oe = (InputEvent.Odometer) event;
                odometerValue = oe.payload();
            }

        } else if (currentMode == MovementMode.Found) {
            // CD-MV-Beh9: Found -> Final, no trigger, no guard, no action.
            currentMode = MovementMode.Final;

        } else if (currentMode == MovementMode.Avoiding) {
            // CD-MV-Beh11: Avoiding -> Found on stop.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh10: Avoiding -> TryingAgain on turn?a.
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.payload();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(DetectorConstants.lv, a);
            }
            // CD-MV-Beh12: Avoiding -> Waiting on resume.
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            else if (event instanceof InputEvent.Odometer) {
                InputEvent.Odometer oe = (InputEvent.Odometer) event;
                odometerValue = oe.payload();
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            // CD-MV-Beh14: TryingAgain -> Found on stop.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh16: TryingAgain -> AvoidingAgain on obstacle?l. Action: odometer?d1.
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obe = (InputEvent.Obstacle) event;
                l = obe.payload();
                d1 = odometerValue;
                currentMode = MovementMode.AvoidingAgain;
            }
            // CD-MV-Beh13: TryingAgain -> TryingAgain on turn?a (self-loop).
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.payload();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(DetectorConstants.lv, a);
            }
            // CD-MV-Beh15: TryingAgain -> Waiting on resume.
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            else if (event instanceof InputEvent.Odometer) {
                InputEvent.Odometer oe = (InputEvent.Odometer) event;
                odometerValue = oe.payload();
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            // CD-MV-Beh19: AvoidingAgain -> Found on stop.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh20: AvoidingAgain -> Waiting on resume.
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh17: AvoidingAgain -> Avoiding when making progress. Action: reset T.
            else if (makingProgress) {
                T = clock.nowMs();
                currentMode = MovementMode.Avoiding;
                d0 = odometerValue;
                changeDirection.compute(l);
                vehicle.pause(DetectorConstants.evadeTime);
            }
            // CD-MV-Beh18: AvoidingAgain -> GettingOut when stuck.
            else if (stuck) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(DetectorConstants.outPeriod);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            // CD-MV-Beh22: GettingOut -> Found on stop.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh21: GettingOut -> Going on turn?a.
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.payload();
                currentMode = MovementMode.Going;
                vehicle.move(DetectorConstants.lv, a);
            }
            // CD-MV-Beh23: GettingOut -> Waiting on resume.
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            else if (event instanceof InputEvent.Odometer) {
                InputEvent.Odometer oe = (InputEvent.Odometer) event;
                odometerValue = oe.payload();
            }

        } else if (currentMode == MovementMode.Final) {
            // Terminal state.
        }
    }
}
