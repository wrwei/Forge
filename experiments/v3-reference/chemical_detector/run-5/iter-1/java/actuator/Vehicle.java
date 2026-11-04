package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.controller.ChemicalDetectorConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.OutputEvent;

/**
 * The Vehicle's motion surface: receives the movement subsystem's commands and
 * records the last one issued.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastVelocity = 0.0;
    private Angle lastDirection = Angle.Front;
    private boolean randomWalkActive = false;
    private boolean shortRandomWalkActive = false;
    private boolean flagRaised = false;

    /**
     * move: moves at a velocity in a body-relative direction until superseded;
     * zero velocity halts the Vehicle.
     *
     * @param velocity the linear velocity
     * @param direction the body-relative direction
     */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        lastVelocity = velocity;
        lastDirection = direction;
        randomWalkActive = false;
        shortRandomWalkActive = false;
    }

    /** randomWalk: starts an unbounded random-walk search. */
    public void randomWalk() {
        randomWalkActive = true;
        shortRandomWalkActive = false;
    }

    /** shortRandomWalk: starts a random walk that runs for a bounded period. */
    public void shortRandomWalk() {
        shortRandomWalkActive = true;
        randomWalkActive = false;
    }

    /**
     * changeDirection: steers away from an obstacle at velocity lv
     * (left turns Right, right turns Left, front turns Back).
     *
     * @param side the side on which the obstacle was detected
     */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            move(ChemicalDetectorConstants.lv, Angle.Right);
        } else if (side == Loc.right) {
            move(ChemicalDetectorConstants.lv, Angle.Left);
        } else {
            move(ChemicalDetectorConstants.lv, Angle.Back);
        }
    }

    /**
     * Receives a signal event addressed to the Vehicle (flag).
     *
     * @param signal the signal sent by the movement subsystem
     */
    public void apply(OutputEvent signal) {
        if (signal instanceof OutputEvent.Flag) {
            flagRaised = true;
        }
    }

    /**
     * The velocity of the last move command.
     *
     * @return the last commanded velocity
     */
    @RoboChartType("real")
    public double lastVelocity() {
        return lastVelocity;
    }

    /**
     * The direction of the last move command.
     *
     * @return the last commanded direction
     */
    public Angle lastDirection() {
        return lastDirection;
    }

    /**
     * Whether the unbounded random walk is the current motion.
     *
     * @return true after randomWalk until the next motion command
     */
    public boolean randomWalkActive() {
        return randomWalkActive;
    }

    /**
     * Whether the bounded random walk is the current motion.
     *
     * @return true after shortRandomWalk until the next motion command
     */
    public boolean shortRandomWalkActive() {
        return shortRandomWalkActive;
    }

    /**
     * Whether the flag signal has been received.
     *
     * @return true once the chemical source has been reported
     */
    public boolean flagRaised() {
        return flagRaised;
    }
}
