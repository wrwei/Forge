// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype InputEvent = NoEvent | Transmission

// Static constants
const STEAM_UNIT: int := 2 * PUMPS + 1
const PUMPS: int := 4
const NORMAL_N1: real := 400.0
const CYCLE_SECONDS: real := 5.0
const NORMAL_N2: real := 600.0
const PUMP_CAPACITY_P: real := 5.0
const STEAM_INCREASE_U1: real := 1.0
const DT: real := 5.0
const CAPACITY_C: real := 1000.0
const PUMP_COUNT: int := 4
const STEAM_DECREASE_U2: real := 1.0
const LIMIT_M2: real := 900.0
const STEAM_MAX_W: real := 10.0
const LIMIT_M1: real := 100.0
const LEVEL_UNIT: int := 2 * PUMPS
const UNIT_COUNT: int := 2 * PUMPS + 2
const STOP_LIMIT: int := 3

class SteamBoilerController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var waterLevel: real
    var steamRate: real
    var pumpRate: real
    var levelLowBound: real
    var levelHighBound: real

    // Abstracted functions (from dependency objects -- uninterpreted)
    function worstCaseLevelLow(): real
        reads this
    function steamDeviceFailed(): bool
        reads this
    function knownLevelLow(): real
        reads this
    function consecutiveStops(): int
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this
    function levelDeviceFailed(): bool
        reads this
    function anyPumpControllerFailed(): bool
        reads this
    function boilerWaitingReceived(): bool
        reads this
    function programReadySent(): bool
        reads this
    function anyPumpFailed(): bool
        reads this
    function valveOpen(): bool
        reads this
    function knownLevelHigh(): real
        reads this
    function worstCaseLevelHigh(): real
        reads this
    function transmissionFailure(): bool
        reads this
    function steamNonZero(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   pumpControlFailure := anyPumpControllerFailed()
    //   initAdjusting := (!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()
    //   waterBelowNormal := knownLevelLow() < 400.0
    //   initHandshake := (!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()
    //   initFault := boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed())
    //   regulatedContinue := !(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))
    //   valveIsOpen := valveOpen()
    //   rescueUntenable := levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())
    //   regulatedFault := transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed()))
    //   rescueContinue := !(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())
    //   readyAnnounced := programReadySent()
    //   initContinue := !(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))
    //   plantWaiting := boilerWaitingReceived()
    //   levelFailure := levelDeviceFailed()
    //   unitsReady := physicalUnitsReadyReceived()
    //   otherFailure := steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()
    //   waterAboveNormal := knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0)
    //   stopDemanded := consecutiveStops() >= 3
    //   steamFailure := steamDeviceFailed()
    //   waterInBand := !(knownLevelLow() < 400.0) && !(knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0))
    //   rescueFault := transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed()
    //   levelRisk := worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0
    //   steamNotZero := steamNonZero()
    //   pumpFailure := anyPumpFailed()
    //   transmissionFailure := transmissionFailure()

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
        waterLevel := 0.0;
        steamRate := 0.0;
        pumpRate := 0.0;
        levelLowBound := 0.0;
        levelHighBound := 0.0;
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(event == Transmission && consecutiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && consecutiveStops() >= 3) && event == Transmission && !(consecutiveStops() >= 3) && (boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(consecutiveStops() >= 3) && (boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(boilerWaitingReceived())) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && (!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(boilerWaitingReceived())) && !(event == Transmission && !(consecutiveStops() >= 3) && (boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) ==> mode == NORMAL
        ensures old(!(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(boilerWaitingReceived())) && !(event == Transmission && !(consecutiveStops() >= 3) && (boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) ==> mode == DEGRADED
        ensures old(!(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(boilerWaitingReceived())) && !(event == Transmission && !(consecutiveStops() >= 3) && (boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && !(physicalUnitsReadyReceived())) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && !(physicalUnitsReadyReceived())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(boilerWaitingReceived())) && !(event == Transmission && !(consecutiveStops() >= 3) && (boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0) && !(valveOpen())) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0) && !(valveOpen())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && !(physicalUnitsReadyReceived())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(boilerWaitingReceived())) && !(event == Transmission && !(consecutiveStops() >= 3) && (boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0) && valveOpen()) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0) && valveOpen()) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0) && !(valveOpen())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && !(physicalUnitsReadyReceived())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(boilerWaitingReceived())) && !(event == Transmission && !(consecutiveStops() >= 3) && (boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelLow() < 400.0 && valveOpen()) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelLow() < 400.0 && valveOpen()) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0) && valveOpen()) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0) && !(valveOpen())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && !(physicalUnitsReadyReceived())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(boilerWaitingReceived())) && !(event == Transmission && !(consecutiveStops() >= 3) && (boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelLow() < 400.0 && !(valveOpen())) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelLow() < 400.0 && !(valveOpen())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelLow() < 400.0 && valveOpen()) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0) && valveOpen()) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0) && !(valveOpen())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && !(physicalUnitsReadyReceived())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(boilerWaitingReceived())) && !(event == Transmission && !(consecutiveStops() >= 3) && (boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && !(knownLevelLow() < 400.0) && !(knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0)) && valveOpen()) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && !(knownLevelLow() < 400.0) && !(knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0)) && valveOpen()) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelLow() < 400.0 && !(valveOpen())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelLow() < 400.0 && valveOpen()) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0) && valveOpen()) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0) && !(valveOpen())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && !(physicalUnitsReadyReceived())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(boilerWaitingReceived())) && !(event == Transmission && !(consecutiveStops() >= 3) && (boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && !(knownLevelLow() < 400.0) && !(knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0)) && !(valveOpen())) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == Transmission && consecutiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && !(consecutiveStops() >= 3) && (boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && !(boilerWaitingReceived())) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
        else if (event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) {
            mode := NORMAL;
            // action: Mode(NORMAL)
        }
        else if (event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) {
            mode := DEGRADED;
            // action: Mode(DEGRADED)
        }
        else if (event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && programReadySent()) && !(physicalUnitsReadyReceived())) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
        else if (event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0) && !(valveOpen())) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
        else if (event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0) && valveOpen()) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
        else if (event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelLow() < 400.0 && valveOpen()) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
        else if (event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && knownLevelLow() < 400.0 && !(valveOpen())) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
        else if (event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && !(knownLevelLow() < 400.0) && !(knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0)) && valveOpen()) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
        else if (event == Transmission && ((!(consecutiveStops() >= 3) && !(boilerWaitingReceived() && (transmissionFailure() || steamNonZero() || levelDeviceFailed()))) && boilerWaitingReceived() && !programReadySent()) && !(knownLevelLow() < 400.0) && !(knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0)) && !(valveOpen())) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == Transmission && consecutiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && consecutiveStops() >= 3) && event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && levelDeviceFailed()) ==> mode == RESCUE
        ensures old(!(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && levelDeviceFailed()) && !(event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) ==> mode == DEGRADED
        ensures old(!(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && levelDeviceFailed()) && !(event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && knownLevelLow() < 400.0) ==> mode == NORMAL
        ensures old(!(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && knownLevelLow() < 400.0) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && levelDeviceFailed()) && !(event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0)) ==> mode == NORMAL
        ensures old(!(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0)) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && knownLevelLow() < 400.0) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && levelDeviceFailed()) && !(event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(knownLevelLow() < 400.0) && !(knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0))) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == Transmission && consecutiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && levelDeviceFailed()) {
            mode := RESCUE;
            // action: Mode(RESCUE)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) {
            mode := DEGRADED;
            // action: Mode(DEGRADED)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && knownLevelLow() < 400.0) {
            mode := NORMAL;
            // action: Mode(NORMAL)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0)) {
            mode := NORMAL;
            // action: Mode(NORMAL)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(knownLevelLow() < 400.0) && !(knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0))) {
            mode := NORMAL;
            // action: Mode(NORMAL)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == Transmission && consecutiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && consecutiveStops() >= 3) && event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && levelDeviceFailed()) ==> mode == RESCUE
        ensures old(!(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && levelDeviceFailed()) && !(event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) ==> mode == NORMAL
        ensures old(!(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && levelDeviceFailed()) && !(event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && knownLevelLow() < 400.0) ==> mode == DEGRADED
        ensures old(!(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && knownLevelLow() < 400.0) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && levelDeviceFailed()) && !(event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0)) ==> mode == DEGRADED
        ensures old(!(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0)) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && knownLevelLow() < 400.0) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && levelDeviceFailed()) && !(event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && !(knownLevelLow() < 400.0) && !(knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0))) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == Transmission && consecutiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && levelDeviceFailed()) {
            mode := RESCUE;
            // action: Mode(RESCUE)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) {
            mode := NORMAL;
            // action: Mode(NORMAL)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && knownLevelLow() < 400.0) {
            mode := DEGRADED;
            // action: Mode(DEGRADED)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0)) {
            mode := DEGRADED;
            // action: Mode(DEGRADED)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || (levelDeviceFailed() && (steamDeviceFailed() || anyPumpControllerFailed())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && !(knownLevelLow() < 400.0) && !(knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0))) {
            mode := DEGRADED;
            // action: Mode(DEGRADED)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == Transmission && consecutiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && consecutiveStops() >= 3) && event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) ==> mode == NORMAL
        ensures old(!(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) ==> mode == DEGRADED
        ensures old(!(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && knownLevelLow() < 400.0) ==> mode == RESCUE
        ensures old(!(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && knownLevelLow() < 400.0) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0)) ==> mode == RESCUE
        ensures old(!(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0)) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && knownLevelLow() < 400.0) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(event == Transmission && consecutiveStops() >= 3) && event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && !(knownLevelLow() < 400.0) && !(knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0))) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == Transmission && consecutiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && !(consecutiveStops() >= 3) && (transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) {
            mode := NORMAL;
            // action: Mode(NORMAL)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) {
            mode := DEGRADED;
            // action: Mode(DEGRADED)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && knownLevelLow() < 400.0) {
            mode := RESCUE;
            // action: Mode(RESCUE)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0)) {
            mode := RESCUE;
            // action: Mode(RESCUE)
        }
        else if (event == Transmission && (!(consecutiveStops() >= 3) && !(transmissionFailure() || (worstCaseLevelLow() <= 100.0 || worstCaseLevelHigh() >= 900.0) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && !(knownLevelLow() < 400.0) && !(knownLevelHigh() > 600.0 && !(knownLevelLow() < 400.0))) {
            mode := RESCUE;
            // action: Mode(RESCUE)
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
