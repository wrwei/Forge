package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.Constants;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.ChemSensor;
import chemical_detector.timing.Clock;
import chemical_detector.types.Angle;
import chemical_detector.types.Loc;

/**
 * Movement subsystem: random-walk search, following turn commands, obstacle avoidance with
 * stuck recovery, and halting with the flag signal once the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final ChemSensor sensor;
    private final Vehicle vehicle;
    private final ChangeDirection steering;
    private final Actuator actuator;
    private final Clock timer;

    private Angle a = Angle.Front;
    @RoboChartType("real")
    private double d0;
    @RoboChartType("real")
    private double d1;
    private Loc l = Loc.front;
    @RoboChartType("nat")
    private long evasionStart;

    public MovementController(ChemSensor sensor, Vehicle vehicle, ChangeDirection steering,
                              Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.vehicle = vehicle;
        this.steering = steering;
        this.actuator = actuator;
        this.timer = timer;
    }

    /** Runs one control cycle: at most one transition, event-triggered or autonomous. */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - evasionStart < Constants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > Constants.STUCK_DIST;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                this.a = turnEvent.angle();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, this.a);
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                this.a = turnEvent.angle();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, this.a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                this.l = obstacleEvent.side();
                currentMode = MovementMode.Avoiding;
                this.evasionStart = timer.nowMs();
                this.d0 = sensor.odometer();
                steering.changeDirection(this.l);
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
                vehicle.move(0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                this.a = turnEvent.angle();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.LV, this.a);
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                this.a = turnEvent.angle();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.LV, this.a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                this.l = obstacleEvent.side();
                currentMode = MovementMode.AvoidingAgain;
                this.d1 = sensor.odometer();
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (withinStuckPeriod || advancedBeyondStuckDist) {
                currentMode = MovementMode.Avoiding;
                this.evasionStart = timer.nowMs();
                this.d0 = sensor.odometer();
                steering.changeDirection(this.l);
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
                vehicle.move(0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                this.a = turnEvent.angle();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, this.a);
            }
        }
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
}
