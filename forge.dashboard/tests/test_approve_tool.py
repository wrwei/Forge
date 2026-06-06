"""Tests for the PreToolUse approval hook script.

The hook is invoked as a subprocess; tests run it against an in-process
HTTP fixture server."""
import json
import os
import subprocess as sp
import sys
import threading
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path

import pytest


HOOK_SCRIPT = Path(__file__).resolve().parents[1] / "agent" / "hooks" / "approve_tool.py"


class _Handler(BaseHTTPRequestHandler):
    response_body: bytes = b""
    status: int = 200

    def do_POST(self):
        length = int(self.headers.get("Content-Length", "0"))
        self.rfile.read(length)
        self.send_response(self.status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(self.response_body)))
        self.end_headers()
        self.wfile.write(self.response_body)

    def log_message(self, fmt, *args):
        pass


@pytest.fixture
def fixture_server():
    server = HTTPServer(("127.0.0.1", 0), _Handler)
    port = server.server_address[1]
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    yield port, _Handler
    server.shutdown()
    server.server_close()


def _run_hook(port: int, payload: dict) -> tuple[str, int]:
    env = {"DASHBOARD_PORT": str(port), "PATH": os.environ.get("PATH", "")}
    if os.name == "nt":
        env["SystemRoot"] = os.environ.get("SystemRoot", "")
    proc = sp.run(
        [sys.executable, str(HOOK_SCRIPT)],
        input=json.dumps(payload),
        capture_output=True, text=True, timeout=10, env=env,
    )
    return proc.stdout, proc.returncode


def test_returns_decision_from_dashboard(fixture_server):
    port, handler = fixture_server
    handler.response_body = json.dumps({
        "hookSpecificOutput": {
            "hookEventName": "PreToolUse",
            "permissionDecision": "allow",
        }
    }).encode()
    handler.status = 200

    stdout, rc = _run_hook(port, {"tool_name": "Edit", "tool_input": {"file_path": "/tmp/foo"}})

    assert rc == 0
    decision = json.loads(stdout)
    assert decision["hookSpecificOutput"]["permissionDecision"] == "allow"


def test_falls_back_to_deny_if_dashboard_unreachable():
    env = {"DASHBOARD_PORT": "1", "PATH": os.environ.get("PATH", "")}
    if os.name == "nt":
        env["SystemRoot"] = os.environ.get("SystemRoot", "")
    proc = sp.run(
        [sys.executable, str(HOOK_SCRIPT)],
        input='{"tool_name":"Edit"}',
        capture_output=True, text=True, timeout=10, env=env,
    )
    assert proc.returncode == 0
    decision = json.loads(proc.stdout)
    assert decision["hookSpecificOutput"]["permissionDecision"] == "deny"
    assert "unreachable" in decision["hookSpecificOutput"]["permissionDecisionReason"].lower()
