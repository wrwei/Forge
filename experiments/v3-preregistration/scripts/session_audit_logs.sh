#!/bin/bash
# session_audit_logs.sh <worktree-name> -- the four session-log checks of a v3 audit, in one run.
#   Usage: bash scripts/session_audit_logs.sh forge-v3r-run3-lre
# Writes the command dump to /tmp/v3-cmds.txt and the hook/skill listing to
# /tmp/v3-hooks.txt (the files the audit reads), and prints the model and
# Claude Code version counts and the non-Bash/Write tool calls of the session.
set -u
WT="${1:?worktree name, e.g. forge-v3r-run3-lre}"
H=$(cd "$(dirname "$0")" && pwd)
PY=${PYTHON:-python3}
# Claude Code names a project folder after the path with / and _ replaced by -
# (the same name session_commands.py uses)
D="${V3_PROJ_ROOT:-$HOME/.claude/projects}/-Users-ranwei-Gitee-$(printf '%s' "$WT" | tr '_' '-')"
shopt -s nullglob
J=( "$D"/*.jsonl )
[ "${#J[@]}" -ge 1 ] || { echo "FAIL no session log at $D"; exit 1; }
echo "== 1. command dump -> /tmp/v3-cmds.txt"
"$PY" "$H/session_commands.py" "$WT" > /tmp/v3-cmds.txt
head -1 /tmp/v3-cmds.txt
echo "== 2. hooks and skills -> /tmp/v3-hooks.txt"
"$PY" "$H/session_hooks_skills.py" "$WT" > /tmp/v3-hooks.txt
echo "   $(wc -l < /tmp/v3-hooks.txt | tr -d ' ') lines"
echo "== 3. model and version"
echo "   models: $(grep -ho '"model":"[^"]*"' "$D"/*.jsonl | sort | uniq -c | tr '\n' ' ')"
echo "   versions: $(grep -ho '"version":"[^"]*"' "$D"/*.jsonl | sort | uniq -c | tr '\n' ' ')"
echo "== 4. tool calls"
"$PY" - "$D" <<'EOF'
import json, glob, sys, collections
d = sys.argv[1]
names = collections.Counter()
for f in glob.glob(d + "/*.jsonl"):
    for line in open(f):
        try: r = json.loads(line)
        except Exception: continue
        c0 = (r.get("message") or {}).get("content")
        for c in (c0 if isinstance(c0, list) else []):
            if isinstance(c, dict) and c.get("type") == "tool_use":
                names[c["name"]] += 1
                if c["name"] not in ("Bash", "Write"):
                    print("  ", c["name"], json.dumps(c.get("input"))[:300])
print("  ", dict(names))
EOF
