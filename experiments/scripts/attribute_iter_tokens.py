#!/usr/bin/env python3
"""Attribute per-iteration token usage to a convergence run, post-hoc.

The live "me-as-developer" session cannot meter its own token usage mid-run, so
each iter's ``summary.json`` records ``codegen_cost`` as null. This script
recovers a per-iter split from the session transcript (a Claude Code ``*.jsonl``
where every assistant turn carries a ``message.usage`` block) by segmenting the
usage blocks on the ``snapshot_iter.py <study> N`` invocations, which mark the
end of each archived iteration exactly:

    iter-1   = [session start, snapshot(1)]   # cold codegen + pipeline + read
    iter-N   = [snapshot(N-1), snapshot(N)]   # fixes + pipeline + read
    write-up = [snapshot(last), session end]  # trajectory + archive (meta)

Usage:
    python experiments/scripts/attribute_iter_tokens.py \
        --transcript <session.jsonl> \
        --run-dir experiments/convergence/<study>/run-<N> \
        --study <study> [--write]

Without --write it only prints the table. With --write it populates each
iter's summary.json ``codegen_cost`` (kind="attributed_posthoc").
"""
import argparse
import datetime
import glob
import json
import os
import sys

FIELDS = ("output_tokens", "input_tokens",
          "cache_read_input_tokens", "cache_creation_input_tokens")


def _parse_ts(s):
    if not s:
        return None
    try:
        return datetime.datetime.fromisoformat(s.replace("Z", "+00:00"))
    except ValueError:
        return None


def load_transcript(path):
    """Return (usages, snapshots, start, end).

    usages: list of (ts, {field: int})
    snapshots: dict {iter_number: ts} from snapshot_iter.py invocations
    """
    usages = []
    snapshots = {}
    all_ts = []
    for line in open(path, encoding="utf-8"):
        try:
            o = json.loads(line)
        except ValueError:
            continue
        ts = _parse_ts(o.get("timestamp"))
        if ts:
            all_ts.append(ts)
        msg = o.get("message") or {}
        u = msg.get("usage")
        if u and ts:
            usages.append((ts, {f: int(u.get(f) or 0) for f in FIELDS}))
        content = msg.get("content")
        if isinstance(content, list):
            for c in content:
                if isinstance(c, dict) and c.get("type") == "tool_use":
                    cmd = (c.get("input") or {}).get("command", "")
                    if "snapshot_iter.py" in cmd and ts:
                        # ...snapshot_iter.py <study> <N> ...
                        toks = cmd.split("snapshot_iter.py", 1)[1].split()
                        for t in toks:
                            if t.isdigit():
                                snapshots[int(t)] = ts
                                break
    start = min(all_ts) if all_ts else None
    end = max(all_ts) if all_ts else None
    return usages, snapshots, start, end


def segment(usages, snapshots, start, end):
    """Return ordered list of (label, lo, hi, totals)."""
    iters = sorted(snapshots)
    bounds = []  # (label, lo, hi)
    prev = start
    for n in iters:
        bounds.append((f"iter-{n}", prev, snapshots[n]))
        prev = snapshots[n]
    bounds.append(("write-up", prev, end))
    out = []
    for label, lo, hi in bounds:
        tot = {f: 0 for f in FIELDS}
        for ts, u in usages:
            # inclusive of hi so the snapshot turn itself counts to that iter
            if (lo is None or ts > lo or (label.startswith("iter-1") and ts >= lo)) and (hi is None or ts <= hi):
                for f in FIELDS:
                    tot[f] += u[f]
        out.append((label, lo, hi, tot))
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--transcript", required=True)
    ap.add_argument("--run-dir", required=True)
    ap.add_argument("--study", required=True)
    ap.add_argument("--write", action="store_true")
    a = ap.parse_args()

    usages, snapshots, start, end = load_transcript(a.transcript)
    if not snapshots:
        sys.exit("no snapshot_iter.py invocations found in transcript")
    segs = segment(usages, snapshots, start, end)

    run_total = {f: sum(u[f] for _, u in usages) for f in FIELDS}
    print(f"transcript: {os.path.basename(a.transcript)}")
    print(f"iters detected: {sorted(snapshots)}   span: {start} -> {end}")
    print(f"{'segment':10} {'output':>10} {'input':>8} {'cache_read':>12} {'cache_creat':>12}")
    for label, lo, hi, tot in segs:
        print(f"{label:10} {tot['output_tokens']:>10,} {tot['input_tokens']:>8,} "
              f"{tot['cache_read_input_tokens']:>12,} {tot['cache_creation_input_tokens']:>12,}")
    print(f"{'RUN TOTAL':10} {run_total['output_tokens']:>10,} {run_total['input_tokens']:>8,} "
          f"{run_total['cache_read_input_tokens']:>12,} {run_total['cache_creation_input_tokens']:>12,}")

    if a.write:
        for label, lo, hi, tot in segs:
            if not label.startswith("iter-"):
                continue
            sj = os.path.join(a.run_dir, label, "summary.json")
            if not os.path.exists(sj):
                print(f"  (skip {label}: no {sj})")
                continue
            d = json.load(open(sj, encoding="utf-8"))
            d["codegen_cost"] = {
                "kind": "attributed_posthoc",
                "tokens_out": tot["output_tokens"],
                "tokens_in": tot["input_tokens"],
                "cache_read_input_tokens": tot["cache_read_input_tokens"],
                "cache_creation_input_tokens": tot["cache_creation_input_tokens"],
                "tool_uses": None,
                "wall_clock_s": None,
                "notes": ("per-iter split recovered post-hoc by attribute_iter_tokens.py: "
                          "usage blocks segmented on snapshot_iter boundaries "
                          "(write-up segment excluded)"),
            }
            json.dump(d, open(sj, "w", encoding="utf-8"), indent=2)
            print(f"  wrote codegen_cost -> {sj}")


if __name__ == "__main__":
    main()
