package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.Actuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.operation.Move;
import chemical_detector.operation.RandomWalk;
import chemical_detector.operation.ShortRandomWalk;
import chemical_detector.sensor.DistanceSensor;
import chemical_detector.timing.Clock;

/**
 * The movement subsystem: random-walk search, travel in the commanded
 * direction, obstacle avoidance with stuck recovery, and halting once the
 * chemical source has been found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;

    private Angle a = Angle.Front;
    @RoboChartType("real") private double d0 = 0.0;
    @RoboChartType("real") private double d1 = 0.0;
    private Loc l = Loc.front;
    @RoboChartType("nat") private long evasionStart = 0;

    private final DistanceSensor distanceSensor;
    private final Clock timeSource;
    private final Actuator actuator;
    private final Move moveOp;
    private final RandomWalk randomWalkOp;
    private final ShortRandomWalk shortRandomWalkOp;
    private final ChangeDirection changeDirectionOp;

    /**
     * @param distanceSensor    the Vehicle's odometer
     * @param timeSource        the time source of the evasion timer
     * @param actuator          receives the flag event
     * @param moveOp            the Vehicle's move operation
     * @param randomWalkOp      the Vehicle's randomWalk operation
     * @param shortRandomWalkOp the Vehicle's shortRandomWalk operation
     * @param changeDirectionOp the changeDirection operation
     */
    public MovementController(DistanceSensor distanceSensor, Clock timeSource, Actuator actuator,
                              Move moveOp, RandomWalk randomWalkOp, ShortRandomWalk shortRandomWalkOp,
                              ChangeDirection changeDirectionOp) {
        this.distanceSensor = distanceSensor;
        this.timeSource = timeSource;
        this.actuator = actuator;
        this.moveOp = moveOp;
        this.randomWalkOp = randomWalkOp;
        this.shortRandomWalkOp = shortRandomWalkOp;
        this.changeDirectionOp = changeDirectionOp;
    }

    /** The current state. */
    public MovementMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle. {@code event} is the event received in this cycle,
     * or {@code null} when none was received.
     */
    public void step(InputEvent event) {
        boolean evasionTimeBelowStuckPeriod =
                timeSource.now() - evasionStart < ChemicalDetectorConstants.STUCK_PERIOD;
        boolean distanceGainAboveStuckDist = d1 - d0 > ChemicalDetectorConstants.STUCK_DIST;
        boolean makingProgress = evasionTimeBelowStuckPeriod || distanceGainAboveStuckDist;

        if (currentMode == MovementMode.Waiting) {
            randomWalkOp.compute();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                moveOp.compute(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.direction();
                moveOp.compute(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                moveOp.compute(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.direction();
                moveOp.compute(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                currentMode = MovementMode.Avoiding;
                l = obstacleEvent.side();
                d0 = distanceSensor.distanceTravelled();
                changeDirectionOp.compute(l);
                evasionStart = timeSource.now();
                timeSource.pause(ChemicalDetectorConstants.EVADE_TIME);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Found) {
            currentMode = MovementMode.Halted;

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                moveOp.compute(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnEvent.direction();
                moveOp.compute(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                moveOp.compute(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnEvent.direction();
                moveOp.compute(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AvoidingAgain;
                l = obstacleEvent.side();
                d1 = distanceSensor.distanceTravelled();
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                moveOp.compute(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (makingProgress) {
                currentMode = MovementMode.Avoiding;
                d0 = distanceSensor.distanceTravelled();
                changeDirectionOp.compute(l);
                evasionStart = timeSource.now();
                timeSource.pause(ChemicalDetectorConstants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GettingOut;
                shortRandomWalkOp.compute();
                timeSource.pause(ChemicalDetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                moveOp.compute(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.direction();
                moveOp.compute(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Halted) {
            // Terminal: the robot stays halted.
        }
    }
}
