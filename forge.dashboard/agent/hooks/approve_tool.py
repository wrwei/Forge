#!/usr/bin/env python
"""PreToolUse hook: bridges Claude Code tool approvals to the dashboard.

Reads the tool-call JSON from stdin, POSTs it to the dashboard, and
writes the dashboard's decision JSON to stdout. If the dashboard is
unreachable, falls back to a 'deny' decision so we never silently
approve when the user can't see the prompt.
"""
import json
import os
import sys
import urllib.error
import urllib.request


DASHBOARD_PORT = int(os.environ.get("DASHBOARD_PORT", "8000"))
ENDPOINT = f"http://127.0.0.1:{DASHBOARD_PORT}/api/agent/approve"
HOOK_HTTP_TIMEOUT_SECONDS = 300

# Tools and a semicolon-separated list of absolute path prefixes whose
# writes are auto-allowed without a dashboard prompt. The dashboard sets
# CLAUDE_AUTO_ALLOW_WRITE_ROOTS to include the codegen target directory
# (e.g. .../java.generated.project) so claude can produce code without
# the user clicking Approve for every file.
_AUTO_ALLOW_WRITE_TOOLS = {"Write", "Edit", "MultiEdit", "NotebookEdit"}
_AUTO_ALLOW_PATH_FIELDS = ("file_path", "path", "notebook_path")
_auto_allow_roots = [
    os.path.normcase(os.path.abspath(p))
    for p in os.environ.get("CLAUDE_AUTO_ALLOW_WRITE_ROOTS", "").split(os.pathsep)
    if p
]


def _allow(reason: str) -> str:
    return json.dumps({
        "hookSpecificOutput": {
            "hookEventName": "PreToolUse",
            "permissionDecision": "allow",
            "permissionDecisionReason": reason,
        }
    })


def _deny(reason: str) -> str:
    return json.dumps({
        "hookSpecificOutput": {
            "hookEventName": "PreToolUse",
            "permissionDecision": "deny",
            "permissionDecisionReason": reason,
        }
    })


def _maybe_auto_allow(payload_obj):
    """Return an allow-decision JSON string if the tool is a write into
    one of the configured roots; otherwise None (caller falls through
    to the dashboard prompt)."""
    if not _auto_allow_roots:
        return None
    tool = payload_obj.get("tool_name", "")
    if tool not in _AUTO_ALLOW_WRITE_TOOLS:
        return None
    tool_input = payload_obj.get("tool_input", {}) or {}
    target = None
    for field in _AUTO_ALLOW_PATH_FIELDS:
        v = tool_input.get(field)
        if isinstance(v, str) and v:
            target = v
            break
    if not target:
        return None
    abs_target = os.path.normcase(os.path.abspath(target))
    for root in _auto_allow_roots:
        # Match the root itself or any descendant; require a path
        # separator boundary so /a/b doesn't match /a/bc.
        if abs_target == root or abs_target.startswith(root + os.sep):
            return _allow(f"auto-allowed: target under {root}")
    return None


def main() -> int:
    payload = sys.stdin.read()
    try:
        payload_obj = json.loads(payload) if payload else {}
    except json.JSONDecodeError:
        payload_obj = {}

    auto = _maybe_auto_allow(payload_obj)
    if auto is not None:
        sys.stdout.write(auto)
        return 0

    try:
        req = urllib.request.Request(
            ENDPOINT,
            data=payload.encode("utf-8"),
            headers={"Content-Type": "application/json"},
        )
        resp = urllib.request.urlopen(req, timeout=HOOK_HTTP_TIMEOUT_SECONDS)
        sys.stdout.write(resp.read().decode("utf-8"))
        return 0
    except urllib.error.URLError as e:
        sys.stdout.write(_deny(f"Dashboard unreachable: {e.reason}"))
        return 0
    except Exception as e:
        sys.stdout.write(_deny(f"Hook error: {type(e).__name__}: {e}"))
        return 0


if __name__ == "__main__":
    sys.exit(main())
