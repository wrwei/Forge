package chemdetector.controller;

import chemdetector.annotation.RoboChartType;
import chemdetector.clock.SystemClock;
import chemdetector.constants.Constants;
import chemdetector.controller.mode.MovementMode;
import chemdetector.domain.Angle;
import chemdetector.domain.Loc;
import chemdetector.event.MovementInputEvent;
import chemdetector.event.VehicleOutputEvent;
import chemdetector.operation.ChangeDirection;
import chemdetector.sensor.Sensor;
import chemdetector.vehicle.Vehicle;

/**
 * Movement subsystem (CD-MV-FR1..FR7 + CD-MV-Beh1..23).
 *
 * <p>Single-method, mode-nested if-else state machine. Waiting is the initial
 * state. Each mode block lists transitions in priority order with high-priority
 * (stop, resume) overrides duplicated as the first inner branches.</p>
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;

    private final Sensor sensor;
    private final Vehicle vehicle;
    private final ChangeDirection changeDirection;
    private final SystemClock clock;

    private Angle a = Angle.Front;
    private Loc l = Loc.front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    @RoboChartType("nat")
    private int T = 0;

    public MovementController(Sensor sensor,
                              Vehicle vehicle,
                              ChangeDirection changeDirection,
                              SystemClock clock) {
        this.sensor = sensor;
        this.vehicle = vehicle;
        this.changeDirection = changeDirection;
        this.clock = clock;
        // Waiting during-action (CD-MV-FR1): unbounded randomWalk()
        this.vehicle.randomWalk();
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

    /**
     * Convenience entry point for inter-controller events. Equivalent to
     * {@link #step(MovementInputEvent)}.
     */
    public void receive(MovementInputEvent event) {
        step(event);
    }

    public void step(MovementInputEvent event) {
        // --- Named boolean predicates (declared BEFORE the outer if-else) ---
        boolean evtIsTurn = event instanceof MovementInputEvent.Turn;
        boolean evtIsStop = event instanceof MovementInputEvent.Stop;
        boolean evtIsResume = event instanceof MovementInputEvent.Resume;
        boolean evtIsObstacle = event instanceof MovementInputEvent.Obstacle;
        boolean elapsedBelowStuckPeriod = (clock.nowMs() - T) < Constants.stuckPeriod;
        boolean distanceProgressed = (d1 - d0) > Constants.stuckDist;
        boolean elapsedAtOrAboveStuckPeriod = (clock.nowMs() - T) >= Constants.stuckPeriod;
        boolean distanceNotProgressed = (d1 - d0) <= Constants.stuckDist;
        boolean stuckDetected = elapsedAtOrAboveStuckPeriod && distanceNotProgressed;
        boolean makingProgress = elapsedBelowStuckPeriod || distanceProgressed;

        if (currentMode == MovementMode.Waiting) {
            // High-priority stop / resume first, then turn.
            if (evtIsStop) {
                // CD-MV-Beh4: -> Found
                currentMode = MovementMode.Found;
                // Found entry action (CD-MV-FR3): send flag; move(0, Front)
                this.vehicle.apply(new VehicleOutputEvent.Flag());
                this.vehicle.move(0.0, Angle.Front);
            } else if (evtIsResume) {
                // CD-MV-Beh3: self-loop -> Waiting
                currentMode = MovementMode.Waiting;
                this.vehicle.randomWalk();
            } else if (evtIsTurn) {
                MovementInputEvent.Turn t = (MovementInputEvent.Turn) event;
                this.a = t.a();
                // CD-MV-Beh2: -> Going
                currentMode = MovementMode.Going;
                // Going entry action (CD-MV-FR2): move(lv, a)
                this.vehicle.move(Constants.lv, this.a);
            }
        } else if (currentMode == MovementMode.Going) {
            if (evtIsStop) {
                // CD-MV-Beh6: -> Found
                currentMode = MovementMode.Found;
                this.vehicle.apply(new VehicleOutputEvent.Flag());
                this.vehicle.move(0.0, Angle.Front);
            } else if (evtIsResume) {
                // CD-MV-Beh8: -> Waiting
                currentMode = MovementMode.Waiting;
                this.vehicle.randomWalk();
            } else if (evtIsTurn) {
                MovementInputEvent.Turn t = (MovementInputEvent.Turn) event;
                this.a = t.a();
                // CD-MV-Beh5: self-loop -> Going (re-fires entry)
                currentMode = MovementMode.Going;
                this.vehicle.move(Constants.lv, this.a);
            } else if (evtIsObstacle) {
                MovementInputEvent.Obstacle o = (MovementInputEvent.Obstacle) event;
                this.l = o.l();
                // CD-MV-Beh7: -> Avoiding; reset clock T
                this.T = clock.nowMs();
                currentMode = MovementMode.Avoiding;
                // Avoiding entry action (CD-MV-FR4):
                //   odometer ? d0; changeDirection(l); wait(evadeTime)
                this.d0 = sensor.odometer();
                this.changeDirection.setL(this.l);
                this.changeDirection.compute();
                this.vehicle.pause(Constants.evadeTime);
            }
        } else if (currentMode == MovementMode.Avoiding) {
            if (evtIsStop) {
                // CD-MV-Beh11: -> Found
                currentMode = MovementMode.Found;
                this.vehicle.apply(new VehicleOutputEvent.Flag());
                this.vehicle.move(0.0, Angle.Front);
            } else if (evtIsResume) {
                // CD-MV-Beh12: -> Waiting
                currentMode = MovementMode.Waiting;
                this.vehicle.randomWalk();
            } else if (evtIsTurn) {
                MovementInputEvent.Turn t = (MovementInputEvent.Turn) event;
                this.a = t.a();
                // CD-MV-Beh10: -> TryingAgain
                currentMode = MovementMode.TryingAgain;
                // TryingAgain entry action (CD-MV-FR5): move(lv, a)
                this.vehicle.move(Constants.lv, this.a);
            }
        } else if (currentMode == MovementMode.TryingAgain) {
            if (evtIsStop) {
                // CD-MV-Beh14: -> Found
                currentMode = MovementMode.Found;
                this.vehicle.apply(new VehicleOutputEvent.Flag());
                this.vehicle.move(0.0, Angle.Front);
            } else if (evtIsResume) {
                // CD-MV-Beh15: -> Waiting
                currentMode = MovementMode.Waiting;
                this.vehicle.randomWalk();
            } else if (evtIsTurn) {
                MovementInputEvent.Turn t = (MovementInputEvent.Turn) event;
                this.a = t.a();
                // CD-MV-Beh13: self-loop -> TryingAgain (re-fires entry)
                currentMode = MovementMode.TryingAgain;
                this.vehicle.move(Constants.lv, this.a);
            } else if (evtIsObstacle) {
                MovementInputEvent.Obstacle o = (MovementInputEvent.Obstacle) event;
                this.l = o.l();
                // CD-MV-Beh16: -> AvoidingAgain; action odometer ? d1
                this.d1 = sensor.odometer();
                currentMode = MovementMode.AvoidingAgain;
            }
        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (evtIsStop) {
                // CD-MV-Beh19: -> Found
                currentMode = MovementMode.Found;
                this.vehicle.apply(new VehicleOutputEvent.Flag());
                this.vehicle.move(0.0, Angle.Front);
            } else if (evtIsResume) {
                // CD-MV-Beh20: -> Waiting
                currentMode = MovementMode.Waiting;
                this.vehicle.randomWalk();
            } else if (makingProgress) {
                // CD-MV-Beh17: -> Avoiding; reset clock T
                this.T = clock.nowMs();
                currentMode = MovementMode.Avoiding;
                this.d0 = sensor.odometer();
                this.changeDirection.setL(this.l);
                this.changeDirection.compute();
                this.vehicle.pause(Constants.evadeTime);
            } else if (stuckDetected) {
                // CD-MV-Beh18: -> GettingOut
                currentMode = MovementMode.GettingOut;
                // GettingOut entry action (CD-MV-FR7): shortRandomWalk(); wait(outPeriod)
                this.vehicle.shortRandomWalk();
                this.vehicle.pause(Constants.outPeriod);
            }
        } else if (currentMode == MovementMode.GettingOut) {
            if (evtIsStop) {
                // CD-MV-Beh22: -> Found
                currentMode = MovementMode.Found;
                this.vehicle.apply(new VehicleOutputEvent.Flag());
                this.vehicle.move(0.0, Angle.Front);
            } else if (evtIsResume) {
                // CD-MV-Beh23: -> Waiting
                currentMode = MovementMode.Waiting;
                this.vehicle.randomWalk();
            } else if (evtIsTurn) {
                MovementInputEvent.Turn t = (MovementInputEvent.Turn) event;
                this.a = t.a();
                // CD-MV-Beh21: -> Going
                currentMode = MovementMode.Going;
                this.vehicle.move(Constants.lv, this.a);
            }
        } else if (currentMode == MovementMode.Found) {
            // CD-MV-Beh9: autonomous -> j1
            currentMode = MovementMode.J1;
        } else if (currentMode == MovementMode.J1) {
            // Final state — no outgoing transitions.
            currentMode = MovementMode.J1;
        }
    }
}
