package chemdetector.event;

/**
 * Trivial in-process channel from the gas-analysis subsystem to the movement
 * subsystem. Only one event can be in flight at a time; consumer reads via
 * {@link #poll()} which returns {@code null} when no event is pending.
 * <p>
 * The {@link #publish(MovementEvent)} call site uses the canonical
 * "actuator.apply(new OutputEvent.SubType(args))" shape that the ETL's
 * {@code extractOutputEventInfo} pattern matcher recognises. That hands the
 * event variant's record-name straight to {@code getOrCreateEvent} (so the
 * RoboChart output event is named {@code turn} / {@code stop} / {@code resume}
 * matching the MovementController input side, making them Shared events in
 * the multi-controller package), and crucially lets {@code markEventTyped}
 * look up {@link MovementEvent.Turn}'s record metadata so the {@code turn}
 * channel is typed as {@code Angle}. Iter-4 used named wrapper methods
 * ({@code sendTurn(Angle)}) which the ETL could not type-infer through;
 * the {@code turn} channel came out as {@code int} and FDR4 rejected the
 * mismatch on {@code sendTurn.out!anl}.
 */
public final class EventBus {

    private MovementEvent pending;

    /**
     * Publish a movement event from the gas-analysis subsystem. The argument
     * MUST be a {@code new MovementEvent.Variant(...)} expression — see the
     * class-level doc for why this exact shape matters to the ETL.
     */
    public void publish(MovementEvent e) {
        this.pending = e;
    }

    public MovementEvent poll() {
        MovementEvent e = this.pending;
        this.pending = null;
        return e;
    }
}
