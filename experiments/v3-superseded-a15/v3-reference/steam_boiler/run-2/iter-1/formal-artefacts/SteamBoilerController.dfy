// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype OperatingMode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP
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
    function highestReachableLevel(): real
        reads this
    function successiveStops(): int
        reads this
    function lowestReachableLevel(): real
        reads this
    function levelDeviceSound(): bool
        reads this
    function otherUnitsSound(): bool
        reads this
    function pumpControllersSound(): bool
        reads this
    function steamRate(): real
        reads this
    function transmissionFailure(): bool
        reads this
    function waterLevel(): real
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this
    function steamDeviceSound(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   initAdjusting := !((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())
    //   waitingReceived := steamBoilerWaitingReceived()
    //   levelRepairedDegraded := !(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && !(!levelDeviceSound()) && !otherUnitsSound()
    //   operatingTransmissionFailed := transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()
    //   estimateUnreliable := !steamDeviceSound() || !pumpControllersSound()
    //   readyAberrant := physicalUnitsReadyReceived() && !programReadySent
    //   soundNoEmergency := !((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (otherUnitsSound() && !(!levelDeviceSound()))
    //   levelBelowN1 := waterLevel() < 400.0
    //   initEmergency := (successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))
    //   initReadyPhase := !((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent
    //   levelAboveN2 := waterLevel() > 600.0
    //   otherUnitsSound := otherUnitsSound()
    //   steamNonZero := steamRate() != 0.0
    //   unitsReady := physicalUnitsReadyReceived()
    //   levelStillEstimated := !(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())
    //   initLevelDefective := (plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound())
    //   levelFailureNoEmergency := !((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (!levelDeviceSound())
    //   operatingEmergency := (successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)
    //   initTransmissionFailed := transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)
    //   initSteamDefective := (plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)
    //   levelInBand := !(waterLevel() < 400.0) && !(waterLevel() > 600.0)
    //   transmissionFailed := transmissionFailure()
    //   levelRepairedSound := !(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (otherUnitsSound() && !(!levelDeviceSound()))
    //   levelDeviceDefective := !levelDeviceSound()
    //   allUnitsSound := otherUnitsSound() && !(!levelDeviceSound())
    //   boilerWaiting := plantWaiting || steamBoilerWaitingReceived()
    //   rescueEmergency := ((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())
    //   levelRisk := lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0
    //   stopRequested := successiveStops() >= 3
    //   unitFailureNoEmergency := !((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(!levelDeviceSound()) && !otherUnitsSound()
    //   initIdle := !((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived())

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
        ensures old(!(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0))) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0))) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0))) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0))) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0))) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0))) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() > 600.0 && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() > 600.0 && !(valveOpen)) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0))) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() > 600.0 && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() > 600.0 && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() > 600.0 && !(valveOpen)) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0))) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() < 400.0 && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() < 400.0 && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() > 600.0 && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() > 600.0 && !(valveOpen)) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0))) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() < 400.0 && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() < 400.0 && !(valveOpen)) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() < 400.0 && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() > 600.0 && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() > 600.0 && !(valveOpen)) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0))) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && !(waterLevel() < 400.0) && !(waterLevel() > 600.0) && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && !(waterLevel() < 400.0) && !(waterLevel() > 600.0) && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() < 400.0 && !(valveOpen)) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() < 400.0 && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() > 600.0 && valveOpen) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() > 600.0 && !(valveOpen)) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !(event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0))) && !(event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && !(waterLevel() < 400.0) && !(waterLevel() > 600.0) && !(valveOpen)) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == MessagesReceived && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && otherUnitsSound() && !(!levelDeviceSound())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && physicalUnitsReadyReceived() && !(otherUnitsSound() && !(!levelDeviceSound()))) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && programReadySent) && !(physicalUnitsReadyReceived())) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && !(plantWaiting || steamBoilerWaitingReceived()))) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() > 600.0 && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() > 600.0 && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() < 400.0 && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && waterLevel() < 400.0 && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && !(waterLevel() < 400.0) && !(waterLevel() > 600.0) && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || (physicalUnitsReadyReceived() && !programReadySent)) || ((plantWaiting || steamBoilerWaitingReceived()) && (steamRate() != 0.0)) || ((plantWaiting || steamBoilerWaitingReceived()) && (!levelDeviceSound()))) && !programReadySent && (plantWaiting || steamBoilerWaitingReceived())) && !(waterLevel() < 400.0) && !(waterLevel() > 600.0) && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (!levelDeviceSound()))) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (!levelDeviceSound()))) && !(event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(!levelDeviceSound()) && !otherUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(!levelDeviceSound()) && !otherUnitsSound())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (!levelDeviceSound()))) && !(event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (otherUnitsSound() && !(!levelDeviceSound()))) && waterLevel() < 400.0) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (otherUnitsSound() && !(!levelDeviceSound()))) && waterLevel() < 400.0) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(!levelDeviceSound()) && !otherUnitsSound())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (!levelDeviceSound()))) && !(event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (otherUnitsSound() && !(!levelDeviceSound()))) && waterLevel() > 600.0) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (otherUnitsSound() && !(!levelDeviceSound()))) && waterLevel() > 600.0) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (otherUnitsSound() && !(!levelDeviceSound()))) && waterLevel() < 400.0) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(!levelDeviceSound()) && !otherUnitsSound())) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (!levelDeviceSound()))) && !(event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (otherUnitsSound() && !(!levelDeviceSound()))) && !(waterLevel() < 400.0) && !(waterLevel() > 600.0)) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == MessagesReceived && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (!levelDeviceSound()))) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(!levelDeviceSound()) && !otherUnitsSound())) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (otherUnitsSound() && !(!levelDeviceSound()))) && waterLevel() < 400.0) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (otherUnitsSound() && !(!levelDeviceSound()))) && waterLevel() > 600.0) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (otherUnitsSound() && !(!levelDeviceSound()))) && !(waterLevel() < 400.0) && !(waterLevel() > 600.0)) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (!levelDeviceSound()))) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (!levelDeviceSound()))) && !(event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (otherUnitsSound() && !(!levelDeviceSound())))) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (otherUnitsSound() && !(!levelDeviceSound())))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (!levelDeviceSound()))) && !(event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(!levelDeviceSound()) && !otherUnitsSound()) && waterLevel() < 400.0) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(!levelDeviceSound()) && !otherUnitsSound()) && waterLevel() < 400.0) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (otherUnitsSound() && !(!levelDeviceSound())))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (!levelDeviceSound()))) && !(event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(!levelDeviceSound()) && !otherUnitsSound()) && waterLevel() > 600.0) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(!levelDeviceSound()) && !otherUnitsSound()) && waterLevel() > 600.0) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(!levelDeviceSound()) && !otherUnitsSound()) && waterLevel() < 400.0) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (otherUnitsSound() && !(!levelDeviceSound())))) && !(event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (!levelDeviceSound()))) && !(event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(!levelDeviceSound()) && !otherUnitsSound()) && !(waterLevel() < 400.0) && !(waterLevel() > 600.0)) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == MessagesReceived && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (!levelDeviceSound()))) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && (otherUnitsSound() && !(!levelDeviceSound())))) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(!levelDeviceSound()) && !otherUnitsSound()) && waterLevel() < 400.0) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(!levelDeviceSound()) && !otherUnitsSound()) && waterLevel() > 600.0) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == MessagesReceived && (!((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(!levelDeviceSound()) && !otherUnitsSound()) && !(waterLevel() < 400.0) && !(waterLevel() > 600.0)) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && successiveStops() >= 3) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (otherUnitsSound() && !(!levelDeviceSound())))) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (otherUnitsSound() && !(!levelDeviceSound())))) && !(event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && !(!levelDeviceSound()) && !otherUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && !(!levelDeviceSound()) && !otherUnitsSound())) && !(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (otherUnitsSound() && !(!levelDeviceSound())))) && !(event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && waterLevel() < 400.0) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && waterLevel() < 400.0) && !(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && !(!levelDeviceSound()) && !otherUnitsSound())) && !(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (otherUnitsSound() && !(!levelDeviceSound())))) && !(event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && waterLevel() > 600.0) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && waterLevel() > 600.0) && !(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && waterLevel() < 400.0) && !(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && !(!levelDeviceSound()) && !otherUnitsSound())) && !(event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (otherUnitsSound() && !(!levelDeviceSound())))) && !(event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) && !(event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) && !(event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !(event == MessagesReceived && successiveStops() >= 3) && event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && !(waterLevel() < 400.0) && !(waterLevel() > 600.0)) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == MessagesReceived && successiveStops() >= 3) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!steamDeviceSound() || !pumpControllersSound())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (otherUnitsSound() && !(!levelDeviceSound())))) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL)
        }
        else if (event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && !(!levelDeviceSound()) && !otherUnitsSound())) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED)
        }
        else if (event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && waterLevel() < 400.0) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && waterLevel() > 600.0) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
        }
        else if (event == MessagesReceived && (!(((successiveStops() >= 3) || (transmissionFailure() || steamBoilerWaitingReceived() || physicalUnitsReadyReceived()) || (lowestReachableLevel() < 150.0 || highestReachableLevel() > 800.0)) || (!steamDeviceSound() || !pumpControllersSound())) && (!levelDeviceSound())) && !(waterLevel() < 400.0) && !(waterLevel() > 600.0)) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE)
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
