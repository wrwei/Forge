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
 * The movement subsystem (CD-MV-*): random-walk search, travel in the
 * commanded direction, obstacle avoidance with stuck recovery, and halting
 * when the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final OdometerSensor odometerSensor;
    private final VehicleActuator vehicle;
    private final Clock timer;

    /** Most recent commanded direction (CD-MV-Var1). */
    private Angle a = Angle.Front;
    /** Side of the most recent obstacle (CD-MV-Var4). */
    private Loc l = Loc.front;
    /** Distance travelled when the current evasion began (CD-MV-Var2). */
    @RoboChartType("real") private double d0 = 0.0;
    /** Distance travelled when the second obstacle was hit (CD-MV-Var3). */
    @RoboChartType("real") private double d1 = 0.0;
    /** Start time of the current evasion sequence: the clock T (CD-MV-Clock1). */
    @RoboChartType("nat") private long evasionStart = 0;

    /** Creates the controller in its initial state, Waiting (CD-MV-Beh1). */
    public MovementController(OdometerSensor odometerSensor, VehicleActuator vehicle, Clock timer) {
        this.odometerSensor = odometerSensor;
        this.vehicle = vehicle;
        this.timer = timer;
    }

    /** The current state. */
    public MovementMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one control cycle.
     *
     * <p>Entry actions, performed inline by every transition into the state:
     * Waiting randomWalk(); Going and TryingAgain move(lv, a); Found flag and
     * move(0, Front); Avoiding restart T, d0 = odometer, changeDirection(l),
     * wait(evadeTime); GettingOut shortRandomWalk(), wait(outPeriod).
     *
     * @param event the event received in this cycle, or null when none
     */
    public void step(InputEvent event) {
        boolean evasionTimeBelowStuckPeriod =
                timer.now() - evasionStart < ChemicalDetectorConstants.STUCK_PERIOD;
        boolean distanceAdvancedAboveStuckDist = d1 - d0 > ChemicalDetectorConstants.STUCK_DIST;
        boolean makingProgress = evasionTimeBelowStuckPeriod || distanceAdvancedAboveStuckDist;

        if (currentMode == MovementMode.Waiting) {
            // CD-MV-Beh4: stop
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh3: resume (self-loop)
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh2: turn(a)
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.direction();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.Going) {
            // CD-MV-Beh6: stop
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh8: resume
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh5: turn(a) (self-loop, re-issues the move)
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.direction();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }
            // CD-MV-Beh7: obstacle(l), a new evasion sequence starts
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                currentMode = MovementMode.Avoiding;
                l = obstacleEvent.side();
                evasionStart = timer.now();
                d0 = odometerSensor.odometer();
                vehicle.changeDirection(l);
                timer.pause(ChemicalDetectorConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.Found) {
            // CD-MV-Beh9: no trigger, no guard; the Vehicle stays halted
            currentMode = MovementMode.Halted;

        } else if (currentMode == MovementMode.Avoiding) {
            // CD-MV-Beh11: stop
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh12: resume
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh10: turn(a)
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnEvent.direction();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            // CD-MV-Beh14: stop
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh15: resume
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh13: turn(a) (self-loop, re-issues the move)
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnEvent.direction();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }
            // CD-MV-Beh16: obstacle(l) / d1 = odometer
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AvoidingAgain;
                l = obstacleEvent.side();
                d1 = odometerSensor.odometer();
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            // CD-MV-Beh19: stop
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh20: resume
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh17: [since(T) < stuckPeriod or d1 - d0 > stuckDist], evasion restarts
            else if (makingProgress) {
                currentMode = MovementMode.Avoiding;
                evasionStart = timer.now();
                d0 = odometerSensor.odometer();
                vehicle.changeDirection(l);
                timer.pause(ChemicalDetectorConstants.EVADE_TIME);
            }
            // CD-MV-Beh18: [since(T) >= stuckPeriod and d1 - d0 <= stuckDist]
            else if (!makingProgress) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                timer.pause(ChemicalDetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            // CD-MV-Beh22: stop
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh23: resume
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh21: turn(a)
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.direction();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.Halted) {
            // Final: the movement subsystem stays halted.
        }
    }
}
