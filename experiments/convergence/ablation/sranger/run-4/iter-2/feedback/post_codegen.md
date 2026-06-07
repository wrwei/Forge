# Post-codegen review — sranger, ablation run-4, iter 2

**Status:** complete

## Summary

Minimal fix driven by iter-1 `post_preflight` (the only failing visible
phase): added `@RoboChartType("real")` to the private constant
`Sensor.NO_READING_DISTANCE` (lint rule4_double_missing_real_annotation).
No other changes. The iter-1 design notes (Halted-not-Final, tick
self-loop on Halted, Move as multi-arg operation call, abstract clock
units, constructor entry action, guarded obstacle transition, 1000.0 m
no-reading default) still stand.

## Issues for user review

(none new this iter)

## Next step

Re-run the pipeline and check the eight visible phases.
