// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype ModeValue = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP
datatype InputEvent = NoEvent | Transmission

// Static constants
const MAX_STEAM_W: real := 10.0
const C: real := 1000.0
const PUMPS: int := 4
const NORMAL_N1: real := 400.0
const CYCLE_SECONDS: real := 5.0
const NORMAL_N2: real := 600.0
const PUMP_CAPACITY_P: real := 5.0
const P: real := 5.0
const DT: real := 5.0
const CAPACITY_C: real := 1000.0
const PUMP_COUNT: int := 4
const SETTLED: int := 2
const STEAM_RISE_U1: real := 1.0
const STEAM_FALL_U2: real := 1.0
const W: real := 10.0
const LIMIT_M2: real := 900.0
const LIMIT_M1: real := 100.0
const U1: real := 1.0
const U2: real := 1.0
const STOP_LIMIT: int := 3

class SteamBoilerController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var readyAnnounced: bool
    var valveOpen: bool

    // Abstracted functions (from dependency objects -- uninterpreted)
    function levelLow(): real
        reads this
    function successiveStops(): int
        reads this
    function transmissionIntact(): bool
        reads this
    function levelHigh(): real
        reads this
    function unitsReadyReceived(): bool
        reads this
    function steamDeviceDefective(): bool
        reads this
    function steamReading(): real
        reads this
    function predictedLevelMin(): real
        reads this
    function levelDeviceDefective(): bool
        reads this
    function boilerWaitingReceived(): bool
        reads this
    function boilerWaitingSeen(): bool
        reads this
    function predictedLevelMax(): real
        reads this
    function pumpDefective(): bool
        reads this
    function pumpControllerDefective(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   pumpsToClose := (levelHigh() > 600.0) && !(levelLow() < 400.0)
    //   readyWithValveClosed := (((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (!(levelLow() < 400.0) && !(levelHigh() > 600.0)) && !valveOpen
    //   estimateUnreliable := steamDeviceDefective() || pumpControllerDefective()
    //   levelWithinNormal := !(levelLow() < 400.0) && !(levelHigh() > 600.0)
    //   plantNotWaiting := !boilerWaitingSeen()
    //   initTransmissionFailure := (!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)
    //   pumpsToHold := !((levelLow() < 400.0) && !(levelHigh() > 600.0)) && !((levelHigh() > 600.0) && !(levelLow() < 400.0))
    //   stopLimitReached := successiveStops() >= 3
    //   levelBelowNormal := levelLow() < 400.0
    //   waitingMessagePresent := boilerWaitingReceived()
    //   initChecksPassed := (!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()
    //   unitsReadyPremature := unitsReadyReceived() && !readyAnnounced
    //   levelMayReachLimit := predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0
    //   fillWithValveOpen := (((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && ((levelLow() < 400.0) && !(levelHigh() > 600.0)) && valveOpen
    //   fillWithValveClosed := (((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && ((levelLow() < 400.0) && !(levelHigh() > 600.0)) && !valveOpen
    //   initProceeding := !(successiveStops() >= 3) && !(!boilerWaitingSeen())
    //   steamRateNonZero := steamReading() != 0.0
    //   readyWithValveOpen := (((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (!(levelLow() < 400.0) && !(levelHigh() > 600.0)) && valveOpen
    //   pumpsToOpen := (levelLow() < 400.0) && !(levelHigh() > 600.0)
    //   pumpControlUnitFailed := pumpControllerDefective()
    //   levelAboveNormal := levelHigh() > 600.0
    //   pumpUnitFailed := pumpDefective()
    //   dataMessagesFaulty := !transmissionIntact()
    //   unitsReadyAnswered := unitsReadyReceived() && readyAnnounced
    //   drainStart := (((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (levelHigh() > 600.0) && !valveOpen
    //   initLevelAdjusting := ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced
    //   unitsReadyPresent := unitsReadyReceived()
    //   otherUnitFailed := steamDeviceDefective() || pumpDefective() || pumpControllerDefective()
    //   allUnitsSound := !levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())
    //   readyRepeating := ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && readyAnnounced && !unitsReadyReceived()
    //   steamUnitFailed := steamDeviceDefective()
    //   noEmergency := !(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)
    //   levelUnitFailed := levelDeviceDefective()
    //   transmissionFailure := (!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()
    //   drainContinue := (((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (levelHigh() > 600.0) && valveOpen

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
        readyAnnounced := false;
        valveOpen := false;
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(event == Transmission && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && successiveStops() >= 3) && event == Transmission && !(successiveStops() >= 3) && !boilerWaitingSeen()) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && !(successiveStops() >= 3) && !boilerWaitingSeen()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && ((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && ((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(event == Transmission && !(successiveStops() >= 3) && !boilerWaitingSeen()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && steamReading() != 0.0) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && steamReading() != 0.0) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && ((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(event == Transmission && !(successiveStops() >= 3) && !boilerWaitingSeen()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(steamReading() != 0.0) && levelDeviceDefective()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(steamReading() != 0.0) && levelDeviceDefective()) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && steamReading() != 0.0) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && ((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(event == Transmission && !(successiveStops() >= 3) && !boilerWaitingSeen()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) ==> mode == NORMAL
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(steamReading() != 0.0) && levelDeviceDefective()) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && steamReading() != 0.0) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && ((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(event == Transmission && !(successiveStops() >= 3) && !boilerWaitingSeen()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && !((!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())))) ==> mode == DEGRADED
        ensures old(!(event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && !((!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(steamReading() != 0.0) && levelDeviceDefective()) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && steamReading() != 0.0) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && ((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(event == Transmission && !(successiveStops() >= 3) && !boilerWaitingSeen()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && readyAnnounced && !unitsReadyReceived())) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && (((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && readyAnnounced && !unitsReadyReceived())) && !(event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && !((!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(steamReading() != 0.0) && levelDeviceDefective()) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && steamReading() != 0.0) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && ((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(event == Transmission && !(successiveStops() >= 3) && !boilerWaitingSeen()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (levelHigh() > 600.0) && !valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (levelHigh() > 600.0) && !valveOpen)) && !(event == Transmission && (((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && readyAnnounced && !unitsReadyReceived())) && !(event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && !((!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(steamReading() != 0.0) && levelDeviceDefective()) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && steamReading() != 0.0) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && ((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(event == Transmission && !(successiveStops() >= 3) && !boilerWaitingSeen()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (levelHigh() > 600.0) && valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (levelHigh() > 600.0) && valveOpen)) && !(event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (levelHigh() > 600.0) && !valveOpen)) && !(event == Transmission && (((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && readyAnnounced && !unitsReadyReceived())) && !(event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && !((!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(steamReading() != 0.0) && levelDeviceDefective()) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && steamReading() != 0.0) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && ((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(event == Transmission && !(successiveStops() >= 3) && !boilerWaitingSeen()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && ((levelLow() < 400.0) && !(levelHigh() > 600.0)) && valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && ((levelLow() < 400.0) && !(levelHigh() > 600.0)) && valveOpen)) && !(event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (levelHigh() > 600.0) && valveOpen)) && !(event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (levelHigh() > 600.0) && !valveOpen)) && !(event == Transmission && (((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && readyAnnounced && !unitsReadyReceived())) && !(event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && !((!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(steamReading() != 0.0) && levelDeviceDefective()) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && steamReading() != 0.0) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && ((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(event == Transmission && !(successiveStops() >= 3) && !boilerWaitingSeen()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && ((levelLow() < 400.0) && !(levelHigh() > 600.0)) && !valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && ((levelLow() < 400.0) && !(levelHigh() > 600.0)) && !valveOpen)) && !(event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && ((levelLow() < 400.0) && !(levelHigh() > 600.0)) && valveOpen)) && !(event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (levelHigh() > 600.0) && valveOpen)) && !(event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (levelHigh() > 600.0) && !valveOpen)) && !(event == Transmission && (((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && readyAnnounced && !unitsReadyReceived())) && !(event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && !((!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(steamReading() != 0.0) && levelDeviceDefective()) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && steamReading() != 0.0) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && ((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(event == Transmission && !(successiveStops() >= 3) && !boilerWaitingSeen()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (!(levelLow() < 400.0) && !(levelHigh() > 600.0)) && valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (!(levelLow() < 400.0) && !(levelHigh() > 600.0)) && valveOpen)) && !(event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && ((levelLow() < 400.0) && !(levelHigh() > 600.0)) && !valveOpen)) && !(event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && ((levelLow() < 400.0) && !(levelHigh() > 600.0)) && valveOpen)) && !(event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (levelHigh() > 600.0) && valveOpen)) && !(event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (levelHigh() > 600.0) && !valveOpen)) && !(event == Transmission && (((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && readyAnnounced && !unitsReadyReceived())) && !(event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && !((!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())))) && !(event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(steamReading() != 0.0) && levelDeviceDefective()) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && steamReading() != 0.0) && !(event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && ((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(event == Transmission && !(successiveStops() >= 3) && !boilerWaitingSeen()) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (!(levelLow() < 400.0) && !(levelHigh() > 600.0)) && !valveOpen)) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == Transmission && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && !(successiveStops() >= 3) && !boilerWaitingSeen()) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
        else if (event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && ((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && steamReading() != 0.0) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && !(successiveStops() >= 3) && !(!boilerWaitingSeen()) && !(((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced))) && !(steamReading() != 0.0) && levelDeviceDefective()) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) {
            mode := NORMAL;
            // action: Mode(NORMAL)
        }
        else if (event == Transmission && ((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && unitsReadyReceived() && readyAnnounced && !((!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())))) {
            mode := DEGRADED;
            // action: Mode(DEGRADED)
        }
        else if (event == Transmission && (((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && readyAnnounced && !unitsReadyReceived())) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
        else if (event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (levelHigh() > 600.0) && !valveOpen)) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
        else if (event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (levelHigh() > 600.0) && valveOpen)) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
        else if (event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && ((levelLow() < 400.0) && !(levelHigh() > 600.0)) && valveOpen)) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
        else if (event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && ((levelLow() < 400.0) && !(levelHigh() > 600.0)) && !valveOpen)) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
        else if (event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (!(levelLow() < 400.0) && !(levelHigh() > 600.0)) && valveOpen)) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
        else if (event == Transmission && ((((!(successiveStops() >= 3) && !(!boilerWaitingSeen())) && !((!transmissionIntact()) || (unitsReadyReceived() && !readyAnnounced)) && !(steamReading() != 0.0) && !levelDeviceDefective()) && !readyAnnounced) && (!(levelLow() < 400.0) && !(levelHigh() > 600.0)) && !valveOpen)) {
            mode := INITIALIZATION;
            // action: Mode(INITIALIZATION)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == Transmission && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && successiveStops() >= 3) && event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective())) && !(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && !((steamDeviceDefective() || pumpControllerDefective()))) ==> mode == RESCUE
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && !((steamDeviceDefective() || pumpControllerDefective()))) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective())) && !(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) ==> mode == DEGRADED
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && !((steamDeviceDefective() || pumpControllerDefective()))) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective())) && !(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && (levelLow() < 400.0) && !(levelHigh() > 600.0)) ==> mode == NORMAL
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && (levelLow() < 400.0) && !(levelHigh() > 600.0)) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && !((steamDeviceDefective() || pumpControllerDefective()))) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective())) && !(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && (levelHigh() > 600.0) && !(levelLow() < 400.0)) ==> mode == NORMAL
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && (levelHigh() > 600.0) && !(levelLow() < 400.0)) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && (levelLow() < 400.0) && !(levelHigh() > 600.0)) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && !((steamDeviceDefective() || pumpControllerDefective()))) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective())) && !(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !((levelLow() < 400.0) && !(levelHigh() > 600.0)) && !((levelHigh() > 600.0) && !(levelLow() < 400.0))) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == Transmission && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective())) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && !((steamDeviceDefective() || pumpControllerDefective()))) {
            mode := RESCUE;
            // action: Mode(RESCUE)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) {
            mode := DEGRADED;
            // action: Mode(DEGRADED)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && (levelLow() < 400.0) && !(levelHigh() > 600.0)) {
            mode := NORMAL;
            // action: Mode(NORMAL)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && (levelHigh() > 600.0) && !(levelLow() < 400.0)) {
            mode := NORMAL;
            // action: Mode(NORMAL)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !((levelLow() < 400.0) && !(levelHigh() > 600.0)) && !((levelHigh() > 600.0) && !(levelLow() < 400.0))) {
            mode := NORMAL;
            // action: Mode(NORMAL)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == Transmission && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && successiveStops() >= 3) && event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective())) && !(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && !((steamDeviceDefective() || pumpControllerDefective()))) ==> mode == RESCUE
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && !((steamDeviceDefective() || pumpControllerDefective()))) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective())) && !(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) ==> mode == NORMAL
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && !((steamDeviceDefective() || pumpControllerDefective()))) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective())) && !(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()) && (levelLow() < 400.0) && !(levelHigh() > 600.0)) ==> mode == DEGRADED
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()) && (levelLow() < 400.0) && !(levelHigh() > 600.0)) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && !((steamDeviceDefective() || pumpControllerDefective()))) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective())) && !(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()) && (levelHigh() > 600.0) && !(levelLow() < 400.0)) ==> mode == DEGRADED
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()) && (levelHigh() > 600.0) && !(levelLow() < 400.0)) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()) && (levelLow() < 400.0) && !(levelHigh() > 600.0)) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && !((steamDeviceDefective() || pumpControllerDefective()))) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective())) && !(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()) && !((levelLow() < 400.0) && !(levelHigh() > 600.0)) && !((levelHigh() > 600.0) && !(levelLow() < 400.0))) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == Transmission && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && (steamDeviceDefective() || pumpControllerDefective())) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && levelDeviceDefective() && !((steamDeviceDefective() || pumpControllerDefective()))) {
            mode := RESCUE;
            // action: Mode(RESCUE)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) {
            mode := NORMAL;
            // action: Mode(NORMAL)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()) && (levelLow() < 400.0) && !(levelHigh() > 600.0)) {
            mode := DEGRADED;
            // action: Mode(DEGRADED)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()) && (levelHigh() > 600.0) && !(levelLow() < 400.0)) {
            mode := DEGRADED;
            // action: Mode(DEGRADED)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective()) && !((levelLow() < 400.0) && !(levelHigh() > 600.0)) && !((levelHigh() > 600.0) && !(levelLow() < 400.0))) {
            mode := DEGRADED;
            // action: Mode(DEGRADED)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == Transmission && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && successiveStops() >= 3) && event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (steamDeviceDefective() || pumpControllerDefective())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (steamDeviceDefective() || pumpControllerDefective())) && !(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) ==> mode == NORMAL
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (steamDeviceDefective() || pumpControllerDefective())) && !(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) ==> mode == DEGRADED
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (steamDeviceDefective() || pumpControllerDefective())) && !(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && levelDeviceDefective() && (levelLow() < 400.0) && !(levelHigh() > 600.0)) ==> mode == RESCUE
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && levelDeviceDefective() && (levelLow() < 400.0) && !(levelHigh() > 600.0)) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (steamDeviceDefective() || pumpControllerDefective())) && !(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && levelDeviceDefective() && (levelHigh() > 600.0) && !(levelLow() < 400.0)) ==> mode == RESCUE
        ensures old(!(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && levelDeviceDefective() && (levelHigh() > 600.0) && !(levelLow() < 400.0)) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && levelDeviceDefective() && (levelLow() < 400.0) && !(levelHigh() > 600.0)) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) && !(event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (steamDeviceDefective() || pumpControllerDefective())) && !(event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !(event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && !(event == Transmission && successiveStops() >= 3) && event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && levelDeviceDefective() && !((levelLow() < 400.0) && !(levelHigh() > 600.0)) && !((levelHigh() > 600.0) && !(levelLow() < 400.0))) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == Transmission && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && !(successiveStops() >= 3) && ((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && !(successiveStops() >= 3) && !(((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived())) && (predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && (steamDeviceDefective() || pumpControllerDefective())) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && (!levelDeviceDefective() && !(steamDeviceDefective() || pumpDefective() || pumpControllerDefective()))) {
            mode := NORMAL;
            // action: Mode(NORMAL)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && !(levelDeviceDefective()) && (steamDeviceDefective() || pumpDefective() || pumpControllerDefective())) {
            mode := DEGRADED;
            // action: Mode(DEGRADED)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && levelDeviceDefective() && (levelLow() < 400.0) && !(levelHigh() > 600.0)) {
            mode := RESCUE;
            // action: Mode(RESCUE)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && levelDeviceDefective() && (levelHigh() > 600.0) && !(levelLow() < 400.0)) {
            mode := RESCUE;
            // action: Mode(RESCUE)
        }
        else if (event == Transmission && (!(successiveStops() >= 3) && !((!transmissionIntact()) || boilerWaitingReceived() || unitsReadyReceived()) && !(predictedLevelMin() <= 100.0 || predictedLevelMax() >= 900.0)) && !((steamDeviceDefective() || pumpControllerDefective())) && levelDeviceDefective() && !((levelLow() < 400.0) && !(levelHigh() > 600.0)) && !((levelHigh() > 600.0) && !(levelLow() < 400.0))) {
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
        if (event == Transmission) {
            mode := EMERGENCY_STOP;
            // action: Mode(EMERGENCY_STOP)
        }
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
