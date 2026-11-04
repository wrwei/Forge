// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype WaterFlow = WATER_NOT_FLOWING | WATER_FLOWING
datatype InputEvent = NoEvent | CycleStart

// Static constants
const PUMPS: int := 4
const LEVEL_N1: real := 400.0
const LEVEL_M1: real := 100.0
const LEVEL_N2: real := 600.0
const CYCLE_SECONDS: real := 5.0
const PUMP_CAPACITY_P: real := 5.0
const STEAM_AT_REST: real := 0.0
const DT: real := 5.0
const CAPACITY_C: real := 1000.0
const PUMP_COUNT: int := 4
const STEAM_RISE_U1: real := 1.0
const STEAM_FALL_U2: real := 1.0
const LEVEL_M2: real := 900.0
const STEAM_MAX_W: real := 10.0
const STOP_REPETITIONS: int := 3

class SteamBoilerController {
    var mode: Mode

    // Abstracted functions (from dependency objects -- uninterpreted)
    function successiveStops(): int
        reads this
    function steamDeviceFailed(): bool
        reads this
    function unitsReadyReceived(): bool
        reads this
    function levelDeviceFailed(): bool
        reads this
    function allUnitsSound(): bool
        reads this
    function predictedMaximumLevel(): real
        reads this
    function waitingSignalled(): bool
        reads this
    function valveOpen(): bool
        reads this
    function estimatedLevel(): real
        reads this
    function pumpControlFailed(): bool
        reads this
    function steamRate(): real
        reads this
    function predictedMinimumLevel(): real
        reads this
    function transmissionFailure(): bool
        reads this
    function waterLevel(): real
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   initAdjusting := (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived()
    //   controlUnitDown := pumpControlFailed()
    //   riskAboveM2 := predictedMaximumLevel() >= 900.0
    //   estimateAboveN2 := estimatedLevel() > 600.0
    //   transmissionLost := transmissionFailure()
    //   riskBelowM1 := predictedMinimumLevel() <= 100.0
    //   valveIsOpen := valveOpen()
    //   riskStop := ((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3)
    //   stopLimitReached := successiveStops() >= 3
    //   levelBelowN1 := waterLevel() < 400.0
    //   initRunning := waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()
    //   levelAboveN2 := waterLevel() > 600.0
    //   initLevelStop := waitingSignalled() && levelDeviceFailed() && !(steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)
    //   estimateBelowN1 := estimatedLevel() < 400.0
    //   plantWaiting := waitingSignalled()
    //   steamUnitDown := steamDeviceFailed()
    //   initSteamStop := waitingSignalled() && (steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)
    //   transmissionStop := transmissionFailure() && !(successiveStops() >= 3)
    //   unitsReadyArrived := unitsReadyReceived()
    //   levelInBand := !(waterLevel() > 600.0) && !(waterLevel() < 400.0)
    //   levelLossBlocked := (!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailed())
    //   levelAtRisk := (predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)
    //   levelUnitDown := levelDeviceFailed()
    //   rescueUnsupported := steamDeviceFailed() || pumpControlFailed()
    //   levelBackAllSound := (!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && !levelDeviceFailed() && allUnitsSound()
    //   rescueContinues := (!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && levelDeviceFailed()
    //   noEmergency := !(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))
    //   steamNotZero := steamRate() != 0.0
    //   rescueBlocked := (!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && (steamDeviceFailed() || pumpControlFailed())
    //   everyUnitSound := allUnitsSound()
    //   levelBackSomeDown := (!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && !levelDeviceFailed() && !allUnitsSound()
    //   estimateInBand := !(estimatedLevel() > 600.0) && !(estimatedLevel() < 400.0)
    //   initTransmissionStop := waitingSignalled() && (transmissionFailure() && !(successiveStops() >= 3))
    //   levelLossRescue := (!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && !(steamDeviceFailed() || pumpControlFailed())
    //   otherUnitDown := (!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && !allUnitsSound()
    //   initIdle := !waitingSignalled() && !(successiveStops() >= 3)
    //   everyUnitRunning := (!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && allUnitsSound()

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
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(event == CycleStart && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && successiveStops() >= 3) && event == CycleStart && waitingSignalled() && (transmissionFailure() && !(successiveStops() >= 3))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && waitingSignalled() && (transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && waitingSignalled() && (steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && waitingSignalled() && (steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && waitingSignalled() && levelDeviceFailed() && !(steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && waitingSignalled() && levelDeviceFailed() && !(steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && allUnitsSound()) ==> mode == NORMAL
        ensures old(!(event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && allUnitsSound()) && !(event == CycleStart && waitingSignalled() && levelDeviceFailed() && !(steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && !(allUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && !(allUnitsSound())) && !(event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && allUnitsSound()) && !(event == CycleStart && waitingSignalled() && levelDeviceFailed() && !(steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() > 600.0 && !(valveOpen())) ==> mode == INITIALIZATION
        ensures old(!(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() > 600.0 && !(valveOpen())) && !(event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && !(allUnitsSound())) && !(event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && allUnitsSound()) && !(event == CycleStart && waitingSignalled() && levelDeviceFailed() && !(steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() > 600.0 && valveOpen()) ==> mode == INITIALIZATION
        ensures old(!(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() > 600.0 && valveOpen()) && !(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() > 600.0 && !(valveOpen())) && !(event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && !(allUnitsSound())) && !(event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && allUnitsSound()) && !(event == CycleStart && waitingSignalled() && levelDeviceFailed() && !(steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() < 400.0 && !(waterLevel() > 600.0) && valveOpen()) ==> mode == INITIALIZATION
        ensures old(!(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() < 400.0 && !(waterLevel() > 600.0) && valveOpen()) && !(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() > 600.0 && valveOpen()) && !(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() > 600.0 && !(valveOpen())) && !(event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && !(allUnitsSound())) && !(event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && allUnitsSound()) && !(event == CycleStart && waitingSignalled() && levelDeviceFailed() && !(steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() < 400.0 && !(waterLevel() > 600.0) && !(valveOpen())) ==> mode == INITIALIZATION
        ensures old(!(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() < 400.0 && !(waterLevel() > 600.0) && !(valveOpen())) && !(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() < 400.0 && !(waterLevel() > 600.0) && valveOpen()) && !(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() > 600.0 && valveOpen()) && !(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() > 600.0 && !(valveOpen())) && !(event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && !(allUnitsSound())) && !(event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && allUnitsSound()) && !(event == CycleStart && waitingSignalled() && levelDeviceFailed() && !(steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && !(waterLevel() > 600.0) && !(waterLevel() < 400.0) && valveOpen()) ==> mode == INITIALIZATION
        ensures old(!(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && !(waterLevel() > 600.0) && !(waterLevel() < 400.0) && valveOpen()) && !(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() < 400.0 && !(waterLevel() > 600.0) && !(valveOpen())) && !(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() < 400.0 && !(waterLevel() > 600.0) && valveOpen()) && !(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() > 600.0 && valveOpen()) && !(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() > 600.0 && !(valveOpen())) && !(event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && !(allUnitsSound())) && !(event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && allUnitsSound()) && !(event == CycleStart && waitingSignalled() && levelDeviceFailed() && !(steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && !(waterLevel() > 600.0) && !(waterLevel() < 400.0) && !(valveOpen())) ==> mode == INITIALIZATION
        ensures old(!(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && !(waterLevel() > 600.0) && !(waterLevel() < 400.0) && !(valveOpen())) && !(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && !(waterLevel() > 600.0) && !(waterLevel() < 400.0) && valveOpen()) && !(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() < 400.0 && !(waterLevel() > 600.0) && !(valveOpen())) && !(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() < 400.0 && !(waterLevel() > 600.0) && valveOpen()) && !(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() > 600.0 && valveOpen()) && !(event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() > 600.0 && !(valveOpen())) && !(event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && !(allUnitsSound())) && !(event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && allUnitsSound()) && !(event == CycleStart && waitingSignalled() && levelDeviceFailed() && !(steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && waitingSignalled() && (transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && !waitingSignalled() && !(successiveStops() >= 3)) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == CycleStart && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && waitingSignalled() && (transmissionFailure() && !(successiveStops() >= 3))) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && waitingSignalled() && (steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && waitingSignalled() && levelDeviceFailed() && !(steamRate() != 0.0) && !transmissionFailure() && !(successiveStops() >= 3)) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && allUnitsSound()) {
            mode := NORMAL;
            // action: ModeReport(NORMAL)
        }
        else if (event == CycleStart && waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed() && unitsReadyReceived() && !(allUnitsSound())) {
            mode := DEGRADED;
            // action: ModeReport(DEGRADED)
        }
        else if (event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() > 600.0 && !(valveOpen())) {
            mode := INITIALIZATION;
            // action: ModeReport(INITIALIZATION)
        }
        else if (event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() > 600.0 && valveOpen()) {
            mode := INITIALIZATION;
            // action: ModeReport(INITIALIZATION)
        }
        else if (event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() < 400.0 && !(waterLevel() > 600.0) && valveOpen()) {
            mode := INITIALIZATION;
            // action: ModeReport(INITIALIZATION)
        }
        else if (event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && waterLevel() < 400.0 && !(waterLevel() > 600.0) && !(valveOpen())) {
            mode := INITIALIZATION;
            // action: ModeReport(INITIALIZATION)
        }
        else if (event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && !(waterLevel() > 600.0) && !(waterLevel() < 400.0) && valveOpen()) {
            mode := INITIALIZATION;
            // action: ModeReport(INITIALIZATION)
        }
        else if (event == CycleStart && (waitingSignalled() && !(successiveStops() >= 3) && !transmissionFailure() && !(steamRate() != 0.0) && !levelDeviceFailed()) && !unitsReadyReceived() && !(waterLevel() > 600.0) && !(waterLevel() < 400.0) && !(valveOpen())) {
            mode := INITIALIZATION;
            // action: ModeReport(INITIALIZATION)
        }
        else if (event == CycleStart && !waitingSignalled() && !(successiveStops() >= 3)) {
            mode := INITIALIZATION;
            // action: ModeReport(INITIALIZATION)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == CycleStart && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && successiveStops() >= 3) && event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailed()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && !(steamDeviceFailed() || pumpControlFailed()))) ==> mode == RESCUE
        ensures old(!(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && !(steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && !allUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && !allUnitsSound())) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && !(steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && allUnitsSound()) && waterLevel() < 400.0 && !(waterLevel() > 600.0)) ==> mode == NORMAL
        ensures old(!(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && allUnitsSound()) && waterLevel() < 400.0 && !(waterLevel() > 600.0)) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && !allUnitsSound())) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && !(steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && allUnitsSound()) && waterLevel() > 600.0) ==> mode == NORMAL
        ensures old(!(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && allUnitsSound()) && waterLevel() > 600.0) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && allUnitsSound()) && waterLevel() < 400.0 && !(waterLevel() > 600.0)) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && !allUnitsSound())) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && !(steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && allUnitsSound()) && !(waterLevel() > 600.0) && !(waterLevel() < 400.0)) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == CycleStart && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailed()))) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && !(steamDeviceFailed() || pumpControlFailed()))) {
            mode := RESCUE;
            // action: ModeReport(RESCUE)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && !allUnitsSound())) {
            mode := DEGRADED;
            // action: ModeReport(DEGRADED)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && allUnitsSound()) && waterLevel() < 400.0 && !(waterLevel() > 600.0)) {
            mode := NORMAL;
            // action: ModeReport(NORMAL)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && allUnitsSound()) && waterLevel() > 600.0) {
            mode := NORMAL;
            // action: ModeReport(NORMAL)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && allUnitsSound()) && !(waterLevel() > 600.0) && !(waterLevel() < 400.0)) {
            mode := NORMAL;
            // action: ModeReport(NORMAL)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == CycleStart && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && successiveStops() >= 3) && event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailed()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && !(steamDeviceFailed() || pumpControlFailed()))) ==> mode == RESCUE
        ensures old(!(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && !(steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && allUnitsSound())) ==> mode == NORMAL
        ensures old(!(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && allUnitsSound())) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && !(steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && !allUnitsSound()) && waterLevel() < 400.0 && !(waterLevel() > 600.0)) ==> mode == DEGRADED
        ensures old(!(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && !allUnitsSound()) && waterLevel() < 400.0 && !(waterLevel() > 600.0)) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && allUnitsSound())) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && !(steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && !allUnitsSound()) && waterLevel() > 600.0) ==> mode == DEGRADED
        ensures old(!(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && !allUnitsSound()) && waterLevel() > 600.0) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && !allUnitsSound()) && waterLevel() < 400.0 && !(waterLevel() > 600.0)) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && allUnitsSound())) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && !(steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && !allUnitsSound()) && !(waterLevel() > 600.0) && !(waterLevel() < 400.0)) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == CycleStart && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailed()))) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && levelDeviceFailed() && !(steamDeviceFailed() || pumpControlFailed()))) {
            mode := RESCUE;
            // action: ModeReport(RESCUE)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && allUnitsSound())) {
            mode := NORMAL;
            // action: ModeReport(NORMAL)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && !allUnitsSound()) && waterLevel() < 400.0 && !(waterLevel() > 600.0)) {
            mode := DEGRADED;
            // action: ModeReport(DEGRADED)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && !allUnitsSound()) && waterLevel() > 600.0) {
            mode := DEGRADED;
            // action: ModeReport(DEGRADED)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !levelDeviceFailed() && !allUnitsSound()) && !(waterLevel() > 600.0) && !(waterLevel() < 400.0)) {
            mode := DEGRADED;
            // action: ModeReport(DEGRADED)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == CycleStart && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && successiveStops() >= 3) && event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && (steamDeviceFailed() || pumpControlFailed()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && (steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && !levelDeviceFailed() && allUnitsSound())) ==> mode == NORMAL
        ensures old(!(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && !levelDeviceFailed() && allUnitsSound())) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && (steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && !levelDeviceFailed() && !allUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && !levelDeviceFailed() && !allUnitsSound())) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && !levelDeviceFailed() && allUnitsSound())) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && (steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && levelDeviceFailed()) && estimatedLevel() < 400.0 && !(estimatedLevel() > 600.0)) ==> mode == RESCUE
        ensures old(!(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && levelDeviceFailed()) && estimatedLevel() < 400.0 && !(estimatedLevel() > 600.0)) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && !levelDeviceFailed() && !allUnitsSound())) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && !levelDeviceFailed() && allUnitsSound())) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && (steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && levelDeviceFailed()) && estimatedLevel() > 600.0) ==> mode == RESCUE
        ensures old(!(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && levelDeviceFailed()) && estimatedLevel() > 600.0) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && levelDeviceFailed()) && estimatedLevel() < 400.0 && !(estimatedLevel() > 600.0)) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && !levelDeviceFailed() && !allUnitsSound())) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && !levelDeviceFailed() && allUnitsSound())) && !(event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && (steamDeviceFailed() || pumpControlFailed()))) && !(event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) && !(event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) && !(event == CycleStart && successiveStops() >= 3) && event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && levelDeviceFailed()) && !(estimatedLevel() > 600.0) && !(estimatedLevel() < 400.0)) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == CycleStart && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && transmissionFailure() && !(successiveStops() >= 3)) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && (((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0)) && !transmissionFailure() && !(successiveStops() >= 3))) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && (steamDeviceFailed() || pumpControlFailed()))) {
            mode := EMERGENCY_STOP;
            // action: ModeReport(EMERGENCY_STOP)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && !levelDeviceFailed() && allUnitsSound())) {
            mode := NORMAL;
            // action: ModeReport(NORMAL)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && !levelDeviceFailed() && !allUnitsSound())) {
            mode := DEGRADED;
            // action: ModeReport(DEGRADED)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && levelDeviceFailed()) && estimatedLevel() < 400.0 && !(estimatedLevel() > 600.0)) {
            mode := RESCUE;
            // action: ModeReport(RESCUE)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && levelDeviceFailed()) && estimatedLevel() > 600.0) {
            mode := RESCUE;
            // action: ModeReport(RESCUE)
        }
        else if (event == CycleStart && ((!(successiveStops() >= 3) && !transmissionFailure() && !((predictedMinimumLevel() <= 100.0) || (predictedMaximumLevel() >= 900.0))) && !(steamDeviceFailed() || pumpControlFailed()) && levelDeviceFailed()) && !(estimatedLevel() > 600.0) && !(estimatedLevel() < 400.0)) {
            mode := RESCUE;
            // action: ModeReport(RESCUE)
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
