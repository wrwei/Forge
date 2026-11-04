// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype OperatingMode = MODE_INITIALIZATION | MODE_NORMAL | MODE_DEGRADED | MODE_RESCUE | MODE_EMERGENCY_STOP
datatype PumpId = PUMP1 | PUMP2 | PUMP3 | PUMP4
datatype InputEvent = NoEvent | MessagesReceived

// Static constants
const CONTROL_DELAY: int := 2
const NORMAL_N1: real := 400.0
const CYCLE_SECONDS: real := 5.0
const NORMAL_N2: real := 600.0
const PUMP_CAPACITY_P: real := 10.0
const STEAM_INCREASE_U1: real := 4.0
const CAPACITY_C: real := 1000.0
const PUMP_COUNT: int := 4
const STEAM_DECREASE_U2: real := 4.0
const LIMIT_M2: real := 800.0
const STEAM_MAX_W: real := 25.0
const LIMIT_M1: real := 150.0
const ESTIMATION_SLICES: int := 100
const STOP_REPETITIONS: int := 3

class SteamBoilerController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var plantWaiting: bool
    var valveOpen: bool
    var programReadySent: bool

    // Abstracted functions (from dependency objects -- uninterpreted)
    function steamBoilerWaitingReceived(): bool
        reads this
    function successiveStops(): int
        reads this
    function levelBelowNormalBand(): bool
        reads this
    function levelAboveNormalBand(): bool
        reads this
    function steamDetected(): bool
        reads this
    function levelDeviceSound(): bool
        reads this
    function otherUnitsSound(): bool
        reads this
    function pumpControllersSound(): bool
        reads this
    function levelLimitAtRisk(): bool
        reads this
    function transmissionFailure(): bool
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this
    function steamDeviceSound(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   initAdjusting := !((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())
    //   waitingReceived := steamBoilerWaitingReceived()
    //   levelRepairedDegraded := !(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && !(!levelDeviceSound()) && !otherUnitsSound()
    //   operatingTransmissionFailed := transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()
    //   estimateUnreliable := !steamDeviceSound() || !pumpControllersSound()
    //   readyAberrant := physicalUnitsReadyReceived() && !programReadySent
    //   soundNoEmergency := !((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (otherUnitsSound() && !(!levelDeviceSound()))
    //   levelBelowN1 := levelBelowNormalBand()
    //   initEmergency := (successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))
    //   initReadyPhase := !((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent
    //   levelAboveN2 := levelAboveNormalBand() && !levelBelowNormalBand()
    //   otherUnitsSound := otherUnitsSound()
    //   steamNonZero := steamDetected()
    //   unitsReady := physicalUnitsReadyReceived()
    //   levelStillEstimated := !(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())
    //   initLevelDefective := (plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound())
    //   levelFailureNoEmergency := !((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (!levelDeviceSound())
    //   operatingEmergency := (successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()
    //   initTransmissionFailed := transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)
    //   initSteamDefective := (plantWaiting || steamBoilerWaitingReceived()) && steamDetected()
    //   levelInBand := !levelBelowNormalBand() && !(levelAboveNormalBand() && !levelBelowNormalBand())
    //   transmissionFailed := transmissionFailure()
    //   levelRepairedSound := !(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (otherUnitsSound() && !(!levelDeviceSound()))
    //   levelDeviceDefective := !levelDeviceSound()
    //   allUnitsSound := otherUnitsSound() && !(!levelDeviceSound())
    //   boilerWaiting := plantWaiting || steamBoilerWaitingReceived()
    //   rescueEmergency := ((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())
    //   levelRisk := levelLimitAtRisk()
    //   stopRequested := successiveStops() >= 3
    //   unitFailureNoEmergency := !((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && !(!levelDeviceSound()) && !otherUnitsSound()
    //   initIdle := !((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived())

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
        plantWaiting := false;
        valveOpen := false;
        programReadySent := false;
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected())) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected())) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected())) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected())) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected())) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected())) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected())) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected())) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelBelowNormalBand() && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelBelowNormalBand() && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected())) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelBelowNormalBand() && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelBelowNormalBand() && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected())) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && !levelBelowNormalBand() && !(levelAboveNormalBand() && !levelBelowNormalBand()) && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && !levelBelowNormalBand() && !(levelAboveNormalBand() && !levelBelowNormalBand()) && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelBelowNormalBand() && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected())) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && !levelBelowNormalBand() && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(valveOpen)) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == MessagesReceived && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelBelowNormalBand() && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && levelBelowNormalBand() && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && !levelBelowNormalBand() && !(levelAboveNormalBand() && !levelBelowNormalBand()) && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && steamDetected()) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && !levelBelowNormalBand() && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && levelLimitAtRisk()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && levelLimitAtRisk()) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (!levelDeviceSound()))) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (!levelDeviceSound()))) && !(event == MessagesReceived && levelLimitAtRisk()) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && !(!levelDeviceSound()) && !otherUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && !(!levelDeviceSound()) && !otherUnitsSound())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (!levelDeviceSound()))) && !(event == MessagesReceived && levelLimitAtRisk()) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (otherUnitsSound() && !(!levelDeviceSound()))) && levelBelowNormalBand()) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (otherUnitsSound() && !(!levelDeviceSound()))) && levelBelowNormalBand()) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && !(!levelDeviceSound()) && !otherUnitsSound())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (!levelDeviceSound()))) && !(event == MessagesReceived && levelLimitAtRisk()) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (otherUnitsSound() && !(!levelDeviceSound()))) && levelAboveNormalBand() && !levelBelowNormalBand()) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (otherUnitsSound() && !(!levelDeviceSound()))) && levelAboveNormalBand() && !levelBelowNormalBand()) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (otherUnitsSound() && !(!levelDeviceSound()))) && levelBelowNormalBand()) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && !(!levelDeviceSound()) && !otherUnitsSound())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (!levelDeviceSound()))) && !(event == MessagesReceived && levelLimitAtRisk()) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (otherUnitsSound() && !(!levelDeviceSound()))) && !levelBelowNormalBand() && !(levelAboveNormalBand() && !levelBelowNormalBand())) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == MessagesReceived && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && levelLimitAtRisk()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (!levelDeviceSound()))) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && !(!levelDeviceSound()) && !otherUnitsSound())) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (otherUnitsSound() && !(!levelDeviceSound()))) && levelBelowNormalBand()) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (otherUnitsSound() && !(!levelDeviceSound()))) && levelAboveNormalBand() && !levelBelowNormalBand()) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (otherUnitsSound() && !(!levelDeviceSound()))) && !levelBelowNormalBand() && !(levelAboveNormalBand() && !levelBelowNormalBand())) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && levelLimitAtRisk()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && levelLimitAtRisk()) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (!levelDeviceSound()))) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (!levelDeviceSound()))) && !(event == MessagesReceived && levelLimitAtRisk()) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (otherUnitsSound() && !(!levelDeviceSound())))) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (otherUnitsSound() && !(!levelDeviceSound())))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (!levelDeviceSound()))) && !(event == MessagesReceived && levelLimitAtRisk()) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && !(!levelDeviceSound()) && !otherUnitsSound()) && levelBelowNormalBand()) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && !(!levelDeviceSound()) && !otherUnitsSound()) && levelBelowNormalBand()) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (otherUnitsSound() && !(!levelDeviceSound())))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (!levelDeviceSound()))) && !(event == MessagesReceived && levelLimitAtRisk()) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && !(!levelDeviceSound()) && !otherUnitsSound()) && levelAboveNormalBand() && !levelBelowNormalBand()) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && !(!levelDeviceSound()) && !otherUnitsSound()) && levelAboveNormalBand() && !levelBelowNormalBand()) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && !(!levelDeviceSound()) && !otherUnitsSound()) && levelBelowNormalBand()) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (otherUnitsSound() && !(!levelDeviceSound())))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (!levelDeviceSound()))) && !(event == MessagesReceived && levelLimitAtRisk()) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && !(!levelDeviceSound()) && !otherUnitsSound()) && !levelBelowNormalBand() && !(levelAboveNormalBand() && !levelBelowNormalBand())) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == MessagesReceived && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && levelLimitAtRisk()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (!levelDeviceSound()))) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && (otherUnitsSound() && !(!levelDeviceSound())))) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && !(!levelDeviceSound()) && !otherUnitsSound()) && levelBelowNormalBand()) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && !(!levelDeviceSound()) && !otherUnitsSound()) && levelAboveNormalBand() && !levelBelowNormalBand()) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) && !(!levelDeviceSound()) && !otherUnitsSound()) && !levelBelowNormalBand() && !(levelAboveNormalBand() && !levelBelowNormalBand())) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && levelLimitAtRisk()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && levelLimitAtRisk()) && !(event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (otherUnitsSound() && !(!levelDeviceSound())))) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (otherUnitsSound() && !(!levelDeviceSound())))) && !(event == MessagesReceived && levelLimitAtRisk()) && !(event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && !(!levelDeviceSound()) && !otherUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && !(!levelDeviceSound()) && !otherUnitsSound())) && !(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (otherUnitsSound() && !(!levelDeviceSound())))) && !(event == MessagesReceived && levelLimitAtRisk()) && !(event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && levelBelowNormalBand()) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && levelBelowNormalBand()) && !(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && !(!levelDeviceSound()) && !otherUnitsSound())) && !(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (otherUnitsSound() && !(!levelDeviceSound())))) && !(event == MessagesReceived && levelLimitAtRisk()) && !(event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && levelAboveNormalBand() && !levelBelowNormalBand()) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && levelAboveNormalBand() && !levelBelowNormalBand()) && !(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && levelBelowNormalBand()) && !(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && !(!levelDeviceSound()) && !otherUnitsSound())) && !(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (otherUnitsSound() && !(!levelDeviceSound())))) && !(event == MessagesReceived && levelLimitAtRisk()) && !(event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && !levelBelowNormalBand() && !(levelAboveNormalBand() && !levelBelowNormalBand())) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == MessagesReceived && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && levelLimitAtRisk()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (otherUnitsSound() && !(!levelDeviceSound())))) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && !(!levelDeviceSound()) && !otherUnitsSound())) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && levelBelowNormalBand()) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && levelAboveNormalBand() && !levelBelowNormalBand()) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || levelLimitAtRisk()) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && !levelBelowNormalBand() && !(levelAboveNormalBand() && !levelBelowNormalBand())) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
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
