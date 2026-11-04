package chemical_detector.controller;

import chemical_detector.actuator.SignalActuator;
import chemical_detector.actuator.VehicleActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.NavigationSensor;

/**
 * Movement subsystem: random-walk search, travel in the commanded
 * direction, obstacle avoidance with stuck recovery, and halting once the
 * source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.WAITING;
    private final NavigationSensor sensor;
    private final VehicleActuator vehicle;
    private final SignalActuator signals;
    private final Clock timer;

    private Angle a = Angle.Front;
    @RoboChartType("real") private double d0 = 0.0;
    @RoboChartType("real") private double d1 = 0.0;
    private Loc l = Loc.front;
    @RoboChartType("nat") private long evasionStart;

    /** Creates the subsystem in Waiting, performing a random walk. */
    public MovementController(NavigationSensor sensor, VehicleActuator vehicle,
                              SignalActuator signals, Clock timer) {
        this.sensor = sensor;
        this.vehicle = vehicle;
        this.signals = signals;
        this.timer = timer;
        vehicle.randomWalk();
    }

    /** The current state. */
    public MovementMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one control cycle. {@code event} is the event received in
     * this cycle, or {@code null} if none; transitions without a trigger
     * fire on any call that no triggered transition consumes.
     */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.now() - evasionStart < DetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > DetectorConstants.STUCK_DIST;
        boolean makingProgress = withinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.WAITING) {
            // CD-MV-Beh4: entry to Found emits flag and halts.
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                signals.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh3: re-entering Waiting resumes the random walk.
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            }
            // CD-MV-Beh2: entry to Going moves at lv in direction a.
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                currentMode = MovementMode.GOING;
                a = turnCommand.direction();
                vehicle.move(DetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.GOING) {
            // CD-MV-Beh6
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                signals.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh8
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            }
            // CD-MV-Beh5
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                currentMode = MovementMode.GOING;
                a = turnCommand.direction();
                vehicle.move(DetectorConstants.LV, a);
            }
            // CD-MV-Beh7: entry to Avoiding restarts the evasion timer, samples
            // the odometer, steers away from the obstacle and waits evadeTime.
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleHit = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AVOIDING;
                l = obstacleHit.side();
                evasionStart = timer.now();
                d0 = sensor.odometer();
                vehicle.move(DetectorConstants.LV, sensor.evasionAngle(l));
                timer.delay(DetectorConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.FOUND) {
            // CD-MV-Beh9: unguarded step to the halted final state.
            currentMode = MovementMode.HALTED;

        } else if (currentMode == MovementMode.AVOIDING) {
            // CD-MV-Beh11
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                signals.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh12
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            }
            // CD-MV-Beh10: entry to TryingAgain restarts motion in direction a.
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                currentMode = MovementMode.TRYING_AGAIN;
                a = turnCommand.direction();
                vehicle.move(DetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.TRYING_AGAIN) {
            // CD-MV-Beh14
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                signals.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh15
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            }
            // CD-MV-Beh13
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                currentMode = MovementMode.TRYING_AGAIN;
                a = turnCommand.direction();
                vehicle.move(DetectorConstants.LV, a);
            }
            // CD-MV-Beh16: sample the odometer to seed stuck detection.
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleHit = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AVOIDING_AGAIN;
                l = obstacleHit.side();
                d1 = sensor.odometer();
            }

        } else if (currentMode == MovementMode.AVOIDING_AGAIN) {
            // CD-MV-Beh19
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                signals.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh20
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            }
            // CD-MV-Beh17
            else if (makingProgress) {
                currentMode = MovementMode.AVOIDING;
                evasionStart = timer.now();
                d0 = sensor.odometer();
                vehicle.move(DetectorConstants.LV, sensor.evasionAngle(l));
                timer.delay(DetectorConstants.EVADE_TIME);
            }
            // CD-MV-Beh18: entry to GettingOut runs a short random walk, then waits.
            else if (!makingProgress) {
                currentMode = MovementMode.GETTING_OUT;
                vehicle.shortRandomWalk();
                timer.delay(DetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GETTING_OUT) {
            // CD-MV-Beh22
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                signals.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }
            // CD-MV-Beh23
            else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            }
            // CD-MV-Beh21
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                currentMode = MovementMode.GOING;
                a = turnCommand.direction();
                vehicle.move(DetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.HALTED) {
            // Terminal: the Vehicle stays halted.
        }
    }
}
