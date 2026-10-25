package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.Constants;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.Sensor;
import chemical_detector.timing.Clock;
import chemical_detector.types.Angle;
import chemical_detector.types.Loc;

/**
 * Movement subsystem: random-walk search, travel in the commanded direction, obstacle
 * avoidance with stuck recovery, and halting with a flag once the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final Sensor sensor;
    private final Vehicle vehicle;
    private final ChangeDirection steering;
    private final Actuator actuator;
    private final Clock timer;

    private Angle a = Angle.Front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    private Loc l = Loc.front;
    @RoboChartType("nat")
    private long T = 0;

    public MovementController(Sensor sensor, Vehicle vehicle, ChangeDirection steering,
                              Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.vehicle = vehicle;
        this.steering = steering;
        this.actuator = actuator;
        this.timer = timer;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    /** Runs one control cycle against {@code event}. */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - T < Constants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > Constants.STUCK_DIST;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.a();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, a);
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.a();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle ob = (InputEvent.Obstacle) event;
                l = ob.l();
                T = timer.nowMs();
                currentMode = MovementMode.Avoiding;
                d0 = sensor.odometer();
                steering.changeDirection(l);
                vehicle.pause(Constants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof InputEvent.Tick) {
                currentMode = MovementMode.Found;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.a();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.LV, a);
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.a();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle ob = (InputEvent.Obstacle) event;
                l = ob.l();
                d1 = sensor.odometer();
                currentMode = MovementMode.AvoidingAgain;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (withinStuckPeriod || advancedBeyondStuckDist) {
                T = timer.nowMs();
                currentMode = MovementMode.Avoiding;
                d0 = sensor.odometer();
                steering.changeDirection(l);
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
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.a();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, a);
            }
        }
    }
}
