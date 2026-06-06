package chemdetector.movement.event;

import chemdetector.data.Angle;
import chemdetector.data.Loc;

/**
 * Events the movement subsystem consumes: Vehicle boundary events
 * (obstacle, odometer) plus the shared events from the gas-analysis
 * subsystem (turn, stop, resume).
 */
public sealed interface InputEvent {

    /** CD-Evt2: obstacle reported by the Vehicle's obstacle detector. */
    record Obstacle(Loc l) implements InputEvent {}

    /** CD-Evt3: current cumulative distance from the odometer. */
    record Odometer(double d) implements InputEvent {}

    /** CD-Evt4: heading update from the gas-analysis subsystem. */
    record Turn(Angle a) implements InputEvent {}

    /** CD-Evt5: stop signal from the gas-analysis subsystem. */
    record Stop() implements InputEvent {}

    /** CD-Evt6: resume signal from the gas-analysis subsystem. */
    record Resume() implements InputEvent {}
}
