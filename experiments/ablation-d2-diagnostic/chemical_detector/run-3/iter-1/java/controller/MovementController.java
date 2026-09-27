package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.Constants;
import chemical_detector.data.Angle;
import chemical_detector.data.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.sensor.Sensor;
import chemical_detector.timing.Clock;

/**
 * Movement subsystem: random-walk search, travel in the commanded direction,
 * obstacle avoidance with stuck recovery, and halting once the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;

    private final Sensor sensor;
    private final Vehicle vehicle;
    private final Actuator actuator;
    private final Clock timer;

    private Angle a = Angle.Front;
    private Loc l = Loc.front;
    @RoboChartType("real")
    private double d0;
    @RoboChartType("real")
    private double d1;
    @RoboChartType("nat")
    private long T;

    public MovementController(Sensor sensor, Vehicle vehicle, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.vehicle = vehicle;
        this.actuator = actuator;
        this.timer = timer;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    /** Executes one control cycle, reacting to {@code event}. */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - T < Constants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > Constants.STUCK_DIST;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                a = turn.a();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                a = turn.a();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacle = (InputEvent.Obstacle) event;
                l = obstacle.l();
                currentMode = MovementMode.Avoiding;
                d0 = sensor.odometer();
                T = timer.nowMs();
                vehicle.changeDirection(l);
                vehicle.pause(Constants.EVADE_TIME);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Found) {
            // The source is found: the Vehicle stays halted; obstacles are only noted.
            if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacle = (InputEvent.Obstacle) event;
                l = obstacle.l();
                currentMode = MovementMode.Found;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                a = turn.a();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                a = turn.a();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacle = (InputEvent.Obstacle) event;
                l = obstacle.l();
                currentMode = MovementMode.AvoidingAgain;
                d1 = sensor.odometer();
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (withinStuckPeriod || advancedBeyondStuckDist) {
                currentMode = MovementMode.Avoiding;
                d0 = sensor.odometer();
                T = timer.nowMs();
                vehicle.changeDirection(l);
                vehicle.pause(Constants.EVADE_TIME);
            } else if (!withinStuckPeriod && !advancedBeyondStuckDist) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(Constants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                a = turn.a();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        }
    }

    Angle a() {
        return a;
    }

    Loc l() {
        return l;
    }

    @RoboChartType("real")
    double d0() {
        return d0;
    }

    @RoboChartType("real")
    double d1() {
        return d1;
    }

    @RoboChartType("nat")
    long evasionStart() {
        return T;
    }
}
