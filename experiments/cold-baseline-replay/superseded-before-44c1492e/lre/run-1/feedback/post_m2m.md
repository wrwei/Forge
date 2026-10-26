# 4 — Model-to-Model (ETL Transformation) — FAILED

## Summary
4 — Model-to-Model (ETL Transformation) failed.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: egl_template_error — EGL template runtime error

**Raw**
```
4 — Model-to-Model (ETL Transformation) failed with exit code 1.
> Task :run FAILED
SLF4J(W): No SLF4J providers were found.
SLF4J(W): Defaulting to no-operation (NOP) logger implementation
SLF4J(W): See https://www.slf4j.org/codes.html#noProviders for further details.
Discovering Java sources in: /Users/ranwei/Git/Forge/java.generated.project/src/main/java
Model saved to: /Users/ranwei/Git/Forge/forge.transformations/output/discovered_model.xmi
Extracting RoboChart state machine...
Exception in thread "main" java.lang.RuntimeException: ETL transformation failed
	at forge.transformations.m2m.Java2RoboChartTransformer.transform(Java2RoboChartTransformer.java:188)
	at forge.transformations.m2m.Java2RoboChartTransformer.transform(Java2RoboChartTransformer.java:108)
	at forge.transformations.m2m.TransformPhase.run(TransformPhase.java:81)
	at forge.transformations.core.App.main(App.java:68)
Caused by: Property 'value' not found in object org.eclipse.emf.ecore.impl.DynamicEObjectImpl@780ee3ad (eClass: org.eclipse.emf.ecore.impl.EClassImpl@56f9de3b (name: StringExp) (instanceClassName: null) (abstract: false, interface: false))
	at (/Users/ranwei/Git/Forge/forge.transformations/build/resources/main/transformations/java2robochart.etl@3954:19-3954:33)
	at (/Users/ranwei/Git/Forge/forge.transformations/build/resources/main/transformations/java2robochart.etl@3954:19-3954:53)
	at (/Users/ranwei/Git/Forge/forge.transformations/build/resources/main/transformations/java2robochart.etl@3954:12-3954:54)
	at (/Users/ranwei/Git/Forge/forge.transformations/build/resources/main/transformations/java2robochart.etl@3953:77-3955:9)
	at (/Users/ranwei/Git/Forge/forge.transformations/build/resources/main/transformations/java2robochart.etl@3953:8-3955:9)
	at (/Users/ranwei/Git/Forge/forge.transformations/build/resources/main/transformations/java2robochart.etl@3951:42-3956:5)
	at (/Users/ranwei/Git/Forge/forge.transformations/build/resources/main/transformations/java2robochart.etl@3951:4-3956:5)
```

**Fix directive**
The EGL template raised a runtime error while generating output from the RoboChart model. This is not a Java compile error. Inspect the .egl@<line> reference in the trace and the model element at that point to identify the missing or malformed structure.

## Files to review
(none identified)

## Next step
Read the issue above, follow the fix directive, edit the Java source, and re-run this phase.
