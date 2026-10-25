package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.DetectorConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.VehicleSensors;
import chemical_detector.timing.Clock;

/** Movement subsystem: random-walk search, following turn commands, obstacle avoidance and stuck recovery. */
public final class Movement {

    private MovementMode currentMode = MovementMode.WAITING;

    private final Vehicle vehicle;

    private final VehicleSensors sensors;

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

    public Movement(Vehicle vehicle, VehicleSensors sensors, ChangeDirection steering, Actuator actuator,
            Clock timer) {
        this.vehicle = vehicle;
        this.sensors = sensors;
        this.steering = steering;
        this.actuator = actuator;
        this.timer = timer;
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

    /** Performs at most one transition of the movement state machine. */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - T < DetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > DetectorConstants.STUCK_DIST;

        if (currentMode == MovementMode.WAITING) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.angle();
                currentMode = MovementMode.GOING;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
            }
        } else if (currentMode == MovementMode.GOING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.angle();
                currentMode = MovementMode.GOING;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                l = obstacleEvent.side();
                currentMode = MovementMode.AVOIDING;
                T = timer.nowMs();
                d0 = sensors.odometer();
                steering.changeDirection(l);
                vehicle.pause(DetectorConstants.EVADE_TIME);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
            }
        } else if (currentMode == MovementMode.FOUND) {
            if (event instanceof InputEvent.Tick) {
                currentMode = MovementMode.FOUND;
            }
        } else if (currentMode == MovementMode.AVOIDING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.angle();
                currentMode = MovementMode.TRYING_AGAIN;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
            }
        } else if (currentMode == MovementMode.TRYING_AGAIN) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.angle();
                currentMode = MovementMode.TRYING_AGAIN;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                l = obstacleEvent.side();
                currentMode = MovementMode.AVOIDING_AGAIN;
                d1 = sensors.odometer();
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
            }
        } else if (currentMode == MovementMode.AVOIDING_AGAIN) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
            } else if (withinStuckPeriod || advancedBeyondStuckDist) {
                currentMode = MovementMode.AVOIDING;
                T = timer.nowMs();
                d0 = sensors.odometer();
                steering.changeDirection(l);
                vehicle.pause(DetectorConstants.EVADE_TIME);
            } else if (!withinStuckPeriod && !advancedBeyondStuckDist) {
                currentMode = MovementMode.GETTING_OUT;
                vehicle.shortRandomWalk();
                vehicle.pause(DetectorConstants.OUT_PERIOD);
            }
        } else if (currentMode == MovementMode.GETTING_OUT) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.angle();
                currentMode = MovementMode.GOING;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
            }
        }
    }
}
