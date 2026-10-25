package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.Constants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.Sensor;

/**
 * Movement subsystem: random-walk search, travel in the commanded direction,
 * obstacle avoidance with stuck recovery, and halting once the chemical source
 * is found.
 */
public final class Movement {

    private MovementMode currentMode = MovementMode.WAITING;
    private final Sensor sensor;
    private final Vehicle vehicle;
    private final ChangeDirection evasion;
    private final Actuator actuator;
    private final Clock timer;

    private Angle a = Angle.Front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    private Loc l = Loc.front;
    /** Start time of the current evasion sequence (clock T). */
    @RoboChartType("nat")
    private long T = 0;

    public Movement(Sensor sensor, Vehicle vehicle, ChangeDirection evasion, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.vehicle = vehicle;
        this.evasion = evasion;
        this.actuator = actuator;
        this.timer = timer;
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

    /** Runs one control cycle of the movement subsystem on {@code event}. */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - this.T < Constants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = this.d1 - this.d0 > Constants.STUCK_DIST;

        if (currentMode == MovementMode.WAITING) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                this.a = turn.angle();
                currentMode = MovementMode.GOING;
                vehicle.move(Constants.LV, this.a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
            }

        } else if (currentMode == MovementMode.GOING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                this.a = turn.angle();
                currentMode = MovementMode.GOING;
                vehicle.move(Constants.LV, this.a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacle = (InputEvent.Obstacle) event;
                this.l = obstacle.loc();
                this.T = timer.nowMs();
                currentMode = MovementMode.AVOIDING;
                this.d0 = sensor.odometer();
                evasion.changeDirection(this.l);
                vehicle.pause(Constants.EVADE_TIME);
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
                InputEvent.Turn turn = (InputEvent.Turn) event;
                this.a = turn.angle();
                currentMode = MovementMode.TRYING_AGAIN;
                vehicle.move(Constants.LV, this.a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
            }

        } else if (currentMode == MovementMode.TRYING_AGAIN) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                this.a = turn.angle();
                currentMode = MovementMode.TRYING_AGAIN;
                vehicle.move(Constants.LV, this.a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacle = (InputEvent.Obstacle) event;
                this.l = obstacle.loc();
                this.d1 = sensor.odometer();
                currentMode = MovementMode.AVOIDING_AGAIN;
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
                this.T = timer.nowMs();
                currentMode = MovementMode.AVOIDING;
                this.d0 = sensor.odometer();
                evasion.changeDirection(this.l);
                vehicle.pause(Constants.EVADE_TIME);
            } else if (!withinStuckPeriod && !advancedBeyondStuckDist) {
                currentMode = MovementMode.GETTING_OUT;
                vehicle.shortRandomWalk();
                vehicle.pause(Constants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GETTING_OUT) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                this.a = turn.angle();
                currentMode = MovementMode.GOING;
                vehicle.move(Constants.LV, this.a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
            }
        }
    }
}
