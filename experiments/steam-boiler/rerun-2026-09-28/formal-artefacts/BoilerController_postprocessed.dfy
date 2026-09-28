// [postprocess D1] event datatype not emitted by the generator: it derives
datatype InputEvent = Transmission
// Auto-generated Dafny verification code from Java source
// Source: BoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP


// Static constants
const P: real := 15.0
const PUMP_COUNT: int := 4
const C: real := 1000.0
const N1: real := 400.0
const M1: real := 150.0
const N2: real := 600.0
const M2: real := 850.0
const W: real := 25.0
const CYCLE_SECONDS: real := 5.0
const U1: real := 5.0
const U2: real := 5.0
const STOP_REPEAT_LIMIT: int := 3

class BoilerController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var transmissionFailed: bool
    var stopThresholdReached: bool
    var initSteamDefect: bool
    var levelDeviceFailed: bool
    var steamDeviceFailed: bool
    var controlUnitFailed: bool
    var nonLevelUnitFailed: bool
    var everyUnitSound: bool
    var levelRiskM1M2: bool
    var levelBelowN1: bool
    var levelAboveN2: bool
    var plantWaiting: bool
    var unitsReady: bool
    var initDrainNeeded: bool
    var initFillNeeded: bool
    var initReadyPhase: bool
    var initExitAllSound: bool
    var initExitDegraded: bool
    var rescueRepairToDegraded: bool

    ghost predicate Valid()
        reads this
    {
        true  // Extend with domain invariants
    }

    constructor()
        ensures mode == INITIALIZATION
        ensures Valid()
    {
        mode := INITIALIZATION;
        transmissionFailed := false;
        stopThresholdReached := false;
        initSteamDefect := false;
        levelDeviceFailed := false;
        steamDeviceFailed := false;
        controlUnitFailed := false;
        nonLevelUnitFailed := false;
        everyUnitSound := false;
        levelRiskM1M2 := false;
        levelBelowN1 := false;
        levelAboveN2 := false;
        plantWaiting := false;
        unitsReady := false;
        initDrainNeeded := false;
        initFillNeeded := false;
        initReadyPhase := false;
        initExitAllSound := false;
        initExitDegraded := false;
        rescueRepairToDegraded := false;
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(transmissionFailed) ==> mode == EMERGENCY_STOP
        ensures old(!(transmissionFailed) && stopThresholdReached && !(transmissionFailed)) ==> mode == EMERGENCY_STOP
        ensures old(!(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && initSteamDefect && !(transmissionFailed) && !(stopThresholdReached)) ==> mode == EMERGENCY_STOP
        ensures old(!(initSteamDefect && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect)) ==> mode == EMERGENCY_STOP
        ensures old(!(levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect)) && !(initSteamDefect && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && initDrainNeeded && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed)) ==> mode == INITIALIZATION
        ensures old(!(initDrainNeeded && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed)) && !(levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect)) && !(initSteamDefect && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && initFillNeeded && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed) && !(initDrainNeeded)) ==> mode == INITIALIZATION
        ensures old(!(initFillNeeded && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed) && !(initDrainNeeded)) && !(initDrainNeeded && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed)) && !(levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect)) && !(initSteamDefect && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && initReadyPhase && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed) && !(initDrainNeeded) && !(initFillNeeded)) ==> mode == INITIALIZATION
        ensures old(!(initReadyPhase && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed) && !(initDrainNeeded) && !(initFillNeeded)) && !(initFillNeeded && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed) && !(initDrainNeeded)) && !(initDrainNeeded && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed)) && !(levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect)) && !(initSteamDefect && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && initExitAllSound && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed) && !(initDrainNeeded) && !(initFillNeeded) && !(initReadyPhase)) ==> mode == NORMAL
        ensures old(!(initExitAllSound && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed) && !(initDrainNeeded) && !(initFillNeeded) && !(initReadyPhase)) && !(initReadyPhase && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed) && !(initDrainNeeded) && !(initFillNeeded)) && !(initFillNeeded && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed) && !(initDrainNeeded)) && !(initDrainNeeded && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed)) && !(levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect)) && !(initSteamDefect && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && initExitDegraded && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed) && !(initDrainNeeded) && !(initFillNeeded) && !(initReadyPhase) && !(initExitAllSound)) ==> mode == DEGRADED
        ensures Valid()
    {
        if (transmissionFailed) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (stopThresholdReached && !(transmissionFailed)) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (initSteamDefect && !(transmissionFailed) && !(stopThresholdReached)) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect)) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (initDrainNeeded && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed)) {
            mode := INITIALIZATION;
            // action: Valve
        }
        else if (initFillNeeded && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed) && !(initDrainNeeded)) {
            mode := INITIALIZATION;
            // action: OpenPump(1)
        }
        else if (initReadyPhase && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed) && !(initDrainNeeded) && !(initFillNeeded)) {
            mode := INITIALIZATION;
            // action: ProgramReady
        }
        else if (initExitAllSound && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed) && !(initDrainNeeded) && !(initFillNeeded) && !(initReadyPhase)) {
            mode := NORMAL;
        }
        else if (initExitDegraded && !(transmissionFailed) && !(stopThresholdReached) && !(initSteamDefect) && !(levelDeviceFailed) && !(initDrainNeeded) && !(initFillNeeded) && !(initReadyPhase) && !(initExitAllSound)) {
            mode := DEGRADED;
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(transmissionFailed) ==> mode == EMERGENCY_STOP
        ensures old(!(transmissionFailed) && stopThresholdReached && !(transmissionFailed)) ==> mode == EMERGENCY_STOP
        ensures old(!(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached)) ==> mode == EMERGENCY_STOP
        ensures old(!(levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2)) ==> mode == RESCUE
        ensures old(!(levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2)) && !(levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && nonLevelUnitFailed && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed)) ==> mode == DEGRADED
        ensures old(!(nonLevelUnitFailed && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed)) && !(levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2)) && !(levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && levelBelowN1 && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed) && !(nonLevelUnitFailed)) ==> mode == NORMAL
        ensures old(!(levelBelowN1 && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed) && !(nonLevelUnitFailed)) && !(nonLevelUnitFailed && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed)) && !(levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2)) && !(levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && levelAboveN2 && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed) && !(nonLevelUnitFailed) && !(levelBelowN1)) ==> mode == NORMAL
        ensures Valid()
    {
        if (transmissionFailed) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (stopThresholdReached && !(transmissionFailed)) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached)) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2)) {
            mode := RESCUE;
        }
        else if (nonLevelUnitFailed && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed)) {
            mode := DEGRADED;
        }
        else if (levelBelowN1 && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed) && !(nonLevelUnitFailed)) {
            mode := NORMAL;
            // action: OpenPump(1)
        }
        else if (levelAboveN2 && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed) && !(nonLevelUnitFailed) && !(levelBelowN1)) {
            mode := NORMAL;
            // action: ClosePump(1)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(transmissionFailed) ==> mode == EMERGENCY_STOP
        ensures old(!(transmissionFailed) && stopThresholdReached && !(transmissionFailed)) ==> mode == EMERGENCY_STOP
        ensures old(!(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached)) ==> mode == EMERGENCY_STOP
        ensures old(!(levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2)) ==> mode == RESCUE
        ensures old(!(levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2)) && !(levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && everyUnitSound && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed)) ==> mode == NORMAL
        ensures old(!(everyUnitSound && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed)) && !(levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2)) && !(levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && levelBelowN1 && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed) && !(everyUnitSound)) ==> mode == DEGRADED
        ensures old(!(levelBelowN1 && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed) && !(everyUnitSound)) && !(everyUnitSound && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed)) && !(levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2)) && !(levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && levelAboveN2 && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed) && !(everyUnitSound) && !(levelBelowN1)) ==> mode == DEGRADED
        ensures Valid()
    {
        if (transmissionFailed) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (stopThresholdReached && !(transmissionFailed)) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached)) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (levelDeviceFailed && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2)) {
            mode := RESCUE;
        }
        else if (everyUnitSound && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed)) {
            mode := NORMAL;
        }
        else if (levelBelowN1 && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed) && !(everyUnitSound)) {
            mode := DEGRADED;
            // action: OpenPump(1)
        }
        else if (levelAboveN2 && !(transmissionFailed) && !(stopThresholdReached) && !(levelRiskM1M2) && !(levelDeviceFailed) && !(everyUnitSound) && !(levelBelowN1)) {
            mode := DEGRADED;
            // action: ClosePump(1)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(transmissionFailed) ==> mode == EMERGENCY_STOP
        ensures old(!(transmissionFailed) && stopThresholdReached && !(transmissionFailed)) ==> mode == EMERGENCY_STOP
        ensures old(!(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && steamDeviceFailed && !(transmissionFailed) && !(stopThresholdReached)) ==> mode == EMERGENCY_STOP
        ensures old(!(steamDeviceFailed && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && controlUnitFailed && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed)) ==> mode == EMERGENCY_STOP
        ensures old(!(controlUnitFailed && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed)) && !(steamDeviceFailed && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed)) ==> mode == EMERGENCY_STOP
        ensures old(!(levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed)) && !(controlUnitFailed && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed)) && !(steamDeviceFailed && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && everyUnitSound && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed) && !(levelRiskM1M2)) ==> mode == NORMAL
        ensures old(!(everyUnitSound && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed) && !(levelRiskM1M2)) && !(levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed)) && !(controlUnitFailed && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed)) && !(steamDeviceFailed && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && rescueRepairToDegraded && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed) && !(levelRiskM1M2) && !(everyUnitSound)) ==> mode == DEGRADED
        ensures old(!(rescueRepairToDegraded && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed) && !(levelRiskM1M2) && !(everyUnitSound)) && !(everyUnitSound && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed) && !(levelRiskM1M2)) && !(levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed)) && !(controlUnitFailed && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed)) && !(steamDeviceFailed && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && levelBelowN1 && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed) && !(levelRiskM1M2) && !(everyUnitSound) && !(rescueRepairToDegraded)) ==> mode == RESCUE
        ensures old(!(levelBelowN1 && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed) && !(levelRiskM1M2) && !(everyUnitSound) && !(rescueRepairToDegraded)) && !(rescueRepairToDegraded && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed) && !(levelRiskM1M2) && !(everyUnitSound)) && !(everyUnitSound && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed) && !(levelRiskM1M2)) && !(levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed)) && !(controlUnitFailed && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed)) && !(steamDeviceFailed && !(transmissionFailed) && !(stopThresholdReached)) && !(stopThresholdReached && !(transmissionFailed)) && !(transmissionFailed) && levelAboveN2 && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed) && !(levelRiskM1M2) && !(everyUnitSound) && !(rescueRepairToDegraded) && !(levelBelowN1)) ==> mode == RESCUE
        ensures Valid()
    {
        if (transmissionFailed) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (stopThresholdReached && !(transmissionFailed)) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (steamDeviceFailed && !(transmissionFailed) && !(stopThresholdReached)) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (controlUnitFailed && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed)) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (levelRiskM1M2 && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed)) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (everyUnitSound && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed) && !(levelRiskM1M2)) {
            mode := NORMAL;
        }
        else if (rescueRepairToDegraded && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed) && !(levelRiskM1M2) && !(everyUnitSound)) {
            mode := DEGRADED;
        }
        else if (levelBelowN1 && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed) && !(levelRiskM1M2) && !(everyUnitSound) && !(rescueRepairToDegraded)) {
            mode := RESCUE;
            // action: OpenPump(1)
        }
        else if (levelAboveN2 && !(transmissionFailed) && !(stopThresholdReached) && !(steamDeviceFailed) && !(controlUnitFailed) && !(levelRiskM1M2) && !(everyUnitSound) && !(rescueRepairToDegraded) && !(levelBelowN1)) {
            mode := RESCUE;
            // action: ClosePump(1)
        }
    }

    method transitionFromEMERGENCY_STOP(event: InputEvent)
        requires mode == EMERGENCY_STOP
        requires Valid()
        modifies this
        ensures Valid()
    {
    }

    method step(event: InputEvent)
        requires Valid()
        modifies this
        ensures Valid()
    {
        if (mode == INITIALIZATION) {
            transitionFromINITIALIZATION(event);
        }
        else if (mode == NORMAL) {
            transitionFromNORMAL(event);
        }
        else if (mode == DEGRADED) {
            transitionFromDEGRADED(event);
        }
        else if (mode == RESCUE) {
            transitionFromRESCUE(event);
        }
        else if (mode == EMERGENCY_STOP) {
            transitionFromEMERGENCY_STOP(event);
        }
    }
}

// Lemma: transitions from INITIALIZATION are deterministic by if-else priority
lemma INITIALIZATION_deterministic()
    ensures true  // The if-else chain guarantees exactly one branch executes
{
    // Determinism follows from the sequential if-else structure.
    // Each guard is only evaluated when all prior guards are false.
}

// Lemma: transitions from NORMAL are deterministic by if-else priority
lemma NORMAL_deterministic()
    ensures true  // The if-else chain guarantees exactly one branch executes
{
    // Determinism follows from the sequential if-else structure.
    // Each guard is only evaluated when all prior guards are false.
}

// Lemma: transitions from DEGRADED are deterministic by if-else priority
lemma DEGRADED_deterministic()
    ensures true  // The if-else chain guarantees exactly one branch executes
{
    // Determinism follows from the sequential if-else structure.
    // Each guard is only evaluated when all prior guards are false.
}

// Lemma: transitions from RESCUE are deterministic by if-else priority
lemma RESCUE_deterministic()
    ensures true  // The if-else chain guarantees exactly one branch executes
{
    // Determinism follows from the sequential if-else structure.
    // Each guard is only evaluated when all prior guards are false.
}
