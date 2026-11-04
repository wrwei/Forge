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
 * Movement subsystem: random-walk search, travel in the commanded direction,
 * obstacle avoidance with stuck detection and recovery, and halting with the
 * flag signal once the source is found.
 */
public final class MovementController {
    private MovementMode currentMode = MovementMode.WAITING;
    private Angle a = Angle.Front;
    @RoboChartType("real") private double d0 = 0.0;
    @RoboChartType("real") private double d1 = 0.0;
    private Loc l = Loc.front;
    @RoboChartType("nat") private long evasionStart = 0;
    private final OdometerSensor sensor;
    private final VehicleActuator actuator;
    private final Clock timer;

    /** Starts in Waiting, whose behaviour is the unbounded random walk. */
    public MovementController(OdometerSensor sensor, VehicleActuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        this.actuator.randomWalk();
    }

    /** The current state. */
    public MovementMode currentMode() {
        return currentMode;
    }

    /**
     * Runs one control cycle.
     *
     * @param event the event received in this cycle, or null if none arrived
     */
    public void step(InputEvent event) {
        // --- Guard predicates ---
        boolean evasionWithinStuckPeriod = timer.now() - evasionStart < ChemicalDetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > ChemicalDetectorConstants.STUCK_DIST;
        boolean makingProgress = evasionWithinStuckPeriod || advancedBeyondStuckDist;

        // --- One block per state; stop and resume first wherever they apply ---
        if (currentMode == MovementMode.WAITING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.direction();
                currentMode = MovementMode.GOING;
                actuator.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.GOING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.direction();
                currentMode = MovementMode.GOING;
                actuator.move(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                l = obstacleEvent.side();
                evasionStart = timer.now();
                currentMode = MovementMode.AVOIDING;
                d0 = sensor.odometer();
                actuator.changeDirection(l);
                timer.delay(ChemicalDetectorConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.FOUND) {
            // Final: halted with the flag sent on entry; stays here.

        } else if (currentMode == MovementMode.AVOIDING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.direction();
                currentMode = MovementMode.TRYING_AGAIN;
                actuator.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.TRYING_AGAIN) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.direction();
                currentMode = MovementMode.TRYING_AGAIN;
                actuator.move(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                l = obstacleEvent.side();
                d1 = sensor.odometer();
                currentMode = MovementMode.AVOIDING_AGAIN;
            }

        } else if (currentMode == MovementMode.AVOIDING_AGAIN) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            } else if (makingProgress) {
                evasionStart = timer.now();
                currentMode = MovementMode.AVOIDING;
                d0 = sensor.odometer();
                actuator.changeDirection(l);
                timer.delay(ChemicalDetectorConstants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GETTING_OUT;
                actuator.shortRandomWalk();
                timer.delay(ChemicalDetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GETTING_OUT) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.direction();
                currentMode = MovementMode.GOING;
                actuator.move(ChemicalDetectorConstants.LV, a);
            }
        }
    }
}
