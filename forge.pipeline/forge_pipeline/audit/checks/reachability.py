"""Signals R1/R2, T1/T2 and P1: modes, transitions and offered pairs unreachable or unfireable, in the symbolic model and under the narrowed instantiation."""
from __future__ import annotations

import json
import math
import re
from pathlib import Path

from .. import sat
from ..model import Finding, rel as _rel


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
    sym_env = sat.Env(checked=False)
    sym = sat.reachability(model, sym_env)
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
          if v == sat.UNSAT and "source state mismatch" not in sym.op_fire_reason[n]]
    # exclude ops already reported by S1 (pre UNSAT at own source)
    s1_names = {op.name for op in model.operations
                if sat.sat_conjunction(op.atoms, model, sym_env).verdict == sat.UNSAT}
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
        chk = sat.reachability(model, checked_env)
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
              if v == sat.UNSAT and sym.op_fireable.get(n) != sat.UNSAT
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
        dead = sat.dead_offered_pairs(model, chk.reachable, checked_env)
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
        sym_dead = sat.dead_offered_pairs(model, sym.reachable, sym_env)
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
