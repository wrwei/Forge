#!/usr/bin/env python3
"""Record one v2 convergence iteration into the central collection.

WHY THIS EXISTS RATHER THAN A PATCH TO snapshot_iter.py
-------------------------------------------------------
The pinned recorder (experiments/scripts/snapshot_iter.py at the v2 SHA) has
two properties that make it unusable for the v2 collection as-is:

  1. it writes to experiments/convergence/<study>/iter-N — with NO run level,
     while the collection (and the v1 archive) is <study>/run-K/iter-N; and
  2. it emits codegen_cost as "TODO" placeholders for a human to fill in by
     hand, which is the direct cause of v1's 36% token coverage.

Patching the pinned script would change the pre-registered SHA. This recorder
lives OUTSIDE the pinned tree instead (like codex_usage.py), so the v2 SHA
stays exactly as pre-registered while the recording improves.

It reuses the pinned script's own copy helpers by importing them, so the
artefact layout is identical to v1's and not a reimplementation.

METERING IS MANDATORY. The recorder REFUSES to write a record with no token
count: an unmetered row is indistinguishable from a free iteration, and
closing that gap is the only reason the v2 collection is being run.

Usage:
  snapshot_v2.py <study> --run K --iter N --worktree <path> \
      --thread <codex-thread-id> [--collection <root>] [--force]

Token counts are read from the Codex log via codex_usage.py, not passed by
hand — a number typed by a human is a number that can drift from the log.
"""
from __future__ import annotations

import argparse
import importlib.util
import json
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path

HERE = Path(__file__).resolve().parent
# The v2 collection is a SEPARATE tree. Writing into experiments/convergence
# would overwrite the v1 archive, whose run-K/iter-N paths collide exactly with
# v2's — that happened once and destroyed archived records (recovered from git).
# The v1 archive is read-only evidence for the published paper; never a target.
DEFAULT_COLLECTION = Path("/Users/ranwei/Gitee/formal_method_guided_vibe_coding/experiments/convergence-v2")
V1_ARCHIVE = Path("/Users/ranwei/Gitee/formal_method_guided_vibe_coding/experiments/convergence")

# The pre-registered v2 infrastructure SHA. Worktrees are created detached at
# it by scripts/setup_v2_run.sh. Recording a run from any OTHER commit silently
# mixes infrastructure versions inside one collection, so it must be a
# deliberate, flagged act rather than a default.
PREREGISTERED_SHA = "b808424c7c1b51157f4f14eac6e76e00d5beed90"

# Phases the pipeline runs, in order. A phase that produces no post_<phase>.json
# is NOT a pass — it usually means the phase died before writing a verdict (the
# fdr4 case: `refines` absent from PATH). The record must say "no verdict"
# rather than leave the phase invisible.
PIPELINE_PHASES = ["compile", "coverage", "preflight", "t2m", "m2m", "m2t",
                   "dafny_gen", "isabelle_gen", "fdr4", "dafny_verify",
                   "isabelle_verify", "vacuity"]


def read_head_sha(wt: Path) -> str:
    """Read the worktree's HEAD, or exit. Called BEFORE anything is written.

    Neutralise the user's global git config: on this machine reading it from
    some contexts raises "Operation not permitted" and git then returns an
    empty SHA instead of failing — which would silently record a run with no
    infrastructure version at all.
    """
    import os as _os
    env = {**_os.environ, "GIT_CONFIG_GLOBAL": "/dev/null",
           "GIT_CONFIG_SYSTEM": "/dev/null"}
    r = subprocess.run(["git", "-C", str(wt), "rev-parse", "HEAD"],
                       capture_output=True, text=True, env=env)
    sha = (r.stdout or "").strip()
    if len(sha) != 40:
        sys.exit("error: could not read the worktree's HEAD SHA "
                 f"({(r.stderr or '').strip() or 'empty output'}).\n"
                 "  REFUSING to write: a record without an infrastructure version "
                 "cannot be tied to the pre-registered SHA.")
    return sha


def check_verdicts_fresh(wt: Path, allow_stale: bool) -> None:
    """Refuse to record verdicts older than the Java they supposedly judge.

    post_*.json/md are gitignored and are NEVER cleared between iterations —
    neither the driver nor this recorder deletes them. So if the pipeline dies
    before a phase runs (or is not run at all), the PREVIOUS iteration's
    verdict files are still on disk, and this recorder would copy them into the
    new iteration's record: iteration N's Java filed against iteration N-1's
    results. That row is false data of exactly the kind the collection exists
    to avoid, and nothing downstream could detect it.

    The check is a timestamp comparison, which is why it is a guard and not a
    proof: it catches the stale case, not a pipeline that reran and produced
    identical output.
    """
    if allow_stale:
        return
    corr = wt / "forge.assets" / "corrections"
    verdicts = sorted(corr.glob("post_*.json")) + sorted(corr.glob("post_*.md"))
    if not verdicts:
        return          # handled by check_pipeline_ran
    java_root = wt / "java.generated.project" / "src"
    javas = list(java_root.rglob("*.java"))
    if not javas:
        return          # handled elsewhere: no Java at all
    newest_java = max(p.stat().st_mtime for p in javas)
    newest_verdict = max(p.stat().st_mtime for p in verdicts)
    if newest_verdict < newest_java:
        import time as _t
        fmt = lambda t: _t.strftime("%Y-%m-%d %H:%M:%S", _t.localtime(t))
        sys.exit(
            "error: the phase verdicts are OLDER than the generated Java.\n"
            f"  newest .java    : {fmt(newest_java)}\n"
            f"  newest post_*.* : {fmt(newest_verdict)}\n"
            "  post_* files are gitignored and are never cleared between\n"
            "  iterations, so these are almost certainly the PREVIOUS\n"
            "  iteration's results. Recording them would file this iteration's\n"
            "  code against last iteration's verdicts.\n"
            "  Re-run the pipeline, then record. Use --allow-stale-verdicts only\n"
            "  if you know the verdicts genuinely belong to this Java.")


def load_pinned(worktree: Path):
    """Import the pinned snapshot_iter.py for its copy helpers."""
    p = worktree / "experiments" / "scripts" / "snapshot_iter.py"
    if not p.exists():
        sys.exit(f"error: pinned recorder not found at {p}")
    spec = importlib.util.spec_from_file_location("pinned_snapshot", p)
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def read_tokens_claude(tokens_arg: str | None, turns_arg: int | None) -> dict:
    """Metering for a non-Codex actor (Claude Code), whose usage is NOT in
    ~/.codex/logs_2.sqlite.

    The Codex path below stays exactly as it was: a Codex run still cannot be
    recorded without a real metered thread. This branch exists only for
    --actor claude-code, and it keeps the same standard of honesty by REFUSING
    to invent a number: the operator must pass the figure Claude Code itself
    reports (/cost, or the session's token line), or explicitly pass
    --tokens unavailable, which records the absence rather than a zero.

    Why not read Claude Code's own log? Its state lives under ~/.claude, which
    is not a granted path for the assistant and has no documented stable
    schema. An operator-supplied figure that is LABELLED as such is more
    defensible than a scrape that may silently drift.
    """
    if tokens_arg is None:
        sys.exit("error: --actor claude-code requires --tokens N (from Claude Code's\n"
                 "  own /cost or session token line), or --tokens unavailable to\n"
                 "  record the absence explicitly.\n"
                 "  REFUSING to write a record with a fabricated or zero cost.")
    if tokens_arg.lower() in ("unavailable", "unknown", "na"):
        return {"total_tokens": None, "turns": turns_arg, "token_schema": "unavailable",
                "cost_kind": "unavailable"}
    try:
        n = int(tokens_arg.replace(",", "").replace("_", ""))
    except ValueError:
        sys.exit(f"error: --tokens {tokens_arg!r} is not an integer and not 'unavailable'")
    if n <= 0:
        sys.exit("error: --tokens must be positive; pass 'unavailable' if unknown")
    return {"total_tokens": str(n), "turns": turns_arg, "token_schema": "operator_reported",
            "cost_kind": "operator_reported"}


def read_tokens(thread: str) -> dict:
    """Ask codex_usage.py for the real counts. Non-zero exit => no data."""
    tool = HERE / "codex_usage.py"
    if not tool.exists():
        sys.exit(f"error: {tool} not found — cannot meter this iteration")
    r = subprocess.run([sys.executable, str(tool), "--thread", thread, "--snapshot-args"],
                       capture_output=True, text=True)
    if r.returncode != 0:
        sys.exit("error: no token usage found for thread " + thread + "\n" +
                 (r.stderr or "").rstrip() +
                 "\n  REFUSING to write an unmetered record. Fix the thread id, or if the\n"
                 "  Codex log has rotated (~9 days), record the loss explicitly instead.")
    out = {}
    toks = (r.stdout or "").split()
    for i, t in enumerate(toks):
        if t.startswith("--") and i + 1 < len(toks):
            out[t.lstrip("-").replace("-", "_")] = toks[i + 1]
    if "total_tokens" not in out:
        sys.exit("error: codex_usage.py returned no total_tokens; refusing to write.")
    return out


def check_pipeline_ran(wt: Path, allow_degenerate: bool) -> None:
    """Refuse to record a run whose phases all failed instantly.

    A phase that fails in 0.0s has not executed anything: the usual cause is an
    environment fault upstream (e.g. a non-executable gradlew making phase 2a
    die with Permission denied), which then cascades so EVERY phase reports
    failure. Such a row looks like a convergence data point but says nothing
    about the generated code, and silently banking one is worse than banking
    nothing. `--force` does NOT override this; pass --allow-degenerate if you
    genuinely want to archive a failed environment for the record.
    """
    if allow_degenerate:
        return
    verdicts = sorted((wt / "forge.assets" / "corrections").glob("post_*.json"))
    if not verdicts:
        # No machine-readable verdicts: fall back to the .md feedback files.
        mds = sorted((wt / "forge.assets" / "corrections").glob("post_*.md"))
        if not mds:
            sys.exit("error: no phase output in forge.assets/corrections/.\n"
                     "  The pipeline does not appear to have run in this worktree.\n"
                     "  REFUSING to record: artefacts alone are not a pipeline result.")
        return

    import json as _json
    statuses, durations = [], []
    for v in verdicts:
        try:
            d = _json.loads(v.read_text())
        except Exception:
            continue
        statuses.append(str(d.get("status", "")).lower())
        for k in ("duration_s", "duration", "elapsed_s", "wall_clock_s"):
            if k in d:
                try:
                    durations.append(float(d[k]))
                except Exception:
                    pass
                break

    if statuses and all(s == "failed" for s in statuses):
        instant = durations and all(x < 0.05 for x in durations)
        if instant or not durations:
            sys.exit(
                f"error: all {len(statuses)} phases failed"
                + (" with ~0s duration" if instant else "") + ".\n"
                "  That is an ENVIRONMENT failure cascading from the first phase, not a\n"
                "  result about the generated code. Check the first phase's post_*.md for\n"
                "  the real cause (a Permission denied, a missing tool on PATH, etc.),\n"
                "  fix it, and re-run the iteration.\n"
                "  REFUSING to record. Use --allow-degenerate to archive it anyway.")


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("study", choices=["sranger", "lre", "chemical_detector", "steam_boiler"])   # steam_boiler: pre-registration v3, Amendment 9
    ap.add_argument("--run", type=int, required=True)
    ap.add_argument("--iter", type=int, required=True, dest="iter_n")
    ap.add_argument("--worktree", required=True)
    ap.add_argument("--thread", required=True,
                    help="Codex thread id for THIS run; for --actor claude-code, a "
                         "session label (no Codex log lookup is performed)")
    ap.add_argument("--actor", default="codex", choices=["codex", "claude-code"],
                    help="which agent produced the Java. DIFFERENT ACTORS ARE "
                         "DIFFERENT CONDITIONS and must not be pooled into one "
                         "convergence rate; the record carries this field so the "
                         "analysis cannot conflate them.")
    ap.add_argument("--tokens", default=None,
                    help="--actor claude-code only: total tokens as reported by the "
                         "actor itself, or 'unavailable'. Never inferred.")
    ap.add_argument("--turns", type=int, default=None,
                    help="--actor claude-code only: turn count if known")
    ap.add_argument("--collection", default=str(DEFAULT_COLLECTION))
    ap.add_argument("--force", action="store_true")
    ap.add_argument("--allow-degenerate", action="store_true",
                    help="record even when every phase failed instantly (an "
                         "environment cascade). Off by default.")
    ap.add_argument("--allow-stale-verdicts", action="store_true",
                    help="record even when post_*.json are older than the Java "
                         "they judge (i.e. the pipeline was probably not re-run). "
                         "Off by default.")
    ap.add_argument("--allow-other-sha", action="store_true",
                    help="record from a worktree whose HEAD is not the "
                         "pre-registered v2 SHA. Off by default.")
    a = ap.parse_args()

    wt = Path(a.worktree)
    if not wt.exists():
        sys.exit(f"error: worktree {wt} does not exist")

    pin = load_pinned(wt)
    # EVERY refusal must fire before the first destructive filesystem op. The
    # SHA check used to run AFTER `rmtree(dst)` + the copies, so a worktree
    # with an unreadable HEAD destroyed the existing record and then exited 1,
    # leaving a half-written directory with no summary.json behind.
    if a.actor == "claude-code":
        tokens = read_tokens_claude(a.tokens, a.turns)
    else:
        if a.tokens is not None or a.turns is not None:
            sys.exit("error: --tokens/--turns apply only to --actor claude-code; a Codex "
                     "run is metered from its own log and must not be hand-fed a figure.")
        tokens = read_tokens(a.thread)
    check_pipeline_ran(wt, a.allow_degenerate)
    check_verdicts_fresh(wt, a.allow_stale_verdicts)
    sha = read_head_sha(wt)
    if sha != PREREGISTERED_SHA and not a.allow_other_sha:
        sys.exit(f"error: worktree HEAD is {sha[:12]}, not the pre-registered v2 SHA\n"
                 f"  ({PREREGISTERED_SHA[:12]}).\n"
                 "  Every record in this collection must come from the same pinned\n"
                 "  infrastructure or the run-to-run comparison is meaningless.\n"
                 "  Use --allow-other-sha to record from a different commit deliberately.")

    coll = Path(a.collection).resolve()
    # Hard refusal, not a warning: the v1 archive's layout is identical to v2's,
    # so a wrong --collection silently overwrites published evidence.
    if coll == V1_ARCHIVE.resolve() or V1_ARCHIVE.resolve() in coll.parents:
        sys.exit(f"error: {coll} is (inside) the v1 archive.\n"
                 "  experiments/convergence holds the PUBLISHED v1 records and must never\n"
                 "  be written to. Use experiments/convergence-v2 (the default).")

    final_dst = coll / a.study / f"run-{a.run}" / f"iter-{a.iter_n}"

    # A run directory marked RETIRED measured a superseded extractor SHA. Writing
    # a new iteration into it would silently pool two extractor states in one run
    # — what the protocol's guard rails forbid, and undetectable downstream
    # because the resulting record would be perfectly well-formed.
    retired = final_dst.parent / "RETIRED.md"
    if retired.exists():
        sys.exit(f"error: {final_dst.parent} is RETIRED (see {retired.name}).\n"
                 "  It holds records from a superseded extractor SHA. Use a new run\n"
                 "  number rather than extending it.")
    if final_dst.exists() and not a.force:
        sys.exit(f"error: {final_dst} exists. Use --force to overwrite.")

    # Stage into a sibling temp dir and swap only once the record is COMPLETE.
    # Writing in place meant any later failure left the destination destroyed
    # (rmtree had already run) and half-populated, which reads as a real record
    # but has no summary.json.
    import shutil as _sh
    dst = final_dst.with_name(final_dst.name + ".partial")
    if dst.exists():
        _sh.rmtree(dst)
    dst.mkdir(parents=True)

    # Java package name may differ from the study name (chemical_detector -> chemdetector).
    java_src = None
    for cand in (a.study, a.study.replace("_", ""), "chemdetector"):
        p = wt / "java.generated.project" / "src" / "main" / "java" / cand
        if p.exists():
            java_src = p
            break
    if java_src is None:
        sys.exit(f"error: no generated Java under {wt}/java.generated.project/src/main/java/")

    n_java = pin.copy_tree_overwrite(java_src, dst / "java")

    # Tests live under src/test/java, not src/main/java, so the main-tree copy
    # above misses them. They are part of what the actor produced and the
    # pipeline compiles them, so record them rather than silently dropping them.
    test_src = wt / "java.generated.project" / "src" / "test" / "java"
    n_test = pin.copy_tree_overwrite(test_src, dst / "java-test") if test_src.exists() else 0
    out_dir = wt / "forge.transformations" / "output"
    n_art = pin.copy_tree_overwrite(out_dir, dst / "formal-artefacts") if out_dir.exists() else 0

    # result_codegen.json — the requirement->code trace the agent emits and the
    # coverage phase (2b) consumes. It was NOT being recorded: the record kept
    # coverage's VERDICT but not the artefact the verdict was computed from, so a
    # coverage pass could not be re-checked or audited after the fact. It is also
    # the only per-iteration evidence of what the agent claims each requirement
    # maps to.
    # The agent's own summary. Prior iterations left this in the chat, so the
    # three narrative fields below stayed None for every record — and for an
    # iteration whose deliverable is a JUDGEMENT (e.g. "no change was warranted
    # because the requirements make this mode terminal") the judgement was the
    # result, and it was not being kept anywhere.
    agent_sum = wt / "forge.assets" / "corrections" / "ITER_SUMMARY.md"
    n_sum = 0
    if agent_sum.exists():
        _sh.copy2(agent_sum, dst / "agent_summary.md")
        n_sum = 1

    trace = wt / "java.generated.project" / "result_codegen.json"
    n_trace = 0
    if trace.exists():
        (dst / "traces").mkdir(parents=True, exist_ok=True)
        _sh.copy2(trace, dst / "traces" / "result_codegen.json")
        n_trace = 1
    fb = wt / "forge.assets" / "corrections"
    n_fb = pin.copy_glob(fb, "post_*.md", dst / "feedback") if fb.exists() else 0
    # The .json verdicts are the MACHINE-READABLE ones — the v1 archive carries
    # both, and copying only the .md left the record un-analysable without
    # re-parsing prose. Same for phase_timings.json, which is the only place a
    # phase's wall-clock lives (post_*.json has no duration field at all).
    n_fb_json = pin.copy_glob(fb, "post_*.json", dst / "feedback") if fb.exists() else 0
    timings = fb / "phase_timings.json"
    if timings.exists():
        (dst / "feedback").mkdir(parents=True, exist_ok=True)
        _sh.copy2(timings, dst / "feedback" / "phase_timings.json")

    # Phase roll-call. A phase with no post_<phase>.json did not pass — it
    # produced no verdict, usually because it died before writing one (fdr4
    # with `refines` absent from PATH is the live example). Without this the
    # phase is simply invisible in the record and a reader counting passes
    # would infer 7/11 rather than 7/12 with one phase unmeasured.
    phase_status = {}
    for ph in PIPELINE_PHASES:
        pj = fb / f"post_{ph}.json"
        if not pj.exists():
            phase_status[ph] = "no_verdict"
            continue
        try:
            phase_status[ph] = str(json.loads(pj.read_text()).get("status", "unknown"))
        except Exception:
            phase_status[ph] = "unparseable"
    missing_verdicts = [p for p, s in phase_status.items() if s == "no_verdict"]

    # REC-4 fix: a re-record must not wipe operator-filled narrative fields.
    # summary.json is rebuilt wholesale below, so carry forward any fields the
    # operator filled in an existing record ("fill in: converged, ..." is this
    # tool's own instruction — clobbering them on the next run made following
    # that instruction futile). Machine-derived fields are NOT carried: they
    # must reflect the current re-record.
    # NOTE: the rescore keys below are HISTORICAL FIELD NAMES — they name the
    # SHA at which a rescore happened and must NEVER be swept by a SHA
    # repoint. (A blind sweep did exactly that twice, rewriting
    # vacuity_rescore_14ab1c3c -> ..._b518c017 and orphaning the fields in
    # every record that carries them.)
    _CARRY = ("converged", "convergence_note", "failure_summary",
              "actor_codegen_summary",
              "vacuity_rescore_14ab1c3c",   # historical: VAC-1 rescore
              "f5_rescore_b518c017")        # historical: repair5 rescore
    _prev = {}
    _old = {}
    _prev_p = final_dst / "summary.json"
    if _prev_p.exists():
        try:
            _old = json.loads(_prev_p.read_text())
            _prev = {k: _old[k] for k in _CARRY if _old.get(k) is not None}
        except Exception:
            _prev, _old = {}, {}

    # REC-5: on a partial re-record (a single-phase re-run after setup cleared
    # the worktree's verdict files), every other phase reads 'no_verdict' —
    # and writing that over the previous record DESTROYS measured history.
    # Observed live: run-3's fdr4-only re-record left 11 phases unmeasured.
    # Phases with no fresh verdict inherit the previous record's verdict,
    # marked in phase_provenance. A fresh verdict always wins.

    summary = {
        "study": a.study, "run": a.run, "iteration": a.iter_n,
        "recorded_utc": datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S"),
        "infrastructure_sha": sha,
        "worktree": str(wt),
        "actor": a.actor,
        "actor_note": ("DIFFERENT ACTORS ARE DIFFERENT CONDITIONS. Do not pool runs with "
                       "different `actor` values into one convergence rate; report per actor."),
        "codex_thread": a.thread if a.actor == "codex" else None,
        "actor_session_label": a.thread if a.actor != "codex" else None,
        "files": {"java": n_java, "java_test": n_test, "formal_artefacts": n_art,
                  "codegen_trace": n_trace, "agent_summary": n_sum,
                  "feedback": n_fb, "feedback_json": n_fb_json},
        "java_loc": pin.java_loc(dst / "java"),
        "phase_status": phase_status,
        "phases_without_verdict": missing_verdicts,
        "codegen_cost": {
            # `kind` must never say "measured" for a figure the operator typed in:
            # measured = read from the actor's own machine-readable log;
            # operator_reported = the actor's self-reported figure, transcribed;
            # unavailable = no cost figure exists for this record.
            "kind": tokens.get("cost_kind", "measured"),
            "source": ("codex log (~/.codex/logs_2.sqlite) via codex_usage.py"
                       if a.actor == "codex" else
                       "operator transcription of Claude Code's own session figure "
                       "(~/.claude has no documented stable schema; a labelled "
                       "self-report is preferred over an undocumented scrape)"),
            "total_tokens": (int(tokens["total_tokens"])
                             if tokens.get("total_tokens") is not None else None),
            "turns": int(tokens["turns"]) if tokens.get("turns") else None,
            "schema": tokens.get("token_schema", "legacy"),
            "tokens_in": None, "tokens_out": None,
            # THREAD-CUMULATIVE, NOT PER-ITERATION. One Codex session = one
            # RUN, and under the current schema the log carries only a running
            # total for the whole thread. codex_usage.py returns its max, so
            # every iteration of a run reports the same thread-to-date figure.
            # Summing total_tokens across a run's iterations therefore
            # multi-counts; use the LAST iteration's value as the run total,
            # or difference consecutive iterations for a per-iteration cost.
            "scope": ("thread_cumulative"
                      if tokens.get("token_schema") == "cumulative" else
                      "per_turn_sum"),
            "notes": ("cumulative total only — Codex stopped logging the "
                      "input/output/cached split after 2026-09-03. This value "
                      "is the thread-to-date total at record time, NOT this "
                      "iteration's marginal cost; do not sum across iterations."
                      if tokens.get("token_schema") == "cumulative" else
                      "full per-turn split available"),
        },
        # Narrative fields: left null rather than faked. The pipeline verdicts
        # are in feedback/ and formal-artefacts/; a human adds the one-liners.
        "converged": None,
        "failure_summary": None,
        # populated from agent_summary.md when the agent wrote one; the
        # operator still fills it in by hand if the agent only spoke in chat.
        "actor_codegen_summary": (
            (dst / "agent_summary.md").read_text(encoding="utf-8", errors="replace").strip()
            if n_sum else None),
    }
    # REC-5 merge: inherit prior verdicts where this run measured nothing
    _prev_ps = _old.get("phase_status") or {}
    _prev_prov = _old.get("phase_provenance") or {}
    if _prev_ps:
        prov = dict(summary.get("phase_provenance") or {})
        for ph, v in list(summary.get("phase_status", {}).items()):
            if v == "no_verdict" and _prev_ps.get(ph) not in (None, "no_verdict"):
                summary["phase_status"][ph] = _prev_ps[ph]
                prov[ph] = _prev_prov.get(ph) or                     "inherited from previous record (not re-measured this run)"
        if prov:
            summary["phase_provenance"] = prov

    # merge carried operator fields BEFORE the write — the first version of
    # this fix merged after the file was already written, so the carry printed
    # but never persisted. A freshly captured agent summary wins over a carried
    # one; everything else fills only where the fresh record has None.
    for k, v in _prev.items():
        if k == "actor_codegen_summary" and summary.get(k):
            continue
        if summary.get(k) is None or k not in summary:
            summary[k] = v
    if _prev:
        print(f"  carried forward from previous record: {sorted(_prev)}")

    (dst / "summary.json").write_text(json.dumps(summary, indent=2) + "\n")

    # Swap in only now that the record is complete.
    if final_dst.exists():
        _sh.rmtree(final_dst)
    dst.rename(final_dst)

    n_pass = sum(1 for s in phase_status.values() if s == "passed")
    n_fail = sum(1 for s in phase_status.values() if s == "failed")
    print(f"[snapshot_v2] {final_dst}")
    print(f"  java={n_java} java_test={n_test} artefacts={n_art} "
          f"feedback={n_fb}md/{n_fb_json}json loc={summary['java_loc']}")
    print(f"  phases: {n_pass} passed, {n_fail} failed, "
          f"{len(missing_verdicts)} without a verdict of {len(PIPELINE_PHASES)}")
    if missing_verdicts:
        print(f"    no verdict written by: {', '.join(missing_verdicts)}")
        print("    (a phase with no verdict is UNMEASURED, not passed)")
    _tt = summary["codegen_cost"]["total_tokens"]
    print(f"  actor={summary['actor']}")
    print(f"  tokens={format(_tt, ',') if _tt is not None else 'unavailable'} "
          f"turns={summary['codegen_cost']['turns']} "
          f"schema={summary['codegen_cost']['schema']} "
          f"scope={summary['codegen_cost']['scope']}")
    if summary["codegen_cost"]["scope"] == "thread_cumulative":
        print("    ^ thread-to-date total, NOT this iteration's marginal cost —"
              " do not sum across iterations")
    print(f"  sha={sha[:8]}")
    print("  fill in: converged, failure_summary, actor_codegen_summary")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
