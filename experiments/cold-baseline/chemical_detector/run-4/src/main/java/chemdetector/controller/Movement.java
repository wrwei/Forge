package chemdetector.controller;

import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.clock.Clock;
import chemdetector.constants.ChemDetectorConstants;
import chemdetector.datatype.Angle;
import chemdetector.datatype.Loc;
import chemdetector.event.MovementInputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.MovementMode;

/**
 * Movement state machine (CD-MV-FR1..7, CD-MV-Beh1..23).
 *
 * Modes: Waiting (initial), Going, Avoiding, TryingAgain, AvoidingAgain,
 *        GettingOut, Found, Final.
 *
 * State variables:
 *   a  : Angle (CD-MV-Var1) — last commanded direction
 *   d0 : real  (CD-MV-Var2) — odometer at first obstacle of evasion sequence
 *   d1 : real  (CD-MV-Var3) — odometer at second obstacle
 *   l  : Loc   (CD-MV-Var4) — last obstacle location
 *
 * Clock T (CD-MV-Clock1) backs stuck-detection.
 *
 * Each step() call advances at most one transition. Shared events from
 * GasAnalysis are delivered via receive(OutputEvent).
 */
public final class Movement {

    private MovementMode currentMode = MovementMode.Waiting;
    private final Vehicle vehicle;
    private final Clock clock;

    private Angle a = Angle.Front;
    @RoboChartType("real")
    private double d0;
    @RoboChartType("real")
    private double d1;
    private Loc l = Loc.front;

    /** Clock T — millisecond timestamp of last reset (CD-MV-Clock1). */
    @RoboChartType("nat")
    private long T;

    public Movement(Vehicle vehicle, Clock clock) {
        this.vehicle = vehicle;
        this.clock = clock;
        this.T = clock.nowMs();
    }

    public MovementMode currentMode() { return currentMode; }
    public Angle a() { return a; }
    @RoboChartType("real")
    public double d0() { return d0; }
    @RoboChartType("real")
    public double d1() { return d1; }
    public Loc l() { return l; }

    /**
     * Shared-event entry point used by GasAnalysis to deliver turn/stop/resume.
     */
    public void receive(OutputEvent event) {
        if (event instanceof OutputEvent.Turn) {
            OutputEvent.Turn t = (OutputEvent.Turn) event;
            step(new MovementInputEvent.Turn(t.a()));
        } else if (event instanceof OutputEvent.Stop) {
            step(new MovementInputEvent.Stop());
        } else if (event instanceof OutputEvent.Resume) {
            step(new MovementInputEvent.Resume());
        }
    }

    public void step(MovementInputEvent event) {
        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean tBelowStuckPeriod = clock.nowMs() - T < ChemDetectorConstants.stuckPeriod;
        boolean tAboveStuckPeriod = clock.nowMs() - T >= ChemDetectorConstants.stuckPeriod;
        boolean distAboveStuckDist = d1 - d0 > ChemDetectorConstants.stuckDist;
        boolean distBelowStuckDist = d1 - d0 <= ChemDetectorConstants.stuckDist;
        boolean makingProgress = tBelowStuckPeriod || distAboveStuckDist;
        boolean stuck = tAboveStuckPeriod && distBelowStuckDist;

        if (currentMode == MovementMode.Waiting) {
            // CD-MV-FR1 during action: randomWalk()
            vehicle.randomWalk();
            // CD-MV-Beh4: stop --> Found
            if (event instanceof MovementInputEvent.Stop) {
                currentMode = MovementMode.Found;
                // CD-MV-FR3 entry: emit flag; move(0, Front)
                vehicle.emit(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh2: turn ? a --> Going
            else if (event instanceof MovementInputEvent.Turn) {
                MovementInputEvent.Turn t = (MovementInputEvent.Turn) event;
                a = t.a();
                currentMode = MovementMode.Going;
                // CD-MV-FR2 entry: move(lv, a)
                vehicle.move(ChemDetectorConstants.lv, a);
            }
            // CD-MV-Beh3: resume --> Waiting (self-loop)
            else if (event instanceof MovementInputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            // CD-MV-Beh6: stop --> Found
            if (event instanceof MovementInputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.emit(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh5: turn ? a --> Going (self-loop)
            else if (event instanceof MovementInputEvent.Turn) {
                MovementInputEvent.Turn t = (MovementInputEvent.Turn) event;
                a = t.a();
                currentMode = MovementMode.Going;
                vehicle.move(ChemDetectorConstants.lv, a);
            }
            // CD-MV-Beh7: obstacle ? l / reset T --> Avoiding
            else if (event instanceof MovementInputEvent.Obstacle) {
                MovementInputEvent.Obstacle o = (MovementInputEvent.Obstacle) event;
                l = o.l();
                T = clock.nowMs();
                currentMode = MovementMode.Avoiding;
                // CD-MV-FR4 entry: odometer ? d0 absorbed via subsequent Odometer event;
                // changeDirection(l) inlined (CD-OP4); wait(evadeTime).
                if (l == Loc.left) {
                    vehicle.move(ChemDetectorConstants.lv, Angle.Right);
                } else if (l == Loc.right) {
                    vehicle.move(ChemDetectorConstants.lv, Angle.Left);
                } else if (l == Loc.front) {
                    vehicle.move(ChemDetectorConstants.lv, Angle.Back);
                }
                vehicle.pause(ChemDetectorConstants.evadeTime);
            }
            // CD-MV-Beh8: resume --> Waiting
            else if (event instanceof MovementInputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            // CD-MV-Beh11: stop --> Found
            if (event instanceof MovementInputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.emit(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh10: turn ? a --> TryingAgain
            else if (event instanceof MovementInputEvent.Turn) {
                MovementInputEvent.Turn t = (MovementInputEvent.Turn) event;
                a = t.a();
                currentMode = MovementMode.TryingAgain;
                // CD-MV-FR5 entry: move(lv, a)
                vehicle.move(ChemDetectorConstants.lv, a);
            }
            // CD-MV-Beh12: resume --> Waiting
            else if (event instanceof MovementInputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
            // odometer ? d0 (Avoiding entry action absorbs odometer arrivals).
            else if (event instanceof MovementInputEvent.Odometer) {
                MovementInputEvent.Odometer od = (MovementInputEvent.Odometer) event;
                d0 = od.d();
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            // CD-MV-Beh14: stop --> Found
            if (event instanceof MovementInputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.emit(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh13: turn ? a --> TryingAgain (self-loop)
            else if (event instanceof MovementInputEvent.Turn) {
                MovementInputEvent.Turn t = (MovementInputEvent.Turn) event;
                a = t.a();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemDetectorConstants.lv, a);
            }
            // CD-MV-Beh16: obstacle ? l / odometer ? d1 --> AvoidingAgain
            else if (event instanceof MovementInputEvent.Obstacle) {
                MovementInputEvent.Obstacle o = (MovementInputEvent.Obstacle) event;
                l = o.l();
                currentMode = MovementMode.AvoidingAgain;
            }
            // CD-MV-Beh15: resume --> Waiting
            else if (event instanceof MovementInputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
            // odometer ? d1 supports CD-MV-Beh16 stuck-detection seeding.
            else if (event instanceof MovementInputEvent.Odometer) {
                MovementInputEvent.Odometer od = (MovementInputEvent.Odometer) event;
                d1 = od.d();
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            // CD-MV-Beh19: stop --> Found
            if (event instanceof MovementInputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.emit(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh20: resume --> Waiting
            else if (event instanceof MovementInputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
            // CD-MV-Beh17: autonomous makingProgress / reset T --> Avoiding
            else if (makingProgress) {
                T = clock.nowMs();
                currentMode = MovementMode.Avoiding;
                if (l == Loc.left) {
                    vehicle.move(ChemDetectorConstants.lv, Angle.Right);
                } else if (l == Loc.right) {
                    vehicle.move(ChemDetectorConstants.lv, Angle.Left);
                } else if (l == Loc.front) {
                    vehicle.move(ChemDetectorConstants.lv, Angle.Back);
                }
                vehicle.pause(ChemDetectorConstants.evadeTime);
            }
            // CD-MV-Beh18: autonomous stuck --> GettingOut
            else if (stuck) {
                currentMode = MovementMode.GettingOut;
                // CD-MV-FR7 entry: shortRandomWalk(); wait(outPeriod)
                vehicle.shortRandomWalk();
                vehicle.pause(ChemDetectorConstants.outPeriod);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            // CD-MV-Beh22: stop --> Found
            if (event instanceof MovementInputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.emit(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh21: turn ? a --> Going
            else if (event instanceof MovementInputEvent.Turn) {
                MovementInputEvent.Turn t = (MovementInputEvent.Turn) event;
                a = t.a();
                currentMode = MovementMode.Going;
                vehicle.move(ChemDetectorConstants.lv, a);
            }
            // CD-MV-Beh23: resume --> Waiting
            else if (event instanceof MovementInputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Found) {
            // CD-MV-Beh9: autonomous --> Final.
            currentMode = MovementMode.Final;

        } else if (currentMode == MovementMode.Final) {
            // Terminal. No outgoing transitions.
        }
    }
}
