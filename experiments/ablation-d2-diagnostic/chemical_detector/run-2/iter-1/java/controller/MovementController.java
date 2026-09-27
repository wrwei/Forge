package chemical_detector.controller;

import chemical_detector.actuator.EventActuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.OdometerSensor;
import chemical_detector.timing.Clock;

/**
 * Movement subsystem: random-walk search, travel in the commanded direction,
 * obstacle avoidance with stuck recovery, and halting once the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final Vehicle vehicle;
    private final ChangeDirection steering;
    private final OdometerSensor odometerSensor;
    private final Clock timer;
    private final EventActuator actuator;

    private Angle a = Angle.Front;
    private Loc l = Loc.front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    @RoboChartType("nat")
    private long evasionStart = 0L;

    public MovementController(Vehicle vehicle, ChangeDirection steering, OdometerSensor odometerSensor,
                              Clock timer, EventActuator actuator) {
        this.vehicle = vehicle;
        this.steering = steering;
        this.odometerSensor = odometerSensor;
        this.timer = timer;
        this.actuator = actuator;
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

    /**
     * Performs one control cycle. {@code event} is the input received this cycle,
     * or {@code null} when none arrived.
     */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - evasionStart < ChemConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > ChemConstants.STUCK_DIST;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn turnEvent = (InputEvent.turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.direction();
                vehicle.move(ChemConstants.LV, a);
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn turnEvent = (InputEvent.turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.direction();
                vehicle.move(ChemConstants.LV, a);
            } else if (event instanceof InputEvent.obstacle) {
                InputEvent.obstacle obstacleEvent = (InputEvent.obstacle) event;
                currentMode = MovementMode.Avoiding;
                l = obstacleEvent.side();
                evasionStart = timer.nowMs();
                d0 = odometerSensor.odometer();
                steering.changeDirection(l);
                vehicle.pause(ChemConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn turnEvent = (InputEvent.turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnEvent.direction();
                vehicle.move(ChemConstants.LV, a);
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn turnEvent = (InputEvent.turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnEvent.direction();
                vehicle.move(ChemConstants.LV, a);
            } else if (event instanceof InputEvent.obstacle) {
                InputEvent.obstacle obstacleEvent = (InputEvent.obstacle) event;
                currentMode = MovementMode.AvoidingAgain;
                l = obstacleEvent.side();
                d1 = odometerSensor.odometer();
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            } else if (withinStuckPeriod || advancedBeyondStuckDist) {
                currentMode = MovementMode.Avoiding;
                evasionStart = timer.nowMs();
                d0 = odometerSensor.odometer();
                steering.changeDirection(l);
                vehicle.pause(ChemConstants.EVADE_TIME);
            } else if (!withinStuckPeriod && !advancedBeyondStuckDist) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(ChemConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn turnEvent = (InputEvent.turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.direction();
                vehicle.move(ChemConstants.LV, a);
            }
        }
    }
}
