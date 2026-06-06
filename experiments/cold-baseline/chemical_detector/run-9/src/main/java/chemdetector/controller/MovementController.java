package chemdetector.controller;

import chemdetector.actuator.Clock;
import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.datatype.Angle;
import chemdetector.datatype.Loc;
import chemdetector.event.MovementEvent;
import chemdetector.operation.ChangeDirection;
import chemdetector.sensor.MovementSensorService;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Movement subsystem controller. Mode-nested if-else state machine over
 * {@link MovementMode}. Receives obstacle / odometer / turn / stop / resume
 * events; emits flag to the Vehicle when the source has been confirmed.
 *
 * <p>Implements requirements CD-MV-FR1..7, CD-MV-Beh1..23, CD-MV-Var1..4,
 * CD-MV-Clock1.</p>
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;

    private final Vehicle vehicle;
    private final MovementSensorService sensor;
    private final ChangeDirection changeDirection;
    private final Clock clock;
    private final Deque<MovementEvent> queue = new ArrayDeque<>();

    /** CD-MV-Var1: most recent direction. */
    private Angle a = Angle.Front;

    /** CD-MV-Var2: distance recorded on first obstacle. */
    @RoboChartType("real")
    private double d0 = 0.0;

    /** CD-MV-Var3: distance recorded on second obstacle. */
    @RoboChartType("real")
    private double d1 = 0.0;

    /** CD-MV-Var4: side of obstacle. */
    private Loc l = Loc.front;

    /** CD-MV-Clock1: timestamp of last clock reset (ms). */
    @RoboChartType("nat")
    private long T = 0L;

    public MovementController(Vehicle vehicle,
                              MovementSensorService sensor,
                              ChangeDirection changeDirection,
                              Clock clock) {
        this.vehicle = vehicle;
        this.sensor = sensor;
        this.changeDirection = changeDirection;
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

    /** Inter-controller bridge: gas-analysis enqueues turn/stop/resume here. */
    public void enqueue(MovementEvent event) {
        queue.add(event);
    }

    /** Returns the next pending inter-controller event, or null when none. */
    public MovementEvent nextPending() {
        return queue.pollFirst();
    }

    public boolean hasPending() {
        return !queue.isEmpty();
    }

    public void step(MovementEvent event) {
        // Named boolean predicates — atomic guard conditions
        boolean clockBelowStuckPeriod = clock.nowMs() - T < Constants.stuckPeriod;
        boolean distanceAboveStuck = d1 - d0 > Constants.stuckDist;
        boolean makingProgress = clockBelowStuckPeriod || distanceAboveStuck;
        boolean stuckDetected = !clockBelowStuckPeriod && !distanceAboveStuck;

        if (currentMode == MovementMode.Waiting) {
            // During action: randomWalk (re-issued on each tick)
            vehicle.randomWalk();

            // CD-MV-Beh4: Waiting -> Found on stop
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
                // Entry of Found: flag; move(0, Front)
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh2: Waiting -> Going on turn ? a
            else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                a = t.angle();
                currentMode = MovementMode.Going;
                // Entry of Going: move(lv, a)
                vehicle.move(Constants.lv, a);
            }
            // CD-MV-Beh3: Waiting self-loop on resume
            else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
            // Odometer reading updates the sensor service in Waiting
            else if (event instanceof MovementEvent.Odometer) {
                MovementEvent.Odometer od = (MovementEvent.Odometer) event;
                sensor.updateOdometer(od.distance());
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            // CD-MV-Beh6: Going -> Found on stop
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh8: Going -> Waiting on resume
            else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
            // CD-MV-Beh5: Going self-loop on turn ? a
            else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                a = t.angle();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.lv, a);
            }
            // CD-MV-Beh7: Going -> Avoiding on obstacle ? l; reset clock T
            else if (event instanceof MovementEvent.Obstacle) {
                MovementEvent.Obstacle ob = (MovementEvent.Obstacle) event;
                l = ob.loc();
                T = clock.nowMs();
                currentMode = MovementMode.Avoiding;
                // Entry of Avoiding: odometer ? d0; changeDirection(l); wait(evadeTime)
                d0 = sensor.currentOdometer();
                changeDirection.setInput(l);
                changeDirection.compute();
                vehicle.pause(Constants.evadeTime);
            }
            // Odometer reading updates the sensor service
            else if (event instanceof MovementEvent.Odometer) {
                MovementEvent.Odometer od = (MovementEvent.Odometer) event;
                sensor.updateOdometer(od.distance());
                currentMode = MovementMode.Going;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            // CD-MV-Beh11: Avoiding -> Found on stop
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh12: Avoiding -> Waiting on resume
            else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
            // CD-MV-Beh10: Avoiding -> TryingAgain on turn ? a
            else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                a = t.angle();
                currentMode = MovementMode.TryingAgain;
                // Entry of TryingAgain: move(lv, a)
                vehicle.move(Constants.lv, a);
            }
            // Odometer reading updates the sensor service
            else if (event instanceof MovementEvent.Odometer) {
                MovementEvent.Odometer od = (MovementEvent.Odometer) event;
                sensor.updateOdometer(od.distance());
                currentMode = MovementMode.Avoiding;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            // CD-MV-Beh14: TryingAgain -> Found on stop
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh15: TryingAgain -> Waiting on resume
            else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
            // CD-MV-Beh13: TryingAgain self-loop on turn ? a
            else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                a = t.angle();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.lv, a);
            }
            // CD-MV-Beh16: TryingAgain -> AvoidingAgain on obstacle ? l; d1 = odometer
            else if (event instanceof MovementEvent.Obstacle) {
                MovementEvent.Obstacle ob = (MovementEvent.Obstacle) event;
                l = ob.loc();
                d1 = sensor.currentOdometer();
                currentMode = MovementMode.AvoidingAgain;
            }
            // Odometer reading updates the sensor service
            else if (event instanceof MovementEvent.Odometer) {
                MovementEvent.Odometer od = (MovementEvent.Odometer) event;
                sensor.updateOdometer(od.distance());
                currentMode = MovementMode.TryingAgain;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            // CD-MV-Beh19: AvoidingAgain -> Found on stop
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh20: AvoidingAgain -> Waiting on resume
            else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
            // CD-MV-Beh17: AvoidingAgain -> Avoiding when making progress; reset clock T
            else if (makingProgress) {
                T = clock.nowMs();
                currentMode = MovementMode.Avoiding;
                // Re-enter Avoiding entry actions
                d0 = sensor.currentOdometer();
                changeDirection.setInput(l);
                changeDirection.compute();
                vehicle.pause(Constants.evadeTime);
            }
            // CD-MV-Beh18: AvoidingAgain -> GettingOut when stuck
            else if (stuckDetected) {
                currentMode = MovementMode.GettingOut;
                // Entry of GettingOut: shortRandomWalk(); wait(outPeriod)
                vehicle.shortRandomWalk();
                vehicle.pause(Constants.outPeriod);
            }
            // Odometer reading updates the sensor service
            else if (event instanceof MovementEvent.Odometer) {
                MovementEvent.Odometer od = (MovementEvent.Odometer) event;
                sensor.updateOdometer(od.distance());
                currentMode = MovementMode.AvoidingAgain;
            }

        } else if (currentMode == MovementMode.GettingOut) {
            // CD-MV-Beh22: GettingOut -> Found on stop
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh23: GettingOut -> Waiting on resume
            else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
            // CD-MV-Beh21: GettingOut -> Going on turn ? a
            else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                a = t.angle();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.lv, a);
            }
            // Odometer reading updates the sensor service
            else if (event instanceof MovementEvent.Odometer) {
                MovementEvent.Odometer od = (MovementEvent.Odometer) event;
                sensor.updateOdometer(od.distance());
                currentMode = MovementMode.GettingOut;
            }

        } else if (currentMode == MovementMode.Found) {
            // CD-MV-Beh9: Found -> Final autonomously
            currentMode = MovementMode.Final;

        } else if (currentMode == MovementMode.Final) {
            // Terminal state — no outgoing transitions
        }
    }
}
