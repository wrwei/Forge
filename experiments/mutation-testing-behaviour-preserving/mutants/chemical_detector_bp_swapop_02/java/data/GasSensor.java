package chemdetector.data;

import chemdetector.annotation.RoboChartType;

/**
 * A single sensor reading: a chemical identity ({@code chemId}, CD-DM4) and
 * its measured intensity ({@code intensityValue}, CD-DM5) flattened to
 * primitive fields.
 * <p>
 * <b>Flattening rationale.</b> The original schema nested two single-field
 * records ({@code Chem} and {@code Intensity}) inside {@code GasSensor}.
 * When the RoboChart CSP generator encoded the nested layout it produced
 * {@code nametype GasSensor = (Chem, Intensity)} where {@code Intensity}
 * was declared <i>after</i> {@code GasSensor} in the generated defs file —
 * the forward reference left the second field of the {@code gas} channel
 * polymorphic ({@code <(Int, a)>}) and FDR4 refused to instantiate it.
 * Collapsing the nested records into primitive fields removes the forward
 * reference: {@code GasSensor} is now {@code (core_nat, core_real)} which
 * the CSP encoder resolves at the channel-declaration scope. The Java-side
 * primitive {@code int}/{@code double} fields map cleanly to RoboChart
 * {@code nat}/{@code real} via the {@code @RoboChartType} annotations, and
 * downstream consumers wrap the primitive intensity into an {@link Intensity}
 * instance only where an {@link Intensity}-typed value is genuinely needed
 * (e.g. the {@code intensity()} aggregation function return type).
 */
public record GasSensor(
        @RoboChartType("nat") int chemId,
        @RoboChartType("real") double intensityValue) {
}
