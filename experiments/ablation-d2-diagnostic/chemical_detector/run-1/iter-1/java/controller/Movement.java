package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.DetectorConstants;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.VehicleSensors;
import chemical_detector.types.Angle;
import chemical_detector.types.Loc;

/**
 * Movement subsystem: random-walk search, following direction commands, obstacle
 * avoidance with stuck recovery, and halting once the chemical source is found.
 */
public final class Movement {

    private MovementMode currentMode = MovementMode.Waiting;

    private final VehicleSensors sensors;

    private final Vehicle vehicle;

    private final Actuator actuator;

    private final Clock timer;

    private Angle a = Angle.Front;

    private Loc l = Loc.front;

    @RoboChartType("real")
    private double d0;

    @RoboChartType("real")
    private double d1;

    @RoboChartType("nat")
    private long T;

    public Movement(VehicleSensors sensors, Vehicle vehicle, Actuator actuator, Clock timer) {
        this.sensors = sensors;
        this.vehicle = vehicle;
        this.actuator = actuator;
        this.timer = timer;
    }

    /**
     * Performs one control cycle. {@code event} is the input received in this cycle,
     * or {@code null} when the cycle carries no input.
     */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - T < DetectorConstants.stuckPeriod;
        boolean advancedBeyondStuckDist = d1 - d0 > DetectorConstants.stuckDist;

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
                currentMode = MovementMode.Going;
                a = command.a();
                vehicle.move(DetectorConstants.lv, a);
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
                currentMode = MovementMode.Going;
                a = command.a();
                vehicle.move(DetectorConstants.lv, a);
            } else if (event instanceof InputEvent.obstacle) {
                InputEvent.obstacle hit = (InputEvent.obstacle) event;
                currentMode = MovementMode.Avoiding;
                l = hit.l();
                T = timer.nowMs();
                d0 = sensors.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(DetectorConstants.evadeTime);
            }

        } else if (currentMode == MovementMode.Found) {
            // Halted for good: later obstacle reports are recorded but cause no motion.
            if (event instanceof InputEvent.obstacle) {
                InputEvent.obstacle hit = (InputEvent.obstacle) event;
                currentMode = MovementMode.Found;
                l = hit.l();
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
                currentMode = MovementMode.TryingAgain;
                a = command.a();
                vehicle.move(DetectorConstants.lv, a);
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
                currentMode = MovementMode.TryingAgain;
                a = command.a();
                vehicle.move(DetectorConstants.lv, a);
            } else if (event instanceof InputEvent.obstacle) {
                InputEvent.obstacle hit = (InputEvent.obstacle) event;
                currentMode = MovementMode.AvoidingAgain;
                l = hit.l();
                d1 = sensors.odometer();
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            } else if (withinStuckPeriod || advancedBeyondStuckDist) {
                currentMode = MovementMode.Avoiding;
                T = timer.nowMs();
                d0 = sensors.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(DetectorConstants.evadeTime);
            } else if (!withinStuckPeriod && !advancedBeyondStuckDist) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(DetectorConstants.outPeriod);
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
                currentMode = MovementMode.Going;
                a = command.a();
                vehicle.move(DetectorConstants.lv, a);
            }
        }
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    public Angle a() {
        return a;
    }

    public Loc l() {
        return l;
    }

    @RoboChartType("real")
    public double d0() {
        return d0;
    }

    @RoboChartType("real")
    public double d1() {
        return d1;
    }
}
