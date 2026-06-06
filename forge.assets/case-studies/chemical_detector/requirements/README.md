# Chemical Detector requirements — ID prefix conventions

The Chemical Detector case study uses 81 structured requirements
organised by prefix. Every requirement carries the schema documented
in CLAUDE.md (`kind`, `name`, `id`, `description`, `priority`,
`types`); the prefix tells you which architectural layer the
requirement belongs to.

## Prefix conventions

| Prefix          | Layer                                | Examples |
|-----------------|--------------------------------------|----------|
| `CD-ARCH*`      | System & subsystem architecture      | `CD-ARCH1` (system overview), `CD-ARCH2` (subsystem composition) |
| `CD-DM*`        | Data types and records               | `CD-DM1` (Status enum), `CD-DM6` (GasSensor record) |
| `CD-Const*`     | Configuration constants              | `CD-Const1` (thr), `CD-Const2` (lv) |
| `CD-Fn*`        | Mathematical / domain functions      | `CD-Fn1` (analysis), `CD-Fn2` (intensity) |
| `CD-Evt*`       | Events on the system event bus       | `CD-Evt1` (gas), `CD-Evt7` (flag) |
| `CD-OP*`        | Operations (platform + local)        | `CD-OP1` (move), `CD-OP4` (changeDirection) |
| `CD-GA-Var*`    | Gas-analysis state variables         | `CD-GA-Var1` (gs), `CD-GA-Var2` (sts) |
| `CD-GA-FR*`     | Gas-analysis states                  | `CD-GA-FR2` (Reading), `CD-GA-FR4` (GasDetected) |
| `CD-GA-Beh*`    | Gas-analysis behavioural transitions | `CD-GA-Beh4` (Analysis → NoGas) |
| `CD-MV-Var*`    | Movement state variables             | `CD-MV-Var1` (a), `CD-MV-Var4` (l) |
| `CD-MV-Clock*`  | Movement clocks                      | `CD-MV-Clock1` (T) |
| `CD-MV-FR*`     | Movement states                      | `CD-MV-FR1` (Waiting), `CD-MV-FR4` (Avoiding) |
| `CD-MV-Beh*`    | Movement behavioural transitions     | `CD-MV-Beh7` (Going → Avoiding on obstacle) |
| `CD-DC*`        | Design constraints                   | `CD-DC1` (UniqueTransitions) |

The system has **two reasoning subsystems**: a gas-analysis subsystem
and a movement subsystem. The `CD-GA-*` and `CD-MV-*` sub-prefixes
scope variables / states / behavioural transitions to one subsystem
each, so a reader can grep `CD-GA-` to see everything about
gas-analysis behaviour without wading through movement logic, and
vice versa.

The `changeDirection` operation is described as a single requirement
(`CD-OP4`) because it is small enough that splitting would add noise.

## Files in this directory

- `requirement_all.json` — canonical source of truth, all 81
  requirements. **Read this** when implementing.
- `requirement_all.txt` — human-readable text rendering of
  `requirement_all.json`, one requirement per paragraph.
- `README.md` — this file.
