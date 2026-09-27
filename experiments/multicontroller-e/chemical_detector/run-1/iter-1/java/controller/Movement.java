package chemical_detector.controller;

import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.Sensor;

/**
 * Movement subsystem: random-walk search, travel in the commanded direction,
 * obstacle avoidance with stuck recovery, and halting once the source is found.
 */
public final class Movement {

    private MovementMode currentMode = MovementMode.Waiting;
    private final Sensor sensor;
    private final Clock timer;
    private final Vehicle vehicle;
    private final ChangeDirection steering;
    private Angle a = Angle.Front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    private Loc l = Loc.front;
    @RoboChartType("nat")
    private long evasionStart = 0;

    public Movement(Sensor sensor, Clock timer, Vehicle vehicle, ChangeDirection steering) {
        this.sensor = sensor;
        this.timer = timer;
        this.vehicle = vehicle;
        this.steering = steering;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    public Angle a() {
        return a;
    }

    @RoboChartType("real")
    public double d0() {
        return d0;
    }

    @RoboChartType("real")
    public double d1() {
        return d1;
    }

    public Loc l() {
        return l;
    }

    /**
     * Runs one control cycle. {@code event} is the event received in this cycle,
     * or {@code null} for a cycle in which no event arrived.
     */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - evasionStart < ChemConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > ChemConstants.STUCK_DIST;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = command.direction();
                vehicle.move(ChemConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = command.direction();
                vehicle.move(ChemConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacle = (InputEvent.Obstacle) event;
                currentMode = MovementMode.Avoiding;
                l = obstacle.side();
                d0 = sensor.odometer();
                evasionStart = timer.nowMs();
                steering.changeDirection(l);
                vehicle.pause(ChemConstants.EVADE_TIME);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = command.direction();
                vehicle.move(ChemConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = command.direction();
                vehicle.move(ChemConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacle = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AvoidingAgain;
                l = obstacle.side();
                d1 = sensor.odometer();
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (withinStuckPeriod || advancedBeyondStuckDist) {
                currentMode = MovementMode.Avoiding;
                d0 = sensor.odometer();
                evasionStart = timer.nowMs();
                steering.changeDirection(l);
                vehicle.pause(ChemConstants.EVADE_TIME);
            } else if (!withinStuckPeriod && !advancedBeyondStuckDist) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(ChemConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn command = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = command.direction();
                vehicle.move(ChemConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        }
    }
}
