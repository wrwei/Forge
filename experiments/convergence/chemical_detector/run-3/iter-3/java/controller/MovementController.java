package chemdetector.controller;

import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.DetectorConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.MovementMode;
import chemdetector.sensor.Clock;
import chemdetector.sensor.GasSensorArray;

/**
 * Movement subsystem: random-walk search, direction following,
 * obstacle avoidance, and stuck recovery. Emits flag to the Vehicle
 * when the chemical source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final GasSensorArray sensor;
    private final Vehicle vehicle;
    private final Clock timer;

    private Angle a = Angle.Front;

    @RoboChartType("real")
    private double d0;

    @RoboChartType("real")
    private double d1;

    private Loc l = Loc.front;

    private long stuckClock;

    public MovementController(GasSensorArray sensor, Vehicle vehicle, Clock timer) {
        this.sensor = sensor;
        this.vehicle = vehicle;
        this.timer = timer;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - stuckClock < DetectorConstants.STUCK_PERIOD;
        boolean escapedDistance = d1 - d0 > DetectorConstants.STUCK_DIST;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.direction();
                currentMode = MovementMode.Going;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.direction();
                currentMode = MovementMode.Going;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle o = (InputEvent.Obstacle) event;
                l = o.side();
                currentMode = MovementMode.Avoiding;
                stuckClock = timer.nowMs();
                d0 = sensor.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(DetectorConstants.EVADE_TIME);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle o = (InputEvent.Obstacle) event;
                l = o.side();
                currentMode = MovementMode.AvoidingAgain;
                d1 = sensor.odometer();
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (withinStuckPeriod || escapedDistance) {
                currentMode = MovementMode.Avoiding;
                stuckClock = timer.nowMs();
                d0 = sensor.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(DetectorConstants.EVADE_TIME);
            } else if (!withinStuckPeriod && !escapedDistance) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(DetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.direction();
                currentMode = MovementMode.Going;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
            }
        }
    }
}
