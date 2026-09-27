package chemical_detector.controller;

import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.DetectorConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.Loc;
import chemical_detector.event.MovementEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.OdometerSensor;

/**
 * Movement subsystem: random-walk search, following turn commands, obstacle
 * avoidance with stuck detection and recovery, and halting once the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.WAITING;
    private final Vehicle vehicle;
    private final ChangeDirection steering;
    private final OdometerSensor odometer;
    private final Clock timer;

    private Angle a = Angle.Front;
    private Loc l = Loc.front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    @RoboChartType("nat")
    private long evasionStart = 0L;

    public MovementController(Vehicle vehicle, ChangeDirection steering, OdometerSensor odometer, Clock timer) {
        this.vehicle = vehicle;
        this.steering = steering;
        this.odometer = odometer;
        this.timer = timer;
    }

    /**
     * Executes one control cycle.
     *
     * @param event the event received this cycle, or {@code null} when none arrived
     */
    public void step(MovementEvent event) {
        boolean withinStuckPeriod = timer.now() - evasionStart < DetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > DetectorConstants.STUCK_DIST;
        boolean makingProgress = withinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.WAITING) {
            vehicle.randomWalk();
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.WAITING;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.direction();
                currentMode = MovementMode.GOING;
                vehicle.move(DetectorConstants.LV, this.a);
            }

        } else if (currentMode == MovementMode.GOING) {
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.WAITING;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.direction();
                currentMode = MovementMode.GOING;
                vehicle.move(DetectorConstants.LV, this.a);
            } else if (event instanceof MovementEvent.Obstacle) {
                MovementEvent.Obstacle o = (MovementEvent.Obstacle) event;
                this.l = o.side();
                currentMode = MovementMode.AVOIDING;
                this.d0 = odometer.distanceTravelled();
                this.evasionStart = timer.now();
                steering.changeDirection(this.l);
                vehicle.pause(DetectorConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.FOUND) {
            if (event instanceof MovementEvent.Obstacle) {
                MovementEvent.Obstacle o = (MovementEvent.Obstacle) event;
                this.l = o.side();
                currentMode = MovementMode.FOUND;
            }

        } else if (currentMode == MovementMode.AVOIDING) {
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.WAITING;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.direction();
                currentMode = MovementMode.TRYING_AGAIN;
                vehicle.move(DetectorConstants.LV, this.a);
            }

        } else if (currentMode == MovementMode.TRYING_AGAIN) {
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.WAITING;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.direction();
                currentMode = MovementMode.TRYING_AGAIN;
                vehicle.move(DetectorConstants.LV, this.a);
            } else if (event instanceof MovementEvent.Obstacle) {
                MovementEvent.Obstacle o = (MovementEvent.Obstacle) event;
                this.l = o.side();
                currentMode = MovementMode.AVOIDING_AGAIN;
                this.d1 = odometer.distanceTravelled();
            }

        } else if (currentMode == MovementMode.AVOIDING_AGAIN) {
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.WAITING;
            } else if (makingProgress) {
                currentMode = MovementMode.AVOIDING;
                this.d0 = odometer.distanceTravelled();
                this.evasionStart = timer.now();
                steering.changeDirection(this.l);
                vehicle.pause(DetectorConstants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GETTING_OUT;
                vehicle.shortRandomWalk();
                vehicle.pause(DetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GETTING_OUT) {
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.WAITING;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.direction();
                currentMode = MovementMode.GOING;
                vehicle.move(DetectorConstants.LV, this.a);
            }
        }
    }

    public MovementMode currentMode() {
        return currentMode;
    }
}
