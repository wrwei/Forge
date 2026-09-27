"""Regression tests for the 2026-08 false-pass audit of the vacuity
and coverage checks.

Each test pins a path where the check previously reported "passed"
while inspecting nothing (empty-set vacuous pass), missing a compound
form, or swallowing an exception/unreadable input.
"""
import json
from pathlib import Path

from web import vacuity
from web.feedback.coverage import build_coverage_issues


# ---------------------------------------------------------------------------
# vacuity: D1 (Dafny)
# ---------------------------------------------------------------------------

def test_d1_fires_on_non_ghost_predicate_valid(tmp_path):
    # The old regex required the literal `ghost` modifier.
    (tmp_path / "A.dfy").write_text(
        "class C { predicate Valid() reads this { true } }")
    r = vacuity.audit(tmp_path, tmp_path)
    assert r.status == "failed"
    assert [f.kind for f in r.findings] == ["dafny_valid_vacuous"]


def test_d1_fires_on_multi_reads_clause(tmp_path):
    # The old regex required exactly `reads this`.
    (tmp_path / "A.dfy").write_text(
        "class C { ghost predicate Valid()\n reads this, other\n{ true } }")
    r = vacuity.audit(tmp_path, tmp_path)
    assert r.status == "failed"


def test_d1_fires_when_valid_absent_and_no_behavioural_ensures(tmp_path):
    # Absent construct used to be an automatic pass.
    (tmp_path / "A.dfy").write_text(
        "class C { method step() modifies this { } }")
    r = vacuity.audit(tmp_path, tmp_path)
    assert r.status == "failed"
    assert r.findings[0].kind == "dafny_valid_vacuous"


def test_d1_silent_when_valid_absent_but_behavioural_ensures_active(tmp_path):
    (tmp_path / "A.dfy").write_text(
        "class C {\n  method step()\n"
        "    ensures camActive ==> mode == CAM\n  { }\n}\n")
    r = vacuity.audit(tmp_path, tmp_path)
    assert r.status == "passed"


def test_d1_commented_ensures_true_not_behavioural(tmp_path):
    # `ensures true  // comment` used to count as a behavioural clause
    # because the trailing comment defeated the `true` exclusion.
    (tmp_path / "A.dfy").write_text(
        "class C { ghost predicate Valid() reads this { true }\n"
        " lemma L() ensures true // if-else chain guarantees\n { } }")
    r = vacuity.audit(tmp_path, tmp_path)
    assert r.status == "failed"


def test_d1_this_qualified_mode_init_not_behavioural(tmp_path):
    # `ensures this.mode == OCM` is an initial-state assertion like
    # `ensures mode == OCM`; the compound (this.) form was missed.
    (tmp_path / "A.dfy").write_text(
        "class C { ghost predicate Valid() reads this { true }\n"
        " constructor() ensures this.mode == OCM { } }")
    r = vacuity.audit(tmp_path, tmp_path)
    assert r.status == "failed"


def test_d1_silent_on_nontrivial_valid(tmp_path):
    (tmp_path / "A.dfy").write_text(
        "class C { ghost predicate Valid() reads this { cstc >= 0 } }")
    r = vacuity.audit(tmp_path, tmp_path)
    assert r.status == "passed"


# ---------------------------------------------------------------------------
# vacuity: I1 (Isabelle)
# ---------------------------------------------------------------------------

def _write_ok_dfy(tmp_path):
    (tmp_path / "A.dfy").write_text(
        "class C { ghost predicate Valid() reads this { x >= 0 } }")


def test_i1_fires_when_inv_clause_absent(tmp_path):
    # Z-Machines defaults a missing inv to True — same vacuity.
    _write_ok_dfy(tmp_path)
    thy = tmp_path / "isabelle"
    thy.mkdir()
    (thy / "T.thy").write_text(
        'zstore C =\n  x :: "nat"\n\nzoperation Foo = over C\n')
    r = vacuity.audit(tmp_path, tmp_path)
    assert r.status == "failed"
    assert r.findings[0].kind == "isabelle_inv_vacuous"


def test_i1_fires_on_second_zstore_with_true_inv(tmp_path):
    # The old DOTALL regex only examined the first zstore/inv pairing.
    _write_ok_dfy(tmp_path)
    thy = tmp_path / "isabelle"
    thy.mkdir()
    (thy / "T.thy").write_text(
        'zstore A =\n  x :: "nat"\n  where inv:\n  "x > 0"\n\n'
        'zstore B =\n  y :: "nat"\n  where inv:\n  "True"\n')
    r = vacuity.audit(tmp_path, tmp_path)
    assert r.status == "failed"


def test_i1_silent_on_nontrivial_inv_with_following_zoperation(tmp_path):
    # `pre "True"` inside a following zoperation must not be confused
    # with the zstore's inv clause.
    _write_ok_dfy(tmp_path)
    thy = tmp_path / "isabelle"
    thy.mkdir()
    (thy / "T.thy").write_text(
        'zstore A =\n  x :: "nat"\n  where inv:\n  "x > 0"\n\n'
        'zoperation Foo =\n  over A\n  pre "True"\n')
    r = vacuity.audit(tmp_path, tmp_path)
    assert r.status == "passed"


# ---------------------------------------------------------------------------
# vacuity: empty artefact set
# ---------------------------------------------------------------------------

def test_audit_fails_on_empty_artefact_set(tmp_path):
    # Zero artefacts used to pass — the audit inspected nothing.
    r = vacuity.audit(tmp_path, tmp_path)
    assert r.status == "failed"
    assert r.findings[0].kind == "vacuity_no_artefacts"


# ---------------------------------------------------------------------------
# coverage
# ---------------------------------------------------------------------------

def _write_requirements(tmp_path):
    req = tmp_path / "requirement_all.json"
    req.write_text(json.dumps(
        [{"id": "R1", "name": "reqOne"}, {"id": "R2", "name": "reqTwo"}]))
    return req


def test_coverage_blocks_on_missing_java_source_root(tmp_path):
    # A complete trace over a nonexistent Java tree used to pass.
    req = _write_requirements(tmp_path)
    trace = tmp_path / "result_codegen.json"
    trace.write_text(json.dumps({"codegen_trace": [
        {"requirement_gid": "R1", "java_file": "C.java", "java_element": "C"},
        {"requirement_gid": "R2", "java_file": "C.java", "java_element": "C"},
    ]}))
    issues = build_coverage_issues(req, trace, tmp_path / "no-such-src")
    assert [i.kind for i in issues] == ["coverage_input_missing"]


def test_coverage_blocks_on_java_free_source_root(tmp_path):
    req = _write_requirements(tmp_path)
    trace = tmp_path / "result_codegen.json"
    trace.write_text(json.dumps({"codegen_trace": []}))
    src = tmp_path / "java"
    src.mkdir()
    issues = build_coverage_issues(req, trace, src)
    assert [i.kind for i in issues] == ["coverage_input_missing"]


def test_coverage_flags_stale_trace_entry(tmp_path):
    req = _write_requirements(tmp_path)
    src = tmp_path / "java"
    src.mkdir()
    (src / "C.java").write_text(
        "package x;\npublic class C {\n"
        "  public void step(Object e) { }\n}\n")
    trace = tmp_path / "result_codegen.json"
    trace.write_text(json.dumps({"codegen_trace": [
        {"requirement_gid": "R1", "java_file": "C.java", "java_element": "C"},
        {"requirement_gid": "R2", "java_file": "Gone.java",
         "java_element": "Gone"},
    ]}))
    issues = build_coverage_issues(req, trace, src)
    assert "stale_trace_entry" in [i.kind for i in issues]


def test_coverage_matches_qualified_trace_element_names(tmp_path):
    # `C.step` in the trace must cover the scanner's simple name `step`
    # (compound-form miss: `this.cstc` vs `cstc` class of bug).
    req = _write_requirements(tmp_path)
    src = tmp_path / "java"
    src.mkdir()
    (src / "C.java").write_text(
        "package x;\npublic class C {\n"
        "  public void step(Object e) { }\n}\n")
    trace = tmp_path / "result_codegen.json"
    trace.write_text(json.dumps({"codegen_trace": [
        {"requirement_gid": "R1", "java_file": "C.java", "java_element": "C"},
        {"requirement_gid": "R2", "java_file": "C.java",
         "java_element": "C.step"},
    ]}))
    issues = build_coverage_issues(req, trace, src)
    assert issues == []
