"""Repository layout, in one place.

Every other module asks :class:`RepoPaths` where things are instead of
computing ``Path(__file__).parents[n]``. The package lives at
``<repo>/forge.pipeline/forge_pipeline/``; the repository root is therefore
two levels above this file's directory. ``RepoPaths.from_root`` builds the
same layout for any other root (a worktree, an archived run).
"""
from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

_PACKAGE_DIR = Path(__file__).resolve().parent
DEFAULT_REPO_ROOT = _PACKAGE_DIR.parent.parent


@dataclass(frozen=True)
class RepoPaths:
    root: Path

    @classmethod
    def from_root(cls, root: Path | str | None = None) -> "RepoPaths":
        return cls(Path(root).resolve() if root else DEFAULT_REPO_ROOT)

    # -- inputs -----------------------------------------------------------
    @property
    def pipeline_yaml(self) -> Path:
        return self.root / "pipeline.yaml"

    @property
    def transformations(self) -> Path:
        return self.root / "forge.transformations"

    @property
    def transformations_output(self) -> Path:
        return self.transformations / "output"

    @property
    def generated_project(self) -> Path:
        return self.root / "java.generated.project"

    @property
    def generated_src(self) -> Path:
        return self.generated_project / "src" / "main" / "java"

    @property
    def codegen_trace(self) -> Path:
        return self.generated_project / "result_codegen.json"

    @property
    def case_studies(self) -> Path:
        return self.root / "forge.assets" / "case-studies"

    # -- outputs ----------------------------------------------------------
    @property
    def feedback_dir(self) -> Path:
        """Where the per-phase ``post_<phase>.{md,json}`` reports go."""
        return self.root / "forge.assets" / "corrections"

    @property
    def llm_cache_dir(self) -> Path:
        return self.feedback_dir / ".llm_cache"

    # -- package data -----------------------------------------------------
    @property
    def manifest_schema(self) -> Path:
        return _PACKAGE_DIR / "manifest.schema.json"

    @property
    def csp_data_dir(self) -> Path:
        """Default CSP type ranges (``type_ranges.json``) and overrides."""
        return _PACKAGE_DIR / "csp" / "data"

    @property
    def gradlew(self) -> str:
        import sys
        return "gradlew.bat" if sys.platform == "win32" else "./gradlew"


DEFAULT = RepoPaths.from_root()
