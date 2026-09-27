"""Satisfiability / reachability core for the extended vacuity audit (6d).

Reviewer R2-4 listed vacuity forms the syntactic D1/I1 checks cannot see:
unsatisfiable preconditions, unreachable modes/transitions, implications
with never-true antecedents, empty event/parameter domains, inconsistent
initial-state predicates. This module supplies the semantic machinery for
those checks over the generated Isabelle/Z-Machines theory plus the CSP
instantiation artefacts.

SOLVER CHOICE. No SMT solver (z3/cvc5) is installed on the reference
machine and the dashboard deliberately has no heavyweight dependencies, so
the satisfiability core is self-contained bounded evaluation. This is NOT
a heuristic: the guard language emitted by thy_generation_rule.egl is
small —

    pre ::= atom (\\<and> atom)*
    atom ::= st = S | v | \\<not>v | t OP c | t OP t' | e \\<in> SETLENS
           | \\<not>(e \\<in> SETLENS) | offered \\<subseteq> listens
           | sts = (ctor) | \\<not>sts = (ctor)
    t ::= numeric lens | f(args)      OP ::= < <= > >= = !=

Every atom constrains a single term against a constant, one other term,
or a (possibly invariant-pinned) event set. Conjunctions of such atoms
are decidable by per-term constraint intersection plus pairwise relation
intersection — no search needed. Anything outside this grammar makes the
verdict UNKNOWN, never a silent SAT/pass.

Verdicts are always one of "SAT" | "UNSAT" | "UNKNOWN".

TWO EVALUATION TIERS.
  * symbolic  — numeric terms range over the (unbounded) reals/ints;
    function calls are uninterpreted. UNSAT here means the precondition
    is contradictory in the unnarrowed model (the contradictory-trigger class).
  * checked   — the model FDR actually checked: numeric domains narrowed
    per corrections/type_ranges.json (default {0..1}), function calls
    replaced by the constant stubs from instantiations.csp, and RoboChart
    constants bound to the values in the emitted .rct. UNSAT here means
    the guard can never fire in the CHECKED model even if it could in the
    unnarrowed one (the C6 class: odist(index)=0 kills odist(cstc)>1.0).

Reachability is a fixpoint over the zoperations from Init's state. The
per-step abstraction is deliberately conservative: non-`st` lenses are
havoc'd each step (any value in their tier-domain), so "unreachable"
verdicts are sound (true in every refinement) while "reachable" verdicts
are only "not shown unreachable". The abstraction is stated in the
report rather than hidden.

Pure-logic module: Path/str in, dataclasses out. No process-wide state.
"""
from __future__ import annotations

import json
import re
from dataclasses import dataclass, field
from fractions import Fraction
from itertools import product
from pathlib import Path

SAT = "SAT"
UNSAT = "UNSAT"
UNKNOWN = "UNKNOWN"

# ---------------------------------------------------------------------------
# Normalisation of Isabelle inner syntax to an ASCII token stream
# ---------------------------------------------------------------------------

_SYMBOL_MAP = [
    ("\\<and>", " && "),
    ("\\<longrightarrow>", " IMPLIES "),
    ("\\<noteq>", " != "),
    ("\\<le>", " <= "),
    ("\\<ge>", " >= "),
    ("\\<in>", " IN "),
    ("\\<subseteq>", " SUBSETEQ "),
    ("\\<not>", " ! "),
    ("\\<Zprime>", "'"),
    ("\\<leadsto>", " ~> "),
]
# Constructs outside the decidable fragment — presence makes an atom UNKNOWN.
_UNSUPPORTED_MARKERS = ("\\<or>", "\\<forall>", "\\<exists>", "\\<lambda>", "IMPLIES")


def normalize(s: str) -> str:
    for a, b in _SYMBOL_MAP:
        s = s.replace(a, b)
    return re.sub(r"\s+", " ", s).strip()


def split_top(s: str, sep: str) -> list[str]:
    """Split on a token at paren/brace/bracket depth 0."""
    parts, depth, cur, i, n = [], 0, [], 0, len(s)
    while i < n:
        c = s[i]
        if c in "({[":
            depth += 1
        elif c in ")}]":
            depth -= 1
        if depth == 0 and s.startswith(sep, i):
            parts.append("".join(cur).strip())
            cur = []
            i += len(sep)
            continue
        cur.append(c)
        i += 1
    parts.append("".join(cur).strip())
    return [p for p in parts if p != ""]


# ---------------------------------------------------------------------------
# Atom parsing
# ---------------------------------------------------------------------------

@dataclass
class Atom:
    kind: str          # bool|enum_eq|cmp|member|subset|set_eq|neq_empty_list|implies|unknown|true
    raw: str = ""
    var: str = ""      # bool/enum/member-set/list variable
    value: str = ""    # enum ctor / member event
    positive: bool = True
    lhs: tuple = ()    # cmp terms: ("num", Fraction) | ("var", name) | ("call", fname, args)
    op: str = ""
    rhs: tuple = ()
    set_value: frozenset = frozenset()
    ante: str = ""     # implies
    cons: str = ""


_NUM_RE = re.compile(r"^-?\d+(?:\.\d+)?$")
_IDENT_RE = re.compile(r"^\w+$")
_CALL_RE = re.compile(r"^(\w+)\(([^()]*)\)$")
_SET_RE = re.compile(r"^\{([^{}]*)\}$")

_FLIP = {"<": ">=", "<=": ">", ">": "<=", ">=": "<", "=": "!=", "!=": "="}


def _parse_term(s: str):
    s = s.strip()
    while s.startswith("(") and s.endswith(")") and _balanced(s[1:-1]):
        s = s[1:-1].strip()
    if _NUM_RE.match(s):
        return ("num", Fraction(s))
    if _IDENT_RE.match(s):
        return ("var", s)
    m = _CALL_RE.match(s)
    if m:
        return ("call", m.group(1), m.group(2).strip())
    if s == "[]":
        return ("emptylist",)
    m = _SET_RE.match(s)
    if m:
        elems = [e.strip() for e in m.group(1).split(",") if e.strip()]
        return ("set", frozenset(elems))
    return None


def _balanced(s: str) -> bool:
    d = 0
    for c in s:
        if c == "(":
            d += 1
        elif c == ")":
            d -= 1
        if d < 0:
            return False
    return d == 0


def parse_atom(s: str) -> Atom:
    raw = s = s.strip()
    if any(mark in s for mark in ("&&",)):
        # nested conjunction inside parens — recurse in caller, not here
        return Atom(kind="unknown", raw=raw)
    if s in ("True", "true"):
        return Atom(kind="true", raw=raw)
    if " IMPLIES " in s:
        # strip one level of parens around the implication if present
        t = s
        while t.startswith("(") and t.endswith(")") and _balanced(t[1:-1]):
            t = t[1:-1].strip()
        parts = split_top(t, " IMPLIES ")
        if len(parts) == 2:
            return Atom(kind="implies", raw=raw, ante=parts[0], cons=parts[1])
        return Atom(kind="unknown", raw=raw)
    if any(mark in s for mark in _UNSUPPORTED_MARKERS):
        return Atom(kind="unknown", raw=raw)

    # negation of a parenthesised atom: ! ( ... )
    m = re.match(r"^!\s*\((.*)\)$", s)
    if m and _balanced(m.group(1)):
        inner = parse_atom(m.group(1))
        if inner.kind in ("bool", "enum_eq", "member"):
            inner.positive = not inner.positive
            inner.raw = raw
            return inner
        if inner.kind == "cmp":
            inner.op = _FLIP[inner.op]
            inner.raw = raw
            return inner
        return Atom(kind="unknown", raw=raw)

    negated = False
    if s.startswith("!"):
        negated = True
        s = s[1:].strip()

    if " SUBSETEQ " in s:
        parts = split_top(s, " SUBSETEQ ")
        if len(parts) == 2 and not negated:
            return Atom(kind="subset", raw=raw, var=parts[0], value=parts[1])
        return Atom(kind="unknown", raw=raw)

    if " IN " in s:
        parts = split_top(s, " IN ")
        if len(parts) == 2 and _IDENT_RE.match(parts[0]) and _IDENT_RE.match(parts[1]):
            return Atom(kind="member", raw=raw, value=parts[0], var=parts[1],
                        positive=not negated)
        return Atom(kind="unknown", raw=raw)

    # comparison — scan for the operator at depth 0 (longest first)
    for op in ("<=", ">=", "!=", "<", ">", "="):
        parts = split_top(s, op)
        if len(parts) == 2 and op != "=" or (op == "=" and len(parts) == 2):
            lhs_s, rhs_s = parts
            # avoid matching '=' inside '<=' '>=' '!=' (split_top on '='
            # would split those); ordering above prevents it because we
            # return on the first op that yields exactly 2 parts.
            lhs, rhs = _parse_term(lhs_s), _parse_term(rhs_s)
            if lhs is None or rhs is None:
                return Atom(kind="unknown", raw=raw)
            eff_op = _FLIP[op] if negated else op
            if rhs and rhs[0] == "emptylist":
                if eff_op == "!=" and lhs[0] == "var":
                    return Atom(kind="neq_empty_list", raw=raw, var=lhs[1])
                return Atom(kind="unknown", raw=raw)
            if rhs and rhs[0] == "set" and lhs[0] == "var" and eff_op == "=":
                return Atom(kind="set_eq", raw=raw, var=lhs[1],
                            set_value=rhs[1])
            return Atom(kind="cmp", raw=raw, lhs=lhs, op=eff_op, rhs=rhs)
    if _IDENT_RE.match(s):
        return Atom(kind="bool", raw=raw, var=s, positive=not negated)
    return Atom(kind="unknown", raw=raw)


def parse_conjunction(s: str) -> list[Atom]:
    atoms: list[Atom] = []
    for chunk in split_top(normalize(s), "&&"):
        t = chunk.strip()
        stripped = t
        while (stripped.startswith("(") and stripped.endswith(")")
               and _balanced(stripped[1:-1]) and "&&" in stripped):
            stripped = stripped[1:-1].strip()
        # Isabelle precedence: \<longrightarrow> binds WEAKER than \<and>,
        # so `A && B IMPLIES C` is `(A && B) IMPLIES C` — check for a
        # top-level IMPLIES before recursing into the conjunction.
        imp_parts = split_top(stripped, " IMPLIES ")
        if len(imp_parts) == 2:
            atoms.append(Atom(kind="implies", raw=stripped,
                              ante=imp_parts[0], cons=imp_parts[1]))
        elif len(imp_parts) > 2:
            # right-associate: A IMPLIES (B IMPLIES C)
            atoms.append(Atom(kind="implies", raw=stripped,
                              ante=imp_parts[0],
                              cons=" IMPLIES ".join(imp_parts[1:])))
        elif stripped.startswith("!") and "&&" in stripped:
            # FIX (2026-09-17): a negated parenthesised conjunction
            # `! (A && B)`. Semantically ¬(A ∧ B) = ¬A ∨ ¬B — a
            # DISJUNCTION, outside this engine's decidable fragment, so
            # per the module contract the verdict contribution is
            # UNKNOWN. (Before the fix the generator mis-emitted these as
            # `!A && B`, which this parser read per-conjunct — wrong
            # semantics; after the fix the text is `!(A && B)`, which used to
            # recurse on itself forever here: the chunk does not start
            # with "(", so the paren-strip loop never fired, and the
            # `"&&" in stripped` branch re-parsed the identical string.)
            atoms.append(Atom(kind="unknown", raw=stripped))
        elif "&&" in stripped:
            atoms.extend(parse_conjunction(stripped))
        else:
            atoms.append(parse_atom(stripped))
    return atoms


# ---------------------------------------------------------------------------
# Theory / artefact parsing
# ---------------------------------------------------------------------------

@dataclass
class ZOperation:
    name: str
    pre_raw: str
    atoms: list[Atom]
    source_state: str | None      # from `st = S` conjunct; None = any state
    target_state: str | None      # from update `st' = T`
    params: list[tuple[str, str]] = field(default_factory=list)


@dataclass
class TheoryModel:
    name: str = ""
    enumtypes: dict[str, list[str]] = field(default_factory=dict)
    lenses: dict[str, str] = field(default_factory=dict)   # name -> raw type
    inv_raw: str = ""
    inv_atoms: list[Atom] = field(default_factory=list)
    pins: dict[str, frozenset] = field(default_factory=dict)  # state -> events
    pin_lens: str = ""            # "listens" (post-split) | "triggers" (pre-split pins) | ""
    operations: list[ZOperation] = field(default_factory=list)
    init: dict[str, str] = field(default_factory=dict)     # lens -> raw rhs (normalized)
    defs: dict[str, str] = field(default_factory=dict)     # definition name -> rhs
    consts: dict[str, str] = field(default_factory=dict)   # consts name -> signature

    def ctor_owner(self, ctor: str) -> str | None:
        for t, cs in self.enumtypes.items():
            if ctor in cs:
                return t
        return None

    def lens_kind(self, name: str) -> str:
        t = self.lenses.get(name, "")
        if "set" in t:
            return "set"
        if "list" in t:
            return "list"
        if t in self.enumtypes:
            return "enum"
        if t in ("bool",):
            return "bool"
        if t in ("real", "int", "nat", "integer"):
            return "numeric"
        return "other"


_ENUMTYPE_RE = re.compile(r"^enumtype\s+(\w+)\s*=\s*(.+)$", re.MULTILINE)
_ZSTORE_RE = re.compile(
    r"zstore\s+(?P<name>\w+)\s*=(?P<body>.*?)where\s+inv\s*:\s*\"(?P<inv>.*?)\"",
    re.DOTALL,
)
_LENS_RE = re.compile(r"^\s*(\w+)\s*::\s*\"([^\"]+)\"", re.MULTILINE)
_ZOP_RE = re.compile(
    r"zoperation\s+(?P<name>\w+)\s*=(?P<body>.*?)(?=zoperation\s|\ndefinition\s+Init|\Z)",
    re.DOTALL,
)
_PRE_RE = re.compile(r"pre\s+\"(?P<pre>.*?)\"", re.DOTALL)
_UPDATE_RE = re.compile(r"update\s+\"(?P<upd>.*?)\"", re.DOTALL)
_PARAMS_RE = re.compile(r"params\s+(\w+)\s*\\<in>\s*\"(\w+)\"")
_INIT_RE = re.compile(r"definition\s+Init\s*::.*?\"Init\s*=\s*\[(?P<body>.*?)\]\s*\"",
                      re.DOTALL)
_DEF_RE = re.compile(
    r"definition\s+(\w+)\s*::\s*(?:\"[^\"]*\"|[^\"\n]*?)\s*where\s*"
    r"(?:\[[^\]]*\]\s*:?)?\s*\"\s*\w+\s*=\s*(.*?)\"",
    re.DOTALL)
_CONSTS_RE = re.compile(r"^consts\s+(\w+)\s*::\s*\"([^\"]+)\"", re.MULTILINE)


def parse_theory(text: str) -> TheoryModel:
    m = TheoryModel()
    for tm in _ENUMTYPE_RE.finditer(text):
        ctors = [c.strip() for c in tm.group(2).split("|") if c.strip()]
        m.enumtypes[tm.group(1)] = ctors
    zm = _ZSTORE_RE.search(text)
    if zm:
        m.name = zm.group("name")
        for lm in _LENS_RE.finditer(zm.group("body")):
            m.lenses[lm.group(1)] = lm.group(2).strip()
        m.inv_raw = zm.group("inv")
        m.inv_atoms = parse_conjunction(m.inv_raw)
        for a in m.inv_atoms:
            if a.kind == "implies":
                ante = parse_conjunction(a.ante)
                cons = parse_conjunction(a.cons)
                if (len(ante) == 1 and len(cons) == 1
                        and ante[0].kind == "cmp"
                        and ante[0].lhs == ("var", "st") and ante[0].op == "="
                        and ante[0].rhs[0] == "var"
                        and cons[0].kind == "set_eq"):
                    m.pins[ante[0].rhs[1]] = cons[0].set_value
                    m.pin_lens = cons[0].var
    for cm in _CONSTS_RE.finditer(text):
        m.consts[cm.group(1)] = cm.group(2)
    for dm in _DEF_RE.finditer(text):
        m.defs[dm.group(1)] = normalize(dm.group(2))
    im = _INIT_RE.search(text)
    if im:
        body = normalize(im.group("body"))
        for pair in split_top(body, ","):
            if " ~> " in pair:
                k, v = pair.split(" ~> ", 1)
                m.init[k.strip()] = v.strip()
    for om in _ZOP_RE.finditer(text):
        body = om.group("body")
        pm = _PRE_RE.search(body)
        um = _UPDATE_RE.search(body)
        pre_raw = pm.group("pre") if pm else "True"
        atoms = parse_conjunction(pre_raw)
        source = None
        for a in atoms:
            if (a.kind == "cmp" and a.op == "=" and a.lhs == ("var", "st")
                    and a.rhs and a.rhs[0] == "var"):
                source = a.rhs[1]
                break
        target = None
        if um:
            tmatch = re.search(r"st'\s*=\s*(\w+)", normalize(um.group("upd")))
            if tmatch:
                target = tmatch.group(1)
        params = [(p, d) for p, d in _PARAMS_RE.findall(body)]
        m.operations.append(ZOperation(
            name=om.group("name"), pre_raw=pre_raw, atoms=atoms,
            source_state=source, target_state=target, params=params))
    return m


# --- CSP / RCT / Dafny / Java constant extraction --------------------------

_NAMETYPE_RE = re.compile(r"^nametype\s+(\w+)\s*=\s*\{\s*(-?\d+)\s*\.\.\s*(-?\d+)\s*\}",
                          re.MULTILINE)
_STUB_RE = re.compile(r"^(\w+)\(([^)]*)\)\s*=\s*(-?\d+(?:\.\d+)?|\w+)\s*$", re.MULTILINE)
_RCT_CONST_RE = re.compile(r"^\s*const\s+(\w+)\s*:\s*\w+\s*=\s*(-?\d+(?:\.\d+)?)",
                           re.MULTILINE)
_DFY_CONST_RE = re.compile(r"^\s*const\s+(\w+)\s*:\s*\w+\s*:=\s*(-?\d+(?:\.\d+)?)",
                           re.MULTILINE)
_CSP_CONST_RE = re.compile(r"^\s*(const_\w+)\s*=\s*(-?\d+(?:\.\d+)?)\s*$", re.MULTILINE)
_JAVA_CONST_RE = re.compile(
    r"public\s+static\s+final\s+(?:double|float|int|long)\s+(\w+)\s*=\s*"
    r"(-?\d+(?:\.\d+)?)(?:d|D|f|F|l|L)?\s*;")


@dataclass
class Instantiation:
    nametypes: dict[str, tuple[int, int]] = field(default_factory=dict)
    stubs: dict[str, str] = field(default_factory=dict)  # fname -> literal (num or enum str)


_ARITH_STUBS = {"Plus", "Minus", "Mult", "Div", "Modulus", "Neg"}


def parse_instantiations(text: str) -> Instantiation:
    inst = Instantiation()
    for nm in _NAMETYPE_RE.finditer(text):
        inst.nametypes[nm.group(1)] = (int(nm.group(2)), int(nm.group(3)))
    for sm in _STUB_RE.finditer(text):
        if sm.group(1) in _ARITH_STUBS:
            continue
        inst.stubs[sm.group(1)] = sm.group(3)
    return inst


def parse_rct_consts(text: str) -> dict[str, Fraction]:
    return {m.group(1): Fraction(m.group(2)) for m in _RCT_CONST_RE.finditer(text)}


def parse_dafny_consts(text: str) -> dict[str, Fraction]:
    return {m.group(1): Fraction(m.group(2)) for m in _DFY_CONST_RE.finditer(text)}


def parse_csp_consts(text: str) -> dict[str, Fraction]:
    return {m.group(1): Fraction(m.group(2)) for m in _CSP_CONST_RE.finditer(text)}


def parse_java_constants(java_root: Path) -> dict[str, Fraction]:
    out: dict[str, Fraction] = {}
    for f in sorted(java_root.rglob("*.java")):
        try:
            text = f.read_text(encoding="utf-8", errors="replace")
        except OSError:
            continue
        for m in _JAVA_CONST_RE.finditer(text):
            out[m.group(1)] = Fraction(m.group(2))
    return out


def norm_const_name(n: str) -> str:
    return re.sub(r"[^a-z0-9]", "", n.lower())


# ---------------------------------------------------------------------------
# Satisfiability of a conjunction
# ---------------------------------------------------------------------------

@dataclass
class Env:
    """Evaluation context for one tier.

    checked=False: symbolic tier (unbounded numerics, calls uninterpreted).
    checked=True : narrowed numerics over `grid`, calls resolved through
                   `const_map` (instantiation stubs + rct constants); calls
                   absent from the map stay symbolic but grid-narrowed.
    """
    checked: bool = False
    grid: tuple = (Fraction(0), Fraction(1))
    const_map: dict = field(default_factory=dict)   # lowercased fname -> Fraction
    fixed_sets: dict = field(default_factory=dict)  # set lens -> frozenset (pins etc.)
    fixed_st: str | None = None


@dataclass
class SatResult:
    verdict: str
    reason: str = ""
    unknown_atoms: list[str] = field(default_factory=list)


_REL = {"<": {"LT"}, "<=": {"LT", "EQ"}, "=": {"EQ"}, "!=": {"LT", "GT"},
        ">": {"GT"}, ">=": {"GT", "EQ"}}


class _Interval:
    __slots__ = ("lo", "hi", "lo_open", "hi_open", "neq")

    def __init__(self):
        self.lo, self.hi = None, None
        self.lo_open = self.hi_open = False
        self.neq: set[Fraction] = set()

    def constrain(self, op: str, c: Fraction) -> None:
        if op in ("<", "<="):
            if self.hi is None or c < self.hi or (c == self.hi and op == "<"):
                self.hi, self.hi_open = c, (op == "<")
        elif op in (">", ">="):
            if self.lo is None or c > self.lo or (c == self.lo and op == ">"):
                self.lo, self.lo_open = c, (op == ">")
        elif op == "=":
            self.constrain("<=", c)
            self.constrain(">=", c)
        elif op == "!=":
            self.neq.add(c)

    def nonempty_dense(self) -> bool:
        if self.lo is not None and self.hi is not None:
            if self.lo > self.hi:
                return False
            if self.lo == self.hi:
                if self.lo_open or self.hi_open:
                    return False
                return self.lo not in self.neq
        return True  # dense domain: finitely many neqs never empty an interval

    def admits(self, v: Fraction) -> bool:
        if v in self.neq:
            return False
        if self.lo is not None and (v < self.lo or (v == self.lo and self.lo_open)):
            return False
        if self.hi is not None and (v > self.hi or (v == self.hi and self.hi_open)):
            return False
        return True

    def nonempty_grid(self, grid) -> bool:
        return any(self.admits(v) for v in grid)


def _term_key(t: tuple) -> str:
    if t[0] == "var":
        return t[1]
    if t[0] == "call":
        return f"{t[1]}({t[2]})"
    return str(t)


def sat_conjunction(atoms: list[Atom], model: TheoryModel, env: Env) -> SatResult:
    unknown: list[str] = []
    bool_pos: set[str] = set()
    bool_neg: set[str] = set()
    enum_dom: dict[str, set[str]] = {}
    intervals: dict[str, _Interval] = {}
    sym_grid_narrow: set[str] = set()
    pair_rel: dict[tuple[str, str], set] = {}
    member_req: dict[str, set[str]] = {}
    member_forb: dict[str, set[str]] = {}
    subset_targets: dict[str, list[frozenset]] = {}

    def enum_domain_of(var: str) -> set[str] | None:
        t = model.lenses.get(var)
        if var == "st" and "St" in model.enumtypes:
            return set(model.enumtypes["St"])
        if t in model.enumtypes:
            return set(model.enumtypes[t])
        return None

    # Pass 1 — pin st, so set lenses can be resolved via invariant pins.
    st_val = env.fixed_st
    for a in atoms:
        if (a.kind == "cmp" and a.op == "=" and a.lhs == ("var", "st")
                and a.rhs and a.rhs[0] == "var"):
            v = a.rhs[1]
            if st_val is not None and st_val != v:
                return SatResult(UNSAT, f"st pinned to both {st_val} and {v}")
            st_val = v
    fixed_sets = dict(env.fixed_sets)
    if st_val is not None and model.pin_lens and model.pin_lens not in fixed_sets:
        if st_val in model.pins:
            fixed_sets[model.pin_lens] = model.pins[st_val]

    def resolve_num(t: tuple):
        """-> ("const", Fraction) | ("sym", key)"""
        if t[0] == "num":
            return ("const", t[1])
        if t[0] == "call":
            if env.checked:
                v = env.const_map.get(t[1].lower())
                if v is not None:
                    return ("const", v)
                sym_grid_narrow.add(_term_key(t))
            return ("sym", _term_key(t))
        if t[0] == "var":
            kind = model.lens_kind(t[1])
            if kind == "numeric" and env.checked:
                sym_grid_narrow.add(t[1])
            return ("sym", t[1])
        return ("sym", _term_key(t))

    for a in atoms:
        if a.kind == "true":
            continue
        if a.kind == "unknown":
            unknown.append(a.raw)
            continue
        if a.kind == "implies":
            # inside a precondition SAT query an implication is satisfiable
            # whenever its antecedent can be false — decide cheaply: if the
            # antecedent is itself UNSAT the implication is True; otherwise
            # the atom does not, alone, constrain anything we track → treat
            # as unknown (conservative: caps verdict at UNKNOWN, never SAT).
            ante_res = sat_conjunction(parse_conjunction(a.ante), model, env)
            if ante_res.verdict != UNSAT:
                unknown.append(a.raw)
            continue
        if a.kind == "bool":
            (bool_pos if a.positive else bool_neg).add(a.var)
            continue
        if a.kind == "neq_empty_list":
            continue  # trace lens: `tr != []` — free lens, satisfiable
        if a.kind == "member":
            sval = fixed_sets.get(a.var)
            if sval is not None:
                inside = a.value in sval
                if inside != a.positive:
                    return SatResult(
                        UNSAT,
                        f"`{a.raw}` is false: {a.var} is pinned to "
                        f"{{{', '.join(sorted(sval))}}} here")
                continue
            if model.lens_kind(a.var) != "set" and a.var not in ("offered", "listens", "triggers"):
                unknown.append(a.raw)
                continue
            (member_req if a.positive else member_forb).setdefault(a.var, set()).add(a.value)
            continue
        if a.kind == "subset":
            lhs_val = fixed_sets.get(a.var)
            rhs_val = fixed_sets.get(a.value)
            if lhs_val is not None and rhs_val is not None:
                if not lhs_val <= rhs_val:
                    return SatResult(UNSAT, f"`{a.raw}` is false under pins")
                continue
            if rhs_val is not None:
                subset_targets.setdefault(a.var, []).append(rhs_val)
                continue
            unknown.append(a.raw)
            continue
        if a.kind == "set_eq":
            sval = fixed_sets.get(a.var)
            if sval is not None:
                if sval != a.set_value:
                    return SatResult(UNSAT, f"`{a.raw}` contradicts pinned {a.var}")
                continue
            fixed_sets[a.var] = a.set_value
            continue
        if a.kind == "cmp":
            # enum equality/disequality?
            lhs_enum = a.lhs[0] == "var" and enum_domain_of(a.lhs[1]) is not None
            rhs_ctor = a.rhs[0] == "var" and model.ctor_owner(a.rhs[1]) is not None
            if lhs_enum or (a.lhs[0] == "var" and rhs_ctor):
                if a.rhs[0] != "var":
                    unknown.append(a.raw)
                    continue
                var, val = a.lhs[1], a.rhs[1]
                dom = enum_domain_of(var) or set(
                    model.enumtypes.get(model.ctor_owner(val) or "", []))
                if val not in dom:
                    return SatResult(UNSAT,
                                     f"`{a.raw}`: {val} not a value of {var}'s type")
                cur = enum_dom.setdefault(var, set(dom))
                if a.op == "=":
                    cur &= {val}
                elif a.op == "!=":
                    cur -= {val}
                else:
                    unknown.append(a.raw)
                    continue
                enum_dom[var] = cur
                if not cur:
                    return SatResult(UNSAT,
                                     f"enum lens `{var}` has no admissible value left "
                                     f"(last atom: `{a.raw}`)")
                continue
            lt, rt = resolve_num(a.lhs), resolve_num(a.rhs)
            if lt[0] == "const" and rt[0] == "const":
                ok = {"<": lt[1] < rt[1], "<=": lt[1] <= rt[1], "=": lt[1] == rt[1],
                      "!=": lt[1] != rt[1], ">": lt[1] > rt[1], ">=": lt[1] >= rt[1]}[a.op]
                if not ok:
                    return SatResult(UNSAT,
                                     f"`{a.raw}` evaluates to {lt[1]} {a.op} {rt[1]} = False")
                continue
            if lt[0] == "sym" and rt[0] == "const":
                intervals.setdefault(lt[1], _Interval()).constrain(a.op, rt[1])
                continue
            if lt[0] == "const" and rt[0] == "sym":
                flip = {"<": ">", "<=": ">=", ">": "<", ">=": "<=", "=": "=", "!=": "!="}
                intervals.setdefault(rt[1], _Interval()).constrain(flip[a.op], lt[1])
                continue
            # sym vs sym — pairwise relation intersection
            k1, k2 = lt[1], rt[1]
            key, rel = ((k1, k2), _REL[a.op]) if k1 <= k2 else (
                (k2, k1), {"LT": "GT", "GT": "LT", "EQ": "EQ"} and
                {r if r == "EQ" else ("GT" if r == "LT" else "LT") for r in _REL[a.op]})
            cur = pair_rel.setdefault(key, {"LT", "EQ", "GT"})
            cur &= rel
            if not cur:
                return SatResult(UNSAT, f"contradictory relations on ({key[0]}, {key[1]})"
                                        f" (last atom: `{a.raw}`)")
            pair_rel[key] = cur
            continue
        unknown.append(a.raw)

    clash = bool_pos & bool_neg
    if clash:
        return SatResult(UNSAT, f"boolean lens(es) both asserted and negated: "
                                f"{', '.join(sorted(clash))}")
    for var, req in member_req.items():
        forb = member_forb.get(var, set())
        both = req & forb
        if both:
            return SatResult(UNSAT, f"event(s) required in and excluded from `{var}`: "
                                    f"{', '.join(sorted(both))}")
        for tgt in subset_targets.get(var, []):
            if not req <= tgt:
                return SatResult(
                    UNSAT,
                    f"`{var}` must contain {{{', '.join(sorted(req - tgt))}}} but is "
                    f"constrained to a subset of {{{', '.join(sorted(tgt))}}}")
    for key, iv in intervals.items():
        if env.checked and key in sym_grid_narrow:
            if not iv.nonempty_grid(env.grid):
                lo = min(env.grid)
                hi = max(env.grid)
                return SatResult(UNSAT,
                                 f"no value of `{key}` in the checked domain "
                                 f"[{lo}..{hi}] satisfies its constraints")
            continue
        if not iv.nonempty_dense():
            return SatResult(UNSAT, f"numeric constraints on `{key}` are contradictory")
    # pairwise relations: naive per-pair check done above; combined with each
    # term's interval on the grid in checked mode:
    if env.checked:
        for (k1, k2), rel in pair_rel.items():
            iv1 = intervals.get(k1, _Interval())
            iv2 = intervals.get(k2, _Interval())
            g1 = env.grid if k1 in sym_grid_narrow else None
            g2 = env.grid if k2 in sym_grid_narrow else None
            if g1 is None and g2 is None:
                continue
            cand1 = [v for v in (g1 or env.grid) if iv1.admits(v)] if g1 else None
            cand2 = [v for v in (g2 or env.grid) if iv2.admits(v)] if g2 else None
            if cand1 is not None and cand2 is not None:
                ok = any(
                    (("LT" in rel and v1 < v2) or ("EQ" in rel and v1 == v2)
                     or ("GT" in rel and v1 > v2))
                    for v1, v2 in product(cand1, cand2))
                if not ok:
                    return SatResult(UNSAT,
                                     f"no grid pair ({k1}, {k2}) satisfies the "
                                     f"required relation(s) {sorted(rel)}")
    if unknown:
        return SatResult(UNKNOWN, "atoms outside the decidable fragment",
                         unknown_atoms=unknown)
    return SatResult(SAT)


# ---------------------------------------------------------------------------
# Reachability
# ---------------------------------------------------------------------------

@dataclass
class ReachResult:
    reachable: set[str]
    op_fireable: dict[str, str]      # op name -> SAT/UNSAT/UNKNOWN at its source
    op_fire_reason: dict[str, str]


def op_sat_at(op: ZOperation, state: str, model: TheoryModel, env: Env,
              fixed_offered: frozenset | None = None) -> SatResult:
    if op.source_state is not None and op.source_state != state:
        return SatResult(UNSAT, "source state mismatch")
    for _, dom in op.params:
        rhs = model.defs.get(dom, "")
        if rhs.strip() == "{}":
            return SatResult(UNSAT, f"parameter domain `{dom}` is empty")
    e = Env(checked=env.checked, grid=env.grid, const_map=env.const_map,
            fixed_sets=dict(env.fixed_sets), fixed_st=state)
    if fixed_offered is not None:
        e.fixed_sets["offered"] = fixed_offered
    return sat_conjunction(op.atoms, model, e)


def reachability(model: TheoryModel, env: Env) -> ReachResult:
    init_st = model.init.get("st")
    reachable: set[str] = set()
    if init_st:
        reachable.add(init_st)
    changed = True
    # Optimistic fixpoint: UNKNOWN preconditions are treated as fireable so
    # that "unreachable" claims stay sound.
    while changed:
        changed = False
        for op in model.operations:
            if op.target_state is None or op.target_state in reachable:
                continue
            sources = [op.source_state] if op.source_state else list(reachable)
            for s in sources:
                if s not in reachable:
                    continue
                if op_sat_at(op, s, model, env).verdict in (SAT, UNKNOWN):
                    reachable.add(op.target_state)
                    changed = True
                    break
    fireable: dict[str, str] = {}
    reasons: dict[str, str] = {}
    for op in model.operations:
        src = op.source_state
        if src is not None and src not in reachable:
            fireable[op.name] = UNSAT
            reasons[op.name] = f"source mode {src} is unreachable"
            continue
        candidates = [src] if src else sorted(reachable)
        best = UNSAT
        why = ""
        for s in candidates:
            r = op_sat_at(op, s, model, env)
            if r.verdict == SAT:
                best, why = SAT, ""
                break
            if r.verdict == UNKNOWN and best == UNSAT:
                best, why = UNKNOWN, r.reason
            elif best == UNSAT:
                why = r.reason
        fireable[op.name] = best
        reasons[op.name] = why
    return ReachResult(reachable=reachable, op_fireable=fireable,
                       op_fire_reason=reasons)


def dead_offered_pairs(model: TheoryModel, reachable: set[str], env: Env,
                       max_events: int = 8) -> list[tuple[str, frozenset]]:
    """(state, offered) pairs where NO operation can fire. Post-split only."""
    if "offered" not in model.lenses:
        return []
    dead: list[tuple[str, frozenset]] = []
    for state in sorted(reachable):
        if state == model.init.get("st"):
            pass  # initial pseudo-state participates too
        listens = model.pins.get(state, frozenset())
        events = sorted(listens)[:max_events]
        subsets = [frozenset(c)
                   for r in range(len(events) + 1)
                   for c in _combos(events, r)]
        ops_here = [op for op in model.operations
                    if op.source_state in (state, None)]
        for off in subsets:
            fired = any(
                op_sat_at(op, state, model, env, fixed_offered=off).verdict
                in (SAT, UNKNOWN)
                for op in ops_here)
            if not fired:
                dead.append((state, off))
    return dead


def _combos(items, r):
    from itertools import combinations
    return combinations(items, r)


# ---------------------------------------------------------------------------
# Checked-instantiation environment assembly
# ---------------------------------------------------------------------------

def load_type_ranges(path: Path | None) -> tuple:
    lo, hi = 0, 1
    if path and path.exists():
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
            ent = data.get("real") or data.get("int") or {}
            lo = int(ent.get("lower", 0))
            hi = int(ent.get("upper", 1))
        except (ValueError, OSError):
            pass
    return tuple(Fraction(v) for v in range(lo, hi + 1))


def build_checked_env(inst: Instantiation | None,
                      rct_consts: dict[str, Fraction] | None,
                      grid: tuple) -> Env:
    const_map: dict[str, Fraction] = {}
    if rct_consts:
        for k, v in rct_consts.items():
            const_map[k.lower()] = v
    if inst:
        for k, v in inst.stubs.items():
            if _NUM_RE.match(v):
                const_map[k.lower()] = Fraction(v)
    return Env(checked=True, grid=grid, const_map=const_map)
