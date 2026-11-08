"""Where the engine reports progress.

The engine and the phase runners never print, log to a UI or touch a
socket: they call one of the three methods below on whatever
:class:`ProgressSink` they were given. The former web dashboard was one
sink (a websocket); the headless driver is another (stdout). Adding a new
front end means adding a sink, not editing the engine.
"""
from __future__ import annotations

import sys
from typing import Protocol, TextIO


class ProgressSink(Protocol):
    def system(self, text: str, phase: str = "") -> None:
        """A message from the pipeline itself (start, stop, result)."""

    def stream(self, text: str, phase: str = "") -> None:
        """One line of a tool's output, as it is produced."""

    def phase_complete(self, phase: str, status: str, error: str = "") -> None:
        """The phase ended with ``status`` (completed, passed, failed, stopped)."""

    def event(self, payload: dict) -> None:
        """A structured event a front end may render (a memory reading, a results table)."""


class NullSink:
    def system(self, text: str, phase: str = "") -> None:  # noqa: D401
        pass

    def event(self, payload: dict) -> None:
        pass

    def stream(self, text: str, phase: str = "") -> None:
        pass

    def phase_complete(self, phase: str, status: str, error: str = "") -> None:
        pass


class ConsoleSink:
    """Prints what the headless driver used to print: system messages
    prefixed ``[sys]``, phase completions, and (optionally) tool output."""

    def __init__(self, out: TextIO | None = None, show_stream: bool = False) -> None:
        self._out = out or sys.stdout
        self._show_stream = show_stream

    def system(self, text: str, phase: str = "") -> None:
        text = text.strip()
        if text:
            print(f"  [sys] {text}", file=self._out, flush=True)

    def stream(self, text: str, phase: str = "") -> None:
        if self._show_stream:
            print(f"  | {text}", file=self._out, flush=True)

    def phase_complete(self, phase: str, status: str, error: str = "") -> None:
        print(f"  -> phase_complete: status={status}", file=self._out, flush=True)

    def event(self, payload: dict) -> None:
        pass


class RecordingSink:
    """Keeps every message in memory; for tests."""

    def __init__(self) -> None:
        self.system_messages: list[str] = []
        self.stream_lines: list[str] = []
        self.completions: list[tuple[str, str, str]] = []
        self.events: list[dict] = []

    def event(self, payload: dict) -> None:
        self.events.append(payload)

    def system(self, text: str, phase: str = "") -> None:
        self.system_messages.append(text)

    def stream(self, text: str, phase: str = "") -> None:
        self.stream_lines.append(text)

    def phase_complete(self, phase: str, status: str, error: str = "") -> None:
        self.completions.append((phase, status, error))
