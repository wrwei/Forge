# Void: Reference v3, LRE run 1 (declared 30 October 2026, after the run was committed)

Forbidden read: the session ran `grep -n -A5 'agent' pipeline.yaml | head -30`
(command 5 of its session log) while reading its inputs. `pipeline.yaml` is not
on the reading list of `review/prereg-v3-study-free.md`, and a forbidden read
voids a run (Validity section). The original audit missed the read; it was found
when the same read was checked across all sessions after cold LRE pass 5. The
lines read were the `agent` section of the configuration at the first pin
(`5280e26e`), which names no study. Replaced by Reference LRE run 6. The record
(`iter-1/`, converged at iteration 1) is kept and not counted.

**Withdrawn (30 October 2026, Amendment 5).** Reading `pipeline.yaml` is a permitted
read: `CLAUDE.md` and layer prompt `01_architecture.md`, both on the reading list,
name its key `agent.active_case_study` as where the study is named. This session is
valid; its replacement is withdrawn unused.
