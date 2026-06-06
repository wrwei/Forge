package chemdetector.controller;

import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.DetectorConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.MoveMode;
import chemdetector.sensor.Clock;
import chemdetector.sensor.DetectorSensors;

/**
 * Movement subsystem (CD-ARCH2): random-walk search, direction-command
 * following, obstacle avoidance, and stuck recovery.
 */
public final class MovementController {

    private MoveMode currentMode = MoveMode.Waiting;
    private final DetectorSensors sensor;
    private final Vehicle vehicle;
    private final Clock clock;

    private Angle a = Angle.Front;
    @RoboChartType("real")
    private double d0;
    @RoboChartType("real")
    private double d1;
    private Loc l = Loc.front;
    private long stuckTimer;

    public MovementController(DetectorSensors sensor, Vehicle vehicle, Clock clock) {
        this.sensor = sensor;
        this.vehicle = vehicle;
        this.clock = clock;
    }

    public MoveMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean withinStuckPeriod = clock.nowMs() - stuckTimer < DetectorConstants.STUCK_PERIOD;
        boolean escapedDistance = d1 - d0 > DetectorConstants.STUCK_DIST;
        boolean makingProgress = withinStuckPeriod || escapedDistance;

        if (currentMode == MoveMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MoveMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MoveMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.payload();
                currentMode = MoveMode.Going;
                vehicle.move(DetectorConstants.LV, a);
            }
        } else if (currentMode == MoveMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MoveMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MoveMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.payload();
                currentMode = MoveMode.Going;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle o = (InputEvent.Obstacle) event;
                l = o.payload();
                stuckTimer = clock.nowMs();
                currentMode = MoveMode.Avoiding;
                d0 = sensor.odometerDistance();
                vehicle.changeDirection(l);
                vehicle.pause(DetectorConstants.EVADE_TIME);
            }
        } else if (currentMode == MoveMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MoveMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MoveMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.payload();
                currentMode = MoveMode.TryingAgain;
                vehicle.move(DetectorConstants.LV, a);
            }
        } else if (currentMode == MoveMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MoveMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MoveMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.payload();
                currentMode = MoveMode.TryingAgain;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle o = (InputEvent.Obstacle) event;
                l = o.payload();
                d1 = sensor.odometerDistance();
                currentMode = MoveMode.AvoidingAgain;
            }
        } else if (currentMode == MoveMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MoveMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MoveMode.Waiting;
            } else if (makingProgress) {
                stuckTimer = clock.nowMs();
                currentMode = MoveMode.Avoiding;
                d0 = sensor.odometerDistance();
                vehicle.changeDirection(l);
                vehicle.pause(DetectorConstants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MoveMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(DetectorConstants.OUT_PERIOD);
            }
        } else if (currentMode == MoveMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MoveMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MoveMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.payload();
                currentMode = MoveMode.Going;
                vehicle.move(DetectorConstants.LV, a);
            }
        } else if (currentMode == MoveMode.Found) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MoveMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
        }
    }
}
