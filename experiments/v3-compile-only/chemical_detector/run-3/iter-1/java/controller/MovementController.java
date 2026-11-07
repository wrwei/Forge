package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.VehicleSensor;
import chemical_detector.timing.Clock;

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
    @RoboChartType("nat") private long evasionStart = 0;

    private final VehicleSensor sensor;
    private final Clock clock;
    private final Vehicle vehicle;
    private final Actuator actuator;

    /**
     * @param sensor   the Vehicle's odometer
     * @param clock    the time source for the evasion timer
     * @param vehicle  the Vehicle's motion operations
     * @param actuator the port carrying flag to the Vehicle
     */
    public MovementController(VehicleSensor sensor, Clock clock, Vehicle vehicle, Actuator actuator) {
        this.sensor = sensor;
        this.clock = clock;
        this.vehicle = vehicle;
        this.actuator = actuator;
        this.vehicle.randomWalk();
    }

    /** The current state of the subsystem. */
    public MovementMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one control cycle.
     *
     * @param event the event received in this cycle, or null if none
     */
    public void step(InputEvent event) {
        boolean evasionWithinStuckPeriod = clock.now() - evasionStart < ChemicalDetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > ChemicalDetectorConstants.STUCK_DIST;
        boolean makingProgress = evasionWithinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.Waiting) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                a = command.direction();
                currentMode = MovementMode.Going;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                a = command.direction();
                currentMode = MovementMode.Going;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle detected = (InputEvent.Obstacle) event;
                l = detected.side();
                currentMode = MovementMode.Avoiding;
                evasionStart = clock.now();
                d0 = sensor.odometer();
                vehicle.changeDirection(l);
                vehicle.delay(ChemicalDetectorConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.Found) {
            // Terminal: the source has been found and the Vehicle stays halted.

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                a = command.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                a = command.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle detected = (InputEvent.Obstacle) event;
                l = detected.side();
                d1 = sensor.odometer();
                currentMode = MovementMode.AvoidingAgain;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (makingProgress) {
                currentMode = MovementMode.Avoiding;
                evasionStart = clock.now();
                d0 = sensor.odometer();
                vehicle.changeDirection(l);
                vehicle.delay(ChemicalDetectorConstants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.delay(ChemicalDetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                a = command.direction();
                currentMode = MovementMode.Going;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }
        }
    }
}
