package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.Constants;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ObstacleEvasion;
import chemical_detector.sensor.Sensor;
import chemical_detector.timing.Clock;
import chemical_detector.types.Angle;
import chemical_detector.types.Loc;

/**
 * Movement subsystem. Random-walks while waiting, follows {@code turn}
 * commands, avoids obstacles, recovers when stuck, and halts and raises
 * {@code flag} once {@code stop} confirms the chemical source.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final Sensor sensor;
    private final Vehicle vehicle;
    private final ObstacleEvasion evasion;
    private final Actuator actuator;
    private final Clock timer;

    private Angle a = Angle.Front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    private Loc l = Loc.front;
    @RoboChartType("nat")
    private long evadeStart = 0;
    private boolean makingProgress = false;

    public MovementController(Sensor sensor, Vehicle vehicle, ObstacleEvasion evasion,
                              Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.vehicle = vehicle;
        this.evasion = evasion;
        this.actuator = actuator;
        this.timer = timer;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    /**
     * Runs one control cycle. {@code event} is the input received in this
     * cycle, or {@code null} when none arrived.
     */
    public void step(InputEvent event) {
        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn command = (InputEvent.turn) event;
                a = command.a();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, a);
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn command = (InputEvent.turn) event;
                a = command.a();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.obstacle) {
                InputEvent.obstacle detected = (InputEvent.obstacle) event;
                l = detected.l();
                currentMode = MovementMode.Avoiding;
                evadeStart = timer.nowMs();
                d0 = sensor.odometer();
                evasion.changeDirection(l);
                vehicle.pause(Constants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn command = (InputEvent.turn) event;
                a = command.a();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.LV, a);
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn command = (InputEvent.turn) event;
                a = command.a();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.obstacle) {
                InputEvent.obstacle detected = (InputEvent.obstacle) event;
                l = detected.l();
                currentMode = MovementMode.AvoidingAgain;
                d1 = sensor.odometer();
                makingProgress = timer.nowMs() - evadeStart < Constants.STUCK_PERIOD
                        || d1 - d0 > Constants.STUCK_DIST;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            } else if (makingProgress) {
                currentMode = MovementMode.Avoiding;
                evadeStart = timer.nowMs();
                d0 = sensor.odometer();
                evasion.changeDirection(l);
                vehicle.pause(Constants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(Constants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn command = (InputEvent.turn) event;
                a = command.a();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, a);
            }
        }
    }
}
