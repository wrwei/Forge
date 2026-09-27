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
import chemical_detector.sensor.OdometerSensor;
import chemical_detector.timing.Clock;

/**
 * Movement subsystem: random-walk search, travel in the commanded direction, obstacle
 * avoidance with stuck detection and recovery, and halting once the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final OdometerSensor odometerSensor;
    private final Vehicle vehicle;
    private final Actuator actuator;
    private final Clock timer;

    private Angle a = Angle.Front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    private Loc l = Loc.front;
    @RoboChartType("nat")
    private long T = 0L;

    public MovementController(OdometerSensor odometerSensor, Vehicle vehicle, Actuator actuator, Clock timer) {
        this.odometerSensor = odometerSensor;
        this.vehicle = vehicle;
        this.actuator = actuator;
        this.timer = timer;
    }

    /**
     * Runs one control cycle.
     *
     * @param event the input received this cycle, or {@code null} when none arrived
     *              (only autonomous transitions can then fire)
     */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - T < DetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > DetectorConstants.STUCK_DIST;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.angle();
                vehicle.move(DetectorConstants.LV, a);
                currentMode = MovementMode.Going;
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.angle();
                vehicle.move(DetectorConstants.LV, a);
                currentMode = MovementMode.Going;
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                l = obstacleEvent.loc();
                T = timer.nowMs();
                d0 = odometerSensor.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(DetectorConstants.EVADE_TIME);
                currentMode = MovementMode.Avoiding;
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                l = obstacleEvent.loc();
                currentMode = MovementMode.Found;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.angle();
                vehicle.move(DetectorConstants.LV, a);
                currentMode = MovementMode.TryingAgain;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.angle();
                vehicle.move(DetectorConstants.LV, a);
                currentMode = MovementMode.TryingAgain;
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                l = obstacleEvent.loc();
                d1 = odometerSensor.odometer();
                currentMode = MovementMode.AvoidingAgain;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (withinStuckPeriod || advancedBeyondStuckDist) {
                T = timer.nowMs();
                d0 = odometerSensor.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(DetectorConstants.EVADE_TIME);
                currentMode = MovementMode.Avoiding;
            } else if (!withinStuckPeriod && !advancedBeyondStuckDist) {
                vehicle.shortRandomWalk();
                vehicle.pause(DetectorConstants.OUT_PERIOD);
                currentMode = MovementMode.GettingOut;
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.angle();
                vehicle.move(DetectorConstants.LV, a);
                currentMode = MovementMode.Going;
            }
        }
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
}
