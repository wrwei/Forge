import re, itertools, sys
def load(path):
    src = open(path).read()
    src = src.replace("actuator.orderPumps(BoilerConstants.PUMP_COUNT);", "actuator.startAllPumps();").replace("actuator.orderPumps(0);", "actuator.stopAllPumps();").replace("actuator.orderPumps(1);", "actuator.startFillPump();")
    body = src[src.index("public void step"):]
    atoms = re.findall(r'boolean (\w+) = ', body)
    out = {}
    parts = re.split(r'(?:if|else if) \(currentMode == OperatingMode\.(\w+)\) \{', body)
    for i in range(1, len(parts), 2):
        mode, text = parts[i], parts[i+1]
        branches = re.findall(r'(?:if|else if) \((event instanceof.*?)\) \{(.*?)\n\s*\}', text, re.S)
        bl = []
        for cond, b in branches:
            e = re.sub(r'\s+', ' ', cond).replace("event instanceof InputEvent.TransmissionReceived", "True").replace("&&", " and ").replace("||", " or ")
            e = re.sub(r'!(\w)', r' not \1', e)
            stmts = tuple(l.strip() for l in b.strip().splitlines() if l.strip() and not l.strip().startswith("//"))
            bl.append((compile(e, mode, "eval"), stmts, cond))
        out[mode] = bl
    return atoms, out
a_old, old = load(sys.argv[1]); a_new, new = load(sys.argv[2])
atoms = sorted(set(a_old) | set(a_new))
for mode in new:
    nb = new[mode]; ob = old.get(mode, [])
    bare_last = nb and re.sub(r'\s+', ' ', nb[-1][2]).strip() == "event instanceof InputEvent.TransmissionReceived"
    diffs = 0; fired_new = set()
    for vals in itertools.product([False, True], repeat=len(atoms)):
        env = dict(zip(atoms, vals))
        f_new = next((i for i, b in enumerate(nb) if eval(b[0], {}, env)), None)
        f_old = next((i for i, b in enumerate(ob) if eval(b[0], {}, env)), None)
        fired_new.add(f_new)
        if (nb[f_new][1] if f_new is not None else None) != (ob[f_old][1] if f_old is not None else None):
            diffs += 1
    print(f"{mode}: {len(nb)} branches, last bare={bare_last}, behaviour differences={diffs}, branches never first={sorted(set(range(len(nb))) - fired_new)}")
