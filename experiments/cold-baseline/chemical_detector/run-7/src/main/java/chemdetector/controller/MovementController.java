package chemdetector.controller;

import chemdetector.actuator.Actuator;
import chemdetector.annotation.RoboChartType;
import chemdetector.clock.SystemClock;
import chemdetector.constants.Constants;
import chemdetector.domain.Angle;
import chemdetector.domain.Loc;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.MovementMode;
import chemdetector.sensor.Sensor;

/**
 * Movement subsystem state machine.
 *
 * States   : Waiting, Going, Avoiding, TryingAgain, AvoidingAgain,
 *            GettingOut, Found, Final.
 * Init     : Waiting (CD-MV-Beh1).
 * Variables: a, d0, d1, l (CD-MV-Var1..4).
 * Clock    : T (CD-MV-Clock1).
 *
 * Triggers consumed: turn ? a, obstacle ? l, stop, resume.
 * The odometer event is captured into d0 / d1 by reading
 * {@link Sensor#odometer()} inside the relevant transition / entry
 * actions (the test harness updates the sensor's odometer between
 * step() calls). This matches the RoboChart {@code odometer ? d0}
 * value-capture semantics without needing a dedicated trigger.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;

    private final Sensor sensor;
    private final Actuator actuator;
    private final SystemClock clock;

    /** CD-MV-Var1 — most recent direction command. */
    private Angle a = Angle.Front;

    /** CD-MV-Var2 — odometer reading at start of evasion sequence. */
    @RoboChartType("real")
    private double d0;

    /** CD-MV-Var3 — odometer reading at second obstacle of evasion. */
    @RoboChartType("real")
    private double d1;

    /** CD-MV-Var4 — last obstacle side. */
    private Loc l = Loc.front;

    /** CD-MV-Clock1 — stuck-detection clock; reset via {@code T = clock.nowMs()}. */
    private long T;

    public MovementController(Sensor sensor, Actuator actuator, SystemClock clock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;
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

    public void step(InputEvent event) {
        // --- Named boolean predicates ---
        boolean sinceTBelowStuckPeriod = clock.nowMs() - T < Constants.stuckPeriod;
        boolean d1MinusD0AboveStuckDist = d1 - d0 > Constants.stuckDist;
        boolean progress = sinceTBelowStuckPeriod || d1MinusD0AboveStuckDist;
        boolean stuck = !sinceTBelowStuckPeriod && !d1MinusD0AboveStuckDist;

        if (currentMode == MovementMode.Waiting) {
            // CD-MV-FR1 during action: randomWalk()
            actuator.randomWalk();

            // CD-MV-Beh4 — stop -> Found
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
            }
            // CD-MV-Beh2 — turn ? a -> Going ; entry: move(lv, a)
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.a();
                actuator.move(Constants.lv, a);
                currentMode = MovementMode.Going;
            }
            // CD-MV-Beh3 — resume -> Waiting (self-loop, no-op)
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            // CD-MV-Beh6 — stop -> Found
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
            }
            // CD-MV-Beh8 — resume -> Waiting
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
            // CD-MV-Beh5 — turn ? a -> Going (self-loop), re-fire move(lv, a)
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.a();
                actuator.move(Constants.lv, a);
                currentMode = MovementMode.Going;
            }
            // CD-MV-Beh7 — obstacle ? l -> Avoiding ; action: reset T
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle oe = (InputEvent.Obstacle) event;
                l = oe.l();
                T = clock.nowMs();
                currentMode = MovementMode.Avoiding;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            // CD-MV-FR4 entry: odometer ? d0; changeDirection(l); wait(evadeTime)
            d0 = sensor.odometer();
            actuator.changeDirection(l);
            actuator.pause(Constants.evadeTime);

            // CD-MV-Beh11 — stop -> Found
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
            }
            // CD-MV-Beh12 — resume -> Waiting
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
            // CD-MV-Beh10 — turn ? a -> TryingAgain ; entry of TryingAgain: move(lv, a)
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.a();
                actuator.move(Constants.lv, a);
                currentMode = MovementMode.TryingAgain;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            // CD-MV-Beh14 — stop -> Found
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
            }
            // CD-MV-Beh15 — resume -> Waiting
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
            // CD-MV-Beh13 — turn ? a -> TryingAgain (self-loop), re-fire move(lv, a)
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.a();
                actuator.move(Constants.lv, a);
                currentMode = MovementMode.TryingAgain;
            }
            // CD-MV-Beh16 — obstacle ? l -> AvoidingAgain ; action: d1 = odometer
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle oe = (InputEvent.Obstacle) event;
                l = oe.l();
                d1 = sensor.odometer();
                currentMode = MovementMode.AvoidingAgain;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            // CD-MV-Beh19 — stop -> Found
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
            }
            // CD-MV-Beh20 — resume -> Waiting
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
            // CD-MV-Beh17 — autonomous: progress -> Avoiding ; action: reset T
            else if (progress) {
                T = clock.nowMs();
                currentMode = MovementMode.Avoiding;
            }
            // CD-MV-Beh18 — autonomous: stuck -> GettingOut
            else if (stuck) {
                currentMode = MovementMode.GettingOut;
            }

        } else if (currentMode == MovementMode.GettingOut) {
            // CD-MV-FR7 entry: shortRandomWalk(); wait(outPeriod)
            actuator.shortRandomWalk();
            actuator.pause(Constants.outPeriod);

            // CD-MV-Beh22 — stop -> Found
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
            }
            // CD-MV-Beh23 — resume -> Waiting
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
            // CD-MV-Beh21 — turn ? a -> Going ; entry of Going: move(lv, a)
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.a();
                actuator.move(Constants.lv, a);
                currentMode = MovementMode.Going;
            }

        } else if (currentMode == MovementMode.Found) {
            // CD-MV-FR3 entry: flag; move(0, Front)
            actuator.flag();
            actuator.move(0.0, Angle.Front);
            // CD-MV-Beh9 — autonomous to Final
            currentMode = MovementMode.Final;
        }
    }
}
