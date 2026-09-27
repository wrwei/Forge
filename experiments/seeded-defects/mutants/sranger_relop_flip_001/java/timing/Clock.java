package sranger.timing;

import sranger.annotation.RoboChartType;

/**
 * Clock dependency for the SRanger controller.
 *
 * <p>The ETL detects this class by simple name {@code Clock} (and additionally by the
 * {@code @Clock} annotation, applied here via fully-qualified name to avoid the
 * type/annotation name clash) so that timed transitions in the controller can be
 * mapped to RoboChart's {@code since(clock)} construct. The canonical clock-call
 * method name recognised by the M2M is {@link #nowMs()} — see the "Naming
 * conventions consumed by the M2M (ETL)" table in CLAUDE.md.</p>
 *
 * <p><b>Iter-4 package rename: {@code sranger.clock} → {@code sranger.timing}</b>.
 * Iter-3 declared this class in package {@code sranger.clock}; even though the
 * field-name {@code clock} was renamed to {@code cycleClock}, the package-name
 * segment {@code clock} still appeared in qualified references the M2M pulled
 * through to the Isabelle theory, surfacing as {@code Extra variables on rhs:
 * "clock"} during {@code isabelle build}. The package is renamed to {@code timing}
 * — one of the documented safe alternatives in {@code java_codegen_rules.txt}
 * ("Reserved-word collisions") — to remove the symbol from every name the
 * pipeline can see. The class name {@code Clock} is preserved because the ETL
 * matches the clock dependency by SIMPLE class name (not package), so the
 * Clock-detection pattern continues to fire.</p>
 *
 * <p>{@link #nowMs()} returns a monotonic time value in milliseconds. The spec's
 * {@code turnDuration} is in seconds; call sites convert via {@code SRangerConstants.turnDurationMs}
 * (a precomputed millisecond-domain mirror of the seconds-domain constant) so that
 * the time-since predicate stays in a single unit, which is what the M2M's clock
 * pattern matcher expects.</p>
 */
@sranger.annotation.Clock
public final class Clock {

    @RoboChartType("real")
    private double offset = 0.0;

    /** Current monotonic time in milliseconds. */
    @RoboChartType("real")
    public double nowMs() {
        return offset + System.nanoTime() / 1.0e6;
    }

    /** Override the monotonic time in milliseconds (test seam). */
    public void setNowMs(@RoboChartType("real") double millis) {
        this.offset = millis - System.nanoTime() / 1.0e6;
    }
}
