package chemical_detector.event;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;

import java.util.List;

/**
 * Events received by the reasoning subsystems. Gas, Obstacle and Odometer come
 * from the Vehicle; Turn, Stop and Resume are sent by the gas-analysis
 * subsystem and received by the movement subsystem.
 */
public sealed interface InputEvent {
    /** One multi-sensor gas reading, ordered by sensing direction (CD-Evt1, CD-DM7). */
    record Gas(List<GasSensor> readings) implements InputEvent {}

    /** The side on which an obstacle has been detected (CD-Evt2). */
    record Obstacle(Loc side) implements InputEvent {}

    /** The cumulative distance travelled by the Vehicle (CD-Evt3). */
    record Odometer(@RoboChartType("real") double distance) implements InputEvent {}

    /** The direction the robot should face next (CD-Evt4). */
    record Turn(Angle direction) implements InputEvent {}

    /** The chemical source has been confirmed (CD-Evt5). */
    record Stop() implements InputEvent {}

    /** A no-gas reading was analysed; continue searching (CD-Evt6). */
    record Resume() implements InputEvent {}
}
