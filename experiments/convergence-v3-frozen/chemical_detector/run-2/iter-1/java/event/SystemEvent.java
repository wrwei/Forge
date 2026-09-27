package chemical_detector.event;

import chemical_detector.data.Angle;
import chemical_detector.data.GasSensor;
import chemical_detector.data.Loc;
import java.util.List;

/**
 * Every event on the system event bus. The Vehicle emits {@code Gas},
 * {@code Obstacle} and {@code Tick}; the gas-analysis subsystem emits
 * {@code Turn}, {@code Stop} and {@code Resume}; the movement subsystem
 * emits {@code Flag}.
 */
public sealed interface SystemEvent {

    /** One multi-sensor gas reading, position-ordered by sensing direction. */
    record Gas(List<GasSensor> gs) implements SystemEvent {}

    /** Side of the robot on which an obstacle has been encountered. */
    record Obstacle(Loc l) implements SystemEvent {}

    /** Direction the robot should face next. */
    record Turn(Angle a) implements SystemEvent {}

    /** Chemical source confirmed: halt. */
    record Stop() implements SystemEvent {}

    /** No target chemical in the last reading: continue searching. */
    record Resume() implements SystemEvent {}

    /** Source located; reported to the Vehicle. */
    record Flag() implements SystemEvent {}

    /** Platform time step, consumed by halted modes to stay live. */
    record Tick() implements SystemEvent {}
}
