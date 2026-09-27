package chemical_detector.controller;

import chemical_detector.actuator.Emitter;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.Loc;
import chemical_detector.event.SystemEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.VehicleSensor;

/**
 * Movement subsystem. Performs random-walk search, follows direction
 * commands from the gas-analysis subsystem, avoids obstacles, and recovers
 * when the robot becomes stuck.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;

    private final VehicleSensor sensor;

    private final Vehicle vehicle;

    private final Emitter emitter;

    private final Clock timer;

    private Angle a = Angle.Front;

    private Loc l = Loc.front;

    @RoboChartType("real")
    private double d0 = 0.0;

    @RoboChartType("real")
    private double d1 = 0.0;

    @RoboChartType("real")
    private double tEvade = 0.0;

    public MovementController(VehicleSensor sensor, Vehicle vehicle, Emitter emitter, Clock timer) {
        this.sensor = sensor;
        this.vehicle = vehicle;
        this.emitter = emitter;
        this.timer = timer;
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

    /** One control cycle: evaluates every transition leaving the current mode. */
    public void step(SystemEvent event) {
        // --- Named boolean predicates ---
        boolean withinStuckPeriod = timer.nowMs() - this.tEvade < ChemConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = this.d1 - this.d0 > ChemConstants.STUCK_DIST;

        // --- Mode-nested if-else ---
        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof SystemEvent.Stop) {
                currentMode = MovementMode.Found;
                emitter.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof SystemEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof SystemEvent.Turn) {
                SystemEvent.Turn t = (SystemEvent.Turn) event;
                this.a = t.a();
                currentMode = MovementMode.Going;
                vehicle.move(ChemConstants.LV, this.a);
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof SystemEvent.Stop) {
                currentMode = MovementMode.Found;
                emitter.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof SystemEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof SystemEvent.Turn) {
                SystemEvent.Turn t = (SystemEvent.Turn) event;
                this.a = t.a();
                currentMode = MovementMode.Going;
                vehicle.move(ChemConstants.LV, this.a);
            } else if (event instanceof SystemEvent.Obstacle) {
                SystemEvent.Obstacle o = (SystemEvent.Obstacle) event;
                this.l = o.l();
                currentMode = MovementMode.Avoiding;
                this.tEvade = timer.nowMs();
                this.d0 = sensor.odometer();
                vehicle.changeDirection(ChemConstants.LV, this.l);
                vehicle.pause(ChemConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof SystemEvent.Tick) {
                currentMode = MovementMode.Found;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof SystemEvent.Stop) {
                currentMode = MovementMode.Found;
                emitter.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof SystemEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof SystemEvent.Turn) {
                SystemEvent.Turn t = (SystemEvent.Turn) event;
                this.a = t.a();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemConstants.LV, this.a);
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof SystemEvent.Stop) {
                currentMode = MovementMode.Found;
                emitter.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof SystemEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof SystemEvent.Turn) {
                SystemEvent.Turn t = (SystemEvent.Turn) event;
                this.a = t.a();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemConstants.LV, this.a);
            } else if (event instanceof SystemEvent.Obstacle) {
                SystemEvent.Obstacle o = (SystemEvent.Obstacle) event;
                this.l = o.l();
                currentMode = MovementMode.AvoidingAgain;
                this.d1 = sensor.odometer();
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof SystemEvent.Stop) {
                currentMode = MovementMode.Found;
                emitter.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof SystemEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (withinStuckPeriod || advancedBeyondStuckDist) {
                currentMode = MovementMode.Avoiding;
                this.tEvade = timer.nowMs();
                this.d0 = sensor.odometer();
                vehicle.changeDirection(ChemConstants.LV, this.l);
                vehicle.pause(ChemConstants.EVADE_TIME);
            } else if (!withinStuckPeriod && !advancedBeyondStuckDist) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(ChemConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof SystemEvent.Stop) {
                currentMode = MovementMode.Found;
                emitter.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof SystemEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof SystemEvent.Turn) {
                SystemEvent.Turn t = (SystemEvent.Turn) event;
                this.a = t.a();
                currentMode = MovementMode.Going;
                vehicle.move(ChemConstants.LV, this.a);
            }
        }
    }
}
