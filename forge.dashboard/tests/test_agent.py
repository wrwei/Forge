from pathlib import Path
import asyncio
import json
import pytest
from web.agent import SessionStore


def test_first_load_creates_session_id(tmp_path):
    store_path = tmp_path / "session.json"
    store = SessionStore(store_path)
    sid = store.current()
    assert isinstance(sid, str) and sid
    assert store_path.exists()
    assert json.loads(store_path.read_text())["session_id"] == sid


def test_second_load_reuses_session_id(tmp_path):
    store_path = tmp_path / "session.json"
    SessionStore(store_path)
    first = json.loads(store_path.read_text())["session_id"]
    assert SessionStore(store_path).current() == first


def test_rotate_generates_new_id(tmp_path):
    store_path = tmp_path / "session.json"
    store = SessionStore(store_path)
    old = store.current()
    new = store.rotate()
    assert new != old
    assert store.current() == new
    assert json.loads(store_path.read_text())["session_id"] == new


def test_corrupt_file_falls_back_to_new_id(tmp_path):
    store_path = tmp_path / "session.json"
    store_path.write_text("not valid json {")
    store = SessionStore(store_path)
    assert store.current()
    assert json.loads(store_path.read_text())["session_id"] == store.current()


from web.agent import SettingsFileWriter


def test_writes_settings_with_absolute_hook_path(tmp_path):
    settings_path = tmp_path / "agent-settings.json"
    hook_path = tmp_path / "hooks" / "approve_tool.py"
    hook_path.parent.mkdir()
    hook_path.write_text("# hook")

    SettingsFileWriter(settings_path, hook_path, timeout_seconds=300).write()

    data = json.loads(settings_path.read_text())
    pre = data["hooks"]["PreToolUse"]
    assert len(pre) == 1
    assert pre[0]["matcher"] == "Edit|Write|Bash"
    assert len(pre[0]["hooks"]) == 1
    cmd = pre[0]["hooks"][0]["command"]
    assert str(hook_path.resolve()) in cmd
    assert pre[0]["hooks"][0]["timeout"] == 300
    assert pre[0]["hooks"][0]["type"] == "command"


def test_settings_writer_rewrites_existing_file(tmp_path):
    settings_path = tmp_path / "agent-settings.json"
    hook_path = tmp_path / "hooks" / "approve_tool.py"
    hook_path.parent.mkdir()
    hook_path.write_text("# hook")
    settings_path.write_text('{"stale": "data"}')

    SettingsFileWriter(settings_path, hook_path, timeout_seconds=300).write()

    data = json.loads(settings_path.read_text())
    assert "stale" not in data
    assert "hooks" in data


from web.agent import StreamJsonParser

FIXTURE_DIR = Path(__file__).parent / "fixtures" / "stream-json"


def test_parser_yields_events_per_line():
    raw = (FIXTURE_DIR / "text-and-tool.txt").read_text(encoding="utf-8")
    parser = StreamJsonParser()
    events = []
    for line in raw.splitlines():
        for ev in parser.feed(line + "\n"):
            events.append(ev)
    types = [e["type"] for e in events]
    assert types == [
        "system", "assistant", "assistant", "user", "result",
    ]


def test_parser_buffers_partial_lines():
    parser = StreamJsonParser()
    raw = '{"type":"text_delta","delta":{"text":"hi"}}\n'
    events = []
    for ch in raw:
        for ev in parser.feed(ch):
            events.append(ev)
    assert len(events) == 1
    assert events[0]["type"] == "text_delta"


def test_parser_skips_malformed_lines():
    parser = StreamJsonParser()
    events = list(parser.feed('not json\n{"type":"ok"}\n'))
    assert len(events) == 1
    assert events[0]["type"] == "ok"


from web.agent import ApprovalRegistry


@pytest.mark.asyncio
async def test_approval_resolves_when_decision_arrives():
    registry = ApprovalRegistry()
    request_id, future = registry.create()
    asyncio.get_event_loop().call_soon(
        registry.resolve, request_id,
        {"hookSpecificOutput": {"permissionDecision": "allow"}})
    decision = await asyncio.wait_for(future, timeout=1.0)
    assert decision["hookSpecificOutput"]["permissionDecision"] == "allow"


@pytest.mark.asyncio
async def test_approval_times_out_with_deny():
    registry = ApprovalRegistry()
    request_id, future = registry.create()
    decision = await registry.await_with_timeout(future, timeout=0.05)
    assert decision["hookSpecificOutput"]["permissionDecision"] == "deny"
    assert "timed out" in decision["hookSpecificOutput"]["permissionDecisionReason"].lower()


@pytest.mark.asyncio
async def test_resolve_unknown_request_id_is_noop():
    registry = ApprovalRegistry()
    registry.resolve("nonexistent", {})  # must not raise


from unittest.mock import AsyncMock, MagicMock
from web.agent import ClaudeAgent


@pytest.mark.asyncio
async def test_send_emits_events_through_lifecycle(tmp_path, monkeypatch):
    fixture = (FIXTURE_DIR / "text-and-tool.txt").read_text(encoding="utf-8")
    fixture_lines = [ln + "\n" for ln in fixture.splitlines() if ln.strip()]

    proc = MagicMock()
    proc.returncode = 0
    proc.pid = 12345
    proc.send_signal = MagicMock()

    line_iter = iter(fixture_lines + [""])

    async def fake_readline():
        nxt = next(line_iter, "")
        return nxt.encode("utf-8")

    proc.stdout = MagicMock()
    proc.stdout.readline = fake_readline
    proc.stderr = MagicMock()
    proc.stderr.read = AsyncMock(return_value=b"")
    proc.wait = AsyncMock(return_value=0)

    async def fake_create(*args, **kwargs):
        return proc

    monkeypatch.setattr("web.agent.asyncio.create_subprocess_exec", fake_create)

    session_path = tmp_path / "session.json"
    settings_path = tmp_path / "agent-settings.json"
    hook_path = tmp_path / "hooks" / "approve.py"
    hook_path.parent.mkdir()
    hook_path.write_text("# hook")

    events_out: list[dict] = []
    agent = ClaudeAgent(
        claude_path="/fake/claude",
        session_store=SessionStore(session_path),
        settings_path=settings_path,
        hook_path=hook_path,
        approval_timeout=10,
        emit=events_out.append,
        approvals=ApprovalRegistry(),
    )

    await agent.send("hello")

    types = [e["type"] for e in events_out]
    assert types[0] == "agent_user_message"
    assert "agent_response_chunk" in types
    assert "agent_tool_use" in types
    assert "agent_tool_result" in types
    assert types[-1] == "agent_done"
