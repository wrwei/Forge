# Post-codegen review — sranger, iter 2

**Status:** complete.

Minimal fix driven by iter-1 `post_preflight` (the only failed visible phase):
added `@RoboChartType("real")` to the five flagged `double` fields —
`SRangerConstants.MOVE_VEL`, `TURN_VEL`, `OBSTACLE_THRESHOLD`, `TURN_DURATION`,
and `Sensor.NO_READING_DEFAULT`. No other changes.

**Next step:** re-run the pipeline; check visible phases.
