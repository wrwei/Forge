package chemdetector.controller;

import chemdetector.actuator.Actuator;
import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.ChemConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.MvMode;
import chemdetector.sensor.Clock;
import chemdetector.sensor.Telemetry;

/**
 * Movement subsystem: random-walk search, direction following,
 * obstacle avoidance, and stuck recovery.
 */
public final class Movement {

    private MvMode currentMode = MvMode.Waiting;
    private final Telemetry telemetry;
    private final Vehicle vehicle;
    private final Actuator actuator;
    private final Clock timer;

    private Angle a = Angle.Front;
    @RoboChartType("real")
    private double d0;
    @RoboChartType("real")
    private double d1;
    private Loc l = Loc.front;
    private long t;

    public Movement(Telemetry telemetry, Vehicle vehicle, Actuator actuator, Clock timer) {
        this.telemetry = telemetry;
        this.vehicle = vehicle;
        this.actuator = actuator;
        this.timer = timer;
    }

    public MvMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - t < ChemConstants.stuckPeriod;
        boolean progressMade = d1 - d0 > ChemConstants.stuckDist;
        boolean makingProgress = withinStuckPeriod || progressMade;

        if (currentMode == MvMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.stop) {
                currentMode = MvMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MvMode.Waiting;
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn tu = (InputEvent.turn) event;
                a = tu.value();
                currentMode = MvMode.Going;
                vehicle.move(ChemConstants.lv, a);
            }

        } else if (currentMode == MvMode.Going) {
            if (event instanceof InputEvent.stop) {
                currentMode = MvMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MvMode.Waiting;
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn tu = (InputEvent.turn) event;
                a = tu.value();
                currentMode = MvMode.Going;
                vehicle.move(ChemConstants.lv, a);
            } else if (event instanceof InputEvent.obstacle) {
                InputEvent.obstacle ob = (InputEvent.obstacle) event;
                l = ob.value();
                currentMode = MvMode.Avoiding;
                t = timer.nowMs();
                d0 = telemetry.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(ChemConstants.evadeTime);
            }

        } else if (currentMode == MvMode.Avoiding) {
            if (event instanceof InputEvent.stop) {
                currentMode = MvMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MvMode.Waiting;
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn tu = (InputEvent.turn) event;
                a = tu.value();
                currentMode = MvMode.TryingAgain;
                vehicle.move(ChemConstants.lv, a);
            }

        } else if (currentMode == MvMode.TryingAgain) {
            if (event instanceof InputEvent.stop) {
                currentMode = MvMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MvMode.Waiting;
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn tu = (InputEvent.turn) event;
                a = tu.value();
                currentMode = MvMode.TryingAgain;
                vehicle.move(ChemConstants.lv, a);
            } else if (event instanceof InputEvent.obstacle) {
                InputEvent.obstacle ob = (InputEvent.obstacle) event;
                l = ob.value();
                currentMode = MvMode.AvoidingAgain;
                d1 = telemetry.odometer();
            }

        } else if (currentMode == MvMode.AvoidingAgain) {
            if (event instanceof InputEvent.stop) {
                currentMode = MvMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MvMode.Waiting;
            } else if (makingProgress) {
                currentMode = MvMode.Avoiding;
                t = timer.nowMs();
                d0 = telemetry.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(ChemConstants.evadeTime);
            } else if (!makingProgress) {
                currentMode = MvMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(ChemConstants.outPeriod);
            }

        } else if (currentMode == MvMode.GettingOut) {
            if (event instanceof InputEvent.stop) {
                currentMode = MvMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MvMode.Waiting;
            } else if (event instanceof InputEvent.turn) {
                InputEvent.turn tu = (InputEvent.turn) event;
                a = tu.value();
                currentMode = MvMode.Going;
                vehicle.move(ChemConstants.lv, a);
            }

        } else if (currentMode == MvMode.Found) {
            if (event instanceof InputEvent.stop) {
                currentMode = MvMode.Found;
            }
        }
    }
}
