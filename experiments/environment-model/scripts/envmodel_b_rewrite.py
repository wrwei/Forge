#!/usr/bin/env python3
"""Rewrite a generated Z-machine theory from environment model A to model B.

Model A (generator of 2026-09-16):   inv  ... /\ offered = listens
Model B (pre-registered 2026-10-18), encoding B3 (2026-10-20):
    B3 = B2 + Idle_X in priority-chain form: not(G_1) /\ ... /\ not(G_n) over the
    operations' RAW guards (C1 negations stripped), which is logically the same
    'else' branch but gives the totality goal polynomial instead of exponential
    branching (LRE MOM: 8 ops, CNF >= 179,200 clauses in complement form).
    Encoding B2 (2026-10-20):
    inv ... /\ (st = X --> (offered = {} \/ offered = {e1} \/ ...))   per state X,
            with e1.. the events X listens for (unpinned states: offered = {}).
    B2 states the same model as a per-state literal disjunction instead of
    `offered <= listens /\ (offered = {} \/ EX e. offered = {e})`: the residual
    totality goals of probe run 3 were valid but metis/blast could not derive
    the case split from the subset+existential form (see results/).
   + every operation takes a parameter `nxt` drawn from
       {{}} U {{e} | e in listens(target)}   and sets  offered' = nxt
   + one Idle_X operation per state X: the Java `else` branch.

Usage: envmodel_b_rewrite.py IN.thy OUTDIR
Writes OUTDIR/<Name>_B_{NoDlf,M,Au,Bl}.thy and a ROOT with one session per arm.
Pure text transformation; prints a self-check summary.
"""
import re, sys, os

OP_RE = re.compile(
    r'(zoperation (\w+) =\s*\n\s*over (\w+)\s*\n)'          # 1 head, 2 name, 3 machine
    r'((?:\s*params [^\n]*\n)?)'                             # 4 params line (optional)
    r'(\s*pre "([^"]*)"\s*\n)'                               # 5 pre line, 6 pre text
    r'(\s*update "\[)(.*?)(\]"\s*\n)', re.S)                 # 7 upd open, 8 upd body, 9 upd close

PIN_RE = re.compile(r"\(st = (\w+) \\<longrightarrow> listens = \{([^}]*)\}\)")
LISTENS_UPD_RE = re.compile(r",listens\\<Zprime> = \{([^}]*)\}")
OFFERED_UPD_RE = re.compile(r"\s*,offered\\<Zprime> = \{[^}]*\}")

def evset(s):
    return [e.strip() for e in s.split(",") if e.strip()]

def nxt_domain(evs):
    return "{{}" + "".join(", {" + e + "}" for e in evs) + "}"

def split_top(sx):
    parts, depth, cur = [], 0, ""
    for ch in sx:
        depth += (ch == "("); depth -= (ch == ")"); cur += ch
        if depth == 0 and cur.endswith("\\<and>"): parts.append(cur[:-6]); cur = ""
    parts.append(cur)
    return [x.strip() for x in parts if x.strip()]

def neg_of(c):
    """the conjunct c written as a negation -> its positive body, else None"""
    m = re.fullmatch(r"\\<not>\s*\((.*)\)", c, re.S)
    if m: return m.group(1).strip()
    m = re.fullmatch(r"\\<not>\s*(\w[\w()]*)", c)
    if m: return m.group(1).strip()
    return None

def raw_guards(pres_in_order):
    """Priority-chain decomposition (encoding B3). Operation i's precondition is
    G_i /\ not(G_j) for earlier j (C1 negations). Strip the conjuncts that are the
    negation of an earlier operation's raw guard; what remains is G_i."""
    raws = []
    for p in pres_in_order:
        cs = [c for c in split_top(p)[1:] if c != "offered \\<subseteq> listens"]
        earlier = [" \\<and> ".join(r) for r in raws] + [a for r in raws for a in r if len(r) == 1]
        earlier_neg = [neg_of(r[0]) for r in raws if len(r) == 1 and neg_of(r[0]) is not None]
        keep = [c for c in cs if not (neg_of(c) is not None and neg_of(c) in earlier)
                and not (neg_of(c) is None and c.strip() in earlier_neg)]
        raws.append(keep)
    return raws

def equiv_check(pres_in_order, raws):
    """Propositional sanity check: /\ not(P_i)  <->  /\ not(G_i), atoms as Booleans,
    syntactic negation pairs linked. Exhaustive up to 18 atoms, sampled above."""
    import itertools, random
    atoms = {}
    def lit(c):
        b = neg_of(c)
        if b is not None and "\\<and>" not in b:
            return (atoms.setdefault(b, len(atoms)), False)
        return (atoms.setdefault(c, len(atoms)), True)
    def conj_lits(cs):  # a conjunct may itself be a negated conjunction -> treat as one literal group
        out = []
        for c in cs:
            b = neg_of(c)
            if b is not None and "\\<and>" in b:
                out.append(("NEGCONJ", [lit(x) for x in split_top(b)]))
            else: out.append(("LIT", lit(c)))
        return out
    P = [conj_lits([c for c in split_top(p)[1:] if c != "offered \\<subseteq> listens"]) for p in pres_in_order]
    G = [conj_lits(r) for r in raws]
    n = len(atoms)
    def ev(group, v):
        for kind, x in group:
            if kind == "LIT":
                i, pos = x
                if v[i] != pos: return False
            else:
                if all(v[i] == pos for i, pos in x): return False
        return True
    vals = itertools.product([False, True], repeat=n) if n <= 18 else (tuple(random.random() < .5 for _ in range(n)) for _ in range(200000))
    for v in vals:
        if (not any(ev(g, v) for g in P)) != (not any(ev(g, v) for g in G)):
            return False, n
    return True, n

def rewrite(src):
    t = open(src).read()
    name = re.search(r"^theory (\w+)", t, re.M).group(1)
    # ---- 1. invariant conjunct -------------------------------------------
    A = r"\<and> offered = listens"
    assert t.count(A) == 1, f"{src}: expected exactly one model-A conjunct, found {t.count(A)}"
    pins = {s: evset(es) for s, es in PIN_RE.findall(t)}
    st_lits = [x.strip() for x in re.search(r"^enumtype St = ([^\n]*)", t, re.M).group(1).split("|") if x.strip()]
    def offer_disj(evs):
        return "offered = {}" + "".join(f" \\<or> offered = {{{e}}}" for e in evs)
    B = "".join(f"\n                      \\<and> (st = {X} \\<longrightarrow> ({offer_disj(pins.get(X, []))}))" for X in st_lits)
    t = t.replace(A, B)
    # ---- 2. operations: parameter + offered' = nxt --------------------------
    ops = []            # (name, machine, state, pre, has_param)
    def fix_op(m):
        head, oname, mach, params, preline, pre, uopen, ubody, uclose = m.groups()
        st = re.match(r"st= (\w+)", pre)
        state = st.group(1) if st else None
        lm = LISTENS_UPD_RE.search(ubody)
        assert lm, f"{oname}: no listens' update"
        target_evs = evset(lm.group(1))
        tm = re.search(r"st\\<Zprime>= (\w+)", ubody)
        assert tm, f"{oname}: no st' update"
        assert set(target_evs) == set(pins.get(tm.group(1), [])), \
            f"{oname}: listens' literal {target_evs} != pin of target {tm.group(1)} {pins.get(tm.group(1))}"
        ubody2, n = OFFERED_UPD_RE.subn("", ubody)
        assert n == 1, f"{oname}: expected one offered' update, found {n}"
        if target_evs:
            ubody2 = ubody2.rstrip() + "\n         ,offered\\<Zprime> = nxt\n         "
            pline = params.rstrip("\n")
            newparams = (pline + " nxt \\<in> \"" + nxt_domain(target_evs) + "\"\n") if pline.strip() \
                        else "  params nxt \\<in> \"" + nxt_domain(target_evs) + "\"\n"
            has_param = True
        else:
            ubody2 = ubody2.rstrip() + "\n         ,offered\\<Zprime> = {}\n         "
            newparams = params
            has_param = bool(params.strip())
        ops.append((oname, mach, state, pre, has_param, bool(params.strip()), target_evs))
        return head + newparams + preline + uopen + ubody2 + uclose
    t = OP_RE.sub(fix_op, t)
    assert ops, "no operations parsed"
    mach = ops[0][1]
    # ---- 3. Idle_X per state ------------------------------------------------
    idle_ops, idle_lemmas = [], []
    states = sorted({o[2] for o in ops if o[2]})
    for X in states:
        pres_X = [o[3] for o in ops if o[2] == X]
        raws = raw_guards(pres_X)
        if any(len(r) == 0 for r in raws):
            continue                      # an unconditional operation: never idle here
        ok, natoms = equiv_check(pres_X, raws)
        stripped = sum(len([c for c in split_top(p)[1:] if c != "offered \\<subseteq> listens"]) for p in pres_X) - sum(len(r) for r in raws)
        assert ok, f"Idle_{X}: chain form not equivalent to the complement form"
        print(f"  Idle_{X}: {len(raws)} raw guards from {len(pres_X)} preconditions; {stripped} C1 negations stripped; equivalence checked over {natoms} atoms")
        evs = pins.get(X, [])
        negs = " \\<and> ".join("\\<not>(" + " \\<and> ".join(r) + ")" if len(r) > 1 else "\\<not>(" + r[0] + ")" for r in raws)
        pre = f"st= {X} \\<and> {negs}" if negs else f"st= {X}"
        if evs:
            params = f"  params nxt \\<in> \"{nxt_domain(evs)}\"\n"
            upd = f"  update \"[st\\<Zprime>= {X}\n         ,tr\\<Zprime> = tr\n         ,offered\\<Zprime> = nxt\n         ]\"\n"
            call = f"Idle_{X} (nxt)"
        else:
            params = ""
            upd = f"  update \"[st\\<Zprime>= {X}\n         ,tr\\<Zprime> = tr\n         ,offered\\<Zprime> = {{}}\n         ]\"\n"
            call = f"Idle_{X}()"
        idle_ops.append(f"zoperation Idle_{X} =\n  over {mach}\n{params}  pre \"{pre}\"\n{upd}\n")
        idle_lemmas.append(f"lemma Idle_{X}_inv [hoare_lemmas]: \"{call} preserves {mach}_inv\"\n"
                           f"  by (zpog_full; auto simp add: wf_rcstore_def)\n\n")
    # insert Idle ops before the Init definition; lemmas after the last *_inv lemma
    k = t.find("definition Init ::")
    assert k > 0
    t = t[:k] + "".join(idle_ops) + t[k:]
    ml = list(re.finditer(r"lemma \w+_inv \[hoare_lemmas\]:[^\n]*\n[^\n]*\n", t))
    assert ml
    e = ml[-1].end()
    t = t[:e] + "\n" + "".join(idle_lemmas) + t[e:]
    # machine operations list
    m = re.search(r"(^\s*operations\s+)([^\n]*)", t, re.M)
    assert m
    t = t[:m.end()] + " " + " ".join(f"Idle_{X}" for X in states if any(f"zoperation Idle_{X} " in o for o in idle_ops)) + t[m.end():]
    # ---- 4. existing *_inv lemma signatures must mention the new param ---------
    for oname, _, _, _, has_param, had_param, target_evs in ops:
        if target_evs and not had_param:
            t = re.sub(rf'"{oname}\(\) preserves', f'"{oname} (nxt) preserves', t)
        elif target_evs and had_param:
            t = re.sub(rf'"{oname} \(([^)]*)\) preserves', lambda mm: f'"{oname} ({mm.group(1)}, nxt) preserves', t)
    return name, t, ops, states, len(idle_ops)

def arms(name, t):
    DL = re.compile(r"(lemma \w+_deadlock_free:[^\n]*\n(?:[^\n]*\n)*?)(  by \(metis [^\n]*\)|  using St\.exhaust_disc by auto)\n")
    m = DL.search(t)
    assert m, "deadlock_free lemma not found"
    facts = re.search(r"metis ([^)]*) insertCI", m.group(2))
    enum_facts = facts.group(1) if facts else "St.exhaust_disc"
    if "Evt.exhaust_disc" not in enum_facts:
        enum_facts += " Evt.exhaust_disc"
    enum_facts += " Evt.distinct singletonD"
    body = m.group(1)
    out = {}
    out["M"]  = t[:m.start()] + body + f"  by (metis {enum_facts} insertCI)\n" + t[m.end():]
    out["Au"] = t[:m.start()] + body + f"  using {enum_facts} by auto\n" + t[m.end():]
    out["Bl"] = t[:m.start()] + body + "  by blast\n" + t[m.end():]
    # structured arms: make the state and offer case splits explicit, simplify each
    # branch (membership atoms become True/False by enum distinctness), then close
    # the small guard-chain tautology that remains.
    split = "  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)\n"
    out["S1"] = t[:m.start()] + body + split + "  by blast+\n" + t[m.end():]
    out["S2"] = t[:m.start()] + body + split + f"  by (metis {enum_facts} insertCI)+\n" + t[m.end():]
    # diagnostic: the deadlock_free method alone, lemma left open (oops) -- measures
    # the simp phase of the method, independent of any closer. NOT a proof.
    out["Dg"] = t[:m.start()] + body + "  oops\n" + t[m.end():]
    # NoDlf: strip the lemma (statement + apply lines + closing) entirely
    start = m.start()
    end = m.end()
    # also drop the trailing TACTIC comment block if present
    cm = re.match(r"\s*\(\*.*?\*\)\s*\n", t[end:], re.S)
    if cm: end += cm.end()
    out["NoDlf"] = t[:start] + t[end:]
    return out

def main(src, outdir):
    os.makedirs(outdir, exist_ok=True)
    name, t, ops, states, n_idle = rewrite(src)
    variants = arms(name, t)
    root = []
    for arm, text in variants.items():
        tn = f"{name}_B3_{arm}"
        text = re.sub(rf"^theory {name}\b", f"theory {tn}", text, count=1, flags=re.M)
        open(os.path.join(outdir, tn + ".thy"), "w").write(text)
        root.append(f'session {tn}_Check = "Z_Machines" +\n  theories\n    {tn}\n')
    open(os.path.join(outdir, "ROOT"), "w").write("\n".join(root))
    # self-check
    nparam = sum(1 for o in ops if o[6])
    print(f"{name}: ops={len(ops)} states={len(states)} idle_ops={n_idle} ops_with_nxt={nparam} arms={list(variants)}")
    chk = variants["M"]
    nocomment = re.sub(r"\(\*.*?\*\)", "", chk, flags=re.S)
    nocomment = re.sub(r"^\s*//.*$", "", nocomment, flags=re.M)
    assert "offered = listens" not in nocomment, "model-A conjunct survives outside comments"
    assert chk.count("offered\\<Zprime> = nxt") == nparam + sum(1 for X in states if f"Idle_{X} (nxt)" in chk)
    assert "Evt.exhaust_disc" in chk

if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
