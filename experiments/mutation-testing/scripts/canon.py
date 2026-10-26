import re
def canon_sets(t):
    # recursively sort elements of balanced {...} literals
    out=[]; i=0
    while i < len(t):
        if t[i]=="{":
            depth=0; j=i
            while j < len(t):
                if t[j]=="{": depth+=1
                elif t[j]=="}":
                    depth-=1
                    if depth==0: break
                j+=1
            inner=canon_sets(t[i+1:j]); parts=[]; d=0; cur=""
            for ch in inner:
                if ch=="{": d+=1
                if ch=="}": d-=1
                if ch=="," and d==0: parts.append(cur.strip()); cur=""
                else: cur+=ch
            if cur.strip() or parts: parts.append(cur.strip())
            out.append("{"+", ".join(sorted(p for p in parts if p))+"}"); i=j+1
        else: out.append(t[i]); i+=1
    return "".join(out)
def canon_or(t):
    # sort disjunct chains inside parentheses: ( A \<or> B \<or> C )
    def f(m):
        parts=[p.strip() for p in m.group(1).split("\\<or>")]
        return "("+" \\<or> ".join(sorted(parts))+")"
    for _ in range(3): t=re.sub(r"\(([^()]*\\<or>[^()]*)\)", f, t)
    return t
def canon_text(t, kind):
    if kind=="thy": t=re.sub(r"\(\*.*?\*\)","",t,flags=re.S)
    else: t=re.sub(r"/\*.*?\*/","",t,flags=re.S); t=re.sub(r"//[^\n]*","",t)
    t=canon_sets(t); t=canon_or(t)
    return tuple(sorted(" ".join(l.split()) for l in t.split("\n") if l.strip()))
