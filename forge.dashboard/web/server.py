"""FastAPI server: REST endpoints + WebSocket for pipeline dashboard."""
from __future__ import annotations

import asyncio
import logging
import sys
import uuid
from pathlib import Path

import json as _json
import yaml
from fastapi import FastAPI, Request, WebSocket, WebSocketDisconnect
from fastapi.responses import FileResponse, JSONResponse
from fastapi.staticfiles import StaticFiles

from web.agent import ApprovalRegistry, ClaudeAgent, SessionStore
from web.bridge import PipelineBridge
from web.manifest import Manifest
from web.state import SessionState

logger = logging.getLogger(__name__)

DASHBOARD_DIR = Path(__file__).resolve().parent.parent
STATIC_DIR = Path(__file__).resolve().parent / "static"
REPO_ROOT = DASHBOARD_DIR.parent
_REPO_ROOT = REPO_ROOT  # alias used by manifest path
_MANIFEST_PATH = _REPO_ROOT / "pipeline.yaml"
TARGET_PROJECT = REPO_ROOT / "java.generated.project"
GENERATED_SRC = TARGET_PROJECT / "src" / "main" / "java"
T2M_OUTPUT = REPO_ROOT / "forge.transformations" / "output"
ASSETS_DIR = REPO_ROOT / "forge.assets"  # prompts, corrections, case studies
PIPELINE_DIR = REPO_ROOT / "java.codegen.pipeline"  # legacy (for CSP corrections module)

app = FastAPI(title="Pipeline Dashboard")
app.mount("/static", StaticFiles(directory=str(STATIC_DIR)), name="static")


@app.middleware("http")
async def no_cache_static(request: Request, call_next):
    response = await call_next(request)
    if request.url.path.startswith("/static"):
        response.headers["Cache-Control"] = "no-store"
    return response


_SESSION_FILE = DASHBOARD_DIR / "output" / ".session.json"
_state = SessionState(persist_path=_SESSION_FILE)
_MANIFEST = Manifest.load(_MANIFEST_PATH)

# ── Claude agent runtime state ────────────────────────────────────────────
_AGENT_DIR = DASHBOARD_DIR / "agent"
_AGENT_DIR.mkdir(exist_ok=True)
(_AGENT_DIR / "hooks").mkdir(exist_ok=True)
_AGENT_SETTINGS_PATH = _AGENT_DIR / "agent-settings.json"
_AGENT_SESSION_PATH = _AGENT_DIR / "session.json"
_AGENT_HOOK_PATH = _AGENT_DIR / "hooks" / "approve_tool.py"

_AGENT_APPROVALS = ApprovalRegistry()
_AGENT_BROADCAST: list = []   # active websockets (typically one)

_AGENT_CFG = (getattr(_MANIFEST, "_raw", {}) or {}).get("agent", {}) or {}
_AGENT_CLAUDE_PATH = (_AGENT_CFG.get("claude_path", {}) or {}).get(sys.platform, "claude")
_AGENT_TIMEOUT = int(_AGENT_CFG.get("approval_timeout_seconds", 300))


# ---------- REST endpoints ------------------------------------------------

@app.get("/")
async def index():
    return FileResponse(
        str(STATIC_DIR / "index.html"),
        headers={"Cache-Control": "no-cache, no-store, must-revalidate"},
    )


@app.get("/api/config")
async def get_config():
    with open(_MANIFEST_PATH, encoding="utf-8") as f:
        return yaml.safe_load(f)




@app.get("/api/case-studies")
async def list_case_studies():
    """List case study directories under forge.assets/case-studies/
    and return the currently active one from the manifest."""
    case_studies_dir = ASSETS_DIR / "case-studies"
    available = []
    if case_studies_dir.exists():
        for entry in sorted(case_studies_dir.iterdir()):
            if entry.is_dir() and (entry / "requirements").is_dir():
                available.append(entry.name)
    active = (getattr(_MANIFEST, "_raw", {}) or {}) \
        .get("agent", {}).get("active_case_study", "")
    return {"active": active, "available": available}


@app.get("/api/requirements")
async def list_requirements(study: str | None = None):
    """List requirement .json files for a case study (default: active).
    Also returns the system_description.txt path for that study."""
    if not study:
        study = (getattr(_MANIFEST, "_raw", {}) or {}) \
            .get("agent", {}).get("active_case_study", "")
    if not study:
        return JSONResponse({"error": "no active case study"}, status_code=400)

    study_dir = ASSETS_DIR / "case-studies" / study
    req_dir = study_dir / "requirements"
    if not req_dir.is_dir():
        return JSONResponse({"error": f"unknown case study: {study}"},
                            status_code=404)

    requirements = []
    for f in sorted(req_dir.glob("*.json")):
        requirements.append({
            "name": f.name,
            "path": f"case-studies/{study}/requirements/{f.name}",
            "size": f.stat().st_size,
        })

    sys_desc = study_dir / "system" / "system_description.txt"
    sys_desc_path = (f"case-studies/{study}/system/system_description.txt"
                     if sys_desc.is_file() else None)

    return {
        "study": study,
        "requirements": requirements,
        "system_description_path": sys_desc_path,
    }


@app.get("/api/source-files")
async def list_source_files():
    """List all .java files under java.generated.project/src/main/java/.
    Used by the Source tab during phase 2 (Java Code Generation)."""
    if not GENERATED_SRC.is_dir():
        return {"root": str(GENERATED_SRC), "files": []}
    files = []
    for f in sorted(GENERATED_SRC.rglob("*.java")):
        rel = f.relative_to(GENERATED_SRC).as_posix()
        files.append({
            "name": f.name,
            "path": rel,
            "size": f.stat().st_size,
        })
    return {"root": str(GENERATED_SRC), "files": files}


@app.get("/api/source/{path:path}")
async def get_source_file(path: str):
    """Return content of a Java source file under java.generated.project."""
    full = (GENERATED_SRC / path).resolve()
    try:
        full.relative_to(GENERATED_SRC.resolve())
    except ValueError:
        return JSONResponse({"error": "invalid path"}, status_code=400)
    if not full.exists() or not full.is_file():
        return JSONResponse({"error": "not found"}, status_code=404)
    try:
        content = full.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        return JSONResponse({"error": "binary file"}, status_code=400)
    return {"path": path, "content": content}


@app.get("/api/codegen-prompts")
async def list_codegen_prompts():
    """List the layered vibe-coding-prompts (selectable .md files) and
    the always-loaded context prompts (.txt files in forge.assets/prompts/).
    Used by the Prompts tab during phase 2 (Java Code Generation)."""
    vibe_dir = ASSETS_DIR / "vibe-coding-prompts"
    ctx_dir = ASSETS_DIR / "prompts"
    layers = []
    if vibe_dir.is_dir():
        for f in sorted(vibe_dir.glob("*.md")):
            if f.name.lower() == "readme.md":
                continue
            layers.append({
                "name": f.name,
                "path": f"vibe-coding-prompts/{f.name}",
                "size": f.stat().st_size,
            })
    context_paths = []
    if ctx_dir.is_dir():
        for f in sorted(ctx_dir.iterdir()):
            if f.is_file() and f.suffix.lower() in {".txt", ".md"}:
                context_paths.append(f"prompts/{f.name}")
    return {"layers": layers, "context_paths": context_paths}


@app.post("/api/active-case-study")
async def set_active_case_study(payload: dict):
    """Update agent.active_case_study in pipeline.yaml and reload the
    in-memory manifest."""
    global _MANIFEST
    study = (payload or {}).get("study", "")
    if not study:
        return JSONResponse({"error": "missing 'study'"}, status_code=400)

    case_studies_dir = ASSETS_DIR / "case-studies"
    if not (case_studies_dir / study / "requirements").is_dir():
        return JSONResponse({"error": f"unknown case study: {study}"},
                            status_code=400)

    raw = getattr(_MANIFEST, "_raw", {}) or {}
    raw.setdefault("agent", {})["active_case_study"] = study
    _MANIFEST.dump(_MANIFEST_PATH)

    _MANIFEST = Manifest.load(_MANIFEST_PATH)

    return {"active": study}


@app.get("/api/ext-files/{path:path}")
async def get_ext_file_content(path: str):
    """Return content of a file in the T2M output directory."""
    full = (T2M_OUTPUT / path).resolve()
    # Path.is_relative_to honors path-component boundaries; string-prefix
    # comparison against `output` would let `output-evil/...` slip through.
    try:
        full.relative_to(T2M_OUTPUT.resolve())
    except ValueError:
        return JSONResponse({"error": "invalid path"}, status_code=400)
    if not full.exists() or not full.is_file():
        return JSONResponse({"error": "not found"}, status_code=404)
    try:
        content = full.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        return JSONResponse({"error": "binary file"}, status_code=400)
    return {"path": path, "content": content}


@app.get("/api/asset/{path:path}")
async def get_asset_file_content(path: str):
    """Return content of a file under forge.assets/.

    Used by the Requirements tab to preview *.json files under
    forge.assets/case-studies/<study>/requirements/ and the
    system_description.txt under <study>/system/.
    """
    full = (ASSETS_DIR / path).resolve()
    try:
        full.relative_to(ASSETS_DIR.resolve())
    except ValueError:
        return JSONResponse({"error": "invalid path"}, status_code=400)
    if not full.exists() or not full.is_file():
        return JSONResponse({"error": "not found"}, status_code=404)
    try:
        content = full.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        return JSONResponse({"error": "binary file"}, status_code=400)
    return {"path": path, "content": content}


@app.get("/api/commands")
async def list_commands():
    """List slash commands available to the dashboard's claude agent.

    Reads ``.claude/commands/*.md`` at the repo root, extracts the
    frontmatter `description` (or the first non-empty body line as a
    fallback), and returns one entry per command. The Phase 7 view in
    the frontend renders this list as clickable buttons that prefill
    `/<name>` into the chat input.
    """
    commands_dir = REPO_ROOT / ".claude" / "commands"
    if not commands_dir.is_dir():
        return {"commands": []}
    out: list[dict] = []
    for md in sorted(commands_dir.glob("*.md")):
        try:
            text = md.read_text(encoding="utf-8")
        except OSError:
            continue
        description = ""
        # Parse YAML-ish frontmatter for a `description:` line.
        if text.startswith("---"):
            end = text.find("\n---", 3)
            if end != -1:
                front = text[3:end]
                for line in front.splitlines():
                    line = line.strip()
                    if line.lower().startswith("description:"):
                        description = line.split(":", 1)[1].strip()
                        break
        if not description:
            # Fallback: first non-empty / non-heading line of the body.
            body_start = text.find("\n---", 3)
            body = text[body_start + 4:] if body_start != -1 else text
            for line in body.splitlines():
                stripped = line.strip()
                if stripped and not stripped.startswith(("#", "---")):
                    description = stripped
                    break
        out.append({"name": md.stem, "description": description})
    return {"commands": out}


@app.get("/api/feedback/{phase}")
async def get_phase_feedback(phase: str):
    """Return the latest post_<phase>.json verification feedback.

    Returns 404 if the phase has not produced feedback yet (e.g. was never
    run in the current working tree). Serves from
    ``forge.assets/corrections/`` which is the unified feedback
    directory written by all dashboard phases.
    """
    # Defensive: prevent path-traversal via the {phase} segment.
    if not phase.replace("_", "").replace("-", "").isalnum():
        return JSONResponse({"error": "invalid phase id"}, status_code=400)
    feedback_dir = ASSETS_DIR / "corrections"
    json_path = feedback_dir / f"post_{phase}.json"
    if not json_path.exists() or not json_path.is_file():
        return JSONResponse({"error": "no feedback yet"}, status_code=404)
    try:
        data = _json.loads(json_path.read_text(encoding="utf-8"))
    except (OSError, _json.JSONDecodeError) as exc:
        return JSONResponse({"error": f"unreadable: {exc}"}, status_code=500)
    # Attach the human-readable markdown body if present alongside the
    # JSON. The chat inline-renders this; the feedback panel still
    # uses the structured fields.
    md_path = feedback_dir / f"post_{phase}.md"
    if md_path.is_file():
        try:
            data["body_md"] = md_path.read_text(encoding="utf-8")
        except OSError:
            pass
    return data


@app.get("/api/files/{path:path}")
async def get_file_content(path: str):
    """Return content of a file in the dashboard output directory."""
    output_dir = DASHBOARD_DIR / "output"
    full = (output_dir / path).resolve()
    # See note on /api/ext-files: don't use string startswith for path
    # containment — boundary-less prefix lets sibling dirs slip through.
    try:
        full.relative_to(output_dir.resolve())
    except ValueError:
        return JSONResponse({"error": "invalid path"}, status_code=400)
    if not full.exists() or not full.is_file():
        return JSONResponse({"error": "not found"}, status_code=404)
    try:
        content = full.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        return JSONResponse({"error": "binary file"}, status_code=400)
    return {"path": path, "content": content}


@app.post("/api/agent/approve")
async def agent_approve(payload: dict):
    """PreToolUse hook calls this; we push the request to the browser and
    await the user's decision."""
    request_id, future = _AGENT_APPROVALS.create()
    for ws in list(_AGENT_BROADCAST):
        try:
            await ws.send_json({
                "type": "agent_tool_request",
                "request_id": request_id,
                "tool": payload.get("tool_name", ""),
                "input": payload.get("tool_input", {}),
            })
        except Exception:
            pass
    wait_seconds = max(10, _AGENT_TIMEOUT - 10)
    return await _AGENT_APPROVALS.await_with_timeout(future, wait_seconds)


# ---------- WebSocket endpoint --------------------------------------------

@app.websocket("/ws")
async def websocket_endpoint(ws: WebSocket):
    await ws.accept()
    loop = asyncio.get_event_loop()
    config_path = str(_MANIFEST_PATH)
    bridge = PipelineBridge(config_path, _state)

    _AGENT_BROADCAST.append(ws)
    session_store = SessionStore(_AGENT_SESSION_PATH)

    def _emit(event: dict):
        asyncio.create_task(ws.send_json(event))

    claude_agent = ClaudeAgent(
        claude_path=_AGENT_CLAUDE_PATH,
        session_store=session_store,
        settings_path=_AGENT_SETTINGS_PATH,
        hook_path=_AGENT_HOOK_PATH,
        approval_timeout=_AGENT_TIMEOUT,
        emit=_emit,
        approvals=_AGENT_APPROVALS,
        # Writes inside the codegen target directory are auto-allowed
        # (phase 2's whole job is filling this tree); other writes still
        # route through the dashboard approval modal.
        auto_allow_write_roots=[TARGET_PROJECT.resolve()],
    )
    agent_task: asyncio.Task | None = None

    try:
        # Build requirements list for the *active* case study (set in
        # pipeline.yaml: agent.active_case_study). The Requirements tab
        # re-fetches via /api/requirements when opened, but the initial
        # WS payload preloads this list so the UI has something to show
        # before the user clicks the tab.
        active_study = (
            (_MANIFEST._raw.get("agent") or {}).get("active_case_study") or "lre"
        )
        req_dir = ASSETS_DIR / "case-studies" / active_study / "requirements"
        req_files = []
        if req_dir.exists():
            for f in sorted(req_dir.glob("*.json")):
                req_files.append({"path": f"requirements/{f.name}", "name": f.stem})

        await ws.send_json({
            "type": "connected",
            "pipeline": bridge.get_pipeline_structure(),
            "requirements": req_files,
            "history": _state.get_messages(),
            "phase_statuses": _state.get_phase_statuses(),
        })

        while True:
            data = await ws.receive_json()
            msg_type = data.get("type")

            if msg_type == "start_command":
                command_id = data.get("command", "")
                bridge.start_command(command_id, ws, loop)

            elif msg_type == "stop_phase":
                bridge.stop_phase()

            elif msg_type == "list_files":
                task_filter = data.get("task")
                files = _collect_files(task_filter)
                await ws.send_json({
                    "type": "file_list",
                    "task": task_filter or "",
                    "files": files,
                })

            elif msg_type == "publish":
                import shutil
                try:
                    copied = 0
                    published_files = []
                    # Publish T2M output to resources
                    resources_dir = TARGET_PROJECT / "src" / "main" / "resources"
                    if T2M_OUTPUT.exists():
                        for src in sorted(T2M_OUTPUT.iterdir()):
                            if src.is_file():
                                resources_dir.mkdir(parents=True, exist_ok=True)
                                shutil.copy2(src, resources_dir / src.name)
                                copied += 1
                                published_files.append("src/main/resources/" + src.name)
                    await ws.send_json({
                        "type": "publish_result",
                        "status": "ok",
                        "count": copied,
                        "target": str(TARGET_PROJECT),
                        "files": published_files,
                    })
                except Exception as exc:
                    logger.exception("Publish failed")
                    await ws.send_json({
                        "type": "publish_result",
                        "status": "error",
                        "message": str(exc),
                    })

            elif msg_type == "clear_output":
                task_filter = data.get("task")
                try:
                    count = _clear_output(task_filter)
                    if task_filter:
                        _state.set_phase_status(task_filter, "idle")
                    else:
                        _state.reset()
                    _state.save()
                    await ws.send_json({
                        "type": "clear_result",
                        "status": "ok",
                        "count": count,
                        "task": task_filter or "",
                    })
                except Exception as exc:
                    logger.exception("Clear output failed")
                    await ws.send_json({
                        "type": "clear_result",
                        "status": "error",
                        "message": str(exc),
                    })

            elif msg_type == "reset_all":
                # Full reset: nuke t2m output + feedback files + phase
                # statuses. Used after a case-study switch when the
                # output dir is contaminated with the previous study's
                # artefacts. Idempotent and confirmation-gated on the
                # frontend side.
                try:
                    output_count = _clear_output(None)
                    feedback_dir = ASSETS_DIR / "corrections"
                    feedback_count = 0
                    if feedback_dir.exists():
                        for f in feedback_dir.glob("post_*.md"):
                            f.unlink(); feedback_count += 1
                        for f in feedback_dir.glob("post_*.json"):
                            f.unlink(); feedback_count += 1
                    _state.reset()
                    _state.save()
                    await ws.send_json({
                        "type": "reset_all_result",
                        "status": "ok",
                        "output_count": output_count,
                        "feedback_count": feedback_count,
                    })
                except Exception as exc:
                    logger.exception("Reset-all failed")
                    await ws.send_json({
                        "type": "reset_all_result",
                        "status": "error",
                        "message": str(exc),
                    })

            elif msg_type == "clean_project":
                import shutil
                try:
                    count = 0
                    for sub in ("src/main/java", "src/test/java", "build"):
                        d = TARGET_PROJECT / sub
                        if d.exists():
                            for f in list(d.rglob("*")):
                                if f.is_file():
                                    count += 1
                            shutil.rmtree(d)
                    if T2M_OUTPUT.exists():
                        for f in list(T2M_OUTPUT.iterdir()):
                            if f.is_file():
                                f.unlink()
                                count += 1
                    resources_dir = TARGET_PROJECT / "src" / "main" / "resources"
                    if resources_dir.exists():
                        for f in list(resources_dir.rglob("*")):
                            if f.is_file():
                                count += 1
                        shutil.rmtree(resources_dir)
                    await ws.send_json({
                        "type": "clean_project_result",
                        "status": "ok",
                        "count": count,
                    })
                except Exception as exc:
                    logger.exception("Clean project failed")
                    await ws.send_json({
                        "type": "clean_project_result",
                        "status": "error",
                        "message": str(exc),
                    })

            elif msg_type == "get_type_ranges":
                try:
                    from web.csp_corrections import (
                        extract_type_ranges, load_saved_ranges,
                    )
                    dashboard_config = str(_MANIFEST_PATH)
                    ranges = extract_type_ranges(dashboard_config)
                    saved = load_saved_ranges(dashboard_config) or {}
                    await ws.send_json({
                        "type": "type_ranges",
                        "ranges": ranges or [],
                        "saved": saved,
                    })
                except Exception as exc:
                    logger.exception("get_type_ranges failed")
                    await ws.send_json({
                        "type": "type_ranges",
                        "ranges": [],
                        "saved": {},
                        "error": str(exc),
                    })

            elif msg_type == "apply_type_ranges":
                try:
                    from web.csp_corrections import (
                        apply_csp_corrections, save_ranges,
                    )
                    dashboard_config = str(_MANIFEST_PATH)
                    user_ranges = data.get("ranges", {})
                    save_ranges(user_ranges, dashboard_config)
                    result = apply_csp_corrections(dashboard_config, user_ranges=user_ranges)
                    await ws.send_json({
                        "type": "type_ranges_applied",
                        "status": "ok",
                        "path": str(result) if result else None,
                    })
                except Exception as exc:
                    logger.exception("apply_type_ranges failed")
                    await ws.send_json({
                        "type": "type_ranges_applied",
                        "status": "error",
                        "message": str(exc),
                    })

            elif msg_type == "reset_type_ranges":
                try:
                    corrections_dir = ASSETS_DIR / "corrections"
                    saved_file = corrections_dir / "type_ranges.json"
                    if saved_file.exists():
                        saved_file.unlink()
                    await ws.send_json({
                        "type": "type_ranges_reset",
                        "status": "ok",
                    })
                except Exception as exc:
                    logger.exception("reset_type_ranges failed")
                    await ws.send_json({
                        "type": "type_ranges_reset",
                        "status": "error",
                        "message": str(exc),
                    })

            elif msg_type == "agent_send":
                if agent_task is not None and not agent_task.done():
                    await ws.send_json({"type": "agent_error", "message": "busy"})
                else:
                    text = data.get("text", "")
                    agent_task = asyncio.create_task(claude_agent.send(text))

            elif msg_type == "agent_tool_response":
                _AGENT_APPROVALS.resolve(
                    data.get("request_id", ""),
                    data.get("decision", {}),
                )

            elif msg_type == "agent_stop":
                claude_agent.stop()

            elif msg_type == "agent_new_chat":
                claude_agent.new_chat()
                await ws.send_json({"type": "agent_new_chat_ack"})

    except WebSocketDisconnect:
        logger.info("WebSocket client disconnected")
    finally:
        if ws in _AGENT_BROADCAST:
            _AGENT_BROADCAST.remove(ws)
        if agent_task is not None and not agent_task.done():
            agent_task.cancel()


def _collect_files(task_filter: str | None) -> list[str]:
    """Collect output files for a given task or all tasks."""
    files: list[str] = []

    if task_filter and task_filter in _MANIFEST.phase_ids:
        phase = _MANIFEST.phase(task_filter)
        ext_dir = phase.output_dir
        expected = phase.output_files
        if ext_dir and Path(ext_dir).exists() and expected:
            for name in expected:
                if "*" in name or "?" in name:
                    for fp in sorted(Path(ext_dir).glob(name)):
                        if fp.is_file():
                            rel = fp.relative_to(T2M_OUTPUT)
                            files.append("@ext/" + str(rel).replace("\\", "/"))
                else:
                    fp = Path(ext_dir) / name
                    if fp.exists() and fp.is_file():
                        rel = fp.relative_to(T2M_OUTPUT)
                        files.append("@ext/" + str(rel).replace("\\", "/"))
    elif not task_filter:
        # All T2M output files
        if T2M_OUTPUT.exists():
            for f in sorted(T2M_OUTPUT.rglob("*")):
                if f.is_file():
                    rel = f.relative_to(T2M_OUTPUT)
                    files.append("@ext/" + str(rel).replace("\\", "/"))

    return files


def _clear_output(task_filter: str | None) -> int:
    """Clear output files for a given task or all tasks."""
    import shutil
    count = 0

    if task_filter and task_filter in _MANIFEST.phase_ids:
        phase = _MANIFEST.phase(task_filter)
        ext_dir = phase.output_dir
        expected = phase.output_files
        if ext_dir and expected:
            for name in expected:
                if "*" in name or "?" in name:
                    for fp in sorted(Path(ext_dir).glob(name)):
                        if fp.is_file():
                            fp.unlink()
                            count += 1
                else:
                    fp = Path(ext_dir) / name
                    if fp.exists() and fp.is_file():
                        fp.unlink()
                        count += 1
        for cdir in cmd_cfg.get("clear_dirs", []):
            cdir = Path(cdir)
            if cdir.exists() and cdir.is_dir():
                for f in list(cdir.rglob("*")):
                    if f.is_file():
                        count += 1
                shutil.rmtree(cdir)
    elif not task_filter:
        if T2M_OUTPUT.exists():
            for f in list(T2M_OUTPUT.rglob("*")):
                if f.is_file():
                    f.unlink()
                    count += 1
            for d in sorted(T2M_OUTPUT.rglob("*"), reverse=True):
                if d.is_dir():
                    try:
                        d.rmdir()
                    except OSError:
                        pass

    return count
