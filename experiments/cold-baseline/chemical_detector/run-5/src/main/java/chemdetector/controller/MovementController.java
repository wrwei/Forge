package chemdetector.controller;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.RoboChartWait;
import chemdetector.actuator.Vehicle;
import chemdetector.clock.Clock;
import chemdetector.constants.Constants;
import chemdetector.data.Angle;
import chemdetector.data.Loc;
import chemdetector.event.MVInputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.MVMode;

/**
 * Movement subsystem controller (CD-MV-FR1..CD-MV-FR7 + CD-MV-Beh1..23).
 *
 * Initial mode: Waiting (CD-MV-Beh1). Drives the Vehicle through Going,
 * Avoiding, TryingAgain, AvoidingAgain, GettingOut, Found per the
 * inter-controller events from the gas-analysis subsystem.
 */
public final class MovementController {

    private MVMode currentMode = MVMode.Waiting;

    /** CD-MV-Var1 — last commanded angle. */
    private Angle a = Angle.Front;
    /** CD-MV-Var2 — distance at start of an evasion sequence. */
    @RoboChartType("real")
    private double d0;
    /** CD-MV-Var3 — distance at second obstacle of an evasion sequence. */
    @RoboChartType("real")
    private double d1;
    /** CD-MV-Var4 — last obstacle side. */
    private Loc l = Loc.front;
    /** CD-MV-Clock1 — clock T for stuck-detection. */
    @RoboChartType("nat")
    private int T;

    /** Buffer of the most recent odometer reading, used by Avoiding / AvoidingAgain entries. */
    @RoboChartType("real")
    private double lastOdometer = 0.0;

    private final Vehicle vehicle;
    private final Clock clock;

    public MovementController(Vehicle vehicle, Clock clock) {
        this.vehicle = vehicle;
        this.clock = clock;
        // Initial during-action of Waiting: randomWalk()
        this.vehicle.randomWalk();
    }

    public MVMode currentMode() {
        return currentMode;
    }

    /** Wait primitive — recognised by the ETL as a RoboChart wait(N) call. */
    @RoboChartWait
    public void pause(@RoboChartType("nat") int duration) {
        // Placeholder for the RoboChart wait(N) action.
    }

    public void step(MVInputEvent event) {
        // --- Buffer most recent odometer reading (CD-Evt3 consumption). ---
        if (event instanceof MVInputEvent.Odometer) {
            MVInputEvent.Odometer od = (MVInputEvent.Odometer) event;
            lastOdometer = od.d();
        }

        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean makingProgressByTime = (clock.nowMs() - T) < Constants.stuckPeriod;
        boolean makingProgressByDist = (d1 - d0) > Constants.stuckDist;
        boolean stuckByTime = (clock.nowMs() - T) >= Constants.stuckPeriod;
        boolean stuckByDist = (d1 - d0) <= Constants.stuckDist;
        boolean makingProgress = makingProgressByTime || makingProgressByDist;
        boolean stuck = stuckByTime && stuckByDist;
        boolean obstacleLeft = l == Loc.left;
        boolean obstacleRight = l == Loc.right;
        boolean obstacleFront = l == Loc.front;

        // --- Pure mode-nested if-else ---
        if (currentMode == MVMode.Waiting) {
            // CD-MV-Beh4 — Waiting -> Found on stop
            if (event instanceof MVInputEvent.Stop) {
                currentMode = MVMode.Found;
                // Entry action of Found: send flag; halt platform.
                vehicle.emit(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh2 — Waiting -> Going on turn ? a
            else if (event instanceof MVInputEvent.Turn) {
                MVInputEvent.Turn te = (MVInputEvent.Turn) event;
                a = te.a();
                currentMode = MVMode.Going;
                // Entry action of Going: move(lv, a)
                vehicle.move(Constants.lv, a);
            }
            // CD-MV-Beh3 — Waiting -> Waiting on resume (self-loop)
            else if (event instanceof MVInputEvent.Resume) {
                currentMode = MVMode.Waiting;
            }

        } else if (currentMode == MVMode.Going) {
            // CD-MV-Beh6 — Going -> Found on stop
            if (event instanceof MVInputEvent.Stop) {
                currentMode = MVMode.Found;
                vehicle.emit(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh8 — Going -> Waiting on resume
            else if (event instanceof MVInputEvent.Resume) {
                currentMode = MVMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh5 — Going -> Going on turn ? a (self-loop; re-fires move(lv,a))
            else if (event instanceof MVInputEvent.Turn) {
                MVInputEvent.Turn te = (MVInputEvent.Turn) event;
                a = te.a();
                currentMode = MVMode.Going;
                vehicle.move(Constants.lv, a);
            }
            // CD-MV-Beh7 — Going -> Avoiding on obstacle ? l (reset clock T)
            else if (event instanceof MVInputEvent.Obstacle) {
                MVInputEvent.Obstacle oe = (MVInputEvent.Obstacle) event;
                l = oe.l();
                T = clock.nowMs();
                currentMode = MVMode.Avoiding;
                // Entry action of Avoiding: odometer ? d0; changeDirection(l); wait(evadeTime)
                d0 = lastOdometer;
                if (obstacleLeft) {
                    vehicle.move(Constants.lv, Angle.Right);
                } else if (obstacleRight) {
                    vehicle.move(Constants.lv, Angle.Left);
                } else if (obstacleFront) {
                    vehicle.move(Constants.lv, Angle.Back);
                }
                pause(Constants.evadeTime);
            }

        } else if (currentMode == MVMode.Avoiding) {
            // CD-MV-Beh11 — Avoiding -> Found on stop
            if (event instanceof MVInputEvent.Stop) {
                currentMode = MVMode.Found;
                vehicle.emit(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh12 — Avoiding -> Waiting on resume
            else if (event instanceof MVInputEvent.Resume) {
                currentMode = MVMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh10 — Avoiding -> TryingAgain on turn ? a
            else if (event instanceof MVInputEvent.Turn) {
                MVInputEvent.Turn te = (MVInputEvent.Turn) event;
                a = te.a();
                currentMode = MVMode.TryingAgain;
                // Entry action of TryingAgain: move(lv, a)
                vehicle.move(Constants.lv, a);
            }

        } else if (currentMode == MVMode.TryingAgain) {
            // CD-MV-Beh14 — TryingAgain -> Found on stop
            if (event instanceof MVInputEvent.Stop) {
                currentMode = MVMode.Found;
                vehicle.emit(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh15 — TryingAgain -> Waiting on resume
            else if (event instanceof MVInputEvent.Resume) {
                currentMode = MVMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh13 — TryingAgain -> TryingAgain on turn ? a (re-fires move(lv,a))
            else if (event instanceof MVInputEvent.Turn) {
                MVInputEvent.Turn te = (MVInputEvent.Turn) event;
                a = te.a();
                currentMode = MVMode.TryingAgain;
                vehicle.move(Constants.lv, a);
            }
            // CD-MV-Beh16 — TryingAgain -> AvoidingAgain on obstacle ? l (action: odometer ? d1)
            else if (event instanceof MVInputEvent.Obstacle) {
                MVInputEvent.Obstacle oe = (MVInputEvent.Obstacle) event;
                l = oe.l();
                d1 = lastOdometer;
                currentMode = MVMode.AvoidingAgain;
            }

        } else if (currentMode == MVMode.AvoidingAgain) {
            // CD-MV-Beh19 — AvoidingAgain -> Found on stop
            if (event instanceof MVInputEvent.Stop) {
                currentMode = MVMode.Found;
                vehicle.emit(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh20 — AvoidingAgain -> Waiting on resume
            else if (event instanceof MVInputEvent.Resume) {
                currentMode = MVMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh17 — AvoidingAgain -> Avoiding when makingProgress (reset clock T)
            else if (makingProgress) {
                T = clock.nowMs();
                currentMode = MVMode.Avoiding;
                // Entry action of Avoiding (on re-entry): odometer ? d0; changeDirection(l); wait(evadeTime)
                d0 = lastOdometer;
                if (obstacleLeft) {
                    vehicle.move(Constants.lv, Angle.Right);
                } else if (obstacleRight) {
                    vehicle.move(Constants.lv, Angle.Left);
                } else if (obstacleFront) {
                    vehicle.move(Constants.lv, Angle.Back);
                }
                pause(Constants.evadeTime);
            }
            // CD-MV-Beh18 — AvoidingAgain -> GettingOut when stuck
            else if (stuck) {
                currentMode = MVMode.GettingOut;
                // Entry action of GettingOut: shortRandomWalk(); wait(outPeriod)
                vehicle.shortRandomWalk();
                pause(Constants.outPeriod);
            }

        } else if (currentMode == MVMode.GettingOut) {
            // CD-MV-Beh22 — GettingOut -> Found on stop
            if (event instanceof MVInputEvent.Stop) {
                currentMode = MVMode.Found;
                vehicle.emit(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh23 — GettingOut -> Waiting on resume
            else if (event instanceof MVInputEvent.Resume) {
                currentMode = MVMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh21 — GettingOut -> Going on turn ? a
            else if (event instanceof MVInputEvent.Turn) {
                MVInputEvent.Turn te = (MVInputEvent.Turn) event;
                a = te.a();
                currentMode = MVMode.Going;
                vehicle.move(Constants.lv, a);
            }

        } else if (currentMode == MVMode.Found) {
            // CD-MV-Beh9 — Found -> Final (autonomous, no guard)
            currentMode = MVMode.Final;

        } else if (currentMode == MVMode.Final) {
            // Terminal — no outgoing transitions.
        }
    }
}
