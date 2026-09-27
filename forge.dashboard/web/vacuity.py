"""Vacuity audit for generated Dafny + Isabelle artefacts.

A verifier "pass" only means something if the obligation it discharged was
non-trivial. The M2T-Dafny and M2T-Isabelle templates can emit boilerplate
obligations as TRUE, so "Dafny verifies; Isabelle proves 24 lemmas" can be
entirely vacuous even on broken Java.

Two specific signals (chosen during the LRE iter-1 audit):

  D1. Dafny `ghost predicate Valid()` body is literally `true` (or empty)
      AND no other behavioural `ensures` clauses are active on the
      transition methods. Every `ensures Valid()` clause is then
      equivalent to `ensures true`, so the verifier discharges no
      behavioural content.

  I1. Isabelle zstore invariant clause `where inv: "True"`. Every
      `xxx preserves <Store>_inv` lemma then proves `True is preserved`,
      which `zpog_full` discharges mechanically.

Pure-logic module: takes ``Path`` arguments, returns a ``Report``. Has no
dependency on ``REPO_ROOT`` or any process-wide state. ``run_vacuity`` in
``runners.py`` wraps this for the pipeline-phase runner contract.
"""
from __future__ import annotations

import json
import math
import re
from dataclasses import dataclass, field
from pathlib import Path


# Severity classification.
#
# BLOCKING  — the finding means a discharged obligation carries no content, or
#             that the backends verified mutually inconsistent systems. Either
#             way a "pass" is not evidence, so the iteration has not converged.
#
# ADVISORY  — the finding is a consequence of a DELIBERATE design decision of
#             the pipeline, not a property of the Java under test, and no
#             amount of codegen iteration can remove it. The CSP data domains
#             are narrowed (`core_int = {0..1}` and similar) because FDR4
#             requires finite types for tractable refinement checking; modes
#             and transitions that are reachable in the symbolic model but not
#             under that narrowing (R2/T2), and domains that are singletons
#             only because of it (E1 narrowed kinds), are the disclosed cost of
#             that choice. They are ALWAYS recorded in the run report — they
#             bound what a passing FDR4 assertion is evidence about — but they
#             do not block convergence, because blocking on them would make the
#             criterion unsatisfiable by the codegen agent.
#
# Symbolic-tier unreachability (R1/T1) is BLOCKING: it holds in the unnarrowed
# model, so it is a property of the extracted machine, not of the abstraction.
ADVISORY_KINDS = frozenset({
    "mode_unreachable_checked",        # R2
    "transition_unfireable_checked",   # T2
    "singleton_domain",                # E1, when induced by narrowing
    "constant_divergence_csp_ceiling", # K1, declared FloatExp workaround
    # P1, empty-offer half. A state with nothing offered is waiting (reactive
    # controller) or absorbing (specified terminal mode); neither is a defect.
    # The non-empty half (dead_state_offered_pair) stays BLOCKING — there the
    # state advertises an event it cannot act on, which is a real gap.
    "dead_state_no_offer",
    # P1, checked-tier-only half (VAC-2). A non-empty-offer pair that fires
    # symbolically but is dead at the narrowed/stubbed instantiation is the
    # narrowing's cost (R2/T2's class), not a Java gap. Blocking on it would
    # push requirement-free edits to satisfy an instrument bound.
    # dead_state_offered_pair (dead in BOTH tiers) remains BLOCKING.
    "dead_state_offered_pair_checked",
})


def classify_severity(signal: str, kind: str) -> str:
    """Return "blocking" | "advisory" for a (signal, kind) pair."""
    return "advisory" if kind in ADVISORY_KINDS else "blocking"


@dataclass
class Finding:
    signal: str            # e.g. "D1", "I1"
    kind: str              # vacuity kind
    title: str
    artifact: str          # file path, repo-root-relative posix style
    raw: str               # the offending snippet
    fix_directive: str
    severity: str = ""     # "blocking" | "advisory"; filled by Report.add()


@dataclass
class Report:
    status: str = "passed"   # "passed" | "failed"
    findings: list[Finding] = field(default_factory=list)
    # Semantic-check verdict tables (per behavioural theory): reachable mode
    # sets and per-operation fireability for both evaluation tiers. Populated
    # by audit(); serialised by the runner into post_vacuity.json so a
    # "passed" is auditable, not just asserted.
    details: dict = field(default_factory=dict)

    def add(self, f: Finding) -> None:
        if not f.severity:
            f.severity = classify_severity(f.signal, f.kind)
        self.findings.append(f)
        if f.severity == "blocking":
            self.status = "failed"

    @property
    def blocking(self) -> list[Finding]:
        return [f for f in self.findings if f.severity == "blocking"]

    @property
    def advisory(self) -> list[Finding]:
        return [f for f in self.findings if f.severity == "advisory"]


# ---------------------------------------------------------------------------
# Signal D1 — Dafny Valid() body is `true` / empty
# ---------------------------------------------------------------------------

_VALID_RE = re.compile(
    r"(?:ghost\s+)?predicate\s+Valid\s*\(\s*\)\s*"   # [ghost] predicate Valid()
    r"(?:reads\s+[^{]*?)?"                           # optional reads clause (any frame)
    r"\{(?P<body>[^}]*)\}",                          # { ... } body
    re.DOTALL,
)
# False-pass audit 2026-08: the old regex required the literal `ghost`
# modifier and exactly `reads this`, so a template drift to plain
# `predicate Valid()` or `reads this, x` made D1 silently stop firing —
# a vacuous Valid() then passed the vacuity audit.


def _strip_dafny_body(text: str) -> str:
    # Drop line comments (`//...`) and block comments (`/* ... */`).
    text = re.sub(r"//[^\n]*", "", text)
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.DOTALL)
    return text.strip()


def _count_behavioural_ensures(text: str) -> int:
    """Count active ``ensures`` clauses on transitionFromX methods that
    reference something other than ``Valid()``. Constructor
    ``ensures mode == X`` is excluded — initial-state assertions don't
    constitute behavioural verification of the controller's stepping logic.

    Comments are stripped BEFORE matching: ``ensures true  // note`` used
    to fail the ``true`` exclusion because of the trailing comment and was
    counted as behavioural (false pass — the archived LRE lemmas carry
    exactly that shape). ``this.mode == X`` is excluded like ``mode == X``.
    """
    active = re.findall(r"^\s*ensures\s+(.+)$", _strip_dafny_body(text), re.MULTILINE)
    count = 0
    for body in active:
        body = body.strip().rstrip(";").strip()
        if body == "Valid()":
            continue
        if re.fullmatch(r"true|(?:this\.)?mode\s*==\s*\w+", body):
            continue
        count += 1
    return count


def check_dafny_valid_vacuous(dafny_path: Path, repo_root: Path) -> Finding | None:
    if not dafny_path.exists():
        return None
    text = dafny_path.read_text(encoding="utf-8")
    m = _VALID_RE.search(text)
    if not m:
        # False-pass audit 2026-08: "no Valid() predicate" used to return
        # None (pass). But a template drift that drops Valid() entirely
        # leaves the same vacuity as `Valid() { true }` — unless other
        # behavioural ensures carry the load, which is checked below.
        if _count_behavioural_ensures(text) > 0:
            return None
        try:
            artifact = str(dafny_path.relative_to(repo_root).as_posix())
        except ValueError:
            artifact = str(dafny_path.as_posix())
        return Finding(
            signal="D1",
            kind="dafny_valid_vacuous",
            title="No Valid() predicate found and no behavioural ensures clauses are active",
            artifact=artifact,
            raw="(no `predicate Valid()` matched; no behavioural `ensures` found)",
            fix_directive=(
                "The generated Dafny has neither a Valid() invariant "
                "predicate nor active behavioural `ensures` clauses, so "
                "`0 errors` from the verifier establishes well-formedness "
                "only. Check the M2T-Dafny template (java2dafny.egl) — "
                "either Valid() was dropped or renamed, or the "
                "per-transition ensures clauses are emitted as comments."
            ),
        )
    body = _strip_dafny_body(m.group("body"))
    if body not in {"", "true", "true;"}:
        return None  # Valid() carries non-trivial content — D1 doesn't fire.

    # Valid() is vacuous — but the verification may still be load-bearing
    # if other behavioural `ensures` clauses are active on the transition
    # methods. Only flag D1 when nothing else carries the load.
    behavioural_count = _count_behavioural_ensures(text)
    if behavioural_count > 0:
        return None  # Active behavioural obligations exist; D1 is moot.

    try:
        artifact = str(dafny_path.relative_to(repo_root).as_posix())
    except ValueError:
        artifact = str(dafny_path.as_posix())

    return Finding(
        signal="D1",
        kind="dafny_valid_vacuous",
        title="Dafny Valid() body is literally `true` (or empty) and no other behavioural ensures clauses are active",
        artifact=artifact,
        raw=m.group(0).strip(),
        fix_directive=(
            "Every `ensures Valid()` clause in the generated Dafny is "
            "equivalent to `ensures true` while Valid() returns true, AND "
            "no per-transition behavioural `ensures` clauses are active "
            "(the template emits them as comments). The Dafny verifier "
            "therefore discharges only `ensures true` obligations — "
            "`0 errors` does not establish behavioural correctness, only "
            "well-formedness.\n\n"
            "To make Dafny verification load-bearing, the M2T-Dafny "
            "template (forge.transformations/src/main/resources/"
            "transformations/java2dafny.egl) must either: "
            "(a) derive a non-trivial Valid() body from the requirements, "
            "or (b) emit per-transition `ensures` clauses as ACTIVE "
            "obligations rather than comments. (b) is the more direct "
            "route to behavioural verification."
        ),
    )


# ---------------------------------------------------------------------------
# Signal I1 — Isabelle zstore invariant is "True"
# ---------------------------------------------------------------------------

# Split the theory into per-zstore blocks so the inv clause is checked
# WITHIN its own zstore. The old single `zstore ... .*? where inv` DOTALL
# regex could pair zstore A (no inv) with zstore B's inv clause, and a
# zstore with NO inv clause at all was never flagged even though
# Z-Machines defaults a missing inv to True (same vacuity).
_ZSTORE_BLOCK_RE = re.compile(
    r"zstore\s+(?P<name>\w+)\s*=(?P<block>.*?)"
    r"(?=\bzstore\s|\bzoperation\s|\bzmachine\s|\bsubsection\b|\bsection\b|\Z)",
    re.DOTALL,
)
_INV_CLAUSE_RE = re.compile(r"where\s+inv\s*:\s*\"(?P<inv>[^\"]*)\"", re.DOTALL)


def check_isabelle_inv_vacuous(thy_path: Path, repo_root: Path) -> Finding | None:
    if not thy_path.exists():
        return None
    text = thy_path.read_text(encoding="utf-8")

    try:
        artifact = str(thy_path.relative_to(repo_root).as_posix())
    except ValueError:
        artifact = str(thy_path.as_posix())

    for zm in _ZSTORE_BLOCK_RE.finditer(text):
        inv_m = _INV_CLAUSE_RE.search(zm.group("block"))
        if inv_m is None:
            return Finding(
                signal="I1",
                kind="isabelle_inv_vacuous",
                title=f"Isabelle zstore {zm.group('name')} has no `where inv:` clause (defaults to True)",
                artifact=artifact,
                raw=f"zstore {zm.group('name')} = ... (no inv clause)",
                fix_directive=(
                    "The zstore declares no invariant clause; Z-Machines "
                    "defaults the invariant to True, so every "
                    "`preserves <Store>_inv` lemma proves `True is "
                    "preserved`. Derive a non-trivial invariant in the "
                    "M2T-Isabelle template."
                ),
            )
        inv = inv_m.group("inv").strip()
        if inv == "True":
            break  # vacuous inv found — fall through to the Finding below
    else:
        return None

    return Finding(
        signal="I1",
        kind="isabelle_inv_vacuous",
        title="Isabelle zstore invariant `where inv:` is `\"True\"`",
        artifact=artifact,
        raw=f'where inv: "{inv}"',
        fix_directive=(
            "The zstore's invariant clause is `\"True\"`, which means "
            "every `xxx preserves <Store>_inv` lemma in the theory "
            "proves `True is preserved`. Isabelle's `zpog_full` tactic "
            "discharges these mechanically; the lemmas verify no "
            "behavioural content.\n\n"
            "To make Isabelle's invariant proofs load-bearing, the "
            "M2T-Isabelle template (forge.transformations/src/main/"
            "resources/transformations/thy_generation_rule.egl) should "
            "derive a non-trivial invariant from the requirements (e.g. "
            "the disjoint mode partition, `cstc >= -1`, post-CalcCPA "
            "invariants on `tcpa`/`cda`). The deadlock-freedom proof "
            "remains substantive even with `inv: True`, but the per-"
            "operation invariant lemmas do not."
        ),
    )


# ---------------------------------------------------------------------------
# Semantic checks (R2-4 extension): satisfiability + reachability
# ---------------------------------------------------------------------------
#
# The D1/I1 signals above are syntactic. The checks below use the bounded
# satisfiability core in ``vacuity_sat`` to catch the vacuity forms the
# reviewer listed: unsatisfiable preconditions (S1), unreachable modes
# (R1 symbolic / R2 checked-instantiation), unfireable transitions and
# dead (state, offered) pairs (T1/T2/P1), never-true implication
# antecedents (A1), empty or singleton domains (E1), inconsistent
# initial states (N1), and cross-backend constant divergence (K1).
#
# Every satisfiability verdict is SAT / UNSAT / UNKNOWN — an atom outside
# the decidable guard fragment caps the verdict at UNKNOWN and is reported
# in the details, never silently passed.

from web import vacuity_sat as vsat


def _rel(path: Path, repo_root: Path) -> str:
    try:
        return str(path.relative_to(repo_root).as_posix())
    except ValueError:
        return str(path.as_posix())


def check_precondition_unsat(model, thy_path: Path, repo_root: Path) -> list[Finding]:
    """S1 — a zoperation whose `pre` is unsatisfiable under the invariant
    pins in the SYMBOLIC (unnarrowed) model. The obligation `Op preserves
    inv` for such an operation is vacuously discharged."""
    out = []
    env = vsat.Env(checked=False)
    for op in model.operations:
        r = vsat.sat_conjunction(op.atoms, model, env)
        if r.verdict == vsat.UNSAT:
            out.append(Finding(
                signal="S1",
                kind="precondition_unsat",
                title=f"Precondition of zoperation {op.name} is unsatisfiable",
                artifact=_rel(thy_path, repo_root),
                raw=f'pre "{op.pre_raw}"  -- {r.reason}',
                fix_directive=(
                    f"The precondition of `{op.name}` can never hold "
                    f"({r.reason}), so every proof obligation about this "
                    "operation is vacuously true and the operation can never "
                    "fire. The extractor prepends the negations of every "
                    "EARLIER branch guard in the same mode (faithful else-if "
                    "semantics), so a contradiction here almost always means "
                    "the SOURCE JAVA TRANSITION IS DEAD CODE: an earlier "
                    "branch in the same mode has a guard that subsumes or "
                    "equals part of this branch's guard, so this branch can "
                    "never be reached. FIX IN THE JAVA: find this operation's "
                    "source branch via the traceability entry, compare its "
                    "guard with the earlier branches in the same mode, and "
                    "reorder the branches (most-specific first) or merge them "
                    "per the requirements' intent. Do NOT touch any .egl "
                    "template. Only if the named atoms are NOT equivalent in "
                    "the Java (i.e. the contradiction was introduced by "
                    "extraction, not by branch ordering) is this an "
                    "instrument issue — report it to the operator instead of "
                    "editing anything."
                ),
            ))
    return out


def check_antecedent_vacuity(model, thy_path: Path, repo_root: Path) -> list[Finding]:
    """A1 — an implication (in the invariant or a precondition) whose
    antecedent is UNSAT: the implication is then vacuously true."""
    out = []
    env = vsat.Env(checked=False)
    seen: set[str] = set()
    sources = [("invariant", model.inv_atoms)] + [
        (f"pre of {op.name}", op.atoms) for op in model.operations]
    for where, atoms in sources:
        for a in atoms:
            if a.kind != "implies" or a.raw in seen:
                continue
            seen.add(a.raw)
            ante_atoms = vsat.parse_conjunction(a.ante)
            r = vsat.sat_conjunction(ante_atoms, model, env)
            if r.verdict == vsat.UNSAT:
                out.append(Finding(
                    signal="A1",
                    kind="antecedent_unsat",
                    title=f"Implication antecedent can never hold ({where})",
                    artifact=_rel(thy_path, repo_root),
                    raw=f"{a.raw}  -- antecedent UNSAT: {r.reason}",
                    fix_directive=(
                        "The antecedent of this implication is "
                        f"unsatisfiable ({r.reason}); the implication is "
                        "vacuously true and contributes no verification "
                        "content. Repair or remove it in the template."
                    ),
                ))
    return out


def check_empty_domain(model, thy_path: Path, repo_root: Path,
                       inst=None, inst_path: Path | None = None) -> list[Finding]:
    """E1 — empty or singleton domains: enumtypes, `params` domains
    (definition ... = {}), and narrowed CSP nametypes. Singleton domains
    are only flagged when a guard/invariant actually compares against
    them (a comparison over a one-value type is constant)."""
    out = []
    # constructors compared against anywhere in preconditions or invariant
    compared_ctors: set[str] = set()
    for atoms in [model.inv_atoms] + [op.atoms for op in model.operations]:
        for a in atoms:
            if (a.kind == "cmp" and a.op in ("=", "!=")
                    and a.rhs and a.rhs[0] == "var"
                    and model.ctor_owner(a.rhs[1]) is not None
                    and a.lhs != ("var", "st")):
                compared_ctors.add(a.rhs[1])
    for tname, ctors in model.enumtypes.items():
        if len(ctors) == 0:
            out.append(Finding(
                signal="E1", kind="empty_domain",
                title=f"enumtype {tname} has an empty domain",
                artifact=_rel(thy_path, repo_root),
                raw=f"enumtype {tname} =",
                fix_directive="A guard quantifying over this type is "
                              "unsatisfiable; a set comprehension over it is "
                              "empty. The type must have at least one value.",
            ))
        elif len(ctors) == 1 and ctors[0] in compared_ctors:
            out.append(Finding(
                signal="E1", kind="singleton_domain",
                title=f"enumtype {tname} has a singleton domain "
                      f"({{{ctors[0]}}})",
                artifact=_rel(thy_path, repo_root),
                raw=f"enumtype {tname} = {ctors[0]}",
                fix_directive=(
                    "Every comparison over this type is constant (always "
                    "true or always false), so guards reading it verify no "
                    "behavioural content. Confirm the Java source really has "
                    "a one-value domain here."
                ),
            ))
    for dname, rhs in model.defs.items():
        if rhs.strip() == "{}" and any(dname == d for op in model.operations
                                       for _, d in op.params):
            out.append(Finding(
                signal="E1", kind="empty_param_domain",
                title=f"Parameter domain {dname} is empty",
                artifact=_rel(thy_path, repo_root),
                raw=f'definition {dname} = "{{}}"',
                fix_directive=(
                    "An operation takes a parameter from this empty set, so "
                    "its enabledness disjunct `\\<exists>x \\<in> {} . ...` "
                    "is False: the operation can never fire and its lemmas "
                    "are vacuous."
                ),
            ))
    if inst is not None and inst_path is not None:
        for nname, (lo, hi) in inst.nametypes.items():
            if lo > hi:
                out.append(Finding(
                    signal="E1", kind="empty_nametype",
                    title=f"CSP nametype {nname} = {{{lo}..{hi}}} is empty",
                    artifact=_rel(inst_path, repo_root),
                    raw=f"nametype {nname} = {{{lo}..{hi}}}",
                    fix_directive="FDR ranges over an empty type: every "
                                  "prefixed communication on it deadlocks and "
                                  "assertions over it are vacuous.",
                ))
    return out


def check_initial_state_consistency(model, thy_path: Path,
                                    repo_root: Path) -> list[Finding]:
    """N1 — Init's assignments vs the invariant: is `Init establishes inv`
    non-vacuously satisfiable? Evaluates each invariant conjunct under the
    literal Init substitution."""
    out = []
    if not model.init:
        return out
    init_st = model.init.get("st")
    for a in model.inv_atoms:
        verdict = None
        reason = ""
        if a.kind == "neq_empty_list":
            rhs = model.init.get(a.var, "")
            if rhs.startswith("[") and rhs.strip() != "[]":
                verdict = True
            elif rhs.strip() == "[]":
                verdict, reason = False, f"Init sets {a.var} = [] but the " \
                                         f"invariant requires {a.var} != []"
        elif a.kind == "implies":
            ante = vsat.parse_conjunction(a.ante)
            if (len(ante) == 1 and ante[0].kind == "cmp"
                    and ante[0].lhs == ("var", "st") and ante[0].op == "="
                    and ante[0].rhs and ante[0].rhs[0] == "var"):
                pin_state = ante[0].rhs[1]
                if init_st is not None and pin_state != init_st:
                    verdict = True  # antecedent false at Init
                else:
                    cons = vsat.parse_conjunction(a.cons)
                    if len(cons) == 1 and cons[0].kind == "set_eq":
                        lens_rhs = model.init.get(cons[0].var)
                        if lens_rhs is not None:
                            init_set = vsat._parse_term(lens_rhs)
                            if init_set and init_set[0] == "set":
                                verdict = init_set[1] == cons[0].set_value
                                if not verdict:
                                    reason = (
                                        f"Init sets {cons[0].var} = "
                                        f"{{{', '.join(sorted(init_set[1]))}}} but the "
                                        f"invariant pins it to "
                                        f"{{{', '.join(sorted(cons[0].set_value))}}} "
                                        f"in state {pin_state}")
        if verdict is False:
            out.append(Finding(
                signal="N1", kind="init_inv_unsat",
                title="Initial state contradicts the invariant",
                artifact=_rel(thy_path, repo_root),
                raw=f"{a.raw}  -- {reason}",
                fix_directive=(
                    f"`Init establishes inv` cannot hold: {reason}. Either "
                    "the Init defaults or the invariant conjunct is wrong in "
                    "thy_generation_rule.egl."
                ),
            ))
    return out


def check_reachability(model, thy_path: Path, repo_root: Path,
                       checked_env=None) -> tuple[list[Finding], dict]:
    """R1/R2/T1/T2/P1 — mode and transition reachability.

    R1: mode unreachable in the symbolic (unnarrowed) model.
    R2: mode reachable symbolically but NOT under the checked CSP
        instantiation (narrowed ranges + constant stubs) — the C6 class.
    T1: operation that can never fire symbolically (beyond S1: source
        mode unreachable).
    T2: operation additionally unfireable under the checked instantiation.
    P1: dead (state, offered) pairs — reachable state + environment offer
        from which NO operation can fire (post-split theories only).
    """
    out: list[Finding] = []
    details: dict = {}
    art = _rel(thy_path, repo_root)
    sym_env = vsat.Env(checked=False)
    sym = vsat.reachability(model, sym_env)
    all_states = set(model.enumtypes.get("St", []))
    init_st = model.init.get("st")
    sym_unreachable = sorted(all_states - sym.reachable)
    details["reachable_symbolic"] = sorted(sym.reachable)
    details["op_fireable_symbolic"] = dict(sym.op_fireable)
    if sym_unreachable:
        out.append(Finding(
            signal="R1", kind="mode_unreachable",
            title=f"Mode(s) unreachable from Init in the symbolic model: "
                  f"{', '.join(sym_unreachable)}",
            artifact=art,
            raw=f"Init st = {init_st}; reachable = "
                f"{{{', '.join(sorted(sym.reachable))}}}",
            fix_directive=(
                "No sequence of operations reaches these modes even with "
                "unnarrowed numeric domains and uninterpreted functions. "
                "Any invariant lemma about them is vacuous. Check the "
                "transition extraction for the missing edges."
            ),
        ))
    t1 = [(n, sym.op_fire_reason[n]) for n, v in sym.op_fireable.items()
          if v == vsat.UNSAT and "source state mismatch" not in sym.op_fire_reason[n]]
    # exclude ops already reported by S1 (pre UNSAT at own source)
    s1_names = {op.name for op in model.operations
                if vsat.sat_conjunction(op.atoms, model, sym_env).verdict == vsat.UNSAT}
    t1 = [(n, r) for (n, r) in t1 if n not in s1_names]
    if t1:
        out.append(Finding(
            signal="T1", kind="transition_unfireable",
            title=f"{len(t1)} transition(s) can never fire in the symbolic model",
            artifact=art,
            raw="; ".join(f"{n}: {r}" for n, r in t1),
            fix_directive=(
                "These operations are dead in the unnarrowed model (their "
                "source mode is unreachable). Their `preserves inv` lemmas "
                "hold vacuously."
            ),
        ))
    if checked_env is not None:
        chk = vsat.reachability(model, checked_env)
        details["reachable_checked"] = sorted(chk.reachable)
        details["op_fireable_checked"] = dict(chk.op_fireable)
        r2 = sorted((all_states & sym.reachable) - chk.reachable)
        if r2:
            out.append(Finding(
                signal="R2", kind="mode_unreachable_checked",
                title=f"Mode(s) unreachable under the CHECKED CSP instantiation: "
                      f"{', '.join(r2)}",
                artifact=art,
                raw=f"checked-reachable = {{{', '.join(sorted(chk.reachable))}}} "
                    f"(narrowed ranges + constant stubs from instantiations.csp)",
                fix_directive=(
                    "These modes are reachable in the unnarrowed model but "
                    "not in the model FDR actually checked: the narrowed "
                    "domains / constant stubs make every guard into them "
                    "false. FDR's verdicts say nothing about behaviour in "
                    "these modes. Widen the instantiation or supply "
                    "non-constant stubs."
                ),
            ))
        t2 = [(n, chk.op_fire_reason[n]) for n, v in chk.op_fireable.items()
              if v == vsat.UNSAT and sym.op_fireable.get(n) != vsat.UNSAT
              and n not in s1_names]
        if t2:
            out.append(Finding(
                signal="T2", kind="transition_unfireable_checked",
                title=f"{len(t2)} transition(s) can never fire under the "
                      f"checked CSP instantiation",
                artifact=art,
                raw="; ".join(f"{n}: {r}" for n, r in t2),
                fix_directive=(
                    "Fireable in the unnarrowed model, dead in the model FDR "
                    "checked (narrowed ranges / constant stubs). FDR's pass "
                    "does not exercise these transitions."
                ),
            ))
        dead = vsat.dead_offered_pairs(model, chk.reachable, checked_env)
        details["dead_offered_pairs"] = [
            (s, sorted(off)) for s, off in dead]

        # VAC-1 fix (2026-09-13). The detection here is deliberate and its
        # guidance was already correct — the original directive said "confirm
        # each pair is intended (e.g. offered = {} usually is)". The defect was
        # the SEVERITY: a single BLOCKING signal covered both
        #
        #   (state, {})        — the environment offers nothing. For a reactive
        #                        controller this is just WAITING, and for a
        #                        terminal mode it is the REQUIRED absorbing
        #                        behaviour. Neither is a defect under any
        #                        reading of the requirements.
        #   (state, non-empty) — the environment offers events the state
        #                        declares it listens for, yet no operation can
        #                        fire. That is a genuine gap: the machine
        #                        advertises an interest it cannot act on.
        #
        # Because BLOCKING prevents convergence, the conflated signal made a
        # correct implementation unconvergeable: the only way to clear a
        # (state, {}) pair is to add a transition, and if no requirement
        # authorises one the loop is pushed into an unjustified edit purely to
        # satisfy the checker. That pressure is real — the archived v1 SRanger
        # carries a tick self-loop on its terminal mode, added (per its own
        # source comment) to give the model checker "a visible event out of
        # Final", with no requirement behind it.
        #
        # Split by offer, keep every detection and every message.
        dead_empty = [(s, off) for s, off in dead if not off]
        dead_offer = [(s, off) for s, off in dead if off]

        # VAC-2 fix (2026-09-14). Second severity split, this time by TIER —
        # the same comparison R2/T2 already make. A non-empty-offer pair can
        # be dead for two different reasons:
        #
        #   dead in BOTH tiers    — the machine advertises an event it cannot
        #                           act on in ANY model. Genuine gap: BLOCKING.
        #   dead only under the   — the guard is satisfiable with unnarrowed
        #   checked instantiation   domains/uninterpreted functions but not at
        #                           the narrowed/stubbed one. That is the
        #                           DELIBERATE FDR4-tractability narrowing
        #                           (disclosed in the letter), not the Java.
        #                           Observed live: LRE run-1's (OCM, {reqMOM})
        #                           — `odist(x) > 1` cannot hold at
        #                           core_real = {0..1} with stubbed odist, yet
        #                           holds symbolically. Blocking on it pushes
        #                           the loop toward requirement-free edits to
        #                           satisfy an instrument bound (the SE-2
        #                           class), exactly what VAC-1 fixed for
        #                           empty offers.
        #
        # Compute symbolic-tier deadness for the same pairs; classify each.
        sym_dead = vsat.dead_offered_pairs(model, sym.reachable, sym_env)
        sym_dead_keys = {(s, frozenset(off)) for s, off in sym_dead}
        dead_offer_both = [(s, off) for s, off in dead_offer
                           if (s, frozenset(off)) in sym_dead_keys]
        dead_offer_narrow = [(s, off) for s, off in dead_offer
                             if (s, frozenset(off)) not in sym_dead_keys]

        if dead_offer_both:
            shown = "; ".join(
                f"({s}, {{{', '.join(sorted(off))}}})"
                for s, off in dead_offer_both[:12])
            out.append(Finding(
                signal="P1", kind="dead_state_offered_pair",
                title=f"{len(dead_offer_both)} (state, offered) pair(s) with a "
                      f"non-empty offer from which no operation can fire",
                artifact=art,
                raw=shown + ("; ..." if len(dead_offer_both) > 12 else ""),
                fix_directive=(
                    "In these reachable states the environment offers events "
                    "the state declares it listens for, yet no zoperation's "
                    "precondition is satisfiable IN EITHER TIER — the machine "
                    "advertises an interest it cannot act on in any model. "
                    "Either the guard is over-constrained or the pin "
                    "advertises an event the state cannot act on."
                ),
            ))
        if dead_offer_narrow:
            shown = "; ".join(
                f"({s}, {{{', '.join(sorted(off))}}})"
                for s, off in dead_offer_narrow[:12])
            out.append(Finding(
                signal="P1", kind="dead_state_offered_pair_checked",
                title=f"{len(dead_offer_narrow)} (state, offered) pair(s) dead "
                      f"only under the checked CSP instantiation",
                artifact=art,
                raw=shown + ("; ..." if len(dead_offer_narrow) > 12 else ""),
                fix_directive=(
                    "ADVISORY — no action for the codegen agent. These pairs "
                    "fire in the unnarrowed model; they are dead only at the "
                    "narrowed domains / constant stubs FDR checks (deliberate "
                    "tractability decision, disclosed). Record with the run: "
                    "FDR's pass does not exercise these configurations."
                ),
            ))

        if dead_empty:
            shown = "; ".join(f"({s}, {{}})" for s, off in dead_empty[:12])
            out.append(Finding(
                signal="P1", kind="dead_state_no_offer",
                title=f"{len(dead_empty)} reachable state(s) from which no "
                      f"operation can fire when the environment offers nothing",
                artifact=art,
                raw=shown + ("; ..." if len(dead_empty) > 12 else ""),
                fix_directive=(
                    "ADVISORY: this is normally intended. A reactive "
                    "controller with nothing offered is waiting, and a "
                    "terminal mode is absorbing by design — for both, the "
                    "absence of a fireable operation is the faithful "
                    "encoding, not a defect. Confirm against the "
                    "requirements that each state listed is either a waiting "
                    "state or a specified terminal mode. Do NOT add a "
                    "transition merely to clear this finding: an edge with no "
                    "requirement behind it makes the verifiers pass while "
                    "implementing behaviour nobody asked for. If a state here "
                    "is meant to make autonomous progress, that is a real "
                    "defect — the requirement should say so, and the guard "
                    "should not need an event."
                ),
            ))
    return out, details


def check_constant_consistency(java_root: Path | None,
                               rct_path: Path | None,
                               dfy_paths: list[Path],
                               csp_assert_paths: list[Path],
                               constant_defaults_path: Path | None,
                               repo_root: Path) -> list[Finding]:
    """K1 — the same named constant must carry the same value in the Java
    source (or constant_defaults.json), the emitted .rct, the emitted .dfy,
    and the CSP assertion files. Divergence means at least one backend
    verified a different system than the code implements (the U4 class)."""
    out = []
    java_consts: dict[str, vsat.Fraction] = {}
    src_label = ""
    if constant_defaults_path is not None and constant_defaults_path.exists():
        try:
            data = json.loads(constant_defaults_path.read_text(encoding="utf-8"))
            for k, v in data.items():
                try:
                    java_consts[vsat.norm_const_name(k)] = vsat.Fraction(str(v))
                except (ValueError, ZeroDivisionError):
                    pass
            src_label = _rel(constant_defaults_path, repo_root)
        except (ValueError, OSError):
            pass
    if not java_consts and java_root is not None and java_root.exists():
        for k, v in vsat.parse_java_constants(java_root).items():
            java_consts[vsat.norm_const_name(k)] = v
        src_label = _rel(java_root, repo_root)
    if not java_consts:
        return out

    backends: dict[str, dict[str, vsat.Fraction]] = {}
    if rct_path is not None and rct_path.exists():
        vals = {}
        for k, v in vsat.parse_rct_consts(
                rct_path.read_text(encoding="utf-8", errors="replace")).items():
            vals[vsat.norm_const_name(k)] = v
        backends[_rel(rct_path, repo_root)] = vals
    for dfy in dfy_paths:
        vals = {}
        for k, v in vsat.parse_dafny_consts(
                dfy.read_text(encoding="utf-8", errors="replace")).items():
            vals[vsat.norm_const_name(k)] = v
        backends[_rel(dfy, repo_root)] = vals
    for cap in csp_assert_paths:
        vals = {}
        for k, v in vsat.parse_csp_consts(
                cap.read_text(encoding="utf-8", errors="replace")).items():
            # const_SRangerController_Ctrl_obstaclethreshold -> obstaclethreshold
            short = vsat.norm_const_name(k.split("_")[-1])
            if short in vals and vals[short] != v:
                vals[short] = None  # internally inconsistent — flag via mismatch
            else:
                vals.setdefault(short, v)
        backends[_rel(cap, repo_root)] = {k: v for k, v in vals.items()
                                          if v is not None}

    for cname, jval in sorted(java_consts.items()):
        divergent = []
        for art, vals in backends.items():
            if cname in vals and vals[cname] != jval:
                divergent.append((art, vals[cname]))
        if divergent:
            shown = "; ".join(f"{a}: {float(v):g}" for a, v in divergent)
            # Distinguish the DECLARED CSP float ceiling from an undeclared
            # mismatch. The RoboChart CSP generator (circus.robocalc.robochart
            # .generator.csp 3.0.0) has no FloatExp overload in its expression
            # dispatcher, so a fractional `const ... : real = 0.5` aborts
            # generation entirely; robochart2rct.egl therefore ceils fractional
            # constants for the .rct/CSP target ONLY, records a first-class
            # pipeline warning, and leaves Isabelle/Dafny (generated from the
            # XMI, which carries the true value) untouched.
            #
            # A divergence that is exactly that ceiling, and confined to the
            # .rct/CSP artefacts, is the disclosed cost of a target limitation
            # no codegen iteration can remove -> ADVISORY. Any other
            # divergence -- a different value, or one reaching Dafny/Isabelle
            # -- is a genuine inconsistency between backends -> BLOCKING.
            csp_only = all(
                (".rct" in a) or ("/csp" in a) or a.endswith(".csp")
                for a, _ in divergent
            )
            is_ceiling = all(
                float(v) == float(math.ceil(float(jval))) and float(jval) != float(v)
                for _, v in divergent
            )
            kind = ("constant_divergence_csp_ceiling"
                    if (csp_only and is_ceiling) else "constant_divergence")
            out.append(Finding(
                signal="K1", kind=kind,
                title=f"Constant `{cname}` diverges between the Java source "
                      f"and generated artefact(s)",
                artifact=divergent[0][0],
                raw=f"Java ({src_label}): {float(jval):g}; {shown}",
                fix_directive=(
                    "ADVISORY - the CSP/.rct value is the ceiling of the Java "
                    "value and no other backend diverges. This is the declared "
                    "FloatExp workaround (the CSP generator cannot compile "
                    "fractional real constants); it is warned about at emission "
                    "time and must be disclosed with the FDR results. No action "
                    "for the codegen agent."
                    if kind == "constant_divergence_csp_ceiling" else
                    "At least one backend verified a system with a different "
                    "constant than the Java implements, and the divergence is "
                    "not the declared CSP float ceiling. Fix the constant "
                    "emission."
                ),
            ))
    return out


# ---------------------------------------------------------------------------
# Audit entry point
# ---------------------------------------------------------------------------

def _find_first(root: Path, patterns: list[str],
                exclude_substr: str = "/timed/") -> Path | None:
    for pat in patterns:
        for p in sorted(root.rglob(pat)):
            if exclude_substr and exclude_substr in p.as_posix():
                continue
            return p
    return None


def audit(t2m_output: Path, repo_root: Path,
          java_root: Path | None = None,
          type_ranges_path: Path | None = None) -> Report:
    """Full vacuity audit over one study's generated artefacts.

    Syntactic signals D1/I1 (unchanged) plus the semantic S1/A1/E1/N1/
    R1/R2/T1/T2/P1/K1 checks. ``t2m_output`` is scanned recursively so
    both the pipeline layout (``output/``, ``output/isabelle/``,
    ``output/csp-gen/``) and the archive layout
    (``formal-artefacts/{dafny,isabelle,csp}``) work.

    Pure: opens files, runs parsers and the bounded evaluator, returns a
    Report. No side effects. The semantic details (verdict tables) are
    attached as ``report.details`` for the runner to serialise.
    """
    report = Report()
    dfy_files = sorted(p for p in t2m_output.rglob("*.dfy"))
    for dfy in dfy_files:
        f = check_dafny_valid_vacuous(dfy, repo_root)
        if f:
            report.add(f)
    thy_files = sorted(p for p in t2m_output.rglob("*.thy"))
    for thy in thy_files:
        f = check_isabelle_inv_vacuous(thy, repo_root)
        if f:
            report.add(f)

    # ── semantic checks over the behavioural theory ─────────────────────────
    report.details = {}
    inst_path = _find_first(t2m_output, ["instantiations.csp"])
    inst = (vsat.parse_instantiations(
        inst_path.read_text(encoding="utf-8", errors="replace"))
        if inst_path else None)
    rct_path = _find_first(t2m_output, ["robochart_controller.rct", "*.rct"])
    if type_ranges_path is None:
        type_ranges_path = (repo_root / "forge.dashboard" / "corrections"
                            / "type_ranges.json")
    grid = vsat.load_type_ranges(type_ranges_path)
    rct_consts = (vsat.parse_rct_consts(
        rct_path.read_text(encoding="utf-8", errors="replace"))
        if rct_path else None)
    checked_env = (vsat.build_checked_env(inst, rct_consts, grid)
                   if (inst is not None or rct_consts) else None)

    beh_files = [p for p in thy_files if p.name.endswith("_Beh.thy")]
    for thy in beh_files:
        model = vsat.parse_theory(
            thy.read_text(encoding="utf-8", errors="replace"))
        if not model.operations:
            continue
        for f in check_precondition_unsat(model, thy, repo_root):
            report.add(f)
        for f in check_antecedent_vacuity(model, thy, repo_root):
            report.add(f)
        for f in check_empty_domain(model, thy, repo_root, inst, inst_path):
            report.add(f)
        for f in check_initial_state_consistency(model, thy, repo_root):
            report.add(f)
        findings, details = check_reachability(model, thy, repo_root,
                                               checked_env)
        for f in findings:
            report.add(f)
        report.details[_rel(thy, repo_root)] = details

    csp_asserts = sorted(t2m_output.rglob("file_*_coreassertions.csp"))
    for f in check_constant_consistency(
            java_root, rct_path, dfy_files, csp_asserts,
            _find_first(t2m_output, ["constant_defaults.json"]),
            repo_root):
        report.add(f)

    # False-pass audit 2026-08: an EMPTY artefact set used to pass. The
    # vacuity phase runs after dafny_gen/isabelle_gen, so zero artefacts
    # means the audit inspected nothing — that is not a pass.
    if not dfy_files and not thy_files:
        report.add(Finding(
            signal="D1",
            kind="vacuity_no_artefacts",
            title="No Dafny or Isabelle artefacts found to audit",
            artifact=str(t2m_output),
            raw=f"glob('*.dfy') and isabelle/*.thy both empty under {t2m_output}",
            fix_directive=(
                "The vacuity audit found nothing to inspect. Run the "
                "dafny_gen / isabelle_gen phases first (or fix their "
                "output paths). A 'passed' vacuity audit over zero "
                "artefacts would itself be vacuous."
            ),
        ))
    return report
