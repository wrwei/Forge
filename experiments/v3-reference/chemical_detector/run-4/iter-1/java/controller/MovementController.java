package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.MovementActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.operation.Move;
import chemical_detector.operation.RandomWalk;
import chemical_detector.operation.ShortRandomWalk;
import chemical_detector.sensor.VehicleSensor;
import chemical_detector.timing.Clock;

/**
 * The movement subsystem: random-walk search, travel in commanded directions,
 * obstacle avoidance with stuck recovery, and halting once the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private Angle a = Angle.Front;
    @RoboChartType("real") private double d0 = 0.0;
    @RoboChartType("real") private double d1 = 0.0;
    private Loc l = Loc.front;
    @RoboChartType("nat") private long evasionTimer = 0;

    private final VehicleSensor sensor;
    private final Clock clock;
    private final MovementActuator actuator;
    private final Move moveOp;
    private final RandomWalk randomWalkOp;
    private final ShortRandomWalk shortRandomWalkOp;
    private final ChangeDirection changeDirectionOp;

    public MovementController(VehicleSensor sensor, Clock clock, MovementActuator actuator,
                              Move moveOp, RandomWalk randomWalkOp,
                              ShortRandomWalk shortRandomWalkOp, ChangeDirection changeDirectionOp) {
        this.sensor = sensor;
        this.clock = clock;
        this.actuator = actuator;
        this.moveOp = moveOp;
        this.randomWalkOp = randomWalkOp;
        this.shortRandomWalkOp = shortRandomWalkOp;
        this.changeDirectionOp = changeDirectionOp;
    }

    /** The current state of the subsystem. */
    public MovementMode currentMode() {
        return currentMode;
    }

    /** Performs one control cycle; {@code event} is the event received, or null. */
    public void step(InputEvent event) {
        boolean evasionWithinStuckPeriod = clock.now() - evasionTimer < ChemicalDetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > ChemicalDetectorConstants.STUCK_DIST;
        boolean evasionProgressing = evasionWithinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.Waiting) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                moveOp.compute(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                randomWalkOp.compute();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCmd = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnCmd.direction();
                moveOp.compute(ChemicalDetectorConstants.LV, a);
            }
        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                moveOp.compute(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                randomWalkOp.compute();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCmd = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnCmd.direction();
                moveOp.compute(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleHit = (InputEvent.Obstacle) event;
                currentMode = MovementMode.Avoiding;
                l = obstacleHit.side();
                evasionTimer = clock.now();
                d0 = sensor.odometer();
                changeDirectionOp.compute(l);
                actuator.pause(ChemicalDetectorConstants.EVADE_TIME);
            }
        } else if (currentMode == MovementMode.Found) {
            // Halted: the flag has been sent and the Vehicle stopped on entry.
        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                moveOp.compute(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                randomWalkOp.compute();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCmd = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnCmd.direction();
                moveOp.compute(ChemicalDetectorConstants.LV, a);
            }
        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                moveOp.compute(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                randomWalkOp.compute();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCmd = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnCmd.direction();
                moveOp.compute(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleHit = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AvoidingAgain;
                l = obstacleHit.side();
                d1 = sensor.odometer();
            }
        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                moveOp.compute(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                randomWalkOp.compute();
            } else if (evasionProgressing) {
                currentMode = MovementMode.Avoiding;
                evasionTimer = clock.now();
                d0 = sensor.odometer();
                changeDirectionOp.compute(l);
                actuator.pause(ChemicalDetectorConstants.EVADE_TIME);
            } else if (!evasionProgressing) {
                currentMode = MovementMode.GettingOut;
                shortRandomWalkOp.compute();
                actuator.pause(ChemicalDetectorConstants.OUT_PERIOD);
            }
        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                moveOp.compute(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                randomWalkOp.compute();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCmd = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnCmd.direction();
                moveOp.compute(ChemicalDetectorConstants.LV, a);
            }
        }
    }
}
