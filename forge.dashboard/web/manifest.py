"""Pipeline manifest loader. Mirrors forge.transformations.phase.Manifest.

Uses ruamel.yaml in round-trip mode so ``Manifest.dump()`` preserves
the comments, quoting, and unicode characters of the on-disk
pipeline.yaml. PyYAML's ``safe_dump`` strips comments and escapes
unicode (em-dashes -> ``\\u2014``); ruamel.yaml's round-trip parser
keeps both. The dashboard's POST /api/active-case-study writes the
file back on every dropdown change, so non-destructive round-trip is
required.
"""
from __future__ import annotations

import json
from dataclasses import dataclass, field
from io import StringIO
from pathlib import Path
from typing import Any

from jsonschema import validate
from ruamel.yaml import YAML


_REPO_ROOT = Path(__file__).resolve().parents[2]
_SCHEMA_PATH = _REPO_ROOT / "docs" / "superpowers" / "specs" / "pipeline-manifest.schema.json"


def _new_yaml() -> YAML:
    """Build the round-trip-configured YAML instance used for load + dump.

    Settings:
      - typ='rt'                 round-trip mode (comments, quotes, ordering)
      - preserve_quotes=True     keep explicit quoting from the source
      - width=4096               don't auto-wrap long lines
      - indent(mapping=2,
               sequence=4,
               offset=2)         matches the file's existing 2-space style
      - allow_unicode default    ruamel preserves unicode without escaping
    """
    yaml = YAML(typ="rt")
    yaml.preserve_quotes = True
    yaml.width = 4096
    yaml.indent(mapping=2, sequence=4, offset=2)
    return yaml


@dataclass(frozen=True)
class Phase:
    """One phase entry from pipeline.yaml."""
    id: str
    label: str
    runner: dict[str, Any]
    args: dict[str, str] = field(default_factory=dict)
    depends_on: list[str] = field(default_factory=list)
    output_dir: Path | None = None
    output_files: list[str] = field(default_factory=list)
    clear_dirs: list[Path] = field(default_factory=list)
    config: dict[str, Any] = field(default_factory=dict)


class Manifest:
    def __init__(self, output_dir: Path, phases: dict[str, Phase], raw: dict | None = None) -> None:
        self._output_dir = output_dir
        self._phases = phases
        self._raw = raw or {}

    @classmethod
    def load(cls, path: Path) -> "Manifest":
        # Use ruamel's round-trip loader so comments / quoting / unicode
        # survive the next dump(). The returned object is a CommentedMap
        # (subclass of dict) so the rest of _parse, jsonschema.validate,
        # and _resolve_deep keep working unchanged.
        yaml = _new_yaml()
        raw = yaml.load(path.read_text(encoding="utf-8"))
        repo_root = path.resolve().parent
        manifest = cls._parse(raw, repo_root)
        manifest._yaml = yaml
        return manifest

    @classmethod
    def _parse(cls, raw: dict[str, Any], repo_root: Path) -> "Manifest":
        schema = json.loads(_SCHEMA_PATH.read_text(encoding="utf-8"))
        validate(raw, schema)

        top_vars: dict[str, str] = {"repo": str(repo_root)}
        for key, value in raw.items():
            if isinstance(value, str):
                top_vars[key] = _resolve_vars(value, top_vars)

        output_dir = Path(top_vars.get("output_dir", str(repo_root)))

        phases: dict[str, Phase] = {}
        for phase_id, body in raw.get("phases", {}).items():
            # Coerce every leaf string to plain str — ruamel's round-trip
            # loader returns str subclasses (DoubleQuotedScalarString etc.)
            # that some downstream consumers (notably subprocess argv on
            # Windows, which interns via sys.intern) refuse to accept.
            args = {
                str(k): _resolve_vars(v, top_vars) if isinstance(v, str) else v
                for k, v in body.get("args", {}).items()
            }
            config = _resolve_deep(
                {str(k): v for k, v in body.items() if k not in {
                    "label", "runner", "args", "depends_on",
                    "output_dir", "output_files", "clear_dirs",
                }},
                top_vars,
            )
            phase_output_dir = body.get("output_dir")
            phases[str(phase_id)] = Phase(
                id=str(phase_id),
                label=str(body["label"]),
                # runner may carry ${...} inside sequence step args / props;
                # walk it recursively so inner steps see resolved values.
                runner=_resolve_deep(body["runner"], top_vars),
                args=args,
                depends_on=[str(d) for d in body.get("depends_on", [])],
                output_dir=Path(_resolve_vars(phase_output_dir, top_vars))
                    if phase_output_dir else None,
                output_files=[str(f) for f in body.get("output_files", [])],
                clear_dirs=[Path(_resolve_vars(d, top_vars))
                            for d in body.get("clear_dirs", [])],
                config=config,
            )

        for phase in phases.values():
            for dep in phase.depends_on:
                if dep not in phases:
                    raise ValueError(
                        f"Phase '{phase.id}' depends on unknown phase '{dep}'")

        return cls(output_dir, phases, raw)

    def dump(self, path: Path) -> None:
        """Write self._raw back to path as YAML, preserving everything
        ruamel's round-trip mode tracks: comments (block and side),
        explicit quoting, unicode characters, and key ordering.

        Falls back to a fresh round-trip YAML instance if the manifest
        was constructed without going through ``load()`` (e.g. in
        tests that build a Manifest directly from a dict).
        """
        yaml = getattr(self, "_yaml", None) or _new_yaml()
        buf = StringIO()
        yaml.dump(self._raw, buf)
        path.write_text(buf.getvalue(), encoding="utf-8")

    def phase(self, phase_id: str) -> Phase:
        if phase_id not in self._phases:
            raise KeyError(phase_id)
        return self._phases[phase_id]

    def ordered(self) -> list[Phase]:
        """Topological sort by depends_on; insertion order for ties."""
        sorted_ids: list[str] = []
        visited: set[str] = set()

        def visit(pid: str) -> None:
            if pid in visited:
                return
            visited.add(pid)
            for dep in self._phases[pid].depends_on:
                visit(dep)
            sorted_ids.append(pid)

        for pid in self._phases:
            visit(pid)
        return [self._phases[pid] for pid in sorted_ids]

    @property
    def output_dir(self) -> Path:
        return self._output_dir

    @property
    def phase_ids(self) -> list[str]:
        return list(self._phases.keys())


def _resolve_deep(value, vars: dict[str, str]):
    """Recursively walk dicts/lists, applying _resolve_vars to every string
    leaf. Used for the runner block where ${...} can appear at any depth
    (e.g. runner.steps[*].args.output, runner.steps[*].props.<key>).

    Coerces all string keys and leaves to plain ``str`` so ruamel.yaml's
    round-trip scalar subclasses don't leak into Phase fields (see the
    docstring of _resolve_vars for why this matters).
    """
    if isinstance(value, str):
        return _resolve_vars(value, vars)
    if isinstance(value, dict):
        return {str(k): _resolve_deep(v, vars) for k, v in value.items()}
    if isinstance(value, list):
        return [_resolve_deep(v, vars) for v in value]
    return value


def _resolve_vars(s: str, vars: dict[str, str]) -> str:
    """Substitute ``${var}`` placeholders and return a plain ``str``.

    Coerces to ``str()`` at the end because ruamel.yaml's round-trip
    loader returns scalars as str-subclasses (``DoubleQuotedScalarString``,
    ``SingleQuotedScalarString``, ``PlainScalarString``). Python's
    subprocess machinery interns argv on some platforms via
    ``sys.intern()``, which refuses str subclasses with
    ``TypeError: can't intern <subclass>``. We coerce *here* — at the
    one point all string-valued manifest leaves flow through — so the
    Phase dataclass and its consumers never see a ruamel scalar.
    """
    out = s
    for key, value in vars.items():
        out = out.replace("${" + key + "}", str(value))
    if "${" in out:
        raise ValueError(f"Unresolved variable in: {s}")
    return str(out)
