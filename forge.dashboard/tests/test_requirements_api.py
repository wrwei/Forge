"""Tests for the requirements/case-studies HTTP endpoints."""
import pytest
from fastapi.testclient import TestClient

from web import server


@pytest.fixture
def client():
    return TestClient(server.app)


def test_lists_available_case_studies(client):
    resp = client.get("/api/case-studies")
    assert resp.status_code == 200
    data = resp.json()
    assert "active" in data
    assert "available" in data
    assert isinstance(data["available"], list)
    assert "lre" in data["available"]
    assert data["active"] == "lre"


def test_lists_requirements_for_active_study(client):
    resp = client.get("/api/requirements")
    assert resp.status_code == 200
    data = resp.json()
    assert data["study"] == "lre"
    names = [r["name"] for r in data["requirements"]]
    assert "requirement_all.json" in names
    assert any(n.startswith("tier1") for n in names)
    assert "system/system_description.txt" in data["system_description_path"]


def test_requirements_for_explicit_study(client):
    resp = client.get("/api/requirements?study=lre")
    assert resp.status_code == 200
    assert resp.json()["study"] == "lre"


def test_requirements_for_missing_study(client):
    resp = client.get("/api/requirements?study=nonexistent")
    assert resp.status_code == 404


from pathlib import Path


def test_set_active_case_study_persists_to_manifest(client, tmp_path, monkeypatch):
    """Setting the active case study should write back to pipeline.yaml
    and update the in-memory manifest."""
    src = Path(server.__file__).resolve().parents[2] / "pipeline.yaml"
    dst = tmp_path / "pipeline.yaml"
    dst.write_text(src.read_text(encoding="utf-8"), encoding="utf-8")

    from web.manifest import Manifest
    monkeypatch.setattr(server, "_MANIFEST_PATH", dst)
    monkeypatch.setattr(server, "_MANIFEST", Manifest.load(dst))

    resp = client.post("/api/active-case-study", json={"study": "lre"})
    assert resp.status_code == 200
    assert resp.json()["active"] == "lre"

    import yaml
    reloaded = yaml.safe_load(dst.read_text(encoding="utf-8"))
    assert reloaded["agent"]["active_case_study"] == "lre"


def test_set_active_case_study_rejects_unknown(client):
    resp = client.post("/api/active-case-study", json={"study": "nonexistent"})
    assert resp.status_code == 400
