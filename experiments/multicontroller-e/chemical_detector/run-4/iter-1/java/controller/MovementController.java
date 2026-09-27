package chemical_detector.controller;

import chemical_detector.actuator.OutputPort;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.Constants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.Sensor;
import chemical_detector.timing.Clock;

/**
 * Movement subsystem: random-walk search, travel in the commanded direction,
 * obstacle avoidance with stuck detection and recovery, and halting once the
 * chemical source has been found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final Sensor sensor;
    private final Vehicle vehicle;
    private final ChangeDirection steering;
    private final OutputPort output;
    private final Clock cycleClock;

    private Angle a = Angle.Front;
    private Loc l = Loc.front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    @RoboChartType("nat")
    private long T = 0L;

    public MovementController(Sensor sensor, Vehicle vehicle, ChangeDirection steering,
                              OutputPort output, Clock cycleClock) {
        this.sensor = sensor;
        this.vehicle = vehicle;
        this.steering = steering;
        this.output = output;
        this.cycleClock = cycleClock;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    /**
     * Performs at most one transition. Pass {@code null} to evaluate only the
     * autonomous (event-free) transitions.
     */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = cycleClock.nowMs() - T < Constants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > Constants.STUCK_DIST;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                output.send(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                a = turn.angle();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                output.send(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                a = turn.angle();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacle = (InputEvent.Obstacle) event;
                l = obstacle.side();
                T = cycleClock.nowMs();
                currentMode = MovementMode.Avoiding;
                d0 = sensor.odometer();
                steering.changeDirection(l);
                vehicle.pause(Constants.EVADE_TIME);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Found) {
            // Halted for good: obstacle reports are recorded but cause no motion.
            if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacle = (InputEvent.Obstacle) event;
                l = obstacle.side();
                currentMode = MovementMode.Found;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                output.send(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                a = turn.angle();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                output.send(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                a = turn.angle();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacle = (InputEvent.Obstacle) event;
                l = obstacle.side();
                d1 = sensor.odometer();
                currentMode = MovementMode.AvoidingAgain;
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                output.send(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (withinStuckPeriod || advancedBeyondStuckDist) {
                T = cycleClock.nowMs();
                currentMode = MovementMode.Avoiding;
                d0 = sensor.odometer();
                steering.changeDirection(l);
                vehicle.pause(Constants.EVADE_TIME);
            } else if (!withinStuckPeriod && !advancedBeyondStuckDist) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(Constants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                output.send(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turn = (InputEvent.Turn) event;
                a = turn.angle();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        }
    }
}
