package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.DetectorConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.Sensor;
import chemical_detector.timing.Clock;

/**
 * Movement subsystem (CD-ARCH2): random-walk search, following turn commands, obstacle
 * avoidance with stuck recovery, and halting with the flag signal once the source is found.
 */
public final class MovementController {

    private final Sensor sensor;
    private final Actuator actuator;
    private final Vehicle vehicle;
    private final ChangeDirection evasion;
    private final Clock timer;

    private MovementMode currentMode = MovementMode.Waiting;

    private Angle a = Angle.Front;
    private Loc l = Loc.front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    @RoboChartType("nat")
    private long T;

    public MovementController(Sensor sensor, Actuator actuator, Vehicle vehicle,
                              ChangeDirection evasion, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.vehicle = vehicle;
        this.evasion = evasion;
        this.timer = timer;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    /** Runs one control cycle, consuming {@code event} if the current state accepts it. */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - T < DetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > DetectorConstants.STUCK_DIST;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(DetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn command = (InputEvent.turn) event;
                a = command.angle();
                currentMode = MovementMode.Going;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(DetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn command = (InputEvent.turn) event;
                a = command.angle();
                currentMode = MovementMode.Going;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.obstacle) {
                InputEvent.obstacle hit = (InputEvent.obstacle) event;
                l = hit.side();
                currentMode = MovementMode.Avoiding;
                T = timer.nowMs();
                d0 = sensor.odometer();
                evasion.changeDirection(l);
                vehicle.pause(DetectorConstants.EVADE_TIME);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(DetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn command = (InputEvent.turn) event;
                a = command.angle();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(DetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn command = (InputEvent.turn) event;
                a = command.angle();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.obstacle) {
                InputEvent.obstacle hit = (InputEvent.obstacle) event;
                l = hit.side();
                currentMode = MovementMode.AvoidingAgain;
                d1 = sensor.odometer();
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(DetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            } else if (withinStuckPeriod || advancedBeyondStuckDist) {
                currentMode = MovementMode.Avoiding;
                T = timer.nowMs();
                d0 = sensor.odometer();
                evasion.changeDirection(l);
                vehicle.pause(DetectorConstants.EVADE_TIME);
            } else if (!withinStuckPeriod && !advancedBeyondStuckDist) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(DetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(DetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn command = (InputEvent.turn) event;
                a = command.angle();
                currentMode = MovementMode.Going;
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            }
        }
    }
}
