package chemical_detector.controller;

import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.VehicleSensors;
import chemical_detector.timing.Clock;

/**
 * Movement subsystem: random-walk search, travel in the commanded direction,
 * obstacle avoidance with stuck detection and recovery, and halting with the
 * flag signal once the source has been found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.WAITING;
    private final Vehicle vehicle;
    private final VehicleSensors sensors;
    private final ChangeDirection avoidance;
    private final Clock clock;

    private Angle a = Angle.Front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    private Loc l = Loc.front;
    /** Start of the current evasion sequence (clock T). */
    @RoboChartType("nat")
    private long evasionTimer = 0L;

    public MovementController(Vehicle vehicle, VehicleSensors sensors, ChangeDirection avoidance, Clock clock) {
        this.vehicle = vehicle;
        this.sensors = sensors;
        this.avoidance = avoidance;
        this.clock = clock;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    /** Most recent commanded direction. */
    public Angle a() {
        return a;
    }

    /** Distance travelled when the current evasion sequence began. */
    @RoboChartType("real")
    public double d0() {
        return d0;
    }

    /** Distance travelled when a second obstacle was hit in the current evasion sequence. */
    @RoboChartType("real")
    public double d1() {
        return d1;
    }

    /** Side of the most recent obstacle. */
    public Loc l() {
        return l;
    }

    /** Performs one control cycle. */
    public void step(InputEvent event) {
        boolean evasionWithinStuckPeriod = clock.now() - this.evasionTimer < ChemConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = this.d1 - this.d0 > ChemConstants.STUCK_DIST;
        boolean makingProgress = evasionWithinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.WAITING) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                this.a = t.a();
                currentMode = MovementMode.GOING;
                vehicle.move(ChemConstants.LV, this.a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
            }

        } else if (currentMode == MovementMode.GOING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                this.a = t.a();
                currentMode = MovementMode.GOING;
                vehicle.move(ChemConstants.LV, this.a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle o = (InputEvent.Obstacle) event;
                this.l = o.l();
                this.evasionTimer = clock.now();
                currentMode = MovementMode.AVOIDING;
                this.d0 = sensors.odometer();
                avoidance.changeDirection(this.l);
                vehicle.pause(ChemConstants.EVADE_TIME);
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
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                this.a = t.a();
                currentMode = MovementMode.TRYING_AGAIN;
                vehicle.move(ChemConstants.LV, this.a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
            }

        } else if (currentMode == MovementMode.TRYING_AGAIN) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                this.a = t.a();
                currentMode = MovementMode.TRYING_AGAIN;
                vehicle.move(ChemConstants.LV, this.a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle o = (InputEvent.Obstacle) event;
                this.l = o.l();
                currentMode = MovementMode.AVOIDING_AGAIN;
                this.d1 = sensors.odometer();
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
            }

        } else if (currentMode == MovementMode.AVOIDING_AGAIN) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
            } else if (makingProgress) {
                this.evasionTimer = clock.now();
                currentMode = MovementMode.AVOIDING;
                this.d0 = sensors.odometer();
                avoidance.changeDirection(this.l);
                vehicle.pause(ChemConstants.EVADE_TIME);
            } else {
                currentMode = MovementMode.GETTING_OUT;
                vehicle.shortRandomWalk();
                vehicle.pause(ChemConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GETTING_OUT) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                this.a = t.a();
                currentMode = MovementMode.GOING;
                vehicle.move(ChemConstants.LV, this.a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
            }
        }
    }
}
