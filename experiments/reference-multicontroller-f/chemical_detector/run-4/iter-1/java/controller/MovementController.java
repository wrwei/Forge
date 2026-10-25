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
 * Movement subsystem: random-walk search, direction following, obstacle avoidance
 * with stuck recovery, and halting with a flag once the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final Vehicle vehicle;
    private final OdometerSensor odometerSensor;
    private final ChangeDirection steering;
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

    public MovementController(Vehicle vehicle, OdometerSensor odometerSensor, ChangeDirection steering,
                              Actuator actuator, Clock timer) {
        this.vehicle = vehicle;
        this.odometerSensor = odometerSensor;
        this.steering = steering;
        this.actuator = actuator;
        this.timer = timer;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    /** Executes one control cycle: at most one transition. */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - T < ChemicalDetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > ChemicalDetectorConstants.STUCK_DIST;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(ChemicalDetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                a = command.a();
                currentMode = MovementMode.Going;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(ChemicalDetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle detected = (InputEvent.Obstacle) event;
                l = detected.l();
                currentMode = MovementMode.Avoiding;
                T = timer.nowMs();
                d0 = odometerSensor.odometer();
                steering.changeDirection(l);
                vehicle.pause(ChemicalDetectorConstants.EVADE_TIME);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                a = command.a();
                currentMode = MovementMode.Going;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof InputEvent.Tick) {
                currentMode = MovementMode.Found;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(ChemicalDetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                a = command.a();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(ChemicalDetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle detected = (InputEvent.Obstacle) event;
                l = detected.l();
                currentMode = MovementMode.AvoidingAgain;
                d1 = odometerSensor.odometer();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                a = command.a();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(ChemicalDetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (withinStuckPeriod || advancedBeyondStuckDist) {
                currentMode = MovementMode.Avoiding;
                T = timer.nowMs();
                d0 = odometerSensor.odometer();
                steering.changeDirection(l);
                vehicle.pause(ChemicalDetectorConstants.EVADE_TIME);
            } else if (!withinStuckPeriod && !advancedBeyondStuckDist) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(ChemicalDetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(ChemicalDetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                a = command.a();
                currentMode = MovementMode.Going;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }
        }
    }
}
