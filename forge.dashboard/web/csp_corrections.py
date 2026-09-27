"""Apply corrections to the auto-generated instantiations.csp.

The RoboChart CSP generator computes type ranges from the model that
can be too large for FDR4 model checking (OOM), and produces stubs
for uninterpreted functions.  This module:

1. Replaces base type ranges with small, tractable defaults (built in).
2. Applies domain-specific overrides from ``corrections/csp_overrides.csp``
   (e.g. distance function interpretations).

Usage
-----
Standalone::

    python phases/apply_csp_corrections.py

From the pipeline (called before FDR4 verification)::

    from phases.apply_csp_corrections import apply_csp_corrections
    apply_csp_corrections("config.yaml")
"""

import logging
import re
from pathlib import Path

import yaml

logger = logging.getLogger(__name__)

# csp_corrections.py lives in forge.dashboard/web/; parent.parent = forge.dashboard/
_DASHBOARD_DIR = Path(__file__).resolve().parent.parent
_MANIFEST_PATH = _DASHBOARD_DIR.parent / "pipeline.yaml"


# ------------------------------------------------------------------
# Default type ranges (generic, not domain-specific)
# ------------------------------------------------------------------

DEFAULT_TYPE_RANGES: dict[str, str] = {
    "nat": "nametype core_nat = {0..1}",
    "int": "nametype core_int = {0..1}",
    "real": "nametype core_real = {0..1}",
    "core_clock_type": "nametype core_clock_type = {0..1}",
    "string": "nametype core_string = {0}",
}


# ------------------------------------------------------------------
# Block parser
# ------------------------------------------------------------------

def _parse_generate_blocks(text: str) -> dict[str, list[str]]:
    """Parse ``-- generate <name>`` blocks from a CSP file.

    Returns ``{name: [content_lines]}``.  Only the last occurrence of
    a name wins (allows later definitions to shadow earlier ones).
    """
    blocks: dict[str, list[str]] = {}
    current_name: str | None = None
    current_lines: list[str] = []

    for raw_line in text.splitlines():
        stripped = raw_line.strip()
        m = re.match(r"^-- generate\s+(.+)$", stripped)
        if m:
            if current_name is not None:
                blocks[current_name] = current_lines
            current_name = m.group(1).strip()
            current_lines = []
            continue

        if current_name is not None:
            current_lines.append(raw_line)

    if current_name is not None:
        blocks[current_name] = current_lines

    return blocks


# ------------------------------------------------------------------
# Merge logic
# ------------------------------------------------------------------

def _merge(generated_text: str, overrides: dict[str, list[str]]) -> str:
    """Replace ``-- generate <name>`` blocks in *generated_text* with
    matching entries from *overrides*.  Non-overridden blocks are kept
    verbatim.
    """
    lines = generated_text.splitlines()
    result: list[str] = []
    i = 0

    while i < len(lines):
        raw = lines[i]
        stripped = raw.strip()

        m = re.match(r"^-- generate\s+(.+)$", stripped)
        if m:
            name = m.group(1).strip()
            if name in overrides:
                # Emit the marker comment
                result.append(raw)
                i += 1

                # Skip all content until the next -- generate marker or -- SECTION header
                while i < len(lines):
                    s = lines[i].strip()
                    if re.match(r'^-- generate\s+', s) or re.match(r'^--\s+[A-Z][A-Z_ ]+$', s):
                        break
                    i += 1

                # Insert override content
                for ol in overrides[name]:
                    result.append(ol)

                logger.debug("Overrode block: %s", name)
                continue

        result.append(raw)
        i += 1

    return "\n".join(result) + "\n"


# ------------------------------------------------------------------
# Type range extraction
# ------------------------------------------------------------------

# Known nametype blocks (blocks whose content is a "nametype ... = {lo..hi}" definition)
_NAMETYPE_BLOCKS = {"nat", "int", "real", "core_clock_type", "string", "boolean"}

_RANGE_RE = re.compile(
    r"nametype\s+(\w+)\s*=\s*(?:union\()?\{?\s*(-?\d+)\s*(?:\.\.\s*(-?\d+))?\s*\}?"
)


def _resolve_inst_path(config_path: str) -> Path | None:
    """Resolve the path to instantiations.csp from config.

    Loads via Manifest so ${output_dir} / ${repo} placeholders inside
    phases.fdr4.csp_file are substituted before we attempt to find
    instantiations.csp on disk.

    Two paths:
      (a) phases.fdr4.csp_file is pinned → derive instantiations.csp
          relative to the pinned file (sibling or parent dir).
      (b) phases.fdr4.csp_file is absent → fall back to the canonical
          location ${output_dir}/csp-gen/instantiations.csp. This is
          the path the RoboChart CSP generator always produces.
    """
    from web.manifest import Manifest
    try:
        manifest = Manifest.load(Path(config_path))
    except Exception:
        return None
    fdr4 = (getattr(manifest, "_raw", {}) or {}).get("phases", {}).get("fdr4", {})
    csp_file = fdr4.get("csp_file", "")
    # Fall back to the canonical location when csp_file is unset/commented
    # (case-study-independent default; matches what the CSP generator writes).
    if not csp_file:
        raw = getattr(manifest, "_raw", {}) or {}
        top_vars = {"repo": str(Path(config_path).resolve().parent)}
        for key, val in raw.items():
            if isinstance(val, str):
                top_vars[key] = _substitute(val, top_vars)
        output_dir = top_vars.get("output_dir", "")
        if output_dir:
            candidate = Path(_substitute(output_dir, top_vars)) / "csp-gen" / "instantiations.csp"
            return candidate if candidate.exists() else None
        return None
    # Re-resolve here too — Manifest leaves config fields outside `args`
    # un-substituted; carry the same top-level vars across.
    raw = getattr(manifest, "_raw", {}) or {}
    top_vars = {"repo": str(Path(config_path).resolve().parent)}
    for key, val in raw.items():
        if isinstance(val, str):
            top_vars[key] = _substitute(val, top_vars)
    csp_file = _substitute(csp_file, top_vars)
    csp_path = Path(csp_file)
    if not csp_path.is_absolute():
        csp_path = _DASHBOARD_DIR / csp_path
    # instantiations.csp may be in the same dir or one level up (if csp_file is in defs/)
    for candidate in [csp_path.parent / "instantiations.csp",
                      csp_path.parent.parent / "instantiations.csp"]:
        if candidate.exists():
            return candidate
    # Fallback: assume sibling of parent
    return csp_path.parent / "instantiations.csp"


def _substitute(s: str, vars: dict) -> str:
    out = s
    for k, v in vars.items():
        out = out.replace("${" + k + "}", str(v))
    return out


def extract_type_ranges(config_path: str = None) -> list[dict] | None:
    """Extract type range blocks from instantiations.csp.

    Returns a list of dicts::

        [{"name": "nat", "csp_name": "core_nat", "lower": 0, "upper": 1}, ...]

    Returns *None* if instantiations.csp is not found.
    """
    if config_path is None:
        config_path = str(_MANIFEST_PATH)
    inst_path = _resolve_inst_path(config_path)
    if not inst_path or not inst_path.exists():
        return None

    text = inst_path.read_text(encoding="utf-8")
    blocks = _parse_generate_blocks(text)

    ranges = []
    for block_name in _NAMETYPE_BLOCKS:
        if block_name not in blocks:
            continue
        content = "\n".join(blocks[block_name])
        m = _RANGE_RE.search(content)
        if m:
            csp_name = m.group(1)
            lower = int(m.group(2))
            upper = int(m.group(3)) if m.group(3) else lower
            ranges.append({
                "name": block_name,
                "csp_name": csp_name,
                "lower": lower,
                "upper": upper,
            })
    return ranges


# ------------------------------------------------------------------
# Persistence for user-configured ranges
# ------------------------------------------------------------------

_RANGES_FILE = "type_ranges.json"


def load_saved_ranges(config_path: str = None) -> dict[str, dict] | None:
    """Load previously saved user type ranges.

    Returns ``{block_name: {"lower": int, "upper": int}}`` or None.
    """
    import json
    corrections_dir = _DASHBOARD_DIR / "corrections"
    path = corrections_dir / _RANGES_FILE
    if path.exists():
        try:
            return json.loads(path.read_text(encoding="utf-8"))
        except (ValueError, OSError):
            return None
    return None


def save_ranges(ranges: dict[str, dict], config_path: str = None) -> Path:
    """Save user-configured type ranges to corrections/type_ranges.json."""
    import json
    corrections_dir = _DASHBOARD_DIR / "corrections"
    corrections_dir.mkdir(exist_ok=True)
    path = corrections_dir / _RANGES_FILE
    path.write_text(json.dumps(ranges, indent=2), encoding="utf-8")
    return path


def _build_nametype(csp_name: str, lower: int, upper: int) -> str:
    """Build a CSP-M nametype definition with correct syntax."""
    if lower == upper:
        return f"nametype {csp_name} = {{{lower}}}"
    # CSP-M quirk: {-1..1} is a multiline comment; use { -1..1}
    lo_str = f" {lower}" if lower < 0 else str(lower)
    return f"nametype {csp_name} = {{{lo_str}..{upper}}}"


# ------------------------------------------------------------------
# Public API
# ------------------------------------------------------------------

def apply_csp_corrections(config_path: str = None,
                          user_ranges: dict[str, dict] | None = None) -> Path | None:
    """Apply type range corrections to instantiations.csp.

    Reads default type ranges (built in) and any overrides from
    ``config.yaml`` under ``phases.fdr4.type_ranges``, then replaces
    matching ``-- generate <name>`` blocks in the generated file.

    Returns the path to the corrected ``instantiations.csp``, or
    *None* if no corrections were applied.
    """
    if config_path is None:
        config_path = str(_MANIFEST_PATH)
    with open(config_path, "r", encoding="utf-8") as f:
        config = yaml.safe_load(f)

    fdr4_cfg = config.get("phases", {}).get("fdr4", {})
    csp_file = fdr4_cfg.get("csp_file", "")

    # Resolve ${...} placeholders (e.g. ${output_dir}) the same way
    # _resolve_inst_path does.
    top_vars = {"repo": str(Path(config_path).resolve().parent)}
    for key, val in config.items():
        if isinstance(val, str):
            top_vars[key] = _substitute(val, top_vars)
    if csp_file:
        csp_file = _substitute(csp_file, top_vars)
        csp_path = Path(csp_file)
        if not csp_path.is_absolute():
            csp_path = _DASHBOARD_DIR / csp_path
    # else: csp_file is unset/commented in pipeline.yaml — fall through to
    # _resolve_inst_path which handles the canonical-location fallback.

    inst_path = _resolve_inst_path(config_path)
    if not inst_path or not inst_path.exists():
        logger.warning("instantiations.csp not found")
        return None

    # 1. Type range overrides: user-provided > saved > defaults
    overrides: dict[str, list[str]] = {}

    # Map block names to CSP nametype names
    csp_names = {
        "nat": "core_nat", "int": "core_int", "real": "core_real",
        "core_clock_type": "core_clock_type", "string": "core_string",
    }

    # Start with defaults
    for name, definition in DEFAULT_TYPE_RANGES.items():
        overrides[name] = [definition]

    # Apply saved ranges (from previous session)
    saved = load_saved_ranges(config_path) if user_ranges is None else None
    if saved:
        for name, cfg in saved.items():
            if name in csp_names:
                overrides[name] = [_build_nametype(
                    csp_names[name], cfg["lower"], cfg["upper"]
                )]

    # Apply user-provided ranges (from UI, highest priority)
    if user_ranges:
        for name, cfg in user_ranges.items():
            if name in csp_names:
                overrides[name] = [_build_nametype(
                    csp_names[name], cfg["lower"], cfg["upper"]
                )]

    # 2. Domain-specific overrides from corrections/csp_overrides.csp
    corrections_dir = _DASHBOARD_DIR / "corrections"
    overrides_file = corrections_dir / "csp_overrides.csp"
    if overrides_file.exists():
        overrides_text = overrides_file.read_text(encoding="utf-8")
        for name, lines in _parse_generate_blocks(overrides_text).items():
            overrides[name] = lines
        logger.info("Loaded %d domain-specific overrides from %s",
                     len(overrides) - len(DEFAULT_TYPE_RANGES), overrides_file)

    # 3. WELL-FORMEDNESS WIDENING (2026-09-14). The narrowed
    # ranges above are a tractability decision, but they carry a
    # well-formedness condition the defaults cannot see: the domain must
    # include every constant the MODEL ITSELF binds into a process
    # instantiation. When a codegen's action shape lifts a constant into an
    # operation argument (e.g. `entry move(0, turnvel)` with turnvel = 2),
    # the coreassertions file binds `const_..._turnvel = 2` and passes it to
    # the process — and a {0..1} domain then cannot compile the process at
    # all: every assertion returns "inconclusive" in seconds, with no error
    # the feedback layer can attribute. Observed live: sranger v3 run-3
    # cold-failed FDR4 this way while runs 1-2 passed, purely because THEIR
    # codegens' shapes never surfaced the velocity constants into the model
    # (semantically identical Java, byte-distinct text). Same class as the
    # Steam Boiler's pump-id widening, now handled where it belongs.
    #
    # Rule: scan the .rct's typed constants; for each narrowed numeric range,
    # widen just enough to include every constant of that type that the
    # coreassertions files actually BIND. Constants that appear only in
    # guards (compared, never communicated) do not force widening — e.g.
    # turnduration = 2 sits outside {0..1} in every sranger run and is
    # harmless — so binding-site presence, not mere existence, is the test.
    # Every widening is logged loudly: a silent domain change would be a
    # tuning knob pretending to be a constant of the experiment.
    widened: dict[str, tuple[int, int, list[str]]] = {}
    try:
        out_dir = inst_path.parent.parent  # csp-gen/.. = output/
        rct = next(out_dir.glob("*.rct"), None)
        rct_consts: dict[str, tuple[str, float]] = {}
        if rct is not None:
            for m in re.finditer(
                    r"^\s*const\s+(\w+)\s*:\s*(nat|int|real)\s*=\s*(-?\d+(?:\.\d+)?)",
                    rct.read_text(encoding="utf-8"), re.M):
                rct_consts[m.group(1).lower()] = (m.group(2), float(m.group(3)))
        # A constant forces widening only when it is COMMUNICATED — named in
        # an action line (entry/exit/during/action), where it becomes an
        # event payload the narrowed nametype must contain. A constant that
        # appears only in `condition` lines is compared, never communicated,
        # and is harmless outside the domain: turnduration = 2 is bound as a
        # process parameter in EVERY sranger run's coreassertions and passed
        # at {0..1} in runs 1-2 — the first version of this scan keyed on
        # binding and spuriously widened those passing runs (caught by the
        # negative control). Note the parenthesis trap: `condition since (
        # clockResetTime ) >= turnduration` puts a CALL in a guard, so
        # "appears inside parens" is also the wrong test; line kind is the
        # discriminator.
        communicated: dict[str, float] = {}
        if rct is not None:
            for line in rct.read_text(encoding="utf-8").splitlines():
                if not re.match(r"\s*(entry|exit|during|action)\b", line):
                    continue
                # FIX (2026-09-19). `wait ( X )` is a timed delay, not a
                # communication: its argument never becomes an event payload, so it
                # must not force widening. MEASURED, not argued — the autonomous
                # arm's chemical_detector run-1 has `const outPeriod : real = 2`
                # inside `entry send shortRandomWalk ; wait ( outPeriod )` and its
                # fdr4 phase PASSED 9/9 at real = {0..1}. Without this exclusion the
                # A case-insensitive fix would widen that passing run's domain {0..1} -> {0..2},
                # enlarging the state space of a run that demonstrably did not need it.
                scan_line = re.sub(r"\bwait\s*\([^)]*\)", " ", line)
                for nm, (typ, v) in rct_consts.items():
                    # FIX (2026-09-19). rct_consts is keyed LOWER-CASED (see the
                    # scan above), but the action line carries the constant's original
                    # spelling. A camelCase constant -- `entry move ( 0 , turnVel )` --
                    # therefore never matched, nothing registered as communicated, and
                    # the widening never fired: FDR aborted with
                    # "moveCall.0.2 ... not a member of {0,1}" and every assertion came
                    # back inconclusive. Found by the autonomous arm's sranger run-2,
                    # which worked around it by renaming its Java constants to single
                    # words rather than editing the instrument (correct conduct: the
                    # prompt forbids instrument edits). Case-insensitive here because
                    # the key is normalised and the line is not.
                    if re.search(rf"\b{re.escape(nm)}\b", scan_line, re.I):
                        communicated[nm] = v
        need: dict[str, tuple[float, float]] = {}   # csp type -> (min, max) required
        for nm, v in communicated.items():
            typ = rct_consts[nm][0]
            lo, hi = need.get(typ, (0.0, 0.0))
            need[typ] = (min(lo, v), max(hi, v))
            widened.setdefault(typ, (0, 0, []))[2].append(f"{nm}={v:g}")
        for typ, (lo, hi) in need.items():
            if typ not in csp_names:
                continue
            cur = overrides.get(typ, [DEFAULT_TYPE_RANGES.get(typ, "")])[0]
            mm = re.search(r"\{(-?\d+)\.\.(-?\d+)\}", cur)
            if not mm:
                continue
            clo, chi = int(mm.group(1)), int(mm.group(2))
            nlo, nhi = min(clo, int(lo)), max(chi, int(hi))
            if (nlo, nhi) != (clo, chi):
                overrides[typ] = [_build_nametype(csp_names[typ], nlo, nhi)]
                consts = widened.get(typ, (0, 0, []))[2]
                widened[typ] = (nlo, nhi, consts)
                logger.warning(
                    "WIDENED %s: {%d..%d} -> {%d..%d} to cover model-bound "
                    "constant(s) %s — the model communicates these values; a "
                    "domain excluding them cannot compile (every assertion "
                    "inconclusive).", csp_names[typ], clo, chi, nlo, nhi,
                    ", ".join(consts))
    except Exception as e:  # never let the widening scan break the phase
        logger.warning("communicated-constants scan failed (%s); ranges "
                       "left as configured — a load failure downstream may "
                       "be this", e)

    # The RoboChart CSP generator emits TWO instantiations.csp files when
    # any controller has a clock field:
    #   csp-gen/instantiations.csp        — untimed module
    #   csp-gen/timed/instantiations.csp  — timed module
    # Clock-using controllers (LRE, SRanger, chemical_detector) load the
    # TIMED module, so its type ranges are the load-bearing ones. Leaving
    # the timed file at model-derived ranges (union({-2..2}, ...)) blows
    # Windows' page-file commit at refines.exe startup even though the
    # untimed file is [0..1]. Patch both consistently.
    targets = [inst_path]
    timed_inst = inst_path.parent / "timed" / "instantiations.csp"
    if timed_inst.exists() and timed_inst != inst_path:
        targets.append(timed_inst)

    for target in targets:
        logger.info("Applying %d corrections to %s", len(overrides), target)
        target_text = target.read_text(encoding="utf-8")
        corrected = _merge(target_text, overrides)
        target.write_text(corrected, encoding="utf-8")
        logger.info("Wrote corrected %s (%d overrides applied)", target.name, len(overrides))

    return inst_path


# ------------------------------------------------------------------
# CLI
# ------------------------------------------------------------------

if __name__ == "__main__":
    import sys

    logging.basicConfig(level=logging.INFO, format="%(levelname)s: %(message)s")

    cfg = sys.argv[1] if len(sys.argv) > 1 else str(_MANIFEST_PATH)
    result = apply_csp_corrections(cfg)
    if result:
        print(f"Corrected: {result}")
    else:
        print("No corrections applied.")
