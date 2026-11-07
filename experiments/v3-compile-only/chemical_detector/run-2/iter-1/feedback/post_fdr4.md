# 6b — Formal Verification (FDR4) — FAILED

## Summary
FDR4: 0 assertion(s) checked, 0 passed, 1 error(s).

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: parse_error — CSP-M parse error

**Raw**
```
/Users/ranwei/Gitee/forge-v3o18-run2-chemical_detector/forge.transformations/output/csp-gen/defs/GasAnalysisController_System_Module.csp:5374:38-49:
    Couldn't match expected type Loc=>InOut with actual type InOut
    whilst matching expected type InOut with actual type InOut.Loc
    whilst matching expected type InOut=>Event
            with actual type InOut=>Loc=>Event
    whilst matching expected type Event with actual type Loc=>Event
    In the expression:
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute
        .out
    In the expression:
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute
        .out
        -> SKIP
    In the expression:
        share__choice(GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute
                      .out
                      -> SKIP)
    Relevant variable types:
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute :: InOut
                                                                             =>Loc=>Event
        out :: InOut
        share__choice :: (Proc) -> Proc
        Behaviour :: Proc
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::exit :: Event
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::exited :: Event
        Inactive :: Proc
        SStop :: Proc
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::terminate :: Event
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::GettingOut::enter :: Event
/Users/ranwei/Gitee/forge-v3o18-run2-chemical_detector/forge.transformations/output/csp-gen/defs/GasAnalysisController_System_Module.csp:5395:38-49:
    Couldn't match expected type Loc=>InOut with actual type InOut
    whilst matching expected type InOut with actual type InOut.Loc
    whilst matching expected type InOut=>Event
            with actual type InOut=>Loc=>Event
    whilst matching expected type Event with actual type Loc=>Event
    In the expression:
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute
        .out
    In the expression:
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute
        .out
        -> SKIP
    In the expression:
        share__choice(GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute
                      .out
                      -> SKIP)
    Relevant variable types:
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute :: InOut
                                                                             =>Loc=>Event
        out :: InOut
        share__choice :: (Proc) -> Proc
        Behaviour :: Proc
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::exit :: Event
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::exited :: Event
        Inactive :: Proc
        SStop :: Proc
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::terminate :: Event
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::GettingOut::enter :: Event
/Users/ranwei/Gitee/forge-v3o18-run2-chemical_detector/forge.transformations/output/csp-gen/defs/GasAnalysisController_System_Module.csp:5450:38-49:
    Couldn't match expected type Loc=>InOut with actual type InOut
    whilst matching expected type InOut with actual type InOut.Loc
    whilst matching expected type InOut=>Event
            with actual type InOut=>Loc=>Event
    whilst matching expected type Event with actual type Loc=>Event
    In the expression:
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute
        .out
    In the expression:
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute
        .out
        -> SKIP
    In the expression:
        share__choice(GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute
                      .out
                      -> SKIP)
    Relevant variable types:
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute :: InOut
                                                                             =>Loc=>Event
        out :: InOut
        share__choice :: (Proc) -> Proc
        Behaviour :: Proc
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::exit :: Event
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::exited :: Event
        Inactive :: Proc
        SStop :: Proc
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::terminate :: Event
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::Waiting::enter :: Event
/Users/ranwei/Gitee/forge-v3o18-run2-chemical_detector/forge.transformations/output/csp-gen/defs/GasAnalysisController_System_Module.csp:5471:38-49:
    Couldn't match expected type Loc=>InOut with actual type InOut
    whilst matching expected type InOut with actual type InOut.Loc
    whilst matching expected type InOut=>Event
            with actual type InOut=>Loc=>Event
    whilst matching expected type Event with actual type Loc=>Event
    In the expression:
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute
        .out
    In the expression:
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute
        .out
        -> SKIP
    In the expression:
        share__choice(GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute
                      .out
                      -> SKIP)
    Relevant variable types:
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::compute :: InOut
                                                                             =>Loc=>Event
        out :: InOut
        share__choice :: (Proc) -> Proc
        Behaviour :: Proc
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::exit :: Event
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::exited :: Event
        Inactive :: Proc
        SStop :: Proc
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::terminate :: Event
        GasAnalysisController_System_Module::ctrl_ref1::stm_ref0::Waiting::enter :: Event
```

**Fix directive**
CSP-M source generated by the pipeline failed to parse. This is usually an EGL template issue or a RoboChart model shape the CSP generator does not support. Inspect the offending .rct / .csp lines and correlate with the Java element above.

## Files to review
- Angle.java
- ChangeDirection.java
- GasAnalysisController.java
- GasAnalysisMode.java
- Loc.java
- Move.java
- MovementMode.java
- RandomWalk.java
- ShortRandomWalk.java
- Status.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, and re-run Phase 6a (FDR4 Verification).
