"""Claude Code agent module: session persistence, settings writer,
stream parsing, subprocess lifecycle, approval registry.

This module is built incrementally across Tasks 2-7 of the
Claude-Code-in-dashboard refactor. Task 2 introduces SessionStore.
"""
from __future__ import annotations

import asyncio as _asyncio
import asyncio
import json
import signal
import uuid
from pathlib import Path
from typing import Callable


class SessionStore:
    """File-backed persistence of a single Claude session id.

    Corrupt or missing files are recovered by writing a fresh id."""

    def __init__(self, path: Path) -> None:
        self._path = path
        self._path.parent.mkdir(parents=True, exist_ok=True)
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
            sid = data["session_id"]
            if not isinstance(sid, str) or not sid:
                raise ValueError("invalid")
            self._id = sid
            # If the file lacks the `initialized` field, this is a
            # pre-fix session.json — claude has already used the id,
            # so default to True. Only freshly-generated ids (in the
            # except branch below or via rotate()) are flagged new.
            self._initialized = bool(data.get("initialized", True))
        except (FileNotFoundError, json.JSONDecodeError, KeyError, ValueError):
            self._id = str(uuid.uuid4())
            self._initialized = False
            self._persist()

    def current(self) -> str:
        return self._id

    def is_new(self) -> bool:
        """True if this UUID has never been used by claude yet (no session
        file on disk). Drives the --session-id (create) vs --resume choice."""
        return not self._initialized

    def mark_initialized(self) -> None:
        if not self._initialized:
            self._initialized = True
            self._persist()

    def rotate(self) -> str:
        self._id = str(uuid.uuid4())
        self._initialized = False
        self._persist()
        return self._id

    def _persist(self) -> None:
        self._path.write_text(
            json.dumps({"session_id": self._id, "initialized": self._initialized}),
            encoding="utf-8",
        )


import sys as _sys


class SettingsFileWriter:
    """Generates the dashboard-owned --settings JSON file.

    The hook command needs an absolute path so it works regardless of
    cwd at subprocess invocation time. We use the current Python
    interpreter to invoke the hook, again as an absolute path, so the
    user's PATH doesn't affect us.
    """

    def __init__(self, settings_path: Path, hook_path: Path,
                 timeout_seconds: int) -> None:
        self._settings_path = settings_path
        self._hook_path = hook_path.resolve()
        self._timeout = timeout_seconds

    def write(self) -> None:
        python_abs = Path(_sys.executable).resolve()
        command = f'"{python_abs}" "{self._hook_path}"'
        data = {
            "hooks": {
                "PreToolUse": [
                    {
                        "matcher": "Edit|Write|Bash",
                        "hooks": [
                            {
                                "type": "command",
                                "command": command,
                                "timeout": self._timeout,
                            }
                        ],
                    }
                ]
            }
        }
        self._settings_path.parent.mkdir(parents=True, exist_ok=True)
        self._settings_path.write_text(
            json.dumps(data, indent=2),
            encoding="utf-8",
        )


from typing import Iterator


class StreamJsonParser:
    """Line-buffered JSON parser for stream-json output.

    feed(chunk) consumes raw stdout bytes (already decoded) and yields
    parsed JSON objects, one per complete line. Partial lines are
    buffered; malformed lines are silently skipped (they may be tool
    output noise rather than protocol events).
    """

    def __init__(self) -> None:
        self._buffer = ""

    def feed(self, chunk: str) -> Iterator[dict]:
        self._buffer += chunk
        while "\n" in self._buffer:
            line, self._buffer = self._buffer.split("\n", 1)
            line = line.strip()
            if not line:
                continue
            try:
                yield json.loads(line)
            except json.JSONDecodeError:
                continue


class ApprovalRegistry:
    """Holds pending tool-approval futures keyed by request id.

    Lifecycle:
        request_id, future = registry.create()
        # push request_id to browser via WebSocket; await decision
        decision = await registry.await_with_timeout(future, timeout=290)

    Browser sends back the decision via the WebSocket handler, which
    calls registry.resolve(request_id, decision).
    """

    def __init__(self) -> None:
        self._pending: dict[str, _asyncio.Future] = {}

    def create(self) -> tuple[str, _asyncio.Future]:
        request_id = str(uuid.uuid4())
        future: _asyncio.Future = _asyncio.get_event_loop().create_future()
        self._pending[request_id] = future
        return request_id, future

    def resolve(self, request_id: str, decision: dict) -> None:
        future = self._pending.pop(request_id, None)
        if future is not None and not future.done():
            future.set_result(decision)

    async def await_with_timeout(self, future: _asyncio.Future,
                                 timeout: float) -> dict:
        try:
            return await _asyncio.wait_for(future, timeout=timeout)
        except _asyncio.TimeoutError:
            stale = [k for k, f in self._pending.items() if f is future]
            for k in stale:
                self._pending.pop(k, None)
            return {
                "hookSpecificOutput": {
                    "hookEventName": "PreToolUse",
                    "permissionDecision": "deny",
                    "permissionDecisionReason": "Approval timed out",
                }
            }


class ClaudeAgent:
    """One persistent Claude Code chat session, server-side."""

    _AUTO_ALLOWED = {"Read", "Glob", "Grep", "WebFetch", "WebSearch",
                     "TodoWrite", "Skill", "Task"}

    def __init__(self, claude_path: str, session_store: SessionStore,
                 settings_path: Path, hook_path: Path,
                 approval_timeout: int,
                 emit: Callable[[dict], None],
                 approvals: "ApprovalRegistry",
                 auto_allow_write_roots: list | None = None) -> None:
        self._claude_path = claude_path
        self._session_store = session_store
        self._settings_path = settings_path
        self._hook_path = hook_path
        self._approval_timeout = approval_timeout
        self._emit = emit
        self._approvals = approvals
        self._auto_allow_write_roots = auto_allow_write_roots or []
        self._proc = None
        self._stopped = False
        SettingsFileWriter(settings_path, hook_path, approval_timeout).write()

    async def send(self, text: str) -> None:
        self._stopped = False
        self._emit({"type": "agent_user_message", "text": text})
        session_flag = "--session-id" if self._session_store.is_new() else "--resume"
        args = [
            self._claude_path,
            "-p", text,
            "--settings", str(self._settings_path),
            session_flag, self._session_store.current(),
            "--output-format", "stream-json",
            "--verbose",
        ]
        self._proc = await self._spawn(args)
        if self._proc is None:
            return
        # Spawn succeeded → claude is creating/using the session file on
        # disk. Mark initialized so subsequent turns use --resume.
        self._session_store.mark_initialized()
        parser = StreamJsonParser()
        stderr_text = ""
        try:
            while True:
                line = await self._proc.stdout.readline()
                if not line:
                    break
                for event in parser.feed(line.decode("utf-8", errors="replace")):
                    self._translate(event)
        except asyncio.CancelledError:
            self.stop()
            self._stopped = True
            raise
        finally:
            if self._proc is not None:
                rc = await self._proc.wait()
                # If the user clicked Stop, surface that explicitly rather
                # than as a generic exit-non-zero error.
                if self._stopped:
                    self._emit({"type": "agent_aborted"})
                elif rc != 0 and self._proc.stderr is not None:
                    stderr_bytes = await self._proc.stderr.read()
                    if stderr_bytes:
                        stderr_text = stderr_bytes.decode("utf-8", errors="replace")[:500]
                    # `claude --resume <id>` against an unknown session
                    # (e.g. after a cwd change moved the per-project
                    # session directory) returns "No conversation found
                    # with session ID: <id>". Rotate to a fresh UUID so
                    # the user's next message goes through with
                    # `--session-id` instead.
                    if "No conversation found with session ID" in stderr_text:
                        old_id = self._session_store.current()
                        new_id = self._session_store.rotate()
                        self._emit({
                            "type": "agent_error",
                            "message": (
                                f"Session {old_id[:8]}… is no longer known to "
                                f"claude (likely the cwd changed). Rotated to a "
                                f"fresh session {new_id[:8]}… — re-send your "
                                f"message to continue."
                            ),
                        })
                    else:
                        self._emit({
                            "type": "agent_error",
                            "message": stderr_text or f"claude exited {rc}",
                        })
                self._proc = None
            self._emit({"type": "agent_done"})

    async def _spawn(self, args: list):
        import os as _os
        _create = asyncio.create_subprocess_exec
        env = _os.environ.copy()
        # auto-allow roots are passed down to the hook script so writes
        # under codegen targets don't pop a modal for every file.
        roots = getattr(self, "_auto_allow_write_roots", None) or []
        if roots:
            env["CLAUDE_AUTO_ALLOW_WRITE_ROOTS"] = _os.pathsep.join(str(r) for r in roots)
        # Spawn `claude` at the repo root so it discovers `.claude/skills/`
        # and `CLAUDE.md`. Without this, the subprocess inherits the
        # dashboard's CWD (forge.dashboard/) and slash commands like
        # /fix-from-feedback go unrecognised.
        repo_root = Path(__file__).resolve().parents[2]
        try:
            return await _create(
                *args,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                env=env,
                cwd=str(repo_root),
            )
        except FileNotFoundError:
            self._emit({
                "type": "agent_error",
                "message": f"claude binary not found at {self._claude_path}",
            })
            return None

    def stop(self) -> None:
        """Kill the running claude subprocess (if any).

        On Windows, SIGINT only propagates to console-attached children, so
        we use terminate() (TerminateProcess) which works regardless of
        console attachment. On Unix, terminate() sends SIGTERM, which
        claude handles cleanly.
        """
        if self._proc is None or self._proc.returncode is not None:
            return
        self._stopped = True
        try:
            self._proc.terminate()
        except (ProcessLookupError, OSError):
            pass

    def new_chat(self) -> str:
        return self._session_store.rotate()

    def _translate(self, event: dict) -> None:
        """Translate one stream-json event into dashboard WebSocket events.

        Stream-json schema (claude-code 2.1.x):
          - {"type":"system", ...}  → session/init/hooks; ignored.
          - {"type":"assistant","message":{"content":[<blocks>],...}}
              where <blocks> are {"type":"text","text":...},
              {"type":"thinking","thinking":...} (ignored),
              {"type":"tool_use","id":...,"name":...,"input":...}.
          - {"type":"user","message":{"content":[<results>]}}
              where <results> are {"type":"tool_result","tool_use_id":...,
              "content":...}.
          - {"type":"result","subtype":"success",...}  → final; ignored
              (agent_done is emitted by the subprocess-exit path).
          - {"type":"rate_limit_event", ...}  → ignored.
        """
        etype = event.get("type")
        if etype == "assistant":
            content = (event.get("message") or {}).get("content") or []
            self._tool_use_names = getattr(self, "_tool_use_names", {})
            for block in content:
                btype = block.get("type")
                if btype == "text":
                    text = block.get("text", "")
                    if text:
                        self._emit({
                            "type": "agent_response_chunk",
                            "text": text,
                        })
                elif btype == "tool_use":
                    tool = block.get("name", "")
                    tool_id = block.get("id", "")
                    if tool_id:
                        self._tool_use_names[tool_id] = tool
                    self._emit({
                        "type": "agent_tool_use",
                        "tool": tool,
                        "input": block.get("input", {}),
                        "status": "auto-allowed" if tool in self._AUTO_ALLOWED else "pending",
                    })
                # "thinking" blocks are not surfaced to the user
        elif etype == "user":
            content = (event.get("message") or {}).get("content") or []
            self._tool_use_names = getattr(self, "_tool_use_names", {})
            for block in content:
                if block.get("type") != "tool_result":
                    continue
                tool_id = block.get("tool_use_id", "")
                tool_name = self._tool_use_names.get(tool_id, "")
                raw = block.get("content", "")
                if isinstance(raw, list):
                    # content may itself be an array of {type:"text",text:...}
                    raw = "".join(
                        (b.get("text", "") if isinstance(b, dict) else str(b))
                        for b in raw
                    )
                excerpt = raw if isinstance(raw, str) else str(raw)
                self._emit({
                    "type": "agent_tool_result",
                    "tool": tool_name,
                    "success": not block.get("is_error", False),
                    "output_excerpt": excerpt[:500],
                })
