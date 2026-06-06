package chemdetector.event;

import chemdetector.annotation.RoboChartType;
import chemdetector.datatype.Angle;
import chemdetector.datatype.Loc;

/**
 * Input events consumed by the movement subsystem.
 * <ul>
 *   <li>CD-Evt2 obstacle: side of robot where an obstacle was detected.</li>
 *   <li>CD-Evt3 odometer: current cumulative distance.</li>
 *   <li>CD-Evt4 turn: angle to face next (from gas-analysis).</li>
 *   <li>CD-Evt5 stop: source confirmed (from gas-analysis).</li>
 *   <li>CD-Evt6 resume: continue searching (from gas-analysis).</li>
 * </ul>
 */
public sealed interface MovementEvent {
    record Obstacle(Loc loc) implements MovementEvent {}
    record Odometer(@RoboChartType("real") double distance) implements MovementEvent {}
    record Turn(Angle angle) implements MovementEvent {}
    record Stop() implements MovementEvent {}
    record Resume() implements MovementEvent {}
}
