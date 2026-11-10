package chemdetector.event;

import chemdetector.annotation.RoboChartType;
import chemdetector.data.Angle;
import chemdetector.data.Loc;

/**
 * Sealed input alphabet of the movement subsystem.
 * <p>
 * Inputs from the gas-analysis subsystem: {@link Turn} (CD-Evt4),
 * {@link Stop} (CD-Evt5), {@link Resume} (CD-Evt6).
 * Inputs from the Vehicle: {@link Obstacle} (CD-Evt2), {@link Odometer}
 * (CD-Evt3). {@link Tick} drives autonomous transitions.
 */
public sealed interface MovementEvent {

    /** CD-Evt4: turn signal carrying the new heading. */
    record Turn(Angle a) implements MovementEvent {
    }

    /** CD-Evt5: chemical-source-found stop signal (no payload). */
    record Stop() implements MovementEvent {
    }

    /** CD-Evt6: resume signal returning the movement subsystem to Waiting. */
    record Resume() implements MovementEvent {
    }

    /** CD-Evt2: obstacle hit, carrying which side it was hit on. */
    record Obstacle(Loc l) implements MovementEvent {
    }

    /** CD-Evt3: cumulative travelled distance from the odometer. */
    record Odometer(@RoboChartType("real") double d) implements MovementEvent {
    }

    /** Cycle tick that lets autonomous transitions fire. */
    record Tick() implements MovementEvent {
    }
}
