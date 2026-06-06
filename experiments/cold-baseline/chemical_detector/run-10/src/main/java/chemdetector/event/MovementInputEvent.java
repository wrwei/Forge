package chemdetector.event;

import chemdetector.annotation.RoboChartType;
import chemdetector.datatype.Angle;
import chemdetector.datatype.Loc;

/**
 * Input events consumed by the movement controller.
 *
 * <p>Boundary inputs from the Vehicle: {@link Obstacle}, {@link Odometer}.
 * Shared inter-controller inputs from the gas-analysis subsystem:
 * {@link Turn}, {@link Stop}, {@link Resume}.
 */
public sealed interface MovementInputEvent {

    /** CD-Evt2. Obstacle detection from the Vehicle. */
    record Obstacle(Loc value) implements MovementInputEvent {
    }

    /** CD-Evt3. Odometer reading from the Vehicle. */
    record Odometer(@RoboChartType("real") double value) implements MovementInputEvent {
    }

    /** CD-Evt4. Turn command from gas-analysis. */
    record Turn(Angle value) implements MovementInputEvent {
    }

    /** CD-Evt5. Stop signal from gas-analysis. */
    record Stop() implements MovementInputEvent {
    }

    /** CD-Evt6. Resume signal from gas-analysis. */
    record Resume() implements MovementInputEvent {
    }
}
