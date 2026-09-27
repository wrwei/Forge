package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemicalDetectorConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.OdometerSensor;
import chemical_detector.timing.Clock;

/**
 * Movement subsystem: random-walk search, travel in a commanded direction, obstacle
 * avoidance with stuck recovery, and halting with a single flag once the source is
 * found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private Angle a = Angle.Front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    private Loc l = Loc.front;
    @RoboChartType("nat")
    private long evasionStart = 0L;

    private final Vehicle vehicle;
    private final OdometerSensor odometerSensor;
    private final ChangeDirection steering;
    private final Clock timer;
    private final Actuator actuator;

    public MovementController(Vehicle vehicle, OdometerSensor odometerSensor,
            ChangeDirection steering, Clock timer, Actuator actuator) {
        this.vehicle = vehicle;
        this.odometerSensor = odometerSensor;
        this.steering = steering;
        this.timer = timer;
        this.actuator = actuator;
    }

    /**
     * Performs one control cycle. {@code event} is the event received in this cycle,
     * or {@code null} if none; autonomous transitions fire on any cycle.
     */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - evasionStart < ChemicalDetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > ChemicalDetectorConstants.STUCK_DIST;
        boolean makingProgress = withinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turn.a();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }
        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacle = (InputEvent.Obstacle) event;
                currentMode = MovementMode.Avoiding;
                l = obstacle.l();
                evasionStart = timer.nowMs();
                d0 = odometerSensor.odometer();
                steering.changeDirection(l);
                vehicle.pause(ChemicalDetectorConstants.EVADE_TIME);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turn.a();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }
        } else if (currentMode == MovementMode.Found) {
            if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacle = (InputEvent.Obstacle) event;
                currentMode = MovementMode.Found;
                l = obstacle.l();
            }
        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turn.a();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }
        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacle = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AvoidingAgain;
                l = obstacle.l();
                d1 = odometerSensor.odometer();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turn.a();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }
        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (makingProgress) {
                currentMode = MovementMode.Avoiding;
                evasionStart = timer.nowMs();
                d0 = odometerSensor.odometer();
                steering.changeDirection(l);
                vehicle.pause(ChemicalDetectorConstants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(ChemicalDetectorConstants.OUT_PERIOD);
            }
        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turn.a();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }
        }
    }

    public MovementMode currentMode() {
        return currentMode;
    }
}
