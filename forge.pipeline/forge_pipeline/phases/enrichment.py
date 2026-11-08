"""Optional LLM-mediated translation of Isabelle residual goals into Java-level suggestions, appended to a report's fix directives. Off unless pipeline.yaml enables it; the suggestions are labelled as unverified."""
from __future__ import annotations

import hashlib
import json
import logging
import shutil
import subprocess
import sys
from pathlib import Path

from .. import feedback
from ..paths import DEFAULT as PATHS

logger = logging.getLogger(__name__)


def _llm_translation_enabled() -> bool:
    """Fix 3: opt-in flag at ``agent.llm_residual_translation`` in pipeline.yaml.

    Off by default. When on, Isabelle issues whose raw output contains
    a residual goal (currently ``isabelle_other``, ``isabelle_type_mismatch``,
    and ``isabelle_proof_failed`` kinds) are passed through a one-shot
    Claude Code subprocess to produce a Java-level fix suggestion.
    """
    try:
        from ..manifest import Manifest
        manifest = Manifest.load(PATHS.pipeline_yaml)
        raw_agent = (manifest._raw.get("agent") or {})
        return bool(raw_agent.get("llm_residual_translation", False))
    except Exception:
        return False


def _claude_binary_path() -> str | None:
    """Resolve the claude CLI path from pipeline.yaml's ``agent.claude_path``."""
    try:
        from ..manifest import Manifest
        manifest = Manifest.load(PATHS.pipeline_yaml)
        raw_agent = (manifest._raw.get("agent") or {})
        per_os = (raw_agent.get("claude_path") or {})
        return per_os.get(sys.platform) or per_os.get("default")
    except Exception:
        return None


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
            p = PATHS.root / f
        h.update(_hash_file(p).encode("ascii"))
    return h.hexdigest()


def _llm_cache_lookup(key: str) -> str | None:
    """Read a cached LLM response by hash key, if present."""
    cache_file = PATHS.llm_cache_dir / f"{key}.txt"
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
        PATHS.llm_cache_dir.mkdir(parents=True, exist_ok=True)
        (PATHS.llm_cache_dir / f"{key}.txt").write_text(response, encoding="utf-8")
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


def apply_llm_residual_translation(fb: feedback.Feedback) -> None:
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
