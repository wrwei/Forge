# SRanger requirements — ID prefix conventions

The SRanger case study uses 23 structured requirements organised by
prefix. Every requirement carries the schema documented in CLAUDE.md
(`kind`, `name`, `id`, `description`, `priority`, `types`); the
prefix tells you which architectural layer the requirement belongs to.

## Prefix conventions

| Prefix      | Layer                            | Examples |
|-------------|----------------------------------|----------|
| `SR-ARCH*`  | System & controller architecture | `SR-ARCH1` (system overview), `SR-ARCH2` (controller composition) |
| `SR-DM*`    | Data types                       | `SR-DM1` (SRangerMode enum), `SR-DM3` (InputEvent), `SR-DM4` (OutputEvent) |
| `SR-SF*`    | Sensor functions                 | `SR-SF1` (Distance) |
| `SR-Var*`   | State variables                  | `SR-Var1` (clockResetTime) |
| `SR-FR*`    | Functional requirements (states) | `SR-FR1` (StateMoving), `SR-FR2` (StateTurning), `SR-FR3` (StateFinal) |
| `SR-GP*`    | Guard predicates                 | `SR-GP1` (obstacleDetected), `SR-GP2` (turnDurationElapsed) |
| `SR-Beh*`   | Behavioural transitions          | the seven state-machine transitions |
| `SR-DC*`    | Design constraints               | `SR-DC1` (UniqueTransitions) |

## Files in this directory

- `requirement_all.json` — canonical source of truth, all 23
  requirements. **Read this** when implementing.
- `requirement_all.txt` — human-readable text rendering of
  `requirement_all.json`.

## Origin

The SRanger model is derived from the University of York RoboStar
group's published case study at
https://robostar.cs.york.ac.uk/case_studies/sranger/index.html.
SRanger is included in this pipeline as a minimal-complexity
third case study alongside LRE (51 requirements / 4 modes / 1
controller / continuous-control) and Chemical Detector
(81 requirements / 13 modes / 2 controllers / task-oriented).
SRanger has 23 requirements, 3 modes, 1 controller, and exercises
the timed-transition encoding (the autonomous Turning → Moving
transition uses a clock-since predicate) that LRE does not.

## Adding a new prefix

If a new architectural layer is needed, add a row to the table
above and use the same `<PROJECT>-<LAYER>n` convention.
