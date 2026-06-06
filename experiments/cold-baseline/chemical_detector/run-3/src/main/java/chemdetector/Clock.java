package chemdetector;

import chemdetector.annotation.RoboChartType;

/**
 * Logical clock dependency. The movement-subsystem clock T
 * (CD-MV-Clock1) is realised by capturing nowMs() into a state
 * field; the ETL rewrites time predicates of the form
 * "clock.nowMs() - <field> < CONST" into "since(<field>) < CONST".
 */
@chemdetector.annotation.Clock
public final class Clock {

    @RoboChartType("nat")
    private long currentMs;

    public Clock() {
        this.currentMs = 0L;
    }

    @RoboChartType("nat")
    public long nowMs() {
        return currentMs;
    }

    public void advance(@RoboChartType("nat") long deltaMs) {
        this.currentMs = this.currentMs + deltaMs;
    }
}
