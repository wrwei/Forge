package chemical_detector.controller;

import chemical_detector.Angle;
import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.Loc;
import chemical_detector.actuator.VehicleActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.VehicleSensor;

/**
 * The movement subsystem (CD-ARCH2): random-walk search, following direction commands,
 * obstacle avoidance, and recovery when stuck.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private Angle a = Angle.Front;
    @RoboChartType("real") private double d0 = 0.0;
    @RoboChartType("real") private double d1 = 0.0;
    private Loc l = Loc.front;
    @RoboChartType("nat") private long evasionStart = 0;

    private final VehicleSensor sensor;
    private final Clock timer;
    private final VehicleActuator vehicle;

    /**
     * @param sensor  the Vehicle's odometer
     * @param timer   the time source for stuck detection
     * @param vehicle the Vehicle's movement operations
     */
    public MovementController(VehicleSensor sensor, Clock timer, VehicleActuator vehicle) {
        this.sensor = sensor;
        this.timer = timer;
        this.vehicle = vehicle;
        // CD-MV-FR1: the initial state Waiting searches by unbounded random walk.
        this.vehicle.randomWalk();
    }

    /** The current state of the movement subsystem. */
    public MovementMode currentMode() {
        return currentMode;
    }

    /**
     * Runs one control cycle.
     *
     * @param event the event received in this cycle, or {@code null} if none
     */
    public void step(InputEvent event) {
        boolean evasionTimeBelowStuckPeriod = timer.now() - evasionStart < ChemicalDetectorConstants.STUCK_PERIOD;
        boolean advanceAboveStuckDist = d1 - d0 > ChemicalDetectorConstants.STUCK_DIST;
        boolean makingProgress = evasionTimeBelowStuckPeriod || advanceAboveStuckDist;

        if (currentMode == MovementMode.Waiting) {
            // CD-MV-Beh4
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh3
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh2
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.direction();
                currentMode = MovementMode.Going;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.Going) {
            // CD-MV-Beh6
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh8
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh5
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.direction();
                currentMode = MovementMode.Going;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }
            // CD-MV-Beh7: a new evasion sequence starts; its timing restarts on entry.
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                l = obstacleEvent.side();
                currentMode = MovementMode.Avoiding;
                evasionStart = timer.now();
                d0 = sensor.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(ChemicalDetectorConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.Found) {
            // CD-MV-Beh9: terminal; the robot stays halted.

        } else if (currentMode == MovementMode.Avoiding) {
            // CD-MV-Beh11
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh12
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh10
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            // CD-MV-Beh14
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh15
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh13
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }
            // CD-MV-Beh16: a second obstacle; record the distance for stuck detection.
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                l = obstacleEvent.side();
                currentMode = MovementMode.AvoidingAgain;
                d1 = sensor.odometer();
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            // CD-MV-Beh19
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh20
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh17: making progress; avoid again and restart the evasion timing.
            else if (makingProgress) {
                currentMode = MovementMode.Avoiding;
                evasionStart = timer.now();
                d0 = sensor.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(ChemicalDetectorConstants.EVADE_TIME);
            }
            // CD-MV-Beh18: stuck; recover with a short random walk.
            else if (!makingProgress) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(ChemicalDetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            // CD-MV-Beh22
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh23
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh21
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.direction();
                currentMode = MovementMode.Going;
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }
        }
    }
}
