#!/usr/bin/env python3
"""Generate the synthetic N-mode controllers of the size check (paper Section 5.4), following the
generic instructions and the SRanger reference's shape: a mode enum, a sensor with one real
reading per mode, a constants class with one threshold per mode, sealed input/output events,
and a controller whose step() computes one boolean per reading and dispatches on the mode
with an `else if` chain ending in a Tick self-transition. Also writes each controller's
requirements file (one requirement per transition) and codegen trace.
usage: make_synthetic.py <out-dir> N [N ...]"""
import json, os, sys

ANNOT = """package syn%(n)02d.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    String value();
}
"""

def gen(n, out):
    pkg = f"syn{n:02d}"; m = n - 1          # modes M1..Mm and Final
    modes = [f"M{i}" for i in range(1, m + 1)] + ["Final"]
    J = os.path.join(out, f"synthetic_m{n:02d}", "java"); os.makedirs(J, exist_ok=True)
    def w(rel, text):
        p = os.path.join(J, rel); os.makedirs(os.path.dirname(p), exist_ok=True); open(p, "w").write(text)
    w("annotation/RoboChartType.java", ANNOT % {"n": n})
    w("mode/SynMode.java", f"package {pkg}.mode;\n\npublic enum SynMode {{\n" + ",\n".join(f"    {x}" for x in modes) + "\n}\n")
    w("event/InputEvent.java", f"package {pkg}.event;\n\npublic sealed interface InputEvent {{\n    record Advance() implements InputEvent {{}}\n    record Retreat() implements InputEvent {{}}\n    record Tick() implements InputEvent {{}}\n    record EndTask() implements InputEvent {{}}\n}}\n")
    w("event/OutputEvent.java", f"package {pkg}.event;\n\nimport {pkg}.annotation.RoboChartType;\n\npublic sealed interface OutputEvent {{\n\n    record Move(@RoboChartType(\"real\") double lv, @RoboChartType(\"real\") double av) implements OutputEvent {{}}\n}}\n")
    consts = "\n".join(f"    @RoboChartType(\"real\")\n    public static final double threshold{i} = {0.5 + 0.25 * i:.2f};\n" for i in range(1, m + 1))
    w("constants/SynConstants.java", f"package {pkg}.constants;\n\nimport {pkg}.annotation.RoboChartType;\n\npublic final class SynConstants {{\n\n    @RoboChartType(\"real\")\n    public static final double moveVel = 1.0;\n\n{consts}\n    private SynConstants() {{\n    }}\n}}\n")
    fields = "\n".join(f"    @RoboChartType(\"real\")\n    private double reading{i} = 0.0;\n" for i in range(1, m + 1))
    setters = "\n".join(f"    public void updateReading{i}(@RoboChartType(\"real\") double value) {{\n        this.reading{i} = value;\n    }}\n" for i in range(1, m + 1))
    getters = "\n".join(f"    @RoboChartType(\"real\")\n    public double reading{i}() {{\n        return reading{i};\n    }}\n" for i in range(1, m + 1))
    w("sensor/Sensor.java", f"package {pkg}.sensor;\n\nimport {pkg}.annotation.RoboChartType;\n\npublic final class Sensor {{\n\n{fields}\n{setters}\n{getters}}}\n")
    w("actuator/Actuator.java", f"package {pkg}.actuator;\n\nimport {pkg}.annotation.RoboChartType;\nimport {pkg}.event.OutputEvent;\n\npublic final class Actuator {{\n\n    @RoboChartType(\"real\")\n    private double lastLv = 0.0;\n\n    @RoboChartType(\"real\")\n    private double lastAv = 0.0;\n\n    public void apply(OutputEvent.Move move) {{\n        this.lastLv = move.lv();\n        this.lastAv = move.av();\n    }}\n\n    @RoboChartType(\"real\")\n    public double lastLv() {{\n        return lastLv;\n    }}\n\n    @RoboChartType(\"real\")\n    public double lastAv() {{\n        return lastAv;\n    }}\n}}\n")
    # controller
    preds = "\n".join(f"        boolean below{i} = sensor.reading{i}() <= SynConstants.threshold{i};" for i in range(1, m + 1))
    reqs = [{"id": "SYN-ARCH1", "name": "SystemArchitecture", "kind": "Non-Functional Requirement", "priority": "1", "types": ["architecture"],
             "description": f"The controller has {n} modes ({', '.join(modes)}), reads {m} real-valued sensor readings, and issues a combined linear/angular velocity command. It starts in M1."}]
    trace = []
    blocks = []; t = 0
    for i in range(1, m + 1):
        nxt = f"M{i + 1}" if i < m else "M1"; prv = f"M{i - 1}" if i > 1 else "M1"
        t += 1; reqs.append({"id": f"SYN-T{t}", "name": f"M{i}EndTask", "kind": "Functional Requirement", "priority": "1", "types": ["behaviour"], "description": f"In mode M{i}, on EndTask the controller enters Final and commands zero velocity."})
        t += 1; reqs.append({"id": f"SYN-T{t}", "name": f"M{i}Advance", "kind": "Functional Requirement", "priority": "1", "types": ["behaviour"], "description": f"In mode M{i}, on Advance when reading{i} is at most threshold{i}, the controller enters {nxt} and commands forward velocity."})
        t += 1; reqs.append({"id": f"SYN-T{t}", "name": f"M{i}Retreat", "kind": "Functional Requirement", "priority": "1", "types": ["behaviour"], "description": f"In mode M{i}, on Retreat when reading{i} exceeds threshold{i}, the controller enters {prv} and commands zero velocity."})
        t += 1; reqs.append({"id": f"SYN-T{t}", "name": f"M{i}Tick", "kind": "Functional Requirement", "priority": "1", "types": ["behaviour"], "description": f"In mode M{i}, a Tick leaves the controller in M{i}."})
        blocks.append(f"""        {"if" if i == 1 else "}} else if"} (currentMode == SynMode.M{i}) {{
            if (event instanceof InputEvent.EndTask) {{
                currentMode = SynMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }}
            else if (event instanceof InputEvent.Advance && below{i}) {{
                currentMode = SynMode.{nxt};
                actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
            }}
            else if (event instanceof InputEvent.Retreat && !below{i}) {{
                currentMode = SynMode.{prv};
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }}
            else if (event instanceof InputEvent.Tick) {{
                currentMode = SynMode.M{i};
            }}
""".replace("{{", "{").replace("}}", "}"))
    t += 1; reqs.append({"id": f"SYN-T{t}", "name": "FinalTick", "kind": "Functional Requirement", "priority": "1", "types": ["behaviour"], "description": "In mode Final, a Tick leaves the controller in Final."})
    blocks.append("""        } else if (currentMode == SynMode.Final) {
            if (event instanceof InputEvent.Tick) {
                currentMode = SynMode.Final;
            }
        }
""")
    ctrl = f"""package {pkg}.controller;

import {pkg}.actuator.Actuator;
import {pkg}.constants.SynConstants;
import {pkg}.event.InputEvent;
import {pkg}.event.OutputEvent;
import {pkg}.mode.SynMode;
import {pkg}.sensor.Sensor;

public final class SynController {{

    private SynMode currentMode = SynMode.M1;

    private final Sensor sensor;
    private final Actuator actuator;

    public SynController(Sensor sensor, Actuator actuator) {{
        this.sensor = sensor;
        this.actuator = actuator;
        this.actuator.apply(new OutputEvent.Move(SynConstants.moveVel, 0.0));
    }}

    public SynMode currentMode() {{
        return currentMode;
    }}

    public void step(InputEvent event) {{
{preds}

{"".join(blocks)}    }}
}}
"""
    w("controller/SynController.java", ctrl)
    files = {"controller/SynController.java": ("SynController", "class"), "sensor/Sensor.java": ("Sensor", "class"), "actuator/Actuator.java": ("Actuator", "class"),
             "event/InputEvent.java": ("InputEvent", "interface"), "event/OutputEvent.java": ("OutputEvent", "interface"), "mode/SynMode.java": ("SynMode", "enum"),
             "constants/SynConstants.java": ("SynConstants", "class"), "annotation/RoboChartType.java": ("RoboChartType", "annotation")}
    for rel, (el, kind) in files.items():
        trace.append({"requirement_gid": "SYN-ARCH1", "java_file": f"src/main/java/{pkg}/{rel}", "java_element": el, "element_type": kind})
    for r in reqs[1:]:
        trace.append({"requirement_gid": r["id"], "java_file": f"src/main/java/{pkg}/controller/SynController.java", "java_element": "SynController.step", "element_type": "method"})
    S = os.path.join(out, f"synthetic_m{n:02d}"); os.makedirs(os.path.join(S, "requirements"), exist_ok=True); os.makedirs(os.path.join(S, "traces"), exist_ok=True)
    json.dump({"requirements": reqs}, open(os.path.join(S, "requirements", "requirement_all.json"), "w"), indent=2)
    open(os.path.join(S, "requirements", "requirement_all.txt"), "w").write("\n".join(f"{r['id']}: {r['description']}" for r in reqs) + "\n")
    json.dump({"codegen_trace": trace}, open(os.path.join(S, "traces", "result_codegen.json"), "w"), indent=2)
    print(f"m{n:02d}: {n} modes, {4 * m + 1} transitions, {m} readings, {len(reqs)} requirements")

out = sys.argv[1]
for n in map(int, sys.argv[2:]): gen(n, out)
