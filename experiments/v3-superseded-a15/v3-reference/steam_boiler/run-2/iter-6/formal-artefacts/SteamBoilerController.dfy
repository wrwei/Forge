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
const MAXIMAL_M2: real := 800.0
const STEAM_DECREASE_U2: real := 4.0
const MINIMAL_M1: real := 150.0
const STEAM_MAX_W: real := 25.0
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
    function estimateTrustworthy(): bool
        reads this
    function levelBelowNormalBand(): bool
        reads this
    function levelAboveNormalBand(): bool
        reads this
    function levelDeviceSound(): bool
        reads this
    function levelMayFallTooLow(): bool
        reads this
    function startupReadingsPlausible(): bool
        reads this
    function otherUnitsSound(): bool
        reads this
    function levelMayRiseTooHigh(): bool
        reads this
    function haltDemanded(): bool
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   initAdjusting := (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived())
    //   levelRepairedDegraded := !((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && levelDeviceSound() && !otherUnitsSound()
    //   levelStillEstimated := !((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && !levelDeviceSound()
    //   levelFailureNoEmergency := !(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && !levelDeviceSound()
    //   readyAberrant := physicalUnitsReadyReceived() && !programReadySent
    //   operatingEmergency := haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()
    //   soundNoEmergency := !(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && (levelDeviceSound() && otherUnitsSound())
    //   levelInBand := !levelBelowNormalBand() && !levelAboveNormalBand()
    //   unitsReadyAllSound := (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())
    //   levelRepairedSound := !((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && (levelDeviceSound() && otherUnitsSound())
    //   rescueEmergency := (haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()
    //   levelBelowN1 := !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())
    //   unitsAllSound := levelDeviceSound() && otherUnitsSound()
    //   levelAboveN2 := levelAboveNormalBand() && !levelBelowNormalBand()
    //   unitsReadySomeDefective := (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())
    //   initUnitDefective := !(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible()
    //   initCalm := !haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())
    //   unitsReadyAwaited := (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && !physicalUnitsReadyReceived()
    //   boilerIdle := !plantWaiting && !steamBoilerWaitingReceived()
    //   unitFailureNoEmergency := !(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && levelDeviceSound() && !otherUnitsSound()
    //   initIdle := (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && (!plantWaiting && !steamBoilerWaitingReceived())

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
        ensures old(event == MessagesReceived && !(!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && !(!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible()))) && event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible()))) && event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible()))) && event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && !physicalUnitsReadyReceived()) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible()))) && event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && (!plantWaiting && !steamBoilerWaitingReceived())) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && (!plantWaiting && !steamBoilerWaitingReceived())) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible()))) && event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && (!plantWaiting && !steamBoilerWaitingReceived())) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible()))) && event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && (!plantWaiting && !steamBoilerWaitingReceived())) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible()))) && event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && valveOpen) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && (!plantWaiting && !steamBoilerWaitingReceived())) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible()))) && event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && !(valveOpen)) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && valveOpen) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && (!plantWaiting && !steamBoilerWaitingReceived())) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible()))) && event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && !levelBelowNormalBand() && !levelAboveNormalBand() && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && !levelBelowNormalBand() && !levelAboveNormalBand() && valveOpen) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && !(valveOpen)) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && valveOpen) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && (!plantWaiting && !steamBoilerWaitingReceived())) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible()))) && event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && !levelBelowNormalBand() && !levelAboveNormalBand() && !(valveOpen)) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == MessagesReceived && !(!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible()))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && programReadySent && !physicalUnitsReadyReceived()) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && (!plantWaiting && !steamBoilerWaitingReceived())) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && !levelBelowNormalBand() && !levelAboveNormalBand() && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!haltDemanded() && !(physicalUnitsReadyReceived() && !programReadySent) && !(!(!plantWaiting && !steamBoilerWaitingReceived()) && !startupReadingsPlausible())) && !programReadySent && !(!plantWaiting && !steamBoilerWaitingReceived()) && !levelBelowNormalBand() && !levelAboveNormalBand() && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && (haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh())) && event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && !levelDeviceSound())) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && !levelDeviceSound())) && !(event == MessagesReceived && (haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh())) && event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && levelDeviceSound() && !otherUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && !levelDeviceSound())) && !(event == MessagesReceived && (haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh())) && event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && (levelDeviceSound() && otherUnitsSound())) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && (levelDeviceSound() && otherUnitsSound())) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) && !(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && !levelDeviceSound())) && !(event == MessagesReceived && (haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh())) && event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && (levelDeviceSound() && otherUnitsSound())) && levelAboveNormalBand() && !levelBelowNormalBand()) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && (levelDeviceSound() && otherUnitsSound())) && levelAboveNormalBand() && !levelBelowNormalBand()) && !(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && (levelDeviceSound() && otherUnitsSound())) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) && !(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && !levelDeviceSound())) && !(event == MessagesReceived && (haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh())) && event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && (levelDeviceSound() && otherUnitsSound())) && !levelBelowNormalBand() && !levelAboveNormalBand()) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == MessagesReceived && (haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && !levelDeviceSound())) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && levelDeviceSound() && !otherUnitsSound())) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && (levelDeviceSound() && otherUnitsSound())) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && (levelDeviceSound() && otherUnitsSound())) && levelAboveNormalBand() && !levelBelowNormalBand()) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && (levelDeviceSound() && otherUnitsSound())) && !levelBelowNormalBand() && !levelAboveNormalBand()) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && (haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh())) && event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && !levelDeviceSound())) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && !levelDeviceSound())) && !(event == MessagesReceived && (haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh())) && event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && (levelDeviceSound() && otherUnitsSound()))) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && (levelDeviceSound() && otherUnitsSound()))) && !(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && !levelDeviceSound())) && !(event == MessagesReceived && (haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh())) && event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && levelDeviceSound() && !otherUnitsSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && levelDeviceSound() && !otherUnitsSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) && !(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && (levelDeviceSound() && otherUnitsSound()))) && !(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && !levelDeviceSound())) && !(event == MessagesReceived && (haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh())) && event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && levelDeviceSound() && !otherUnitsSound()) && levelAboveNormalBand() && !levelBelowNormalBand()) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && levelDeviceSound() && !otherUnitsSound()) && levelAboveNormalBand() && !levelBelowNormalBand()) && !(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && levelDeviceSound() && !otherUnitsSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) && !(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && (levelDeviceSound() && otherUnitsSound()))) && !(event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && !levelDeviceSound())) && !(event == MessagesReceived && (haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh())) && event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && levelDeviceSound() && !otherUnitsSound()) && !levelBelowNormalBand() && !levelAboveNormalBand()) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == MessagesReceived && (haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && !levelDeviceSound())) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && (levelDeviceSound() && otherUnitsSound()))) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && levelDeviceSound() && !otherUnitsSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && levelDeviceSound() && !otherUnitsSound()) && levelAboveNormalBand() && !levelBelowNormalBand()) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!(haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) && levelDeviceSound() && !otherUnitsSound()) && !levelBelowNormalBand() && !levelAboveNormalBand()) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && ((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && ((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy())) && event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && (levelDeviceSound() && otherUnitsSound()))) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && (levelDeviceSound() && otherUnitsSound()))) && !(event == MessagesReceived && ((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy())) && event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && levelDeviceSound() && !otherUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && (levelDeviceSound() && otherUnitsSound()))) && !(event == MessagesReceived && ((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy())) && event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && !levelDeviceSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && !levelDeviceSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) && !(event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && (levelDeviceSound() && otherUnitsSound()))) && !(event == MessagesReceived && ((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy())) && event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && !levelDeviceSound()) && levelAboveNormalBand() && !levelBelowNormalBand()) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && !levelDeviceSound()) && levelAboveNormalBand() && !levelBelowNormalBand()) && !(event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && !levelDeviceSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) && !(event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && (levelDeviceSound() && otherUnitsSound()))) && !(event == MessagesReceived && ((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy())) && event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && !levelDeviceSound()) && !levelBelowNormalBand() && !levelAboveNormalBand()) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == MessagesReceived && ((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && (levelDeviceSound() && otherUnitsSound()))) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && levelDeviceSound() && !otherUnitsSound())) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && !levelDeviceSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && !levelDeviceSound()) && levelAboveNormalBand() && !levelBelowNormalBand()) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && (!((haltDemanded() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived() || levelMayFallTooLow() || levelMayRiseTooHigh()) || !estimateTrustworthy()) && !levelDeviceSound()) && !levelBelowNormalBand() && !levelAboveNormalBand()) {
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
