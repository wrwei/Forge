package chemical_detector.controller;

import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.MovementEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.Clock;

/**
 * Movement subsystem: random-walk search, travel in a commanded direction,
 * obstacle avoidance with stuck recovery, and halting once the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.WAITING;

    private final Vehicle vehicle;
    private final ChangeDirection steering;
    private final Clock timer;

    private Angle a = Angle.Front;
    private Loc l = Loc.front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    @RoboChartType("nat")
    private long evasionStart = 0L;

    public MovementController(Vehicle vehicle, ChangeDirection steering, Clock timer) {
        this.vehicle = vehicle;
        this.steering = steering;
        this.timer = timer;
    }

    /** Executes one control cycle, handling {@code event} and any autonomous transition. */
    public void step(MovementEvent event) {
        boolean withinStuckPeriod = this.timer.now() - this.evasionStart < ChemConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = this.d1 - this.d0 > ChemConstants.STUCK_DIST;

        if (this.currentMode == MovementMode.WAITING) {
            this.vehicle.randomWalk();
            if (event instanceof MovementEvent.Stop) {
                this.currentMode = MovementMode.FOUND;
                this.vehicle.flag();
                this.vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Resume) {
                this.currentMode = MovementMode.WAITING;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn turn = (MovementEvent.Turn) event;
                this.a = turn.direction();
                this.currentMode = MovementMode.GOING;
                this.vehicle.move(ChemConstants.LV, this.a);
            }
        } else if (this.currentMode == MovementMode.GOING) {
            if (event instanceof MovementEvent.Stop) {
                this.currentMode = MovementMode.FOUND;
                this.vehicle.flag();
                this.vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Resume) {
                this.currentMode = MovementMode.WAITING;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn turn = (MovementEvent.Turn) event;
                this.a = turn.direction();
                this.currentMode = MovementMode.GOING;
                this.vehicle.move(ChemConstants.LV, this.a);
            } else if (event instanceof MovementEvent.Obstacle) {
                MovementEvent.Obstacle obstacle = (MovementEvent.Obstacle) event;
                this.l = obstacle.side();
                this.currentMode = MovementMode.AVOIDING;
                this.evasionStart = this.timer.now();
                this.d0 = this.vehicle.odometer();
                this.steering.changeDirection(this.l);
                this.vehicle.pause(ChemConstants.EVADE_TIME);
            }
        } else if (this.currentMode == MovementMode.AVOIDING) {
            if (event instanceof MovementEvent.Stop) {
                this.currentMode = MovementMode.FOUND;
                this.vehicle.flag();
                this.vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Resume) {
                this.currentMode = MovementMode.WAITING;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn turn = (MovementEvent.Turn) event;
                this.a = turn.direction();
                this.currentMode = MovementMode.TRYING_AGAIN;
                this.vehicle.move(ChemConstants.LV, this.a);
            }
        } else if (this.currentMode == MovementMode.TRYING_AGAIN) {
            if (event instanceof MovementEvent.Stop) {
                this.currentMode = MovementMode.FOUND;
                this.vehicle.flag();
                this.vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Resume) {
                this.currentMode = MovementMode.WAITING;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn turn = (MovementEvent.Turn) event;
                this.a = turn.direction();
                this.currentMode = MovementMode.TRYING_AGAIN;
                this.vehicle.move(ChemConstants.LV, this.a);
            } else if (event instanceof MovementEvent.Obstacle) {
                MovementEvent.Obstacle obstacle = (MovementEvent.Obstacle) event;
                this.l = obstacle.side();
                this.currentMode = MovementMode.AVOIDING_AGAIN;
                this.d1 = this.vehicle.odometer();
            }
        } else if (this.currentMode == MovementMode.AVOIDING_AGAIN) {
            if (event instanceof MovementEvent.Stop) {
                this.currentMode = MovementMode.FOUND;
                this.vehicle.flag();
                this.vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Resume) {
                this.currentMode = MovementMode.WAITING;
            } else if (withinStuckPeriod || advancedBeyondStuckDist) {
                this.currentMode = MovementMode.AVOIDING;
                this.evasionStart = this.timer.now();
                this.d0 = this.vehicle.odometer();
                this.steering.changeDirection(this.l);
                this.vehicle.pause(ChemConstants.EVADE_TIME);
            } else {
                this.currentMode = MovementMode.GETTING_OUT;
                this.vehicle.shortRandomWalk();
                this.vehicle.pause(ChemConstants.OUT_PERIOD);
            }
        } else if (this.currentMode == MovementMode.GETTING_OUT) {
            if (event instanceof MovementEvent.Stop) {
                this.currentMode = MovementMode.FOUND;
                this.vehicle.flag();
                this.vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Resume) {
                this.currentMode = MovementMode.WAITING;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn turn = (MovementEvent.Turn) event;
                this.a = turn.direction();
                this.currentMode = MovementMode.GOING;
                this.vehicle.move(ChemConstants.LV, this.a);
            }
        } else if (this.currentMode == MovementMode.FOUND) {
            this.currentMode = MovementMode.HALTED;
        } else if (this.currentMode == MovementMode.HALTED) {
            if (event instanceof MovementEvent.Stop) {
                this.currentMode = MovementMode.HALTED;
            }
        }
    }

    public MovementMode currentMode() {
        return this.currentMode;
    }

    public Angle a() {
        return this.a;
    }

    public Loc l() {
        return this.l;
    }

    @RoboChartType("real")
    public double d0() {
        return this.d0;
    }

    @RoboChartType("real")
    public double d1() {
        return this.d1;
    }
}
