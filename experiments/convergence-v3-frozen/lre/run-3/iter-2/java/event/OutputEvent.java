package lre.event;

/** Events the Last Response Engine sends to the autopilot controller. */
public sealed interface OutputEvent {

    /** Advises a velocity, in m/s. */
    record advVel(double value) implements OutputEvent {
    }

    /** Advises a heading, in degrees. */
    record advHdng(double value) implements OutputEvent {
    }
}
