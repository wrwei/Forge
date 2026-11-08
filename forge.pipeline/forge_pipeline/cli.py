"""Command line: ``python -m forge_pipeline <command>``.

run-iteration [--phases ...]   every phase in order (the evaluation's driver)
run-phase <id>                 one phase
audit <output-dir> [--java-root DIR] [--type-ranges FILE] [--json]
                               the vacuity audit over an artefact directory
csp-corrections [pipeline.yaml]
                               narrow instantiations.csp's type ranges
"""
from __future__ import annotations

import argparse
import json
import logging
import sys
from pathlib import Path

from .iteration import PHASES_IN_ORDER, Iteration
from .paths import RepoPaths
from .sink import ConsoleSink


def _utf8_stdout() -> None:
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass


def cmd_run_iteration(args: argparse.Namespace) -> int:
    it = Iteration(RepoPaths.from_root(args.repo), ConsoleSink(show_stream=args.verbose))
    phases = args.phases or PHASES_IN_ORDER
    outcomes = []
    for phase_id in phases:
        print(f"\n===== {phase_id} =====", flush=True)
        o = it.run_phase(phase_id)
        outcomes.append(o)
        if o.source == "post" and o.status != o.exec_status:
            print(f"===== {phase_id}: {o.status} ({o.seconds:.1f}s) "
                  f"[authoritative post_{phase_id}.json; execution status was '{o.exec_status}'] =====", flush=True)
        else:
            tag = {"post": "", "stale": " [not run -- previous verdict discarded]"}.get(
                o.source, " [execution status — no post file]")
            print(f"===== {phase_id}: {o.status} ({o.seconds:.1f}s){tag} =====", flush=True)
    from .iteration import write_timings
    write_timings(it.paths.feedback_dir, outcomes)

    print("\n===== SUMMARY (authoritative post_<phase>.json verdicts) =====", flush=True)
    width = max(len(o.phase) for o in outcomes) + 2
    for o in outcomes:
        print(f"  {'    ' if o.ok else 'FAIL'} {o.phase:<{width}} {o.status:<12} {o.seconds:>6.1f}s", flush=True)
    failures = [o.phase for o in outcomes if not o.ok]
    if failures:
        print(f"\nFAILED phases (authoritative): {', '.join(failures)}", flush=True)
    return 1 if failures else 0


def cmd_run_phase(args: argparse.Namespace) -> int:
    it = Iteration(RepoPaths.from_root(args.repo), ConsoleSink(show_stream=args.verbose))
    o = it.run_phase(args.phase)
    print(f"{o.phase}: {o.status} ({o.seconds:.1f}s) [{o.source}]")
    return 0 if o.ok else 1


def cmd_audit(args: argparse.Namespace) -> int:
    from .audit import audit
    paths = RepoPaths.from_root(args.repo)
    report = audit(Path(args.output_dir).resolve(), paths.root,
                   java_root=Path(args.java_root).resolve() if args.java_root else None,
                   type_ranges_path=Path(args.type_ranges) if args.type_ranges else None)
    if args.json:
        print(json.dumps({"findings": [f.__dict__ for f in report.findings],
                          "details": report.details}, indent=2, default=str))
    else:
        print(f"{report.status}: {len(report.blocking)} blocking, {len(report.advisory)} advisory")
        for f in report.findings:
            print(f"  [{f.signal}/{f.severity}] {f.title}  ({f.artifact})")
    return 1 if report.blocking else 0


def cmd_csp_corrections(args: argparse.Namespace) -> int:
    from .csp.corrections import apply_csp_corrections
    paths = RepoPaths.from_root(args.repo)
    result = apply_csp_corrections(args.manifest or str(paths.pipeline_yaml))
    print(f"Corrected: {result}" if result else "No corrections applied.")
    return 0


def main(argv: list[str] | None = None) -> int:
    _utf8_stdout()
    p = argparse.ArgumentParser(prog="forge_pipeline", description=__doc__,
                                formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--repo", default=None, help="repository root (default: the one this package sits in)")
    p.add_argument("-v", "--verbose", action="store_true", help="show tool output lines")
    p.add_argument("--log", default="WARNING")
    sub = p.add_subparsers(dest="command", required=True)

    s = sub.add_parser("run-iteration"); s.add_argument("--phases", nargs="*", default=None)
    s.set_defaults(fn=cmd_run_iteration)
    s = sub.add_parser("run-phase"); s.add_argument("phase"); s.set_defaults(fn=cmd_run_phase)
    s = sub.add_parser("audit"); s.add_argument("output_dir"); s.add_argument("--java-root")
    s.add_argument("--type-ranges"); s.add_argument("--json", action="store_true"); s.set_defaults(fn=cmd_audit)
    s = sub.add_parser("csp-corrections"); s.add_argument("manifest", nargs="?"); s.set_defaults(fn=cmd_csp_corrections)

    args = p.parse_args(argv)
    logging.basicConfig(level=getattr(logging, args.log.upper(), logging.WARNING),
                        format="%(levelname)s %(name)s: %(message)s")
    return args.fn(args)
