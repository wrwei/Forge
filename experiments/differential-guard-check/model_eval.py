#!/usr/bin/env python3
"""Interpreter for the guard and update expressions of an extracted Isabelle/UTP theory, in
exact real arithmetic.

Values are `Real`: an exact Fraction, or the square root of a non-square Fraction (kept
symbolically so that sqrt(a)*sqrt(a) == a), or, after mixing a root into further arithmetic,
a 200-digit Decimal approximation flagged `approx`. Division by zero is 0 (Isabelle/HOL).
A comparison between approximations that differ by less than 1e-150 raises Undecided."""
import re
from decimal import Decimal, getcontext
from fractions import Fraction
from math import isqrt

getcontext().prec = 200
EPS = Decimal("1e-150")


class Undecided(Exception):
    pass


def _isq(n):
    r = isqrt(n); return r if r * r == n else None


class Real:
    __slots__ = ("q", "rad", "dec")      # exactly one of: q (Fraction); rad (sqrt of Fraction); dec (approx)

    def __init__(self, q=None, rad=None, dec=None):
        self.q, self.rad, self.dec = q, rad, dec

    @staticmethod
    def of(x):
        if isinstance(x, Real): return x
        if isinstance(x, bool): raise TypeError("bool is not a real")
        if isinstance(x, (int, Fraction)): return Real(q=Fraction(x))
        if isinstance(x, float): return Real(q=Fraction(x))          # exact value of the double
        if isinstance(x, str): return Real(q=Fraction(x))
        raise TypeError(type(x))

    def approx(self):
        if self.q is not None: return Decimal(self.q.numerator) / Decimal(self.q.denominator)
        if self.rad is not None: return (Decimal(self.rad.numerator) / Decimal(self.rad.denominator)).sqrt()
        return self.dec

    @property
    def exact(self): return self.dec is None

    def _bin(self, o, f, fd):
        o = Real.of(o)
        if self.q is not None and o.q is not None: return Real(q=f(self.q, o.q))
        return Real(dec=fd(self.approx(), o.approx()))

    def __add__(self, o): return self._bin(o, lambda a, b: a + b, lambda a, b: a + b)
    __radd__ = __add__
    def __sub__(self, o): return self._bin(o, lambda a, b: a - b, lambda a, b: a - b)
    def __rsub__(self, o): return Real.of(o).__sub__(self)

    def __mul__(self, o):
        o = Real.of(o)
        if self.rad is not None and o.rad is not None and self.rad == o.rad: return Real(q=self.rad)
        if self.rad is not None and o.q is not None and o.q == 0: return Real(q=Fraction(0))
        if o.rad is not None and self.q is not None and self.q == 0: return Real(q=Fraction(0))
        return self._bin(o, lambda a, b: a * b, lambda a, b: a * b)
    __rmul__ = __mul__

    def __truediv__(self, o):
        o = Real.of(o)
        if (o.q is not None and o.q == 0): return Real(q=Fraction(0))            # HOL: x / 0 = 0
        if o.dec is not None and o.dec == 0: return Real(q=Fraction(0))
        return self._bin(o, lambda a, b: a / b, lambda a, b: a / b)

    def __neg__(self):
        if self.q is not None: return Real(q=-self.q)
        return Real(dec=-self.approx())

    def _cmp(self, o):
        o = Real.of(o)
        if self.q is not None and o.q is not None: return (self.q > o.q) - (self.q < o.q)
        if self.rad is not None and o.rad is not None: return (self.rad > o.rad) - (self.rad < o.rad)
        # sqrt(r) vs q with q exact: compare r with q^2 when q >= 0
        if self.rad is not None and o.q is not None:
            if o.q < 0: return 1
            return (self.rad > o.q * o.q) - (self.rad < o.q * o.q)
        if o.rad is not None and self.q is not None: return -o._cmp(self)
        d = self.approx() - o.approx()
        if abs(d) < EPS: raise Undecided(f"{self.approx():.30e} vs {o.approx():.30e}")
        return 1 if d > 0 else -1

    def __lt__(self, o): return self._cmp(o) < 0
    def __le__(self, o): return self._cmp(o) <= 0
    def __gt__(self, o): return self._cmp(o) > 0
    def __ge__(self, o): return self._cmp(o) >= 0
    def __eq__(self, o):
        if not isinstance(o, (Real, int, float, Fraction)) or isinstance(o, bool): return False
        return self._cmp(o) == 0
    def __ne__(self, o): return not self.__eq__(o)
    __hash__ = None
    def __repr__(self):
        if self.q is not None: return f"{float(self.q)!r}" if self.q.denominator != 1 or abs(self.q) < 10**18 else f"{self.q:.3e}"
        return f"~{float(self.approx())!r}"


def r_sqrt(x):
    x = Real.of(x)
    if x.q is not None:
        if x.q < 0: return Real(q=Fraction(0))          # sqrt of a negative: unspecified in HOL; never reached here
        a, b = _isq(x.q.numerator), _isq(x.q.denominator)
        if a is not None and b is not None: return Real(q=Fraction(a, b))
        return Real(rad=x.q)
    return Real(dec=x.approx().sqrt())


# ---------------------------------------------------------------- theory parsing

SYM = [(r"\<le>", " <= "), (r"\<ge>", " >= "), (r"\<and>", " and "), (r"\<or>", " or "),
       (r"\<not>", " not "), (r"\<noteq>", " != "), (r"\<in>", " in "), (r"\<subseteq>", " <= "),
       (r"\<longrightarrow>", " IMPLIES ")]


def to_python(expr):
    s = " ".join(expr.split())
    for a, b in SYM: s = s.replace(a, b)
    s = re.sub(r"(?<![<>=!])=(?!=)", "==", s)                         # HOL equality
    s = re.sub(r"\b(\w+)\(\s*\)", r"CALL('\1')", s)                  # nullary call f()
    s = re.sub(r"\b(?!CALL\b)([A-Za-z_]\w*)\(", r"CALLN('\1', ", s)   # f(args)
    s = re.sub(r"(?<![\w.'])(\d+\.\d+|\d+)(?![\w.])", r"NUM('\1')", s)
    return s


def parse_theory(path):
    txt = open(path).read()
    defs = {m.group(1): m.group(2) for m in re.finditer(
        r'definition\s+(\w+)\s*::\s*"unit\s*\\<Rightarrow>\s*\w+"\s*where\s*"\w+\s+u\s*=\s*([^"]+)"', txt)}
    listens = {}
    inv = re.search(r'where inv:\s*"(.*?)"', txt, re.S)
    if inv:
        for st, body in re.findall(r'\(st = (\w+) \\<longrightarrow> listens = \{(.*?)\}\)', inv.group(1)):
            listens[st] = {x.strip() for x in body.split(",") if x.strip()}
    ops = []
    for b in re.split(r"\nzoperation\s+", txt)[1:]:
        name = b.split("=")[0].strip()
        m_pre = re.search(r'\n\s*pre\s+"(.*?)"\s*\n', b, re.S)
        m_up = re.search(r'update\s+"\[(.*?)\]"', b, re.S)
        if not m_pre or not m_up: continue
        ups = {}
        for part in re.split(r"\n\s*,", "\n," + m_up.group(1).strip()):
            part = " ".join(part.split())
            m = re.match(r"(\w+)\\<Zprime>\s*=\s*(.*)$", part)
            if m: ups[m.group(1)] = m.group(2)
        tgt = ups.get("st", "").strip()
        ops.append(dict(name=name, pre=" ".join(m_pre.group(1).split()), target=tgt, updates=ups))
    return dict(defs=defs, listens=listens, ops=ops)


def refreshed_vars(th):
    """Variables every non-initial operation assigns with the same expression."""
    triv = ("st", "tr", "listens", "offered")
    ops = [o for o in th["ops"] if "initial" not in o["pre"] and any(k not in triv for k in o["updates"])]
    common = None
    for o in ops:
        u = {k: v for k, v in o["updates"].items() if k not in ("st", "tr", "listens", "offered")}
        common = dict(u) if common is None else {k: v for k, v in common.items() if u.get(k) == v}
    return common or {}


_CACHE = {}


class Env:
    def __init__(self, th, funcs, state, enums=()):
        self.th, self.funcs, self.state, self.enums = th, funcs, dict(state), set(enums)

    def CALL(self, name):
        if name in self.th["defs"]: return self.eval(self.th["defs"][name])
        if name in self.funcs: return self.funcs[name]
        raise KeyError(f"no value for nullary function {name}()")

    def CALLN(self, name, *args):
        if name == "sqrt": return r_sqrt(args[0])
        f = self.funcs.get(name)
        if f is None: raise KeyError(f"no value for function {name}")
        return f(*args) if callable(f) else f[args[0] if len(args) == 1 else tuple(args)]

    def eval(self, expr, extra=None):
        ns = {"CALL": self.CALL, "CALLN": self.CALLN, "NUM": lambda s: Real.of(s),
              "True": True, "False": False}
        ns.update({e: e for e in self.enums})
        ns.update(self.state)
        if extra: ns.update(extra)
        code = _CACHE.get(expr)
        if code is None:
            code = _CACHE[expr] = compile(to_python(expr), "<thy>", "eval")
        return eval(code, {"__builtins__": {}}, ns)


def admitted(th, env, mode, offered_event):
    """Target modes of the operations enabled in `mode` with `offered_event` (None = no event)."""
    lset = th["listens"].get(mode, set())
    offered = {offered_event} if offered_event is not None and offered_event in lset else set()
    out = []
    for op in th["ops"]:
        extra = {"st": None, "offered": offered, "listens": lset}
        # every identifier that is an event name evaluates to itself
        for evs in th["listens"].values():
            for e in evs: extra.setdefault(e, e)
        if offered_event is not None: extra.setdefault(offered_event, offered_event)
        extra["st"] = mode
        pre = op["pre"]
        # mode names appear as bare identifiers: bind them to themselves
        for m in re.findall(r"st\s*=\s*(\w+)", pre): extra.setdefault(m, m)
        if not re.search(r"st\s*=\s*" + re.escape(mode) + r"\b", pre): continue
        if env.eval(pre, extra): out.append(op["target"])
    return out


# ---------------------------------------------------------------- IEEE double evaluation
# The same expressions evaluated in Java's double arithmetic (Python floats are IEEE 754
# doubles; math.sqrt is correctly rounded like Java's Math.sqrt). Used to attribute a
# disagreement: if the theory evaluated in double arithmetic agrees with the Java, the
# disagreement comes from real versus floating-point arithmetic, not from the translation.
import math as _m


class D(float):
    @staticmethod
    def of(x):
        if isinstance(x, Real): return D(float(x.approx()) if x.dec is not None or x.rad is not None else float(x.q))
        return D(float(x))
    def __add__(s, o): return D(float(s) + float(o))
    __radd__ = __add__
    def __sub__(s, o): return D(float(s) - float(o))
    def __rsub__(s, o): return D(float(o) - float(s))
    def __mul__(s, o): return D(float(s) * float(o))
    __rmul__ = __mul__
    def __truediv__(s, o):
        a, b = float(s), float(o)
        if b == 0.0:
            if a == 0.0 or a != a: return D(float("nan"))
            return D(_m.copysign(float("inf"), a) * (_m.copysign(1.0, b)))
        return D(a / b)
    def __neg__(s): return D(-float(s))


def d_sqrt(x):
    x = float(x)
    return D(float("nan")) if x < 0 else D(_m.sqrt(x))


def as_double_env(env):
    """A copy of `env` whose numbers are IEEE doubles."""
    funcs = {}
    for k, v in env.funcs.items():
        if callable(v): funcs[k] = (lambda f: (lambda *a: D.of(f(*[Real.of(int(float(x))) if float(x).is_integer() else Real.of(float(x)) for x in a]))))(v)
        elif isinstance(v, Real): funcs[k] = D.of(v)
        else: funcs[k] = v
    state = {k: (D.of(v) if isinstance(v, Real) else v) for k, v in env.state.items()}
    e = Env(env.th, funcs, state, env.enums); e.double = True
    return e


_orig_calln = Env.CALLN
def _calln(self, name, *args):
    if getattr(self, "double", False) and name == "sqrt": return d_sqrt(args[0])
    return _orig_calln(self, name, *args)
Env.CALLN = _calln

_orig_eval = Env.eval
def _eval(self, expr, extra=None):
    if getattr(self, "double", False):
        ns = {"CALL": self.CALL, "CALLN": self.CALLN, "NUM": lambda s: D(float(s)), "True": True, "False": False}
        ns.update({e: e for e in self.enums}); ns.update(self.state)
        if extra: ns.update(extra)
        code = _CACHE.get(expr)
        if code is None: code = _CACHE[expr] = compile(to_python(expr), "<thy>", "eval")
        return eval(code, {"__builtins__": {}}, ns)
    return _orig_eval(self, expr, extra)
Env.eval = _eval


def refresh(env, rv):
    """Evaluate the refreshed variables, in whatever order their dependencies allow."""
    todo = dict(rv); state = dict(env.state)
    while todo:
        done = False
        for k, ex in list(todo.items()):
            try:
                env.state = state; state[k] = env.eval(ex); del todo[k]; done = True
            except NameError:
                continue
        if not done: raise RuntimeError(f"cannot order refreshed variables {sorted(todo)}")
    env.state = state
    return state
