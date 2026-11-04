import re, itertools
src = open("/Users/ranwei/Gitee/forge-v3r7-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java").read()
body = src[src.index("public void step"):]
atoms = re.findall(r'boolean (\w+) = ', body)
blocks = re.split(r'(?:if|else if) \(currentMode == OperatingMode\.(\w+)\) \{', body)
for i in range(1, len(blocks), 2):
    mode, text = blocks[i], blocks[i+1]
    conds = [re.sub(r'\s+', ' ', c) for c in re.findall(r'(?:if|else if) \((event instanceof.*?)\) \{', text, re.S)]
    if not conds:
        print(mode, "terminal (no branches)"); continue
    py = []
    for c in conds:
        e = c.replace("event instanceof InputEvent.TransmissionReceived", "True").replace("&&", " and ").replace("||", " or ")
        e = re.sub(r'!(\w)', r' not \1', e)
        py.append(compile(e, mode, "eval"))
    used = sorted({a for a in atoms if any(a in c for c in conds)})
    gaps = overlaps = 0
    for vals in itertools.product([False, True], repeat=len(used)):
        env = dict(zip(used, vals))
        n = sum(1 for p in py if eval(p, {}, env))
        gaps += n == 0; overlaps += n > 1
    print(f"{mode}: {len(conds)} branches over {len(used)} atoms -> uncovered={gaps} overlapping={overlaps}")
