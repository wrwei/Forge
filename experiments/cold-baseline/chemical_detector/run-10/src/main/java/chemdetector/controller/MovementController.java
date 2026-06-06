package chemdetector.controller;

import chemdetector.annotation.RoboChartType;
import chemdetector.clock.Clock;
import chemdetector.constants.Constants;
import chemdetector.datatype.Angle;
import chemdetector.datatype.Loc;
import chemdetector.event.MovementInputEvent;
import chemdetector.event.MovementOutputEvent;
import chemdetector.operation.ChangeDirection;
import chemdetector.actuator.Actuator;
import chemdetector.vehicle.Vehicle;

/**
 * Movement subsystem controller. Implements CD-MV-FR1..7 and
 * CD-MV-Beh1..23 in the mode-nested if-else pattern required by the
 * formal-extraction pipeline.
 *
 * <p>State variables: {@code a} (CD-MV-Var1), {@code d0} (CD-MV-Var2),
 * {@code d1} (CD-MV-Var3), {@code l} (CD-MV-Var4). Clock T
 * (CD-MV-Clock1) tracks the current evasion sequence.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;

    private final Vehicle vehicle;
    private final Actuator actuator;
    private final ChangeDirection changeDirection;
    private final Clock T;

    private Angle a;
    private Loc l;

    @RoboChartType("real")
    private double d0;

    @RoboChartType("real")
    private double d1;

    /**
     * Latest odometer reading. Captured on every Odometer event so the
     * Avoiding entry-action {@code odometer ? d0} and the
     * TryingAgain -> AvoidingAgain action {@code odometer ? d1} can
     * sample a current value.
     */
    @RoboChartType("real")
    private double odometer;

    public MovementController(Vehicle vehicle, Actuator actuator,
                              ChangeDirection changeDirection, Clock T) {
        this.vehicle = vehicle;
        this.actuator = actuator;
        this.changeDirection = changeDirection;
        this.T = T;
        this.a = Angle.Front;
        this.l = Loc.front;
        this.d0 = 0.0;
        this.d1 = 0.0;
        this.odometer = 0.0;
        // CD-MV-FR1: Waiting during action is randomWalk.
        vehicle.randomWalk();
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    public Angle a() {
        return a;
    }

    public Loc l() {
        return l;
    }

    @RoboChartType("real")
    public double d0() {
        return d0;
    }

    @RoboChartType("real")
    public double d1() {
        return d1;
    }

    public void step(MovementInputEvent event) {
        // --- Side effect: capture latest odometer reading. ---
        if (event instanceof MovementInputEvent.Odometer) {
            MovementInputEvent.Odometer o = (MovementInputEvent.Odometer) event;
            odometer = o.value();
        }

        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean evtIsTurn = event instanceof MovementInputEvent.Turn;
        boolean evtIsStop = event instanceof MovementInputEvent.Stop;
        boolean evtIsResume = event instanceof MovementInputEvent.Resume;
        boolean evtIsObstacle = event instanceof MovementInputEvent.Obstacle;

        boolean withinStuckPeriod = T.since() < (long) Constants.stuckPeriod;
        boolean distanceProgress = d1 - d0 > Constants.stuckDist;
        boolean makingProgress = withinStuckPeriod || distanceProgress;
        boolean periodElapsed = T.since() >= (long) Constants.stuckPeriod;
        boolean noDistanceProgress = d1 - d0 <= Constants.stuckDist;
        boolean stuck = periodElapsed && noDistanceProgress;

        // --- Pure mode-nested if-else with inline entry actions ---
        if (currentMode == MovementMode.Waiting) {
            // CD-MV-Beh4: Stop -> Found
            if (evtIsStop) {
                currentMode = MovementMode.Found;
                actuator.emit(new MovementOutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh2: Turn ? a -> Going
            else if (evtIsTurn) {
                MovementInputEvent.Turn t = (MovementInputEvent.Turn) event;
                a = t.value();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.lv, a);
            }
            // CD-MV-Beh3: Resume -> Waiting (self-loop)
            else if (evtIsResume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
        } else if (currentMode == MovementMode.Going) {
            // CD-MV-Beh6: Stop -> Found
            if (evtIsStop) {
                currentMode = MovementMode.Found;
                actuator.emit(new MovementOutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh8: Resume -> Waiting
            else if (evtIsResume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh7: Obstacle ? l -> Avoiding, reset clock T
            else if (evtIsObstacle) {
                MovementInputEvent.Obstacle o = (MovementInputEvent.Obstacle) event;
                l = o.value();
                T.reset();
                currentMode = MovementMode.Avoiding;
                d0 = odometer;
                changeDirection.setL(l);
                changeDirection.compute();
                vehicle.pause(Constants.evadeTime);
            }
            // CD-MV-Beh5: Turn ? a -> Going (self-loop)
            else if (evtIsTurn) {
                MovementInputEvent.Turn t = (MovementInputEvent.Turn) event;
                a = t.value();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.lv, a);
            }
        } else if (currentMode == MovementMode.Avoiding) {
            // CD-MV-Beh11: Stop -> Found
            if (evtIsStop) {
                currentMode = MovementMode.Found;
                actuator.emit(new MovementOutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh12: Resume -> Waiting
            else if (evtIsResume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh10: Turn ? a -> TryingAgain
            else if (evtIsTurn) {
                MovementInputEvent.Turn t = (MovementInputEvent.Turn) event;
                a = t.value();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.lv, a);
            }
        } else if (currentMode == MovementMode.TryingAgain) {
            // CD-MV-Beh14: Stop -> Found
            if (evtIsStop) {
                currentMode = MovementMode.Found;
                actuator.emit(new MovementOutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh15: Resume -> Waiting
            else if (evtIsResume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh16: Obstacle ? l -> AvoidingAgain; d1 = odometer
            else if (evtIsObstacle) {
                MovementInputEvent.Obstacle o = (MovementInputEvent.Obstacle) event;
                l = o.value();
                d1 = odometer;
                currentMode = MovementMode.AvoidingAgain;
            }
            // CD-MV-Beh13: Turn ? a -> TryingAgain (self-loop)
            else if (evtIsTurn) {
                MovementInputEvent.Turn t = (MovementInputEvent.Turn) event;
                a = t.value();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.lv, a);
            }
        } else if (currentMode == MovementMode.AvoidingAgain) {
            // CD-MV-Beh19: Stop -> Found
            if (evtIsStop) {
                currentMode = MovementMode.Found;
                actuator.emit(new MovementOutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh20: Resume -> Waiting
            else if (evtIsResume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh17: making progress -> Avoiding (reset T)
            else if (makingProgress) {
                T.reset();
                currentMode = MovementMode.Avoiding;
                d0 = odometer;
                changeDirection.setL(l);
                changeDirection.compute();
                vehicle.pause(Constants.evadeTime);
            }
            // CD-MV-Beh18: stuck -> GettingOut
            else if (stuck) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(Constants.outPeriod);
            }
        } else if (currentMode == MovementMode.GettingOut) {
            // CD-MV-Beh22: Stop -> Found
            if (evtIsStop) {
                currentMode = MovementMode.Found;
                actuator.emit(new MovementOutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh23: Resume -> Waiting
            else if (evtIsResume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh21: Turn ? a -> Going
            else if (evtIsTurn) {
                MovementInputEvent.Turn t = (MovementInputEvent.Turn) event;
                a = t.value();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.lv, a);
            }
        } else if (currentMode == MovementMode.Found) {
            // CD-MV-Beh9: Found -> Final (autonomous).
            currentMode = MovementMode.Final;
        } else if (currentMode == MovementMode.Final) {
            // Terminal state — no outgoing transitions.
        }
    }
}
