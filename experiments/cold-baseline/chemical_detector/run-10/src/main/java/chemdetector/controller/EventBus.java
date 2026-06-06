package chemdetector.controller;

import java.util.ArrayDeque;
import java.util.Deque;
import chemdetector.event.GasAnalysisOutputEvent;
import chemdetector.event.MovementInputEvent;

/**
 * Wires gas-analysis output events (turn / stop / resume) into movement
 * controller inputs. Operates as a simple in-memory queue; the driver
 * polls {@link #poll()} after each gas-analysis step to forward any
 * emitted events to the movement controller.
 */
public final class EventBus {

    private final Deque<MovementInputEvent> queue;

    public EventBus() {
        this.queue = new ArrayDeque<>();
    }

    public void publish(GasAnalysisOutputEvent event) {
        if (event instanceof GasAnalysisOutputEvent.Turn) {
            GasAnalysisOutputEvent.Turn t = (GasAnalysisOutputEvent.Turn) event;
            this.queue.addLast(new MovementInputEvent.Turn(t.value()));
        } else if (event instanceof GasAnalysisOutputEvent.Stop) {
            this.queue.addLast(new MovementInputEvent.Stop());
        } else if (event instanceof GasAnalysisOutputEvent.Resume) {
            this.queue.addLast(new MovementInputEvent.Resume());
        }
    }

    public MovementInputEvent poll() {
        return this.queue.pollFirst();
    }

    public boolean isEmpty() {
        return this.queue.isEmpty();
    }
}
