package chemical_detector.controller;

import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.OdometerSensor;
import chemical_detector.timing.Clock;

/**
 * The movement subsystem: random-walk search, travel in commanded directions,
 * obstacle avoidance with stuck detection and recovery, and halting once the
 * chemical source has been found.
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
    private long evasionStart = 0;

    private final Vehicle vehicle;
    private final OdometerSensor odometer;
    private final Clock clock;

    /**
     * Creates the movement subsystem in its initial mode, Waiting, where the
     * Vehicle performs an unbounded random walk.
     *
     * @param vehicle the Vehicle's motion surface
     * @param odometer the odometer
     * @param clock the time source
     */
    public MovementController(Vehicle vehicle, OdometerSensor odometer, Clock clock) {
        this.vehicle = vehicle;
        this.odometer = odometer;
        this.clock = clock;
        vehicle.randomWalk();
    }

    /**
     * The current mode.
     *
     * @return the mode the subsystem is in
     */
    public MovementMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one control cycle: takes at most one transition of the current mode.
     *
     * @param event the event received in this cycle, or null if there is none
     */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = clock.now() - evasionStart < ChemicalDetectorConstants.stuckPeriod;
        boolean advancedBeyondStuckDist = d1 - d0 > ChemicalDetectorConstants.stuckDist;
        boolean makingProgress = withinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.Waiting) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = command.direction();
                vehicle.move(ChemicalDetectorConstants.lv, a);
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
                InputEvent.Turn command = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = command.direction();
                vehicle.move(ChemicalDetectorConstants.lv, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle detected = (InputEvent.Obstacle) event;
                currentMode = MovementMode.Avoiding;
                l = detected.side();
                d0 = odometer.distanceTravelled();
                evasionStart = clock.now();
                vehicle.changeDirection(l);
                clock.delay(ChemicalDetectorConstants.evadeTime);
            }

        } else if (currentMode == MovementMode.Found) {
            // Terminal mode: the flag has been sent and the Vehicle halted; it stays halted.

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = command.direction();
                vehicle.move(ChemicalDetectorConstants.lv, a);
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
                InputEvent.Turn command = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = command.direction();
                vehicle.move(ChemicalDetectorConstants.lv, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle detected = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AvoidingAgain;
                l = detected.side();
                d1 = odometer.distanceTravelled();
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
                d0 = odometer.distanceTravelled();
                evasionStart = clock.now();
                vehicle.changeDirection(l);
                clock.delay(ChemicalDetectorConstants.evadeTime);
            } else if (!makingProgress) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                clock.delay(ChemicalDetectorConstants.outPeriod);
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
                InputEvent.Turn command = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = command.direction();
                vehicle.move(ChemicalDetectorConstants.lv, a);
            }
        }
    }
}
