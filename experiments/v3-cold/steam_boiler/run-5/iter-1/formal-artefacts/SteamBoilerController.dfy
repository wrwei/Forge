// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype FlowObservation = FLOW | NO_FLOW
datatype ModeIndicator = INITIALIZATION_MODE | NORMAL_MODE | DEGRADED_MODE | RESCUE_MODE | EMERGENCY_STOP_MODE
datatype InputEvent = NoEvent | TransmissionReceived

// Static constants
const MAX_STEAM_W: real := 25.0
const STEAM_UNIT: int := LEVEL_UNIT + 1
const PUMPS: int := 4
const MIN_LIMIT_M1: real := 150.0
const CYCLE_SECONDS: real := 5.0
const PUMP_CAPACITY_P: real := 10.0
const STEAM_INCREASE_U1: real := 2.0
const DT: real := 5.0
const MAX_LIMIT_M2: real := 850.0
const CAPACITY_C: real := 1000.0
const PUMP_COUNT: int := 4
const MAX_NORMAL_N2: real := 600.0
const STEAM_DECREASE_U2: real := 2.0
const LEVEL_UNIT: int := 2 * 4
const UNIT_COUNT: int := STEAM_UNIT + 1
const MIN_NORMAL_N1: real := 400.0
const STOP_LIMIT: int := 3

class SteamBoilerController {
    var mode: Mode

    // Abstracted functions (from dependency objects -- uninterpreted)
    function successiveStops(): int
        reads this
    function steamDeviceFailed(): bool
        reads this
    function unitsReadyReceived(): bool
        reads this
    function levelForecastMax(): real
        reads this
    function pumpFailurePresent(): bool
        reads this
    function openablePumpCount(): int
        reads this
    function levelDeviceFailed(): bool
        reads this
    function levelForecastMin(): real
        reads this
    function closablePumpCount(): int
        reads this
    function pumpControlFailurePresent(): bool
        reads this
    function boilerWaitingReceived(): bool
        reads this
    function valveOpen(): bool
        reads this
    function steamRate(): real
        reads this
    function transmissionFailure(): bool
        reads this
    function waterLevel(): real
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   operatorStopDemanded := successiveStops() >= 3
    //   initAdjusting := !((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()
    //   estimateUnreliable := steamDeviceFailed() || pumpControlFailurePresent()
    //   levelWithinNormal := !(waterLevel() > 600.0) && !(waterLevel() < 400.0)
    //   levelRiskHigh := levelForecastMax() >= 850.0
    //   openOnePump := (levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0)
    //   closeOnePump := (levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)
    //   forecastBelowNormal := levelForecastMin() < 400.0
    //   regulatedEmergency := ((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent()))
    //   fillStart := (waterLevel() < 400.0) && !(waterLevel() > 600.0) && !valveOpen() && !(closablePumpCount() > 0) && (openablePumpCount() > 0)
    //   valveIsOpen := valveOpen()
    //   levelBelowNormal := waterLevel() < 400.0
    //   levelFailed := levelDeviceFailed()
    //   valveOnly := ((waterLevel() > 600.0) && !valveOpen()) || ((!(waterLevel() > 600.0) && valveOpen()) && !(!(waterLevel() > 600.0) && !(waterLevel() < 400.0)))
    //   levelSettling := !((waterLevel() > 600.0) && !valveOpen()) && !(!(waterLevel() > 600.0) && valveOpen()) && !((waterLevel() < 400.0) && !(waterLevel() > 600.0) && !valveOpen() && !(closablePumpCount() > 0) && (openablePumpCount() > 0)) && !((!(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !valveOpen() && (closablePumpCount() > 0)) && !((!(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !valveOpen() && !(closablePumpCount() > 0))
    //   forecastAboveNormal := levelForecastMax() > 600.0
    //   initEmergency := (successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))
    //   readyToStart := (!(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !valveOpen() && !(closablePumpCount() > 0)
    //   levelRiskLow := levelForecastMin() <= 150.0
    //   plantWaiting := boilerWaitingReceived()
    //   holdPumps := !((levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) && !((levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0))
    //   levelAboveNormal := waterLevel() > 600.0
    //   unitsReady := unitsReadyReceived()
    //   emergencyCondition := (successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)
    //   pumpCanClose := closablePumpCount() > 0
    //   transmissionFailed := transmissionFailure()
    //   drainStart := (waterLevel() > 600.0) && !valveOpen()
    //   otherUnitFailed := steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()
    //   allUnitsSound := !levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())
    //   fillStop := (!(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !valveOpen() && (closablePumpCount() > 0)
    //   rescueEmergency := ((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent())
    //   pumpCanOpen := openablePumpCount() > 0
    //   steamFailed := steamDeviceFailed()
    //   pumpControlFailed := pumpControlFailurePresent()
    //   pumpFailed := pumpFailurePresent()
    //   steamNotZero := steamRate() != 0.0
    //   drainStop := !(waterLevel() > 600.0) && valveOpen()

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
        ensures old(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && !((!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())))) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && !((!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())))) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && !(boilerWaitingReceived())) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && !(boilerWaitingReceived())) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && !((!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())))) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (((waterLevel() > 600.0) && !valveOpen()) || ((!(waterLevel() > 600.0) && valveOpen()) && !(!(waterLevel() > 600.0) && !(waterLevel() < 400.0))))) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (((waterLevel() > 600.0) && !valveOpen()) || ((!(waterLevel() > 600.0) && valveOpen()) && !(!(waterLevel() > 600.0) && !(waterLevel() < 400.0))))) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && !(boilerWaitingReceived())) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && !((!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())))) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && !(waterLevel() > 600.0) && valveOpen() && !(waterLevel() > 600.0) && !(waterLevel() < 400.0)) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && !(waterLevel() > 600.0) && valveOpen() && !(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (((waterLevel() > 600.0) && !valveOpen()) || ((!(waterLevel() > 600.0) && valveOpen()) && !(!(waterLevel() > 600.0) && !(waterLevel() < 400.0))))) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && !(boilerWaitingReceived())) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && !((!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())))) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (waterLevel() < 400.0) && !(waterLevel() > 600.0) && !valveOpen() && !(closablePumpCount() > 0) && (openablePumpCount() > 0)) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (waterLevel() < 400.0) && !(waterLevel() > 600.0) && !valveOpen() && !(closablePumpCount() > 0) && (openablePumpCount() > 0)) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && !(waterLevel() > 600.0) && valveOpen() && !(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (((waterLevel() > 600.0) && !valveOpen()) || ((!(waterLevel() > 600.0) && valveOpen()) && !(!(waterLevel() > 600.0) && !(waterLevel() < 400.0))))) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && !(boilerWaitingReceived())) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && !((!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())))) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (!(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !valveOpen() && (closablePumpCount() > 0)) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (!(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !valveOpen() && (closablePumpCount() > 0)) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (waterLevel() < 400.0) && !(waterLevel() > 600.0) && !valveOpen() && !(closablePumpCount() > 0) && (openablePumpCount() > 0)) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && !(waterLevel() > 600.0) && valveOpen() && !(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (((waterLevel() > 600.0) && !valveOpen()) || ((!(waterLevel() > 600.0) && valveOpen()) && !(!(waterLevel() > 600.0) && !(waterLevel() < 400.0))))) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && !(boilerWaitingReceived())) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && !((!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())))) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (!(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !valveOpen() && !(closablePumpCount() > 0)) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (!(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !valveOpen() && !(closablePumpCount() > 0)) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (!(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !valveOpen() && (closablePumpCount() > 0)) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (waterLevel() < 400.0) && !(waterLevel() > 600.0) && !valveOpen() && !(closablePumpCount() > 0) && (openablePumpCount() > 0)) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && !(waterLevel() > 600.0) && valveOpen() && !(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !(event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (((waterLevel() > 600.0) && !valveOpen()) || ((!(waterLevel() > 600.0) && valveOpen()) && !(!(waterLevel() > 600.0) && !(waterLevel() < 400.0))))) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && !(boilerWaitingReceived())) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && !((!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())))) && !(event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) && !(event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && !((waterLevel() > 600.0) && !valveOpen()) && !(!(waterLevel() > 600.0) && valveOpen()) && !((waterLevel() < 400.0) && !(waterLevel() > 600.0) && !valveOpen() && !(closablePumpCount() > 0) && (openablePumpCount() > 0)) && !((!(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !valveOpen() && (closablePumpCount() > 0)) && !((!(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !valveOpen() && !(closablePumpCount() > 0))) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == TransmissionReceived && ((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && boilerWaitingReceived() && unitsReadyReceived() && !((!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())))) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && !(((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed())))) && !(boilerWaitingReceived())) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (((waterLevel() > 600.0) && !valveOpen()) || ((!(waterLevel() > 600.0) && valveOpen()) && !(!(waterLevel() > 600.0) && !(waterLevel() < 400.0))))) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && !(waterLevel() > 600.0) && valveOpen() && !(waterLevel() > 600.0) && !(waterLevel() < 400.0)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (waterLevel() < 400.0) && !(waterLevel() > 600.0) && !valveOpen() && !(closablePumpCount() > 0) && (openablePumpCount() > 0)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (!(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !valveOpen() && (closablePumpCount() > 0)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && (!(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !valveOpen() && !(closablePumpCount() > 0)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && (!((successiveStops() >= 3) || transmissionFailure() || (boilerWaitingReceived() && ((steamRate() != 0.0) || levelDeviceFailed()))) && boilerWaitingReceived() && !unitsReadyReceived()) && !((waterLevel() > 600.0) && !valveOpen()) && !(!(waterLevel() > 600.0) && valveOpen()) && !((waterLevel() < 400.0) && !(waterLevel() > 600.0) && !valveOpen() && !(closablePumpCount() > 0) && (openablePumpCount() > 0)) && !((!(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !valveOpen() && (closablePumpCount() > 0)) && !((!(waterLevel() > 600.0) && !(waterLevel() < 400.0)) && !valveOpen() && !(closablePumpCount() > 0))) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && levelDeviceFailed()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && levelDeviceFailed()) && !(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && levelDeviceFailed()) && !(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) && (levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) && (levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && levelDeviceFailed()) && !(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) && (levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0)) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) && (levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0)) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) && (levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && levelDeviceFailed()) && !(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) && !((levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) && !((levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0))) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && levelDeviceFailed()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) && (levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) && (levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0)) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) && !((levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) && !((levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0))) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && levelDeviceFailed()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && levelDeviceFailed()) && !(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && levelDeviceFailed()) && !(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()) && (levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()) && (levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && levelDeviceFailed()) && !(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()) && (levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0)) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()) && (levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0)) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()) && (levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && levelDeviceFailed()) && !(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()) && !((levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) && !((levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0))) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && levelDeviceFailed()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()) && (levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()) && (levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0)) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (levelDeviceFailed() && (steamDeviceFailed() || pumpControlFailurePresent())))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()) && !((levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) && !((levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0))) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) && !(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) && !(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && levelDeviceFailed() && (levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && levelDeviceFailed() && (levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) && !(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && levelDeviceFailed() && (levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0)) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && levelDeviceFailed() && (levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0)) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && levelDeviceFailed() && (levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) && !(event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) && !(event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && levelDeviceFailed() && !((levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) && !((levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0))) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == TransmissionReceived && (((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && (!levelDeviceFailed() && !(steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent()))) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && !(levelDeviceFailed()) && (steamDeviceFailed() || pumpFailurePresent() || pumpControlFailurePresent())) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && levelDeviceFailed() && (levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && levelDeviceFailed() && (levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0)) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && !((((successiveStops() >= 3) || transmissionFailure() || (levelForecastMin() <= 150.0) || (levelForecastMax() >= 850.0)) || (steamDeviceFailed() || pumpControlFailurePresent()))) && levelDeviceFailed() && !((levelForecastMax() > 600.0) && !(levelForecastMin() < 400.0) && (closablePumpCount() > 0)) && !((levelForecastMin() < 400.0) && !(levelForecastMax() > 600.0) && (openablePumpCount() > 0))) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
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
