package chemdetector.event;

import chemdetector.annotation.RoboChartType;
import chemdetector.datatype.Angle;
import chemdetector.datatype.Loc;

/**
 * Input events consumed by the movement state machine.
 * Includes Vehicle-sourced events (obstacle, odometer) and shared events from
 * gas-analysis (turn, stop, resume).
 */
public sealed interface MovementInputEvent {
    record Obstacle(Loc l) implements MovementInputEvent {}
    record Odometer(@RoboChartType("real") double d) implements MovementInputEvent {}
    record Turn(Angle a) implements MovementInputEvent {}
    record Stop() implements MovementInputEvent {}
    record Resume() implements MovementInputEvent {}
}
