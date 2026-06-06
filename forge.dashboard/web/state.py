"""Session state with optional disk persistence for phase statuses and log."""
from __future__ import annotations

import json
import logging
import threading
from pathlib import Path
from typing import Any

logger = logging.getLogger(__name__)


class SessionState:
    """Thread-safe state container for a single pipeline session.

    When *persist_path* is given, state is saved to disk after important
    events and reloaded on construction so phase statuses survive server
    restarts.
    """

    def __init__(self, persist_path: Path | None = None) -> None:
        self._lock = threading.Lock()
        self._phases: dict[str, dict[str, Any]] = {}
        self._messages: list[dict] = []
        self._persist_path = persist_path
        if persist_path:
            self._load()

    # ---- Message log --------------------------------------------------------

    def add_message(self, msg: dict) -> None:
        with self._lock:
            self._messages.append(msg)

    def get_messages(self) -> list[dict]:
        with self._lock:
            return list(self._messages)

    def clear_messages(self) -> None:
        with self._lock:
            self._messages.clear()

    # ---- Phase status -------------------------------------------------------

    def set_phase_status(self, phase: str, status: str) -> None:
        with self._lock:
            self._phases.setdefault(phase, {})["status"] = status

    def get_phase_status(self, phase: str) -> str:
        with self._lock:
            return self._phases.get(phase, {}).get("status", "idle")

    def set_phase_result(self, phase: str, result: Any) -> None:
        with self._lock:
            self._phases.setdefault(phase, {})["result"] = result

    def set_phase_error(self, phase: str, error: str) -> None:
        with self._lock:
            self._phases.setdefault(phase, {})["error"] = error

    def get_phase_statuses(self) -> dict[str, str]:
        with self._lock:
            return {
                name: info.get("status", "idle")
                for name, info in self._phases.items()
            }

    def reset(self) -> None:
        with self._lock:
            self._phases.clear()
            self._messages.clear()
        self.save()

    # ---- Persistence --------------------------------------------------------

    def save(self) -> None:
        if not self._persist_path:
            return
        with self._lock:
            data = {
                "messages": self._messages,
                "phases": {
                    name: {"status": info.get("status", "idle")}
                    for name, info in self._phases.items()
                },
            }
        try:
            self._persist_path.parent.mkdir(parents=True, exist_ok=True)
            self._persist_path.write_text(
                json.dumps(data, ensure_ascii=False), encoding="utf-8",
            )
        except OSError:
            logger.debug("Failed to save session state")

    def _load(self) -> None:
        if not self._persist_path or not self._persist_path.exists():
            return
        try:
            data = json.loads(self._persist_path.read_text(encoding="utf-8"))
            self._messages = data.get("messages", [])
            for name, info in data.get("phases", {}).items():
                self._phases[name] = {"status": info.get("status", "idle")}
            # Reset any "running" phases (server crashed mid-phase)
            for info in self._phases.values():
                if info.get("status") == "running":
                    info["status"] = "stopped"
        except (json.JSONDecodeError, OSError):
            logger.debug("Failed to load session state")
