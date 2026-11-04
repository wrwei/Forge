# Void: cold v3, LRE pass 5

Forbidden read: the session ran `cat pipeline.yaml | head -40` (command 13 of its
session log) while reading its inputs. `pipeline.yaml` is not on the reading list
of `review/prereg-v3-study-free.md`, and a forbidden read voids a pass (Validity
section). The 40 lines read name no study. Replaced by cold LRE pass 11. The record
(`iter-1/`, converged) is kept and not counted.

**Withdrawn (30 October 2026, Amendment 5).** Reading `pipeline.yaml` is a permitted
read: `CLAUDE.md` and layer prompt `01_architecture.md`, both on the reading list,
name its key `agent.active_case_study` as where the study is named. This session is
valid; its replacement is withdrawn unused.
