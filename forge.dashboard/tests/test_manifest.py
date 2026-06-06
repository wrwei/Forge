from pathlib import Path
import pytest
from web.manifest import Manifest, Phase


FIXTURE = Path(__file__).parent / "fixtures" / "test-manifest.yaml"


def test_loads_phase_definitions():
    manifest = Manifest.load(FIXTURE)
    hello = manifest.phase("hello")
    assert hello.label == "Hello phase"
    assert hello.runner["kind"] == "java"
    assert hello.runner["class"] == "forge.transformations.phase.HelloPhase"
    assert hello.args == {"name": "world"}


def test_resolves_depends_on():
    manifest = Manifest.load(FIXTURE)
    assert manifest.phase("goodbye").depends_on == ["hello"]


def test_rejects_unknown_phase_id():
    manifest = Manifest.load(FIXTURE)
    with pytest.raises(KeyError):
        manifest.phase("nope")


def test_topological_order():
    manifest = Manifest.load(FIXTURE)
    ordered = [p.id for p in manifest.ordered()]
    assert ordered.index("hello") < ordered.index("goodbye")


def test_unresolved_variable_raises(tmp_path):
    bad = tmp_path / "bad.yaml"
    bad.write_text("phases:\n  x:\n    label: bad\n    runner: { kind: java, class: Foo }\n    args: { p: '${unset}' }\n")
    with pytest.raises(ValueError, match="Unresolved variable"):
        Manifest.load(bad)


def test_dump_writes_back_yaml(tmp_path):
    src = Path(__file__).parent / "fixtures" / "test-manifest.yaml"
    dst = tmp_path / "test-manifest.yaml"
    dst.write_text(src.read_text(encoding="utf-8"), encoding="utf-8")
    manifest = Manifest.load(dst)

    # Mutate via the public dump-update path.
    manifest._raw.setdefault("agent", {})["active_case_study"] = "demo"
    manifest.dump(dst)

    import yaml
    reloaded = yaml.safe_load(dst.read_text(encoding="utf-8"))
    assert reloaded["agent"]["active_case_study"] == "demo"
    assert "hello" in reloaded["phases"]  # original phases preserved
