package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.VehicleActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.OdometerSensor;

/**
 * The movement subsystem (CD-ARCH2): random-walk search, travel in the
 * commanded direction, obstacle avoidance with stuck recovery, and the halt
 * and flag once the source is found.
 */
public final class MovementController {
    private MovementMode currentMode = MovementMode.Waiting;
    private final VehicleActuator vehicle;
    private final OdometerSensor odometer;
    private final Clock timer;

    private Angle a = Angle.Front;
    private Loc l = Loc.front;
    @RoboChartType("real") private double d0 = 0.0;
    @RoboChartType("real") private double d1 = 0.0;
    @RoboChartType("nat") private long T = 0;

    /**
     * @param vehicle  the Vehicle's movement operations
     * @param odometer the Vehicle's odometer
     * @param timer    the time source of the evasion timer
     */
    public MovementController(VehicleActuator vehicle, OdometerSensor odometer, Clock timer) {
        this.vehicle = vehicle;
        this.odometer = odometer;
        this.timer = timer;
    }

    /** The current state. */
    public MovementMode currentMode() {
        return currentMode;
    }

    /** One control cycle: takes at most one transition from the current state. */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.now() - T < ChemicalDetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > ChemicalDetectorConstants.STUCK_DIST;
        boolean makingProgress = withinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.Waiting) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                a = turn.direction();
                currentMode = MovementMode.Going;
                vehicle.move(ChemicalDetectorConstants.LV, a);
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
                InputEvent.Turn turn = (InputEvent.Turn) event;
                a = turn.direction();
                currentMode = MovementMode.Going;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacle = (InputEvent.Obstacle) event;
                l = obstacle.side();
                T = timer.now();
                currentMode = MovementMode.Avoiding;
                d0 = odometer.distance();
                vehicle.changeDirection(l);
                vehicle.pause(ChemicalDetectorConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.Found) {
            // Final: the Vehicle stays halted.

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                a = turn.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemicalDetectorConstants.LV, a);
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
                InputEvent.Turn turn = (InputEvent.Turn) event;
                a = turn.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacle = (InputEvent.Obstacle) event;
                l = obstacle.side();
                d1 = odometer.distance();
                currentMode = MovementMode.AvoidingAgain;
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
                T = timer.now();
                currentMode = MovementMode.Avoiding;
                d0 = odometer.distance();
                vehicle.changeDirection(l);
                vehicle.pause(ChemicalDetectorConstants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(ChemicalDetectorConstants.OUT_PERIOD);
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
                InputEvent.Turn turn = (InputEvent.Turn) event;
                a = turn.direction();
                currentMode = MovementMode.Going;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }
        }
    }
}
