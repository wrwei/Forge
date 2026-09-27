package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;

/**
 * Destination of the events a controller emits.
 */
public interface OutputPort {
    void send(OutputEvent outputEvent);
}
