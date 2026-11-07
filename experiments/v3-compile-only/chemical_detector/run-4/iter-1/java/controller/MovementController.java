package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.VehicleActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.OdometerSensor;
import chemical_detector.timing.Clock;

/**
 * Movement subsystem (CD-ARCH2): random-walk search, following turn
 * commands, obstacle avoidance with stuck detection and recovery, and
 * halting with the flag signal once the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.WAITING;
    private final OdometerSensor odometerSensor;
    private final Clock timer;
    private final VehicleActuator actuator;

    /** a: the most recent commanded direction (CD-MV-Var1). */
    private Angle a = Angle.Front;
    /** d0: distance travelled when the current evasion began (CD-MV-Var2). */
    @RoboChartType("real") private double d0 = 0.0;
    /** d1: distance travelled when a second obstacle was hit (CD-MV-Var3). */
    @RoboChartType("real") private double d1 = 0.0;
    /** l: the side of the most recent obstacle (CD-MV-Var4). */
    private Loc l = Loc.front;
    /** T: start of the current evasion sequence, a clock (CD-MV-Clock1). */
    @RoboChartType("nat") private long evasionStart = 0L;

    /**
     * @param odometerSensor the Vehicle's odometer
     * @param timer          the time source
     * @param actuator       the Vehicle's motion operations and signals
     */
    public MovementController(OdometerSensor odometerSensor, Clock timer, VehicleActuator actuator) {
        this.odometerSensor = odometerSensor;
        this.timer = timer;
        this.actuator = actuator;
    }

    /** The current state. */
    public MovementMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle: takes at most one transition of the movement state
     * machine. {@code event} is the event of this cycle, or {@code null} if
     * there is none.
     */
    public void step(InputEvent event) {
        // Guard predicates (stuck detection, CD-MV-Beh17 and CD-MV-Beh18).
        boolean withinStuckPeriod = timer.now() - evasionStart < ChemicalDetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > ChemicalDetectorConstants.STUCK_DIST;
        boolean makingProgress = withinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.WAITING) {
            // CD-MV-Beh4: Waiting --stop--> Found; entry: flag, halt.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            }
            // CD-MV-Beh3: Waiting --resume--> Waiting; during: randomWalk().
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            }
            // CD-MV-Beh2: Waiting --turn?a--> Going; entry: move(lv, a).
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.direction();
                currentMode = MovementMode.GOING;
                actuator.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.GOING) {
            // CD-MV-Beh6: Going --stop--> Found; entry: flag, halt.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            }
            // CD-MV-Beh8: Going --resume--> Waiting; during: randomWalk().
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            }
            // CD-MV-Beh5: Going --turn?a--> Going; entry: move(lv, a).
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.direction();
                currentMode = MovementMode.GOING;
                actuator.move(ChemicalDetectorConstants.LV, a);
            }
            // CD-MV-Beh7: Going --obstacle?l--> Avoiding;
            // entry: d0 = odometer, reset T, changeDirection(l), wait(evadeTime).
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                l = obstacleEvent.side();
                currentMode = MovementMode.AVOIDING;
                d0 = odometerSensor.odometer();
                evasionStart = timer.now();
                actuator.changeDirection(l);
                timer.pause(ChemicalDetectorConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.FOUND) {
            // CD-MV-Beh9: Found is final; the Vehicle stays halted.

        } else if (currentMode == MovementMode.AVOIDING) {
            // CD-MV-Beh11: Avoiding --stop--> Found; entry: flag, halt.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            }
            // CD-MV-Beh12: Avoiding --resume--> Waiting; during: randomWalk().
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            }
            // CD-MV-Beh10: Avoiding --turn?a--> TryingAgain; entry: move(lv, a).
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.direction();
                currentMode = MovementMode.TRYING_AGAIN;
                actuator.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.TRYING_AGAIN) {
            // CD-MV-Beh14: TryingAgain --stop--> Found; entry: flag, halt.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            }
            // CD-MV-Beh15: TryingAgain --resume--> Waiting; during: randomWalk().
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            }
            // CD-MV-Beh13: TryingAgain --turn?a--> TryingAgain; entry: move(lv, a).
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.direction();
                currentMode = MovementMode.TRYING_AGAIN;
                actuator.move(ChemicalDetectorConstants.LV, a);
            }
            // CD-MV-Beh16: TryingAgain --obstacle?l / d1 = odometer--> AvoidingAgain.
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                l = obstacleEvent.side();
                currentMode = MovementMode.AVOIDING_AGAIN;
                d1 = odometerSensor.odometer();
            }

        } else if (currentMode == MovementMode.AVOIDING_AGAIN) {
            // CD-MV-Beh19: AvoidingAgain --stop--> Found; entry: flag, halt.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            }
            // CD-MV-Beh20: AvoidingAgain --resume--> Waiting; during: randomWalk().
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            }
            // CD-MV-Beh17: AvoidingAgain --[since(T) < stuckPeriod or d1 - d0 > stuckDist]--> Avoiding;
            // entry: d0 = odometer, reset T, changeDirection(l), wait(evadeTime).
            else if (makingProgress) {
                currentMode = MovementMode.AVOIDING;
                d0 = odometerSensor.odometer();
                evasionStart = timer.now();
                actuator.changeDirection(l);
                timer.pause(ChemicalDetectorConstants.EVADE_TIME);
            }
            // CD-MV-Beh18: AvoidingAgain --[since(T) >= stuckPeriod and d1 - d0 <= stuckDist]--> GettingOut;
            // entry: shortRandomWalk(), wait(outPeriod).
            else if (!makingProgress) {
                currentMode = MovementMode.GETTING_OUT;
                actuator.shortRandomWalk();
                timer.pause(ChemicalDetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GETTING_OUT) {
            // CD-MV-Beh22: GettingOut --stop--> Found; entry: flag, halt.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                actuator.apply(new OutputEvent.Flag());
                actuator.move(0.0, Angle.Front);
            }
            // CD-MV-Beh23: GettingOut --resume--> Waiting; during: randomWalk().
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                actuator.randomWalk();
            }
            // CD-MV-Beh21: GettingOut --turn?a--> Going; entry: move(lv, a).
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                a = turnEvent.direction();
                currentMode = MovementMode.GOING;
                actuator.move(ChemicalDetectorConstants.LV, a);
            }
        }
    }
}
