package chemical_detector.controller;

import chemical_detector.DetectorConstants;
import chemical_detector.actuator.VehicleActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.OdometerSensor;

/**
 * The movement subsystem (CD-ARCH2): random-walk search, travel in the
 * direction gas analysis commands, obstacle avoidance with stuck recovery,
 * and the halt and flag once the source is found.
 */
public final class MovementController {
    private MovementMode currentMode = MovementMode.Waiting;

    // CD-MV-Var1: the most recent commanded direction.
    private Angle a = Angle.Front;
    // CD-MV-Var2: distance travelled when the current evasion began.
    @RoboChartType("real") private double d0 = 0.0;
    // CD-MV-Var3: distance travelled when the second obstacle was hit.
    @RoboChartType("real") private double d1 = 0.0;
    // CD-MV-Var4: the side of the most recent obstacle.
    private Loc l = Loc.front;
    // CD-MV-Clock1: start of the current evasion sequence.
    @RoboChartType("nat") private long evasionStart = 0;

    private final OdometerSensor sensor;
    private final Clock timer;
    private final VehicleActuator vehicle;

    /** Creates the controller in Waiting, which random-walks (CD-MV-Beh1, CD-MV-FR1). */
    public MovementController(OdometerSensor sensor, Clock timer, VehicleActuator vehicle) {
        this.sensor = sensor;
        this.timer = timer;
        this.vehicle = vehicle;
        this.vehicle.randomWalk();
    }

    /** The current state. */
    public MovementMode currentMode() {
        return currentMode;
    }

    /** One control cycle: takes at most one transition. */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.now() - evasionStart < DetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > DetectorConstants.STUCK_DIST;
        boolean makingProgress = withinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.Waiting) {
            // CD-MV-Beh4
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh3: re-entering Waiting resumes the random walk.
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
            // CD-MV-Beh2
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnSignal = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnSignal.direction();
                vehicle.move(DetectorConstants.LV, a);
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
                InputEvent.Turn turnSignal = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnSignal.direction();
                vehicle.move(DetectorConstants.LV, a);
            }
            // CD-MV-Beh7: a fresh evasion starts; restart its timing.
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleSignal = (InputEvent.Obstacle) event;
                currentMode = MovementMode.Avoiding;
                l = obstacleSignal.side();
                evasionStart = timer.now();
                d0 = sensor.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(DetectorConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.Found) {
            // CD-MV-Beh9: terminal; flag was sent and the Vehicle halted on entry.

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
                InputEvent.Turn turnSignal = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnSignal.direction();
                vehicle.move(DetectorConstants.LV, a);
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
                InputEvent.Turn turnSignal = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnSignal.direction();
                vehicle.move(DetectorConstants.LV, a);
            }
            // CD-MV-Beh16: second obstacle of the evasion; seed stuck detection.
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleSignal = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AvoidingAgain;
                l = obstacleSignal.side();
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
            // CD-MV-Beh17: making progress, so avoid again and restart the timing.
            else if (makingProgress) {
                currentMode = MovementMode.Avoiding;
                evasionStart = timer.now();
                d0 = sensor.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(DetectorConstants.EVADE_TIME);
            }
            // CD-MV-Beh18: stuck, so get out.
            else if (!makingProgress) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(DetectorConstants.OUT_PERIOD);
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
                InputEvent.Turn turnSignal = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnSignal.direction();
                vehicle.move(DetectorConstants.LV, a);
            }
        }
    }
}
