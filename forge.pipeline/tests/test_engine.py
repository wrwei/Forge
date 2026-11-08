"""The engine, the registry and the iteration driver, without running any tool."""
import json
import time
from pathlib import Path

import pytest

from forge_pipeline.engine import PipelineEngine, count_lint_errors
from forge_pipeline.iteration import authoritative_status
from forge_pipeline.manifest import Manifest
from forge_pipeline.paths import RepoPaths
from forge_pipeline.phases import REGISTRY, PhaseContext, Registry
from forge_pipeline.sink import RecordingSink
from forge_pipeline.state import SessionState

FIXTURES = Path(__file__).parent / "fixtures"


def test_registry_resolves_legacy_names_to_the_new_modules():
    from forge_pipeline.phases import fdr4, vacuity
    assert REGISTRY.resolve("forge.dashboard.web.runners:run_fdr4") is fdr4.run
    assert REGISTRY.resolve("forge_pipeline.phases.vacuity:run") is vacuity.run


def test_registry_rejects_malformed_names():
    with pytest.raises(KeyError):
        Registry().resolve("no_colon_here")
    with pytest.raises(KeyError):
        Registry().resolve("forge_pipeline.phases.fdr4:no_such_function")


def test_python_phase_runs_through_the_registry_and_reports_to_the_sink(tmp_path, monkeypatch):
    calls = []

    def fake_run(ctx: PhaseContext):
        calls.append(ctx.phase.id)
        ctx.sys_msg("hello from the phase")
        return "passed", ""

    import forge_pipeline.phases.vacuity as vac
    monkeypatch.setattr(vac, "run", fake_run)

    manifest = Manifest.load(FIXTURES / "test-manifest.yaml")
    python_phases = [p for p in manifest.ordered() if p.runner["kind"] == "python"]
    if not python_phases:
        pytest.skip("fixture manifest has no python phase")
    phase = python_phases[0]
    phase.runner["function"] = "forge_pipeline.phases.vacuity:run"

    sink = RecordingSink()
    engine = PipelineEngine(manifest, SessionState(), sink, RepoPaths.from_root(tmp_path))
    status, err = engine.run_phase(phase.id)

    assert (status, err) == ("passed", "")
    assert calls == [phase.id]
    assert "hello from the phase" in sink.system_messages
    assert sink.completions == [(phase.id, "passed", "")]
    assert engine.state.get_phase_status(phase.id) == "passed"


def test_unknown_runner_kind_fails_cleanly(tmp_path):
    manifest = Manifest.load(FIXTURES / "test-manifest.yaml")
    phase = manifest.ordered()[0]
    phase.runner["kind"] = "carrier-pigeon"
    sink = RecordingSink()
    engine = PipelineEngine(manifest, SessionState(), sink, RepoPaths.from_root(tmp_path))
    status, err = engine.run_phase(phase.id)
    assert status == "failed" and "Unknown runner kind" in err
    assert sink.completions[-1][1] == "failed"


def test_count_lint_errors(tmp_path):
    assert count_lint_errors(tmp_path / "missing.json") is None
    p = tmp_path / "lint_report.json"
    p.write_text(json.dumps({"violations": [{"severity": "error"}, {"severity": "warning"}, {"severity": "error"}]}))
    assert count_lint_errors(p) == 2
    p.write_text("not json")
    assert count_lint_errors(p) is None


def test_report_verdict_beats_execution_status(tmp_path):
    (tmp_path / "post_fdr4.json").write_text(json.dumps({"status": "failed"}))
    assert authoritative_status(tmp_path, "fdr4", "completed") == ("failed", "post")


def test_missing_report_falls_back_to_execution_status(tmp_path):
    assert authoritative_status(tmp_path, "fdr4", "completed") == ("completed", "exec")


def test_stale_report_is_replaced_by_skipped(tmp_path):
    post = tmp_path / "post_fdr4.json"
    post.write_text(json.dumps({"status": "passed"}))
    started = time.time() + 10          # the phase "started" after the file was written
    status, source = authoritative_status(tmp_path, "fdr4", "completed", started_at=started)
    assert (status, source) == ("skipped", "stale")
    assert json.loads(post.read_text())["status"] == "skipped"
    assert (tmp_path / "post_fdr4.md").exists()


def test_default_audit_keeps_the_report_order():
    from forge_pipeline.audit.checks import default_audit
    a = default_audit()
    assert [type(c).__name__ for c in a.dafny_checks] == ["DafnyValidPlaceholder"]
    assert [type(c).__name__ for c in a.theory_checks] == ["IsabelleInvariantPlaceholder", "IsabelleRequirementPlaceholder"]
    assert [type(c).__name__ for c in a.behaviour_checks] == [
        "PreconditionSatisfiable", "PreconditionSatisfiableUnderEnvironment", "AntecedentHolds",
        "DomainsNonDegenerate", "InitialStateConsistent", "Reachability"]
    assert [type(c).__name__ for c in a.artefact_set_checks] == ["ConstantsAgree", "ArtefactsPresent"]


def test_empty_artefact_directory_is_a_blocking_finding(tmp_path):
    from forge_pipeline.audit import audit
    report = audit(tmp_path, tmp_path)
    assert report.status == "failed"
    assert [f.kind for f in report.findings] == ["vacuity_no_artefacts"]
