package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.Actuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.VehicleSensor;

/**
 * Movement subsystem: random-walk search, travel in the commanded direction,
 * obstacle avoidance with stuck recovery, and halting once the source is found.
 */
public final class MovementController {
    private MovementMode currentMode = MovementMode.Waiting;
    private Angle a = Angle.Front;
    @RoboChartType("real") private double d0 = 0.0;
    @RoboChartType("real") private double d1 = 0.0;
    private Loc l = Loc.front;
    @RoboChartType("nat") private long T = 0;
    private final VehicleSensor sensor;
    private final Actuator actuator;
    private final Clock clock;

    /**
     * @param sensor the Vehicle's sensing surface (odometer)
     * @param actuator the Vehicle's movement operations and the flag output
     * @param clock the time source for stuck detection
     */
    public MovementController(VehicleSensor sensor, Actuator actuator, Clock clock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;
    }

    /** @return the current mode */
    public MovementMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one control cycle.
     *
     * @param event the event received in this cycle, or {@code null} if none
     */
    public void step(InputEvent event) {
        boolean evasionWithinStuckPeriod = clock.now() - T < ChemicalDetectorConstants.STUCK_PERIOD;
        boolean advanceAboveStuckDist = d1 - d0 > ChemicalDetectorConstants.STUCK_DIST;
        boolean makingProgress = evasionWithinStuckPeriod || advanceAboveStuckDist;

        if (currentMode == MovementMode.Waiting) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(ChemicalDetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                actuator.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.value();
                actuator.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(ChemicalDetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                actuator.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.value();
                actuator.move(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                currentMode = MovementMode.Avoiding;
                l = obstacleEvent.value();
                T = clock.now();
                d0 = sensor.odometer();
                actuator.changeDirection(l);
                actuator.pause(ChemicalDetectorConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.Found) {
            // Found -> final: the flag has been sent and the Vehicle halted on entry.
            currentMode = MovementMode.Halted;

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(ChemicalDetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                actuator.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnEvent.value();
                actuator.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(ChemicalDetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                actuator.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnEvent.value();
                actuator.move(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AvoidingAgain;
                l = obstacleEvent.value();
                d1 = sensor.odometer();
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(ChemicalDetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                actuator.randomWalk();
            } else if (makingProgress) {
                currentMode = MovementMode.Avoiding;
                T = clock.now();
                d0 = sensor.odometer();
                actuator.changeDirection(l);
                actuator.pause(ChemicalDetectorConstants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GettingOut;
                actuator.shortRandomWalk();
                actuator.pause(ChemicalDetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(ChemicalDetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                actuator.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.value();
                actuator.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.Halted) {
            // Terminal: the Vehicle stays halted.
        }
    }
}
