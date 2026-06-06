"""Python phase runners. Each function takes a RunnerContext and returns
(status, error_msg). Functions are looked up by manifest's
runner.function field (module:function format)."""
from __future__ import annotations

import json
import logging
import os
import re
import shutil
import subprocess
import sys
import threading
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Callable, Any

from web import feedback, vacuity
from web.manifest import Phase
from web.state import SessionState

logger = logging.getLogger(__name__)

# Paths (mirrors bridge.py module-level constants)
_DASHBOARD_DIR = Path(__file__).resolve().parent.parent
_REPO_ROOT = _DASHBOARD_DIR.parent
_T2M_DIR = _REPO_ROOT / "forge.transformations"
_GENERATED_PROJECT = _REPO_ROOT / "java.generated.project"
_GENERATED_SRC = _GENERATED_PROJECT / "src" / "main" / "java"
_T2M_OUTPUT = _T2M_DIR / "output"
_CASE_STUDIES_DIR = _REPO_ROOT / "forge.assets" / "case-studies"
_PIPELINE_YAML = _REPO_ROOT / "pipeline.yaml"


def _active_requirements_file() -> Path:
    """Resolve the requirements file for the currently-active case study.

    Reads ``agent.active_case_study`` from pipeline.yaml on every call so
    a switch via the dashboard's POST /api/active-case-study takes effect
    on the next phase run without a server restart. Falls back to the
    alphabetically-first sub-directory of forge.assets/case-studies
    if the manifest can't be read — no specific case study is hardcoded.
    """
    study: str | None = None
    try:
        from web.manifest import Manifest
        manifest = Manifest.load(_PIPELINE_YAML)
        raw_agent = (manifest._raw.get("agent") or {})
        candidate = raw_agent.get("active_case_study")
        if candidate:
            study = candidate
    except Exception:
        logger.exception("Failed to resolve active case study from manifest")
    if study is None:
        try:
            candidates = sorted(
                p.name for p in _CASE_STUDIES_DIR.iterdir() if p.is_dir()
            )
            if candidates:
                study = candidates[0]
                logger.warning(
                    "Falling back to first case study on disk: %s", study
                )
        except OSError:
            pass
    if study is None:
        raise RuntimeError(
            "No active case study set in pipeline.yaml and no case study "
            f"directories found under {_CASE_STUDIES_DIR}"
        )
    return _CASE_STUDIES_DIR / study / "requirements" / "requirement_all.json"


_CODEGEN_TRACE_FILE = _GENERATED_PROJECT / "result_codegen.json"
_FEEDBACK_DIR = _REPO_ROOT / "forge.assets" / "corrections"
_GRADLEW = "gradlew.bat" if sys.platform == "win32" else "./gradlew"


@dataclass
class RunnerContext:
    phase: Phase | None
    config: dict[str, Any]          # phase.config from manifest (was phase_cfg)
    state: SessionState
    send: Callable[[dict], None]     # was bridge.PipelineBridge._send
    sys_msg: Callable[[str], None]   # was bridge.PipelineBridge._sys_msg
    stop_requested: Callable[[], bool]   # was bridge._stop_event.is_set
    current_phase: str | None = None     # was bridge._current_phase
    emit_feedback: Callable[[feedback.Feedback], None] | None = None  # was bridge._emit_feedback


RunnerResult = tuple[str, str]   # (status, error_msg)


# ---------------------------------------------------------------------------
# Helpers (formerly staticmethods on PipelineBridge)
# ---------------------------------------------------------------------------

def _resolve_os_path(value: Any) -> str:
    """Resolve an OS-aware path config value."""
    if isinstance(value, dict):
        return value.get(sys.platform, "")
    if isinstance(value, str):
        return value
    return ""


def _resolve_tool(value: Any) -> str:
    """Resolve a tool path: explicit if it has a path separator, else
    PATH lookup via shutil.which. Returns "" if not found, so the
    caller's "executable not found" branch fires with the original
    unresolved value.

    Lets pipeline.yaml stay portable — entries like `dafny.exe`,
    `refines.exe` resolve to whatever the user has on PATH, while
    legacy absolute paths still work.
    """
    raw = _resolve_os_path(value)
    if not raw:
        return ""
    resolved = shutil.which(raw)
    return resolved or ""


def _parse_framed_json(raw_output: str) -> list:
    """Parse FDR4 framed_json output (one JSON object per line)."""
    results = []
    for line in raw_output.strip().splitlines():
        line = line.strip()
        if not line:
            continue
        try:
            results.append(json.loads(line))
        except json.JSONDecodeError:
            pass
    return results


def _parse_stderr_stats(stderr_text: str) -> dict:
    """Parse FDR4 stderr progress output for per-assertion visit stats."""
    stats: dict[str, dict] = {}
    current_assertion = None
    for line in (stderr_text or "").splitlines():
        line = line.strip()
        m = re.match(r"Checking\s+(.+)", line)
        if m:
            current_assertion = m.group(1).strip().rstrip(".")
            continue
        m = re.search(r"(\d+)\s+states?\b.*?(\d+)\s+transitions?", line)
        if m and current_assertion:
            stats[current_assertion] = {
                "states": int(m.group(1)),
                "transitions": int(m.group(2)),
            }
        m2 = re.search(r"ply\s+(\d+)", line, re.IGNORECASE)
        if m2 and current_assertion and current_assertion in stats:
            stats[current_assertion]["plys"] = int(m2.group(1))
    return stats


def _get_assertion_stats(assertion: dict, stderr_stats: dict) -> dict:
    """Extract visit stats for an assertion from JSON fields or stderr."""
    for container in (
        assertion.get("status", {}),
        assertion.get("statistics", {}),
        assertion,
    ):
        if not isinstance(container, dict):
            continue
        states = (
            container.get("visited_states")
            or container.get("states_visited")
            or container.get("states")
            or container.get("explored_states")
        )
        transitions = (
            container.get("visited_transitions")
            or container.get("transitions_visited")
            or container.get("transitions")
            or container.get("explored_transitions")
        )
        if states is not None and transitions is not None:
            plys = (
                container.get("visited_plys")
                or container.get("plys")
                or container.get("ply")
            )
            return {"states": states, "transitions": transitions, "plys": plys}

    assert_str = assertion.get("assertion_string", "")
    for key, val in stderr_stats.items():
        if key in assert_str or assert_str in key:
            return val

    return {"states": None, "transitions": None, "plys": None}


def _get_process_memory_mb(pid: int) -> float:
    """Get total RSS memory usage of a process tree in MB."""
    try:
        if sys.platform == "win32":
            result = subprocess.run(
                ["tasklist", "/FI", f"PID eq {pid}", "/FO", "CSV", "/NH"],
                capture_output=True, text=True, timeout=5,
            )
            if result.returncode == 0 and result.stdout.strip():
                for line in result.stdout.strip().splitlines():
                    parts = line.strip().strip('"').split('","')
                    if len(parts) >= 5:
                        mem_str = (parts[4].replace('"', '').replace(',', '')
                                   .replace(' K', '').replace(' ', ''))
                        return int(mem_str) / 1024
        elif sys.platform == "darwin":
            result = subprocess.run(
                ["ps", "-o", "rss=", "-p", str(pid)],
                capture_output=True, text=True, timeout=5,
            )
            if result.returncode == 0 and result.stdout.strip():
                return int(result.stdout.strip()) / 1024
        elif sys.platform == "linux":
            status_file = Path(f"/proc/{pid}/status")
            if status_file.exists():
                for line in status_file.read_text().splitlines():
                    if line.startswith("VmRSS:"):
                        return int(line.split()[1]) / 1024
    except Exception:
        pass
    return 0


def _windows_to_wsl_path(p: str) -> str:
    """Translate a Windows-style path to a WSL /mnt/<drive>/... path."""
    if not p or p.startswith("/"):
        return p
    if len(p) >= 2 and p[1] == ":":
        drive = p[0].lower()
        rest = p[2:].replace("\\", "/")
        if rest.startswith("/"):
            rest = rest[1:]
        return f"/mnt/{drive}/{rest}"
    return p.replace("\\", "/")


def _active_case_study_dir() -> Path | None:
    """Resolve the active case study directory, or None if unresolvable.

    Mirrors :func:`_active_requirements_file` but returns the directory
    containing both ``requirements/`` and ``system/`` for the active
    study.
    """
    study: str | None = None
    try:
        from web.manifest import Manifest
        manifest = Manifest.load(_PIPELINE_YAML)
        raw_agent = (manifest._raw.get("agent") or {})
        candidate = raw_agent.get("active_case_study")
        if candidate:
            study = candidate
    except Exception:
        pass
    if study is None:
        try:
            candidates = sorted(
                p.name for p in _CASE_STUDIES_DIR.iterdir() if p.is_dir()
            )
            if candidates:
                study = candidates[0]
        except OSError:
            return None
    if study is None:
        return None
    return _CASE_STUDIES_DIR / study


def _amplify_with_requirements(fb: feedback.Feedback) -> None:
    """Fix 1: populate TraceRef.requirement_ids and amplify fix_directives.

    For each Issue, look up the requirement IDs that the codegen agent
    traced to each TraceRef's (file, element), then append a one-line
    description of each requirement to the issue's ``fix_directive``.
    Best-effort: silently no-ops if the codegen trace or requirement
    catalogue are unavailable.
    """
    codegen_trace = feedback.load_codegen_trace(_GENERATED_PROJECT)
    if not codegen_trace:
        return
    case_dir = _active_case_study_dir()
    requirements = (
        feedback.load_requirements(case_dir) if case_dir else None
    )
    for issue in fb.issues:
        feedback.populate_trace_refs_with_requirements(
            issue.java_trace, codegen_trace
        )
        # Aggregate requirement_ids across the issue's TraceRefs.
        aggregated: list[str] = []
        seen: set[str] = set()
        for ref in issue.java_trace:
            for rid in ref.requirement_ids:
                if rid not in seen:
                    seen.add(rid)
                    aggregated.append(rid)
        if aggregated:
            issue.fix_directive = feedback.amplify_directive_with_requirements(
                issue.fix_directive, aggregated, requirements
            )


def _llm_translation_enabled() -> bool:
    """Fix 3: opt-in flag at ``agent.llm_residual_translation`` in pipeline.yaml.

    Off by default. When on, Isabelle issues whose raw output contains
    a residual goal (currently ``isabelle_other``, ``isabelle_type_mismatch``,
    and ``isabelle_proof_failed`` kinds) are passed through a one-shot
    Claude Code subprocess to produce a Java-level fix suggestion.
    """
    try:
        from web.manifest import Manifest
        manifest = Manifest.load(_PIPELINE_YAML)
        raw_agent = (manifest._raw.get("agent") or {})
        return bool(raw_agent.get("llm_residual_translation", False))
    except Exception:
        return False


def _claude_binary_path() -> str | None:
    """Resolve the claude CLI path from pipeline.yaml's ``agent.claude_path``."""
    try:
        from web.manifest import Manifest
        manifest = Manifest.load(_PIPELINE_YAML)
        raw_agent = (manifest._raw.get("agent") or {})
        per_os = (raw_agent.get("claude_path") or {})
        return per_os.get(sys.platform) or per_os.get("default")
    except Exception:
        return None


# Content-addressed cache for LLM translations. Keyed by SHA-256 of
# (residual goal + sorted file paths + sorted file content hashes), so
# repeated identical failures yield byte-identical feedback within the
# same checkout state. Cache file lives next to the feedback directory
# so it ships with the corrections workflow.
_LLM_CACHE_DIR = _REPO_ROOT / "forge.assets" / "corrections" / ".llm_cache"


def _hash_file(path: Path) -> str:
    """SHA-256 of a file's bytes, or empty string if unreadable."""
    try:
        import hashlib
        return hashlib.sha256(path.read_bytes()).hexdigest()
    except OSError:
        return ""


def _llm_cache_key(residual_goal: str, java_files: list[str]) -> str:
    """Stable cache key: hash of the goal plus the sorted file paths and
    their content hashes. A change in any input invalidates the cache
    entry naturally (no manual eviction).
    """
    import hashlib
    h = hashlib.sha256()
    h.update(b"residual:")
    h.update(residual_goal.strip().encode("utf-8"))
    h.update(b"\nfiles:")
    for f in sorted(set(java_files)):
        h.update(f"\n  {f}\n  ".encode("utf-8"))
        p = Path(f)
        if not p.is_absolute():
            p = _REPO_ROOT / f
        h.update(_hash_file(p).encode("ascii"))
    return h.hexdigest()


def _llm_cache_lookup(key: str) -> str | None:
    """Read a cached LLM response by hash key, if present."""
    cache_file = _LLM_CACHE_DIR / f"{key}.txt"
    if not cache_file.exists():
        return None
    try:
        return cache_file.read_text(encoding="utf-8")
    except OSError:
        return None


def _llm_cache_store(key: str, response: str) -> None:
    """Write an LLM response to the cache. Best-effort: failures don't
    break the feedback flow.
    """
    try:
        _LLM_CACHE_DIR.mkdir(parents=True, exist_ok=True)
        (_LLM_CACHE_DIR / f"{key}.txt").write_text(response, encoding="utf-8")
    except OSError as exc:
        logger.warning("Could not cache LLM response: %s", exc)


def _llm_translate_residual(
    residual_goal: str,
    java_files: list[str],
    claude_path: str,
) -> str | None:
    """Invoke ``claude -p`` for a single-shot Isabelle-residual translation,
    with content-addressed caching.

    Caching key is a SHA-256 over the goal + Java file paths + Java file
    content hashes. Repeated failures with identical inputs return the
    cached response so feedback stays deterministic across runs within
    the same checkout state. Returns ``None`` on subprocess error,
    timeout, or empty reply.
    """
    if not residual_goal.strip():
        return None
    cache_key = _llm_cache_key(residual_goal, java_files)
    cached = _llm_cache_lookup(cache_key)
    if cached is not None:
        return cached

    prompt_lines = [
        "You are an expert in Isabelle/UTP Z-machine proofs and Java reactive controllers.",
        "An Isabelle proof method has failed with the residual goal below.",
        "The Z-machine theory is auto-generated from a RoboChart model that was itself",
        "extracted from Java source code. Suggest a JAVA-level fix the developer should",
        "make in the controller's step() method (not a change to the Isabelle theory or",
        "the EGL template; those are downstream).",
        "",
        "Residual goal:",
        "```",
        residual_goal.strip()[:3000],
        "```",
    ]
    if java_files:
        prompt_lines.append("")
        prompt_lines.append("Relevant Java files:")
        for f in java_files[:5]:
            prompt_lines.append(f"  - {f}")
    prompt_lines.extend([
        "",
        "Respond with at most three sentences. Identify the specific mode/transition/guard",
        "that needs to change, and what to change it to. Do not include the goal verbatim.",
    ])
    prompt = "\n".join(prompt_lines)
    try:
        result = subprocess.run(
            [claude_path, "-p", prompt],
            capture_output=True, text=True, timeout=120,
        )
    except (subprocess.TimeoutExpired, FileNotFoundError, OSError) as exc:
        logger.warning("LLM residual translation subprocess failed: %s", exc)
        return None
    if result.returncode != 0:
        logger.warning(
            "LLM residual translation exit %d: %s",
            result.returncode, (result.stderr or "")[:200],
        )
        return None
    text = (result.stdout or "").strip()
    if not text:
        return None
    _llm_cache_store(cache_key, text)
    return text


def _apply_llm_residual_translation(fb: feedback.Feedback) -> None:
    """Fix 3: for each Isabelle issue carrying a residual goal, append an
    LLM-generated Java-level fix suggestion alongside the raw goal it
    is interpreting. Gated on ``agent.llm_residual_translation``.

    Design choices honouring the trade-offs discussed in the paper's
    research agenda (Section 6.7):

    1. Hand-coded heuristics in feedback/isabelle.py classify first;
       this routine only fires for the residual ``isabelle_other``,
       ``isabelle_type_mismatch``, and ``isabelle_proof_failed`` kinds.
    2. Responses are cached by (goal, source file content) hash so
       feedback remains deterministic across runs for unchanged inputs.
    3. The raw residual goal is preserved verbatim in the fix_directive
       alongside the LLM paraphrase; the codegen LLM sees both. The
       paraphrase is a hint, not a replacement.
    4. The paraphrase block is clearly labelled as suggested-not-verified.
    """
    if fb.phase != "isabelle_verify":
        return
    if not _llm_translation_enabled():
        return
    claude_path = _claude_binary_path()
    if not claude_path:
        logger.info("LLM residual translation skipped: no claude binary configured")
        return
    eligible_kinds = {"isabelle_other", "isabelle_type_mismatch", "isabelle_proof_failed"}
    for issue in fb.issues:
        if issue.kind not in eligible_kinds:
            continue
        if not issue.raw.strip():
            continue
        java_files = [ref.file for ref in issue.java_trace if ref.file]
        suggestion = _llm_translate_residual(issue.raw, java_files, claude_path)
        if not suggestion:
            continue
        # Always retain the raw verifier output as ground truth, with
        # the LLM paraphrase appended as a labelled hint. The codegen
        # LLM sees both; the raw goal is the source of truth if the
        # paraphrase hallucinates.
        issue.fix_directive = (
            issue.fix_directive.rstrip()
            + "\n\nVerifier residual goal (ground truth):\n"
            + "```\n" + issue.raw.strip() + "\n```\n"
            + "\nLLM-suggested interpretation (hint, not verified --- "
            + "review before applying):\n"
            + suggestion
        )


def _do_emit_feedback(ctx: RunnerContext, fb: feedback.Feedback) -> None:
    """Emit feedback via ctx.emit_feedback if set, else write directly."""
    # Fix 1: requirement-trace amplification. Joins the codegen agent's
    # requirement-to-Java trace with the verifier's Java-locating
    # failure pointers, then surfaces the requirement IDs and a
    # one-line restatement in each issue's fix_directive.
    try:
        _amplify_with_requirements(fb)
    except Exception:
        logger.exception("Requirement amplification failed for phase %s", fb.phase)

    # Fix 3: optional LLM-mediated translation of Isabelle residual goals
    # into Java-level fix suggestions. Off by default; enable via
    # agent.llm_residual_translation in pipeline.yaml. Clearly labels its
    # output as "LLM-suggested (not verified)".
    try:
        _apply_llm_residual_translation(fb)
    except Exception:
        logger.exception("LLM residual translation failed for phase %s", fb.phase)

    if ctx.emit_feedback is not None:
        ctx.emit_feedback(fb)
    else:
        try:
            md_path, _ = feedback.write_feedback(_FEEDBACK_DIR, fb)
            rel = md_path.relative_to(_REPO_ROOT) if md_path.is_absolute() else md_path
            ctx.sys_msg(f"Feedback written: {rel.as_posix()}")
        except Exception:
            logger.exception("Failed to write feedback for phase %s", fb.phase)


def _stream_line(ctx: RunnerContext, text: str, phase: str = "") -> None:
    """Stream a single output line to the chat."""
    ctx.send({
        "type": "agent_stream",
        "phase": phase or ctx.current_phase or "",
        "agent": "pipeline",
        "content": text,
    })


_LEMMA_DECL_RE_RUNNER = re.compile(r"^\s*(?:lemma|theorem)\b\s+(\w+)")


def _scan_isabelle_lemmas(session_dir: Path) -> dict:
    """Scan .thy files in ``session_dir`` for lemma declarations.

    Returns::

        {
          "theory_count": int,
          "total_lemmas": int,
          "deadlock_free_names": [str, ...],
          "by_theory": [{
              "theory": "<stem>",
              "lemmas": [str, ...],
              "deadlock_free": [str, ...],
              "invariant_count": int,
          }, ...],
        }

    Used on the success path so the chat message and feedback body can
    name exactly what was proven.
    """
    out = {
        "theory_count": 0,
        "total_lemmas": 0,
        "deadlock_free_names": [],
        "by_theory": [],
    }
    try:
        thy_files = sorted(session_dir.glob("*.thy"))
    except OSError:
        return out
    for thy in thy_files:
        try:
            text = thy.read_text(encoding="utf-8")
        except OSError:
            continue
        names = [
            m.group(1)
            for line in text.splitlines()
            for m in [_LEMMA_DECL_RE_RUNNER.match(line)]
            if m
        ]
        if not names:
            continue
        deadlock_free = [n for n in names if n.endswith("_deadlock_free")]
        invariants = [n for n in names if n.endswith("_inv")]
        out["theory_count"] += 1
        out["total_lemmas"] += len(names)
        out["deadlock_free_names"].extend(deadlock_free)
        out["by_theory"].append({
            "theory": thy.stem,
            "lemmas": names,
            "deadlock_free": deadlock_free,
            "invariant_count": len(invariants),
        })
    return out


def _classify_fdr4_stderr(stderr_text: str, memory_limit_mb: int) -> dict:
    """Classify FDR4 stderr to distinguish memory/runtime errors from parse errors.

    Returns a dict with chat (`headline`), feedback-summary (`summary`),
    and feedback-issue (`kind`, `title`, `fix_directive`, `error`) fields.
    The feedback-issue fields are consumed by
    feedback.fdr4.build_fdr4_feedback's errors loop — when `kind` is
    present it bypasses the default parse_error builder.
    """
    low = stderr_text.lower()

    # Windows page-file exhaustion: GHC RTS tries to commit address
    # space up-front and fails when the page file can't grow.
    if ("virtualalloc" in low and "mem_commit" in low) or "paging file is too small" in low:
        return {
            "headline": "FDR4 ran out of address space (Windows page file too small).",
            "summary": (
                "FDR4 could not commit memory: Windows page file is smaller "
                "than what the GHC runtime in refines.exe wants up-front."
            ),
            "kind": "memory_error",
            "title": "Windows page file too small for refines.exe",
            "fix_directive": (
                "Fix any one:\n"
                "  • Grow the system page file: System Properties → Advanced → "
                "Performance Settings → Advanced → Virtual memory. Set the page "
                "file to at least 8 GB on the drive that hosts refines.exe.\n"
                f"  • Lower phases.fdr4.memory_limit_mb in pipeline.yaml "
                f"(currently {memory_limit_mb}) — the python-side monitor will "
                "stop the run earlier, but doesn't shrink the up-front commit.\n"
                "  • Run on a machine with more physical RAM."
            ),
            "error": stderr_text[:2000],
        }

    if "most rts options are disabled" in low:
        return {
            "headline": "FDR4 rejected +RTS arguments (build was compiled with -rtsopts=some).",
            "summary": "FDR4 rejected the +RTS heap-cap flag.",
            "kind": "runtime_config_error",
            "title": "refines.exe rejected +RTS arguments",
            "fix_directive": (
                "refines.exe was built with restricted RTS options. Remove "
                "any +RTS arguments from the run_fdr4 command in "
                "forge.dashboard/web/runners.py."
            ),
            "error": stderr_text[:2000],
        }

    if "out of memory" in low or "getmblocks" in low or "heap exhausted" in low:
        return {
            "headline": "FDR4 ran out of memory.",
            "summary": "FDR4 exhausted the GHC heap before producing results.",
            "kind": "memory_error",
            "title": "FDR4 out of memory",
            "fix_directive": (
                "Reduce the state space (shrink phases.fdr4 type_ranges in "
                "pipeline.yaml or the corrections/type_ranges.json overrides) "
                "or grow the Windows page file / RAM and retry."
            ),
            "error": stderr_text[:2000],
        }

    # Fall through: no special classification, let the default
    # parse_error builder format the error.
    return {
        "headline": "CSP-M parse error:",
        "summary": "FDR4 could not parse the generated CSP-M.",
        "error": stderr_text[:2000],
    }


# ---------------------------------------------------------------------------
# run_fdr4
# ---------------------------------------------------------------------------

def _discover_fdr4_csp_file(ctx: RunnerContext) -> "Path | None":
    """Auto-discover the FDR4 input CSP file under <output>/csp-gen/defs/.

    Preference order:
      1. *_System_Module_coreassertions.csp  — multi-controller aggregate
      2. *<Stm>Controller_coreassertions.csp — single-controller controller-only
                                              (excludes *_Module_*, *_Ctrl_*,
                                              *_InputEnv_*, *_OutputEnv_*)
      3. *_Module_coreassertions.csp         — single-controller module level
                                              (wraps controller + InputEnv +
                                              OutputEnv; larger state space)
      4. exactly one *_coreassertions.csp    — fallback when only one exists

    Tier 2 was added after the SRanger convergence-replay experiment
    (experiments/convergence/sranger/) showed that Module-level assertions
    over the composed (controller || InputEnv || OutputEnv) system can
    exceed FDR4's state-space budget at [0..1] type ranges on Windows,
    returning "inconclusive" for every assertion. The controller-only
    coreassertions cover the same deadlock-freedom / divergence-freedom /
    determinism properties on the controller itself with a much smaller
    exploration cost — when that variant is present (which it always is
    for single-controller studies), it should be preferred.

    Returns the chosen file path, or None if none qualifies. The function
    is deliberately quiet on ambiguity (multiple candidates at the same
    preference tier) — the caller can still pin csp_file explicitly in
    pipeline.yaml if the default heuristic picks the wrong one.
    """
    defs_dir = _T2M_OUTPUT / "csp-gen" / "defs"
    if not defs_dir.exists():
        return None

    candidates = sorted(defs_dir.glob("*_coreassertions.csp"))
    if not candidates:
        return None

    # Tier 1: multi-controller aggregate
    for p in candidates:
        if "_System_Module_coreassertions.csp" in p.name:
            return p
    # Tier 2: single-controller controller-only (no Module / Ctrl / env infix)
    for p in candidates:
        name = p.name
        if (
            "Controller_coreassertions.csp" in name
            and "_Module_" not in name
            and "_Ctrl_" not in name
            and "_InputEnv_" not in name
            and "_OutputEnv_" not in name
        ):
            return p
    # Tier 3: single-controller module level
    for p in candidates:
        if "_Module_coreassertions.csp" in p.name:
            return p
    # Tier 4: unique fallback
    if len(candidates) == 1:
        return candidates[0]
    # Multiple controller-level files and no module-level aggregate —
    # surface the ambiguity rather than picking arbitrarily.
    names = ", ".join(p.name for p in candidates)
    ctx.sys_msg(
        f"FDR4: multiple *_coreassertions.csp candidates and no "
        f"*_Module_coreassertions.csp aggregate — auto-discovery cannot "
        f"choose. Pin one in pipeline.yaml phases.fdr4.csp_file. "
        f"Candidates: {names}"
    )
    return None


def run_fdr4(ctx: RunnerContext) -> RunnerResult:
    """Run FDR4 verification as a subprocess."""
    phase_cfg = ctx.config
    label = ctx.phase.label if ctx.phase else "FDR4 Verification"

    raw_fdr4 = _resolve_os_path(phase_cfg.get("fdr4_path", ""))
    fdr4_path = _resolve_tool(phase_cfg.get("fdr4_path", ""))
    csp_file = phase_cfg.get("csp_file", "")
    timeout = phase_cfg.get("timeout", 600)
    expected = phase_cfg.get("expected_failures", [])
    memory_limit_mb = phase_cfg.get("memory_limit_mb", 8192)

    if not fdr4_path:
        msg = (f"FDR4 executable not found: {raw_fdr4 or '(empty)'}. "
               "Add its install dir to PATH or set "
               "phases.fdr4.fdr4_path to an absolute path in pipeline.yaml.")
        ctx.sys_msg(msg)
        return "failed", msg

    # Resolve CSP file. Two paths:
    # (a) pipeline.yaml's csp_file points at an existing file → use it
    #     (preserves explicit pin; useful when the user wants to verify a
    #     specific controller rather than the top-level module).
    # (b) otherwise auto-discover under <output_dir>/csp-gen/defs/ in
    #     preference order:
    #       1. <Diagram>_System_Module_coreassertions.csp  (multi-controller)
    #       2. <Stm>_Module_coreassertions.csp             (single-controller)
    #       3. unique *_coreassertions.csp file            (fallback)
    # The auto-discovery keeps pipeline.yaml case-study-independent — the
    # original hardcoded "LreController_coreassertions.csp" path broke as
    # soon as the active_case_study switched.
    csp_path = _DASHBOARD_DIR / csp_file if csp_file else None
    if csp_path is None or not csp_path.exists():
        csp_path = _discover_fdr4_csp_file(ctx)
        if csp_path is None:
            msg = (
                "FDR4 CSP file not found. Auto-discovery looked under "
                f"{_T2M_OUTPUT / 'csp-gen' / 'defs'} for "
                "*_System_Module_coreassertions.csp, *_Module_coreassertions.csp, "
                "or *_coreassertions.csp; nothing matched. Either the m2t phase "
                "did not produce csp-gen output, or pin a path explicitly in "
                "phases.fdr4.csp_file."
            )
            ctx.sys_msg(msg)
            return "failed", msg
        ctx.sys_msg(f"FDR4: auto-discovered CSP file {csp_path.name}")

    # Determinism is not a property the pipeline verifies. RoboChart gives a
    # state's outgoing transitions as concurrent choices (not a prioritised
    # list), and an autonomous guard-only transition compiles to a hidden
    # internal event, so the extracted models are non-deterministic by
    # construction even though the generated Java is deterministic. We
    # therefore strip the official generator's `:[deterministic]` assertions
    # before invoking FDR, so the property is neither checked nor reported.
    # (We cannot suppress emission: the assertions come from the vendored
    # RoboChart CSP generator, not our templates.)
    try:
        original_text = csp_path.read_text(encoding="utf-8")
        det_lines = [ln for ln in original_text.splitlines()
                     if ln.lstrip().startswith("assert") and ":[deterministic]" in ln]
        if det_lines:
            filtered = "\n".join(
                ("-- determinism not verified (see runners.run_fdr4): " + ln)
                if (ln.lstrip().startswith("assert") and ":[deterministic]" in ln)
                else ln
                for ln in original_text.splitlines()
            )
            nodet_path = csp_path.with_name(
                csp_path.stem + "_nodet" + csp_path.suffix)
            nodet_path.write_text(filtered + "\n", encoding="utf-8")
            ctx.sys_msg(
                f"FDR4: determinism not verified; commented out "
                f"{len(det_lines)} :[deterministic] assertion(s).")
            csp_path = nodet_path
    except OSError as exc:
        logger.warning("Could not filter determinism assertions: %s", exc)

    # refines.exe is built with restricted RTS options ("-rtsopts=some"
    # or similar), so +RTS -M<N>m -RTS to cap GHC's heap is rejected
    # ("Most RTS options are disabled"). The only knobs we have are
    # the python-side memory monitor below and the user's Windows
    # page-file size. We detect the page-file failure from stderr and
    # surface an actionable message.
    cmd = [fdr4_path, "--format", "framed_json", str(csp_path)]
    logger.info("Running FDR4: %s", " ".join(cmd))
    env = os.environ.copy()
    env["LC_ALL"] = "C"

    try:
        proc = subprocess.Popen(
            cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            text=True, encoding="utf-8", errors="replace", env=env,
        )

        # Memory monitoring thread
        peak_memory_mb = [0]
        killed_for_memory = threading.Event()

        def monitor_memory():
            while proc.poll() is None and not ctx.stop_requested():
                mem = _get_process_memory_mb(proc.pid)
                if mem > peak_memory_mb[0]:
                    peak_memory_mb[0] = mem
                ctx.send({
                    "type": "fdr4_memory",
                    "memory_mb": round(mem),
                    "peak_mb": round(peak_memory_mb[0]),
                    "limit_mb": memory_limit_mb,
                })
                if mem > memory_limit_mb:
                    logger.error("FDR4 exceeded memory limit: %.0f MB > %d MB", mem, memory_limit_mb)
                    killed_for_memory.set()
                    proc.kill()
                    return
                time.sleep(1)

        mem_thread = threading.Thread(target=monitor_memory, daemon=True)
        mem_thread.start()

        stderr_chunks: list[str] = []

        def drain_stderr():
            if proc.stderr is None:
                return
            try:
                for chunk in iter(lambda: proc.stderr.read(4096), ""):
                    stderr_chunks.append(chunk)
            except (OSError, ValueError):
                pass

        stderr_thread = threading.Thread(target=drain_stderr, daemon=True)
        stderr_thread.start()

        output_lines = []
        for line in proc.stdout:
            if ctx.stop_requested():
                proc.terminate()
                stderr_thread.join(timeout=2)
                return "stopped", ""
            stripped = line.rstrip("\r\n")
            output_lines.append(stripped)

        proc.wait(timeout=timeout)
        stderr_thread.join(timeout=5)
        stderr_text = "".join(stderr_chunks)

        mem_thread.join(timeout=2)
        ctx.send({
            "type": "fdr4_memory",
            "memory_mb": 0,
            "peak_mb": round(peak_memory_mb[0]),
            "limit_mb": memory_limit_mb,
        })

        if killed_for_memory.is_set():
            msg = (f"FDR4 killed: memory usage exceeded {memory_limit_mb} MB limit "
                   f"(peak: {peak_memory_mb[0]:.0f} MB)")
            ctx.sys_msg(msg)
            trace_data = feedback.load_trace(_T2M_OUTPUT)
            fb = feedback.build_fdr4_feedback(
                label=label, status="failed", summary=msg,
                failed_assertions=[],
                errors=[{"error": msg}],
                csp_file=csp_path,
                trace_data=trace_data,
                expected_failures=[],
            )
            _do_emit_feedback(ctx, fb)
            return "failed", msg

        # Parse framed JSON results
        full_output = "\n".join(output_lines)
        raw_results = _parse_framed_json(full_output)
        stderr_stats = _parse_stderr_stats(stderr_text)

        if not raw_results and stderr_text:
            classified = _classify_fdr4_stderr(stderr_text, memory_limit_mb)
            ctx.sys_msg(f"{classified['headline']}\n{stderr_text[:2000]}")
            trace_data = feedback.load_trace(_T2M_OUTPUT)
            err_entry: dict[str, str] = {
                "error": classified["error"],
                "raw": stderr_text[:2000],
            }
            for key in ("kind", "title", "fix_directive"):
                if key in classified:
                    err_entry[key] = classified[key]
            fb = feedback.build_fdr4_feedback(
                label=label, status="failed",
                summary=classified["summary"],
                failed_assertions=[],
                errors=[err_entry],
                csp_file=csp_path,
                trace_data=trace_data,
                expected_failures=[],
            )
            _do_emit_feedback(ctx, fb)
            return "failed", classified["summary"]

        assertions = []
        errors = []
        for frame in raw_results:
            if "errors" in frame and isinstance(frame["errors"], list):
                for err_msg in frame["errors"]:
                    if err_msg:
                        errors.append({"error": err_msg})
            for ar in frame.get("results", []):
                assertions.append(ar)
            if "error" in frame:
                errors.append(frame)

        passed = [a for a in assertions if a.get("result") in (1, True)]
        failed = [a for a in assertions if a.get("result") in (0, False)]
        inconclusive = [a for a in assertions if a not in passed and a not in failed]

        expected_failed = []
        unexpected_failed = []
        for a in failed:
            astr = a.get("assertion_string", "")
            if any(pat in astr for pat in expected):
                expected_failed.append(a)
            else:
                unexpected_failed.append(a)

        table_lines = [
            "| Assertion | Result | States | Transitions |",
            "|---|---|---|---|",
        ]
        for a in assertions:
            assert_str = a.get("assertion_string", "?")
            stats = _get_assertion_stats(a, stderr_stats)
            states_str = str(stats["states"]) if stats.get("states") is not None else "-"
            trans_str = str(stats["transitions"]) if stats.get("transitions") is not None else "-"

            if a in passed:
                result_str = "PASS"
            elif a in expected_failed:
                plys = stats.get("plys")
                result_str = f"EXPECTED FAIL (ply {plys})" if plys else "EXPECTED FAIL"
            elif a in failed:
                plys = stats.get("plys")
                result_str = f"FAIL (ply {plys})" if plys else "FAIL"
            else:
                result_str = "INCONCLUSIVE"

            table_lines.append(
                f"| {assert_str} | {result_str} | {states_str} | {trans_str} |"
            )

        summary_parts = [
            f"FDR4: {len(assertions)} assertion(s) checked",
            f"{len(passed)} passed",
        ]
        if unexpected_failed:
            summary_parts.append(f"{len(unexpected_failed)} failed")
        if expected_failed:
            summary_parts.append(f"{len(expected_failed)} expected failure(s)")
        if inconclusive:
            summary_parts.append(f"{len(inconclusive)} inconclusive")
        summary_parts.append(f"{len(errors)} error(s)")
        ctx.sys_msg(", ".join(summary_parts))

        table_msg = {
            "type": "agent_message",
            "phase": ctx.current_phase or "",
            "agent": "fdr4",
            "content": "\n".join(table_lines),
        }
        ctx.send(table_msg)
        ctx.state.add_message(table_msg)

        for a in failed:
            assert_str = a.get("assertion_string", "?")
            counterexamples = a.get("counterexamples", [])
            traces = []
            for ce in counterexamples:
                trace = ce.get("trace", [])
                if trace:
                    traces.append(" -> ".join(str(ev) for ev in trace))
            if traces:
                trace_desc = "; ".join(traces)
                ctx.sys_msg(f"Counterexample for {assert_str}: {trace_desc}")

        for e in errors:
            ctx.sys_msg(f"ERROR: {e.get('error', '?')}")

        has_issues = bool(unexpected_failed) or bool(errors) or bool(inconclusive)

        event_map: dict[str, str] = {}
        for frame in raw_results:
            fmap = frame.get("event_map", {}) or {}
            for eid, ename in fmap.items():
                event_map[str(eid)] = str(ename)

        trace_data = feedback.load_trace(_T2M_OUTPUT)
        fb_status = "failed" if has_issues else "passed"
        summary_parts_fb = [
            f"{len(assertions)} assertion(s) checked",
            f"{len(passed)} passed",
        ]
        if unexpected_failed:
            summary_parts_fb.append(f"{len(unexpected_failed)} failed")
        if expected_failed:
            summary_parts_fb.append(f"{len(expected_failed)} expected failure(s)")
        if inconclusive:
            summary_parts_fb.append(f"{len(inconclusive)} inconclusive")
        if errors:
            summary_parts_fb.append(f"{len(errors)} error(s)")
        fb = feedback.build_fdr4_feedback(
            label=label,
            status=fb_status,
            summary="FDR4: " + ", ".join(summary_parts_fb) + ".",
            failed_assertions=unexpected_failed,
            errors=errors,
            csp_file=csp_path,
            trace_data=trace_data,
            expected_failures=expected_failed,
            event_map=event_map,
        )
        _do_emit_feedback(ctx, fb)

        if has_issues:
            return "failed", full_output[:2000]
        else:
            if expected_failed:
                ctx.sys_msg(
                    f"{label} completed — failures are expected "
                    f"({', '.join(expected)})."
                )
            else:
                ctx.sys_msg(f"{label} completed — all assertions passed.")
            return "completed", ""

    except subprocess.TimeoutExpired:
        proc.kill()
        msg = f"FDR4 timed out after {timeout}s"
        ctx.sys_msg(msg)
        trace_data = feedback.load_trace(_T2M_OUTPUT)
        fb = feedback.build_fdr4_feedback(
            label=label, status="failed", summary=msg,
            failed_assertions=[],
            errors=[{"error": msg}],
            csp_file=csp_path,
            trace_data=trace_data,
            expected_failures=[],
        )
        _do_emit_feedback(ctx, fb)
        return "failed", msg


# ---------------------------------------------------------------------------
# run_dafny_verify
# ---------------------------------------------------------------------------

def run_dafny_verify(ctx: RunnerContext) -> RunnerResult:
    """Run Dafny verification as a subprocess."""
    phase_cfg = ctx.config
    label = ctx.phase.label if ctx.phase else "Dafny Verification"

    raw_dafny = _resolve_os_path(phase_cfg.get("dafny_path", ""))
    dafny_path = _resolve_tool(phase_cfg.get("dafny_path", ""))
    timeout = phase_cfg.get("timeout", 120)

    if not dafny_path:
        msg = (f"Dafny executable not found: {raw_dafny or '(empty)'}. "
               "Add its install dir to PATH or set "
               "phases.dafny_verify.dafny_path to an absolute path in pipeline.yaml.")
        ctx.sys_msg(msg)
        return "failed", msg

    dfy_files = sorted(_T2M_OUTPUT.glob("*.dfy"))
    if not dfy_files:
        msg = "No .dfy files found in output"
        ctx.sys_msg(msg)
        return "failed", msg

    dafny_mappings = feedback.load_dafny_trace(_T2M_OUTPUT)
    trace_data = feedback.load_trace(_T2M_OUTPUT)

    all_passed = True
    error_output = []
    results_table = []
    combined_raw_output_parts: list[str] = []

    for dfy_file in dfy_files:
        ctx.sys_msg(f"Verifying {dfy_file.name}...")
        cmd = [dafny_path, "verify", str(dfy_file)]

        try:
            proc = subprocess.Popen(
                cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                text=True, encoding="utf-8", errors="replace",
            )

            file_output_lines = []
            for line in proc.stdout:
                if ctx.stop_requested():
                    proc.terminate()
                    return "stopped", ""
                stripped = line.rstrip("\r\n")
                file_output_lines.append(stripped)
                _stream_line(ctx, stripped)

            proc.wait(timeout=timeout)
            file_output = "\n".join(file_output_lines)
            combined_raw_output_parts.append(file_output)

            verified = 0
            errors = 0
            match = re.search(r"(\d+)\s+verified,\s+(\d+)\s+error", file_output)
            if match:
                verified = int(match.group(1))
                errors = int(match.group(2))

            status = "PASS" if errors == 0 and proc.returncode == 0 else "FAIL"
            if status == "PASS":
                detail = f"All {verified} conditions verified" if verified else "No conditions found"
            else:
                all_passed = False
                detail = ""
                err_lines = [l for l in file_output.split("\n") if "Error" in l or "error" in l.lower()]
                if err_lines:
                    detail = err_lines[0].strip()[:120]
                error_output.append(f"{dfy_file.name}: {detail or f'exit code {proc.returncode}'}")

            results_table.append({
                "file": dfy_file.name,
                "status": status,
                "verified": verified,
                "errors": errors,
                "detail": detail,
            })

        except subprocess.TimeoutExpired:
            proc.kill()
            all_passed = False
            timeout_msg = f"{dfy_file.name}: timed out after {timeout}s"
            error_output.append(timeout_msg)
            combined_raw_output_parts.append(
                f"{dfy_file.name}(0,0): Error: verifier timed out after {timeout}s"
            )
            results_table.append({
                "file": dfy_file.name,
                "status": "FAIL",
                "verified": 0,
                "errors": 0,
                "detail": f"Timeout after {timeout}s",
            })

    table_lines = [
        "| File | Status | Verified | Errors | Details |",
        "|------|--------|----------|--------|---------|",
    ]
    for r in results_table:
        table_lines.append(
            f"| {r['file']} | {r['status']} | {r['verified']} | "
            f"{r['errors']} | {r.get('detail', '')} |"
        )
    table_msg = {
        "type": "agent_message",
        "phase": ctx.current_phase or "",
        "agent": "dafny_verify",
        "content": "\n".join(table_lines),
    }
    ctx.send(table_msg)
    ctx.state.add_message(table_msg)

    fb_status = "passed" if all_passed else "failed"
    total_verified = sum(r["verified"] for r in results_table)
    total_errors = sum(r["errors"] for r in results_table)
    summary = (
        f"Dafny: {len(dfy_files)} file(s), "
        f"{total_verified} verified, {total_errors} errors."
    )
    fb = feedback.build_dafny_feedback(
        label=label,
        status=fb_status,
        summary=summary,
        raw_output="\n".join(combined_raw_output_parts),
        dafny_mappings=dafny_mappings,
        trace_data=trace_data,
    )
    _do_emit_feedback(ctx, fb)

    if all_passed:
        ctx.sys_msg(f"{label} completed — all files verified.")
        return "completed", ""
    else:
        msg = f"{label} failed:\n" + "\n".join(error_output)
        ctx.sys_msg(msg)
        return "failed", msg


# ---------------------------------------------------------------------------
# run_isabelle_verify
# ---------------------------------------------------------------------------

def _emit_isabelle_feedback(ctx: RunnerContext, fb_status: str, summary: str,
                            raw_output: str,
                            success_stats: dict | None = None) -> None:
    """Build + write the unified feedback record for isabelle_verify."""
    trace_data = feedback.load_trace(_T2M_OUTPUT)
    # Mirror the session_dir resolution from run_isabelle_verify so the
    # feedback builder can enrich Timeout/Interrupt issues with the .thy
    # line number of the lemma Isabelle was working on (and use it to
    # count lemmas on the success path).
    session_dir_cfg = (ctx.config or {}).get("session_dir", "")
    if session_dir_cfg:
        session_dir = (_DASHBOARD_DIR / session_dir_cfg).resolve()
    else:
        session_dir = (_T2M_OUTPUT / "isabelle").resolve()
    fb = feedback.build_isabelle_feedback(
        label="Isabelle Verification",
        status=fb_status,
        summary=summary,
        raw_output=raw_output,
        trace_data=trace_data,
        session_dir=session_dir if session_dir.exists() else None,
        success_stats=success_stats,
    )
    _do_emit_feedback(ctx, fb)


def run_isabelle_verify(ctx: RunnerContext) -> RunnerResult:
    """Run `isabelle build` on the session at output/isabelle/.

    On macOS/Linux: invokes the configured isabelle binary directly.
    On Windows: invokes via wsl.exe and translates the session path
    to /mnt/c/... form. The CyPhyAssure distribution is Linux-only.
    """
    phase_cfg = ctx.config
    label = ctx.phase.label if ctx.phase else "Isabelle Verification"

    raw_isabelle = _resolve_os_path(phase_cfg.get("isabelle_path", ""))
    isabelle_path = _resolve_tool(phase_cfg.get("isabelle_path", ""))
    wsl_isabelle_bin = phase_cfg.get("wsl_isabelle_bin", "")
    wsl_distro = phase_cfg.get("wsl_distro", "")
    timeout = phase_cfg.get("timeout", 1800)
    proof_timeout = phase_cfg.get("proof_timeout", 600)
    memory_limit_mb = phase_cfg.get("memory_limit_mb", 16384)

    session_dir_cfg = phase_cfg.get("session_dir", "")
    if session_dir_cfg:
        session_dir = (_DASHBOARD_DIR / session_dir_cfg).resolve()
    else:
        session_dir = (_T2M_OUTPUT / "isabelle").resolve()

    if not session_dir.exists():
        msg = (f"Isabelle session directory not found: {session_dir}. "
               "Run phase 5c (Isabelle Theory Generation) first.")
        ctx.sys_msg(msg)
        _emit_isabelle_feedback(ctx, "failed", msg, msg)
        return "failed", msg

    # Decide branch from the *raw* (user-written) value so a missing
    # wsl.exe still produces a wsl-flavoured error, not a generic one.
    is_wsl = sys.platform == "win32" and "wsl" in raw_isabelle.lower()

    if is_wsl:
        if not isabelle_path:
            msg = (f"wsl.exe not found on PATH (configured as {raw_isabelle!r}). "
                   "Install WSL or set phases.isabelle_verify.isabelle_path "
                   "to an absolute path.")
            ctx.sys_msg(msg)
            _emit_isabelle_feedback(ctx, "failed", msg, msg)
            return "failed", msg
        if not wsl_isabelle_bin:
            msg = ("isabelle_path is wsl.exe but wsl_isabelle_bin is "
                   "not configured. Set phases.isabelle_verify.wsl_isabelle_bin "
                   "to the path of `isabelle` inside WSL.")
            ctx.sys_msg(msg)
            _emit_isabelle_feedback(ctx, "failed", msg, msg)
            return "failed", msg
        wsl_session = _windows_to_wsl_path(str(session_dir))
        # `:; ` no-op prefix prevents wsl.exe from path-translating the
        # first token: with Git Bash installed, `/bin/echo` would be
        # rewritten to `C:/Program Files/Git/usr/bin/echo` (and the
        # same for /home/... mappings), breaking the bash -lc command.
        # A leading `:;` keeps the first token benign.
        inner = (
            f':; {wsl_isabelle_bin} build -D {wsl_session} '
            f'-v -o timeout={proof_timeout}'
        )
        # When the default WSL distro is something other than the one
        # holding Isabelle (e.g. docker-desktop on machines with Docker
        # Desktop installed), -d <distro> forces the right target.
        cmd = [isabelle_path]
        if wsl_distro:
            cmd += ["-d", wsl_distro]
        cmd += ["--", "bash", "-lc", inner]
    else:
        if not isabelle_path:
            msg = (f"Isabelle executable not found: {raw_isabelle or '(empty)'}. "
                   "Add the install dir to PATH or set "
                   "phases.isabelle_verify.isabelle_path to an absolute path. "
                   "See docs/design/isabelle_wsl_setup.md.")
            ctx.sys_msg(msg)
            _emit_isabelle_feedback(ctx, "failed", msg, msg)
            return "failed", msg
        cmd = [isabelle_path, "build", "-D", str(session_dir),
               "-v", "-o", f"timeout={proof_timeout}"]

    logger.info("Running Isabelle: %s", " ".join(cmd))
    ctx.sys_msg(f"{label}: starting (this can take minutes — "
                f"first run cold loads the Z_Machine heap)...")

    try:
        proc = subprocess.Popen(
            cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
            text=True, encoding="utf-8", errors="replace",
        )
    except FileNotFoundError as exc:
        msg = f"Failed to launch Isabelle: {exc}"
        ctx.sys_msg(msg)
        _emit_isabelle_feedback(ctx, "failed", msg, msg)
        return "failed", msg

    peak_memory_mb = [0]
    killed_for_memory = threading.Event()

    def monitor_memory():
        while proc.poll() is None and not ctx.stop_requested():
            mem = _get_process_memory_mb(proc.pid)
            if mem > peak_memory_mb[0]:
                peak_memory_mb[0] = mem
            if mem > memory_limit_mb:
                logger.error("Isabelle exceeded memory limit: %.0f MB", mem)
                killed_for_memory.set()
                proc.kill()
                return
            time.sleep(2)

    mem_thread = threading.Thread(target=monitor_memory, daemon=True)
    mem_thread.start()

    output_lines: list[str] = []
    try:
        for line in proc.stdout:
            if ctx.stop_requested():
                proc.terminate()
                return "stopped", ""
            stripped = line.rstrip("\r\n")
            output_lines.append(stripped)
            if stripped.strip():
                _stream_line(ctx, stripped)
        proc.wait(timeout=timeout)
    except subprocess.TimeoutExpired:
        proc.kill()
        msg = f"Isabelle timed out after {timeout}s"
        ctx.sys_msg(msg)
        _emit_isabelle_feedback(ctx, "failed", msg, "\n".join(output_lines))
        return "failed", msg
    finally:
        mem_thread.join(timeout=2)

    full_output = "\n".join(output_lines)

    if killed_for_memory.is_set():
        msg = (f"Isabelle killed: memory exceeded {memory_limit_mb} MB "
               f"(peak {peak_memory_mb[0]:.0f} MB)")
        ctx.sys_msg(msg)
        _emit_isabelle_feedback(ctx, "failed", msg, full_output)
        return "failed", msg

    # Isabelle 2023 `build -v` emits per-theory markers as
    # `<session>: theory <session>.<theory>` — one per theory processed.
    # No per-theory "Finished (Xs)" line is printed, so count from the
    # session-theory markers; total elapsed comes from the closing line.
    theory_markers = re.findall(
        r"^\s*(\S+):\s+theory\s+\S+\.(\w+)\s*$",
        full_output,
        re.MULTILINE,
    )
    elapsed = re.search(r"^([\d:]+) elapsed time", full_output, re.MULTILINE)
    failed_session = re.search(r"Session\s+(\S+)\s+FAILED", full_output)
    any_star_error = "***" in full_output

    if proc.returncode == 0 and not failed_session and not any_star_error:
        elapsed_str = elapsed.group(1) if elapsed else ""
        # Scan the .thy files in session_dir for lemma counts + names so
        # both the chat message and the feedback body can be specific
        # about what was actually proven (not just "all proofs closed").
        session_dir = (_T2M_OUTPUT / "isabelle").resolve()
        lemma_stats = _scan_isabelle_lemmas(session_dir)
        # Compact chat message: theory count + total lemmas + named
        # deadlock-freedom result (the user-facing safety property).
        chat_bits = [
            f"{lemma_stats['theory_count']} "
            f"theor{'y' if lemma_stats['theory_count'] == 1 else 'ies'}",
            f"{lemma_stats['total_lemmas']} "
            f"lemma{'' if lemma_stats['total_lemmas'] == 1 else 's'} verified",
        ]
        if lemma_stats["deadlock_free_names"]:
            dfs = ", ".join(f"`{n}`" for n in lemma_stats["deadlock_free_names"])
            chat_bits.append(f"deadlock-freedom proved for {dfs}")
        if elapsed_str:
            chat_bits.append(f"elapsed {elapsed_str}")
        ctx.sys_msg(f"{label} — " + "; ".join(chat_bits) + ".")
        summary = "Isabelle: " + ", ".join([
            f"{lemma_stats['theory_count']} theor"
            f"{'y' if lemma_stats['theory_count'] == 1 else 'ies'}, "
            f"{lemma_stats['total_lemmas']} lemma(s) verified",
            f"elapsed {elapsed_str}" if elapsed_str else "",
            f"peak {peak_memory_mb[0]:.0f} MB",
        ]).replace(", ,", ",") + "."
        _emit_isabelle_feedback(
            ctx, "passed", summary, full_output,
            success_stats={
                "theory_markers": theory_markers,
                "elapsed": elapsed_str,
                "peak_memory_mb": int(peak_memory_mb[0]),
                "lemma_stats": lemma_stats,
            },
        )
        return "completed", ""

    summary_parts = [f"Isabelle: exit {proc.returncode}"]
    if theory_markers:
        summary_parts.append(f"{len(theory_markers)} theory marker(s) seen before failure")
    if failed_session:
        summary_parts.append(f"session {failed_session.group(1)} FAILED")
    summary = ", ".join(summary_parts) + "."
    ctx.sys_msg(f"{label} failed — see Feedback tab.")
    _emit_isabelle_feedback(ctx, "failed", summary, full_output)
    return "failed", full_output[:2000]


# ---------------------------------------------------------------------------
# run_compile
# ---------------------------------------------------------------------------

def _emit_compile_feedback(ctx: RunnerContext, fb_status: str, summary: str,
                           raw_output: str) -> None:
    """Build + write post_compile.{md,json} via the gradle classifier."""
    trace_data = feedback.load_trace(_T2M_OUTPUT)
    fb = feedback.build_gradle_phase_feedback(
        phase_id="compile",
        label="Compile (gradle build)",
        status="passed" if fb_status == "passed" else "failed",
        summary=summary,
        error_summary=raw_output if fb_status != "passed" else "",
        trace_data=trace_data,
    )
    _do_emit_feedback(ctx, fb)


def run_compile(ctx: RunnerContext) -> RunnerResult:
    """Run `gradlew build` against java.generated.project/. Captures
    stderr, classifies failures via the gradle-phase classifier, and
    emits post_compile.{md,json}."""
    phase_cfg = ctx.config
    label = ctx.phase.label if ctx.phase else "Compile (gradle build)"
    del phase_cfg  # config currently unused; gradle has all it needs

    if not (_GENERATED_PROJECT / "build.gradle").exists() and \
       not (_GENERATED_PROJECT / "build.gradle.kts").exists():
        msg = (f"No build.gradle in {_GENERATED_PROJECT}. "
               "Cannot run compile phase.")
        ctx.sys_msg(msg)
        _emit_compile_feedback(ctx, "failed", msg, msg)
        return "failed", msg

    cmd = [str(_GENERATED_PROJECT / _GRADLEW), "build", "--no-daemon", "-x", "test"]
    ctx.sys_msg(f"{label}: starting...")
    try:
        proc = subprocess.Popen(
            cmd, cwd=str(_GENERATED_PROJECT),
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
            text=True, encoding="utf-8", errors="replace",
        )
    except (OSError, subprocess.SubprocessError) as exc:
        msg = f"Failed to launch gradle: {exc}"
        ctx.sys_msg(msg)
        _emit_compile_feedback(ctx, "failed", msg, msg)
        return "failed", msg

    output_lines: list[str] = []
    try:
        for line in proc.stdout or []:
            if ctx.stop_requested():
                proc.terminate()
                break
            output_lines.append(line.rstrip())
            _stream_line(ctx, line.rstrip(), phase="compile")
        proc.wait(timeout=30)
    except subprocess.TimeoutExpired:
        proc.kill()
        msg = "gradle build timed out during shutdown"
        _emit_compile_feedback(ctx, "failed", msg, "\n".join(output_lines))
        return "failed", msg

    full_output = "\n".join(output_lines)
    if proc.returncode == 0:
        summary = "Java project compiled successfully."
        _emit_compile_feedback(ctx, "passed", summary, full_output)
        return "completed", ""

    tail = "\n".join(output_lines[-80:]) if output_lines else "(no output)"
    summary = f"gradle build failed (exit {proc.returncode})"
    _emit_compile_feedback(ctx, "failed", summary, tail)
    return "failed", summary


# ---------------------------------------------------------------------------
# run_coverage
# ---------------------------------------------------------------------------

def run_coverage(ctx: RunnerContext) -> RunnerResult:
    """Run the bidirectional coverage check: requirements ↔
    result_codegen.json ↔ Java source. Pure Python, no subprocess."""
    phase_cfg = ctx.config
    label = ctx.phase.label if ctx.phase else "Coverage (Requirement ↔ Java)"
    del phase_cfg  # paths come from module-level constants

    ctx.sys_msg(f"{label}: starting...")

    trace_data = feedback.load_trace(_T2M_OUTPUT)
    requirements_path = _active_requirements_file()
    if not requirements_path.exists():
        msg = (f"Requirements file not found: {requirements_path}. "
               "Check pipeline.yaml's agent.active_case_study points at "
               "a real case study under forge.assets/case-studies/.")
        ctx.sys_msg(msg)
        _do_emit_feedback(ctx, feedback.Feedback(
            phase="coverage", label="Coverage", status="failed",
            summary=msg,
        ))
        return "failed", msg
    try:
        fb = feedback.build_coverage_feedback(
            label="Coverage (Requirement ↔ Java)",
            requirements_path=requirements_path,
            codegen_trace_path=_CODEGEN_TRACE_FILE,
            java_source_root=_GENERATED_SRC,
            trace_data=trace_data,
        )
    except Exception as exc:
        logger.exception("Coverage check raised an exception")
        msg = f"Coverage check failed with exception: {exc}"
        ctx.sys_msg(msg)
        _do_emit_feedback(ctx, feedback.Feedback(
            phase="coverage", label="Coverage", status="failed",
            summary=msg,
        ))
        return "failed", msg

    _do_emit_feedback(ctx, fb)
    if fb.status == "passed":
        return "completed", ""
    return "failed", fb.summary


def run_vacuity(ctx: RunnerContext) -> RunnerResult:
    """Vacuity audit on generated Dafny + Isabelle artefacts.

    Runs after ``dafny_gen`` / ``isabelle_gen``. Flags two specific
    "verifier passes nothing because the obligation was True"
    patterns — see :mod:`web.vacuity` for the D1 / I1 signal
    definitions. Writes ``post_vacuity.{md,json}`` like every other
    phase; ``passed`` iff neither signal fires.
    """
    label = ctx.phase.label if ctx.phase else "Vacuity audit"
    ctx.sys_msg(f"{label}: scanning {_T2M_OUTPUT}...")

    try:
        report = vacuity.audit(_T2M_OUTPUT, _REPO_ROOT)
    except Exception as exc:
        logger.exception("Vacuity audit raised an exception")
        msg = f"Vacuity audit failed with exception: {exc}"
        ctx.sys_msg(msg)
        _do_emit_feedback(ctx, feedback.Feedback(
            phase="vacuity", label=label, status="failed",
            summary=msg,
        ))
        return "failed", msg

    issues = [
        feedback.Issue(
            kind=f.kind,
            title=f.title,
            raw=f.raw,
            fix_directive=f.fix_directive,
        )
        for f in report.findings
    ]

    if report.status == "passed":
        summary = (
            "No vacuity signals detected. Any `0 errors` from Dafny / "
            "Isabelle is at least potentially load-bearing (only the "
            "two specific signals D1, I1 are checked)."
        )
        next_step = "Nothing to do. Re-run after each iteration to catch regressions."
    else:
        summary = (
            f"{len(report.findings)} vacuity signal(s) detected. The "
            "corresponding verifier passes discharge `True` obligations "
            "and do not establish behavioural content."
        )
        next_step = (
            "Strengthen the flagged template(s) so the verifier discharges "
            "behavioural obligations rather than `True`. Until that is done, "
            "do not count the corresponding verifier pass as evidence of "
            "behavioural correctness in the experiment write-up."
        )

    fb = feedback.Feedback(
        phase="vacuity",
        label=label,
        status=report.status,
        summary=summary,
        issues=issues,
        next_step=next_step,
    )
    _do_emit_feedback(ctx, fb)

    ctx.sys_msg(
        f"{label}: {report.status} ({len(report.findings)} finding(s))"
    )
    if report.status == "passed":
        return "completed", ""
    return "failed", summary


# ---------------------------------------------------------------------------
# Registry
# ---------------------------------------------------------------------------

PYTHON_RUNNERS: dict[str, Callable[[RunnerContext], RunnerResult]] = {
    "forge.dashboard.web.runners:run_fdr4": run_fdr4,
    "forge.dashboard.web.runners:run_dafny_verify": run_dafny_verify,
    "forge.dashboard.web.runners:run_isabelle_verify": run_isabelle_verify,
    "forge.dashboard.web.runners:run_compile": run_compile,
    "forge.dashboard.web.runners:run_coverage": run_coverage,
    "forge.dashboard.web.runners:run_vacuity": run_vacuity,
}
