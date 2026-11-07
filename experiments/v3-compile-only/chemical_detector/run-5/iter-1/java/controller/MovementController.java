package chemical_detector.controller;

import chemical_detector.actuator.VehicleActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.sensor.VehicleSensor;
import chemical_detector.types.Angle;
import chemical_detector.types.Loc;

/**
 * The movement subsystem (CD-ARCH2): random-walk search, travel in the
 * commanded direction, obstacle avoidance with stuck recovery, and halting
 * with the flag signal once the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.WAITING;

    /** The most recent commanded direction (CD-MV-Var1). */
    private Angle a = Angle.Front;
    /** The side of the most recent obstacle (CD-MV-Var4). */
    private Loc l = Loc.front;
    /** Distance travelled when the current evasion began (CD-MV-Var2). */
    @RoboChartType("real") private double d0 = 0.0;
    /** Distance travelled at the second obstacle of the evasion (CD-MV-Var3). */
    @RoboChartType("real") private double d1 = 0.0;
    /** Time at which the current evasion began (clock T, CD-MV-Clock1). */
    @RoboChartType("nat") private long evasionStart = 0;

    private final VehicleSensor sensor;
    private final VehicleActuator actuator;
    private final Clock timeSource;

    /** Creates the subsystem in its initial state, Waiting (CD-MV-Beh1). */
    public MovementController(VehicleSensor sensor, VehicleActuator actuator, Clock timeSource) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timeSource = timeSource;
    }

    /** The current state. */
    public MovementMode currentMode() {
        return currentMode;
    }

    /** Runs one control cycle with the event received in it (null if none). */
    public void step(InputEvent event) {
        // Guard predicates (stuck detection, CD-Const4 and CD-Const5).
        boolean evasionRecent = timeSource.now() - evasionStart < ChemicalDetectorConstants.STUCK_PERIOD;
        boolean advancedEnough = d1 - d0 > ChemicalDetectorConstants.STUCK_DIST;
        boolean makingProgress = evasionRecent || advancedEnough;

        if (currentMode == MovementMode.WAITING) {
            // CD-MV-Beh4: Waiting --stop--> Found; entry: flag; move(0, Front).
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            // CD-MV-Beh2: Waiting --turn?a--> Going; entry: move(lv, a).
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                currentMode = MovementMode.GOING;
                a = turnCommand.direction();
                actuator.move(ChemicalDetectorConstants.LV, a);
            // CD-MV-Beh3: Waiting --resume--> Waiting; Waiting: randomWalk().
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            }

        } else if (currentMode == MovementMode.GOING) {
            // CD-MV-Beh6: Going --stop--> Found.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            // CD-MV-Beh5: Going --turn?a--> Going; entry: move(lv, a).
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                currentMode = MovementMode.GOING;
                a = turnCommand.direction();
                actuator.move(ChemicalDetectorConstants.LV, a);
            // CD-MV-Beh7: Going --obstacle?l / start T--> Avoiding;
            // entry: d0 = odometer; changeDirection(l); wait(evadeTime).
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleHit = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AVOIDING;
                l = obstacleHit.side();
                evasionStart = timeSource.now();
                d0 = sensor.odometer();
                actuator.changeDirection(l);
                timeSource.pause(ChemicalDetectorConstants.EVADE_TIME);
            // CD-MV-Beh8: Going --resume--> Waiting.
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            }

        } else if (currentMode == MovementMode.FOUND) {
            // CD-MV-Beh9: terminal; the robot stays halted.

        } else if (currentMode == MovementMode.AVOIDING) {
            // CD-MV-Beh11: Avoiding --stop--> Found.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            // CD-MV-Beh10: Avoiding --turn?a--> TryingAgain; entry: move(lv, a).
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                currentMode = MovementMode.TRYING_AGAIN;
                a = turnCommand.direction();
                actuator.move(ChemicalDetectorConstants.LV, a);
            // CD-MV-Beh12: Avoiding --resume--> Waiting.
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            }

        } else if (currentMode == MovementMode.TRYING_AGAIN) {
            // CD-MV-Beh14: TryingAgain --stop--> Found.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            // CD-MV-Beh13: TryingAgain --turn?a--> TryingAgain; entry: move(lv, a).
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                currentMode = MovementMode.TRYING_AGAIN;
                a = turnCommand.direction();
                actuator.move(ChemicalDetectorConstants.LV, a);
            // CD-MV-Beh16: TryingAgain --obstacle?l / d1 = odometer--> AvoidingAgain.
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleHit = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AVOIDING_AGAIN;
                l = obstacleHit.side();
                d1 = sensor.odometer();
            // CD-MV-Beh15: TryingAgain --resume--> Waiting.
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            }

        } else if (currentMode == MovementMode.AVOIDING_AGAIN) {
            // CD-MV-Beh19: AvoidingAgain --stop--> Found.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            // CD-MV-Beh20: AvoidingAgain --resume--> Waiting.
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            // CD-MV-Beh17: AvoidingAgain --[since(T) < stuckPeriod or d1 - d0 > stuckDist]
            // / restart T--> Avoiding; entry: d0 = odometer; changeDirection(l); wait(evadeTime).
            } else if (makingProgress) {
                currentMode = MovementMode.AVOIDING;
                evasionStart = timeSource.now();
                d0 = sensor.odometer();
                actuator.changeDirection(l);
                timeSource.pause(ChemicalDetectorConstants.EVADE_TIME);
            // CD-MV-Beh18: AvoidingAgain --[since(T) >= stuckPeriod and d1 - d0 <= stuckDist]-->
            // GettingOut; entry: shortRandomWalk(); wait(outPeriod).
            } else if (!makingProgress) {
                currentMode = MovementMode.GETTING_OUT;
                actuator.shortRandomWalk();
                timeSource.pause(ChemicalDetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GETTING_OUT) {
            // CD-MV-Beh22: GettingOut --stop--> Found.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            // CD-MV-Beh21: GettingOut --turn?a--> Going; entry: move(lv, a).
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                currentMode = MovementMode.GOING;
                a = turnCommand.direction();
                actuator.move(ChemicalDetectorConstants.LV, a);
            // CD-MV-Beh23: GettingOut --resume--> Waiting.
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            }
        }
    }
}
