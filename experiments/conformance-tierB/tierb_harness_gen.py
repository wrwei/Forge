"""Generalised Tier-B guard-level conformance enumeration.

ONE enumeration loop, ONE agreement definition, ONE Java side; the only arm
difference is how the *triggering event* is recovered from the theory:

  arm='prefix'  the pre-fix encoding puts no event in `pre`. The published
                harness recovered it from the trace update `[Event e]`, and
                ONLY for operations whose precondition is a bare `st= X`
                (operations that carry a real guard were treated as
                event-independent). Reproduced verbatim here.
  arm='fixed'   the post-fix encoding puts the event in `pre` as
                `e \\<in> offered` / `\\<not>(e \\<in> offered)`, with
                `offered \\<subseteq> listens` and the per-state `listens` pin
                in the zstore invariant. Tier-B instantiation (same as the
                archived harness's reading of `triggers`):
                offered := {event} if event in listens(state) else {}.

Agreement (identical in both arms): let ADM be the set of target states of the
enabled operations, or {mode} when none is enabled; the row agrees iff
ADM == {java_next}.
"""
import re
import itertools
from collections import OrderedDict

NOT = '\\<not>'
AND = '\\<and>'
IN = '\\<in>'


# ---------------------------------------------------------------- theory side

def parse_thy(path):
    txt = open(path).read()
    inv = re.search(r'where inv:\s*"(.*?)"', txt, re.S)
    listens = {}
    if inv:
        for st, _lens, body in re.findall(
                r'\(st = (\w+) \\<longrightarrow> (triggers|listens) = \{(.*?)\}\)', inv.group(1)):
            listens[st] = {x.strip() for x in body.split(',') if x.strip()}
    ops = []
    for b in re.split(r'\nzoperation\s+', txt)[1:]:
        name = b.split('=')[0].strip()
        m_pre = re.search(r'\n\s*pre\s+"(.*?)"\s*\n', b, re.S)
        m_up = re.search(r'update\s+"\[(.*?)\]"', b, re.S)
        m_tgt = re.search(r'st\\<Zprime>\s*=\s*(\w+)', b)
        if not m_pre or not m_tgt:
            continue
        pre = ' '.join(m_pre.group(1).split())
        conj = [c.strip() for c in pre.split(AND)]
        state = None
        atoms, presence, absence = [], [], []
        for c in conj:
            m = re.match(r'^st\s*=\s*(\w+)$', c)
            if m:
                state = m.group(1); continue
            if re.match(r'^(offered|triggers|listens)\s*\\<subseteq>\s*(offered|triggers|listens)$', c):
                continue
            m = re.match(r'^(\w+)\s*' + re.escape(IN) + r'\s*(?:offered|triggers)$', c)
            if m:
                presence.append(m.group(1)); continue
            m = re.match(r'^' + re.escape(NOT) + r'\s*\(\s*(\w+)\s*' + re.escape(IN)
                         + r'\s*(?:offered|triggers)\s*\)$', c)
            if m:
                absence.append(m.group(1)); continue
            atoms.append(c)
        trace_ev = re.findall(r'\[Event (\w+)\]', m_up.group(1)) if m_up else []
        ops.append(dict(name=name, state=state, atoms=atoms, presence=presence,
                        absence=absence, target=m_tgt.group(1), trace=trace_ev,
                        pre=pre))
    return ops, listens


def eval_atom(a, g, atom_table, unknown):
    neg = False
    if a.startswith(NOT):
        neg, a = True, a[len(NOT):].strip()
    if a in atom_table:
        v = bool(atom_table[a](g))
        return (not v) if neg else v
    unknown.add(a)
    return None


def model_admits(ops, listens, mode, g, ev_thy, cfg, arm, unknown):
    """Return list of enabled ops for (mode, g, event)."""
    inputs = cfg['input_events']
    out = []
    for op in ops:
        if op['state'] != mode:
            continue
        ok = True
        for a in op['atoms']:
            v = eval_atom(a, g, cfg['atoms'], unknown)
            if v is None:
                continue
            if not v:
                ok = False; break
        if not ok:
            continue
        if arm == 'prefix':
            # Published recovery: the pre-fix `pre` carries no event, so the
            # trigger is read off the trace update, and ONLY for operations
            # whose precondition is a bare `st = X` (an operation carrying a
            # real guard is guard-only, hence event-independent).  An operation
            # with a bare pre and no *input* event in its trace (only output
            # events, or none) is likewise ungated: its precondition is
            # literally `st = X`, true for every event.  This is exactly the
            # published CD_TRIG / SR_TRIG / ISO_EVENT membership test.
            req = None
            if not op['atoms']:
                trace_in = [e for e in op['trace'] if e in inputs]
                req = trace_in[0] if trace_in else None
            if req is not None and req != ev_thy:
                continue
        else:
            lset = listens.get(mode, set())
            offered = {ev_thy} if (ev_thy is not None and ev_thy in lset) else set()
            if any(e not in offered for e in op['presence']):
                continue
            if any(e in offered for e in op['absence']):
                continue
        out.append(op)
    return out


# ------------------------------------------------------------------ Java side

def match_block(s, i):
    assert s[i] == "{"
    d = 0
    for j in range(i, len(s)):
        if s[j] == "{":
            d += 1
        elif s[j] == "}":
            d -= 1
            if d == 0:
                return s[i + 1:j], j + 1
    raise ValueError("unbalanced")


def branches(block):
    out, k, n = [], 0, len(block)
    while k < n:
        m = re.compile(r"\b(if)\s*\(").search(block, k)
        if not m:
            break
        p = m.end() - 1
        d = 0
        for j in range(p, n):
            if block[j] == "(":
                d += 1
            elif block[j] == ")":
                d -= 1
                if d == 0:
                    break
        cond = block[p + 1:j]
        b = block.index("{", j)
        inner, after = match_block(block, b)
        out.append((" ".join(cond.split()), inner))
        k = after
    return out


def lre_java_chains(java_path, mode_enum="LreMode"):
    """Parse LreController.step()'s mode-nested if-else chain (published code)."""
    java = open(java_path).read()
    body = java[java.index("public void step(InputEvent event)"):]
    body = body[:body.index("\n    }\n")]
    chain = body[body.index("if (currentMode == %s.OCM)" % mode_enum):]
    modes = {}
    for cond, blk in branches(chain):
        m = re.match(r"currentMode == %s\.(\w+)$" % mode_enum, cond.strip())
        if m:
            modes[m.group(1)] = branches(blk)
    return modes


def lre_java_next(modes, mode, g, ev, preds):
    for cond, blk in modes[mode]:
        e = cond
        e = re.sub(r"event instanceof InputEvent\.(\w+)",
                   lambda m: str(ev == m.group(1)), e)
        for p in preds:
            e = re.sub(r"\b%s\b" % p, str(g[p]), e)
        e = e.replace("&&", " and ").replace("!", " not ")
        if eval(e, {"__builtins__": {}}, {"True": True, "False": False}):
            nx = re.findall(r"currentMode = %s\.(\w+)" % "LreMode", blk)
            return (nx[0] if nx else mode)
    return mode


def sr_java_next(mode, p, ev):
    if mode == "Moving":
        if ev == "endTask":
            return "Final"
        if ev == "obstacle" and p["obstacleDetected"]:
            return "Turning"
        return "Moving"
    if mode == "Turning":
        if p["turnDurationElapsed"]:
            return "Moving"
        if ev == "endTask":
            return "Final"
        return "Turning"
    return "Final"


def cd_java_next(mode, p, ev):
    if mode == "Reading":
        return "Analysis" if ev == "gas" else "Reading"
    if mode == "Analysis":
        return "GasDetected" if p["stsIsGasD"] else "NoGas"
    if mode == "NoGas":
        return "Reading"
    if mode == "GasDetected":
        return "Final" if p["insAboveThr"] else "Reading"
    return "Final"
