package chemdetector.controller;

import chemdetector.actuator.Actuator;
import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.MovementMode;
import chemdetector.sensor.Clock;
import chemdetector.sensor.VehicleSensors;

/**
 * Movement subsystem: random-walk search, direction following,
 * obstacle avoidance, and stuck recovery.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;

    private Angle a = Angle.Front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    private Loc l = Loc.front;
    private long tStuck = 0L;

    private final VehicleSensors sensors;
    private final Vehicle vehicle;
    private final Actuator actuator;
    private final Clock timer;

    public MovementController(VehicleSensors sensors, Vehicle vehicle,
            Actuator actuator, Clock timer) {
        this.sensors = sensors;
        this.vehicle = vehicle;
        this.actuator = actuator;
        this.timer = timer;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - tStuck < Constants.STUCK_PERIOD;
        boolean progressAboveStuckDist = d1 - d0 > Constants.STUCK_DIST;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn tn = (InputEvent.Turn) event;
                this.a = tn.payload();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, this.a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn tn = (InputEvent.Turn) event;
                this.a = tn.payload();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, this.a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle ob = (InputEvent.Obstacle) event;
                this.l = ob.payload();
                currentMode = MovementMode.Avoiding;
                this.tStuck = timer.nowMs();
                this.d0 = sensors.odometer();
                vehicle.changeDirection(this.l);
                vehicle.pause(Constants.EVADE_TIME);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn tn = (InputEvent.Turn) event;
                this.a = tn.payload();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.LV, this.a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn tn = (InputEvent.Turn) event;
                this.a = tn.payload();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.LV, this.a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle ob = (InputEvent.Obstacle) event;
                this.l = ob.payload();
                currentMode = MovementMode.AvoidingAgain;
                this.d1 = sensors.odometer();
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
            } else if (withinStuckPeriod || progressAboveStuckDist) {
                currentMode = MovementMode.Avoiding;
                this.tStuck = timer.nowMs();
                this.d0 = sensors.odometer();
                vehicle.changeDirection(this.l);
                vehicle.pause(Constants.EVADE_TIME);
            } else if (!withinStuckPeriod && !progressAboveStuckDist) {
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
                InputEvent.Turn tn = (InputEvent.Turn) event;
                this.a = tn.payload();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, this.a);
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
