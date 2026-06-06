You are doing **Layer 2** of the top-down codegen sequence
documented in `forge.assets/vibe-coding-prompts/README.md`.

# Layer 2 — Skeleton: types, enums, events, interfaces

Using the architecture from Layer 1, generate the Java skeleton:

- Records for immutable value types.
- Enums (with the initial mode in the first position).
- Sealed event hierarchies (`InputEvent`, `OutputEvent`) with one
  `record` per event variant.
- Classes with their public method signatures, but **empty bodies**
  (return default values where needed so the project compiles).
- The `@RoboChartType` annotation class itself, in an `annotation`
  subpackage.

Apply the rules in `forge.assets/prompts/java_codegen_rules.txt`:

- `@RoboChartType("nat")` on int fields/parameters representing
  natural numbers; `@RoboChartType("real")` on double fields.
- No lambdas, streams, method references, ternary, pattern-matching
  instanceof.
- Records for immutable types; final fields by default.
- Package-private class visibility unless public is required.
- No wildcard imports.

The output of this layer is a Java project that compiles cleanly
(`./gradlew.bat build`) but does no real work yet.

## Wrap-up

Follow `forge.assets/prompts/codegen_trace_rules.txt` to write
`java.generated.project/result_codegen.json` — the requirement → Java
traceability map that the dashboard's Coverage phase consumes. Do it
every layer so coverage is checkable at any point.

## Stop condition

Project compiles, all public surfaces declared, all bodies empty,
`result_codegen.json` regenerated. Wait for me to confirm before
Layer 3.
