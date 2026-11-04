package chemical_detector.controller;

import chemical_detector.actuator.SteeringActuator;
import chemical_detector.actuator.VehicleActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.OdometerSensor;

/**
 * Movement subsystem (CD-ARCH2): random-walk search, following turn commands,
 * obstacle avoidance with stuck recovery, and halting when the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private Angle a = Angle.Front;
    @RoboChartType("real") private double d0 = 0.0;
    @RoboChartType("real") private double d1 = 0.0;
    private Loc l = Loc.front;
    private long evasionStart = 0L;

    private final OdometerSensor sensor;
    private final Clock timer;
    private final VehicleActuator vehicle;
    private final SteeringActuator steering;

    /** Creates the subsystem over the odometer, the clock, the Vehicle and the steering operation. */
    public MovementController(OdometerSensor sensor, Clock timer, VehicleActuator vehicle,
                              SteeringActuator steering) {
        this.sensor = sensor;
        this.timer = timer;
        this.vehicle = vehicle;
        this.steering = steering;
    }

    /** The current state. */
    public MovementMode currentMode() {
        return currentMode;
    }

    /** One control cycle: handles the given event (or none) and autonomous transitions. */
    public void step(InputEvent event) {
        // --- Named guard predicates ---
        boolean withinStuckPeriod = timer.now() - evasionStart < DetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > DetectorConstants.STUCK_DIST;
        boolean makingProgress = withinStuckPeriod || advancedBeyondStuckDist;

        // --- One block per state; stop, then resume, come first wherever they apply ---
        if (currentMode == MovementMode.Waiting) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                a = turnCommand.direction();
                currentMode = MovementMode.Going;
                vehicle.move(DetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                a = turnCommand.direction();
                currentMode = MovementMode.Going;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleHit = (InputEvent.Obstacle) event;
                l = obstacleHit.side();
                currentMode = MovementMode.Avoiding;
                evasionStart = timer.now();
                d0 = sensor.odometer();
                steering.changeDirection(l);
                timer.pause(DetectorConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.Found) {
            // Terminal: the flag has been sent and the Vehicle halted on entry.

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                a = turnCommand.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(DetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                a = turnCommand.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleHit = (InputEvent.Obstacle) event;
                l = obstacleHit.side();
                currentMode = MovementMode.AvoidingAgain;
                d1 = sensor.odometer();
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (makingProgress) {
                currentMode = MovementMode.Avoiding;
                evasionStart = timer.now();
                d0 = sensor.odometer();
                steering.changeDirection(l);
                timer.pause(DetectorConstants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                timer.pause(DetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                a = turnCommand.direction();
                currentMode = MovementMode.Going;
                vehicle.move(DetectorConstants.LV, a);
            }
        }
    }
}
