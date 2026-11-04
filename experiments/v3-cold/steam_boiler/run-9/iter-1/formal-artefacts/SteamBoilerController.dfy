// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype ValvePosition = CLOSED | OPEN
datatype InputEvent = NoEvent | TransmissionComplete

// Static constants
const MAX_STEAM_W: real := 25.0
const PUMPS: int := 4
const MIN_LIMIT_M1: real := 100.0
const CYCLE_SECONDS: real := 5.0
const PUMP_CAPACITY_P: real := 10.0
const STEAM_INCREASE_U1: real := 2.0
const MAX_LIMIT_M2: real := 900.0
const CAPACITY_C: real := 1000.0
const PUMP_COUNT: int := 4
const SETTLED: int := 3
const MAX_NORMAL_N2: real := 600.0
const STEAM_DECREASE_U2: real := 2.0
const STOP_REPETITIONS: int := 3
const MIN_NORMAL_N1: real := 400.0

class SteamBoilerController {
    var mode: Mode

    // State variables (updated from sensor each step)
    var plantWaiting: bool
    var readyAnnounced: bool

    // Abstracted functions (from dependency objects -- uninterpreted)
    function steamBoilerWaitingReceived(): bool
        reads this
    function successiveStops(): int
        reads this
    function predictedLevelLow(): real
        reads this
    function steamDeviceFailed(): bool
        reads this
    function levelEstimate(): real
        reads this
    function steamReading(): real
        reads this
    function levelReading(): real
        reads this
    function levelDeviceFailed(): bool
        reads this
    function anyPumpControllerFailed(): bool
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this
    function anyPumpFailed(): bool
        reads this
    function predictedLevelHigh(): real
        reads this
    function transmissionFailure(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   waterBelowNormal := levelEstimate() < 400.0
    //   operatorStop := successiveStops() >= 3
    //   unitsReady := physicalUnitsReadyReceived()
    //   readingAboveNormal := levelReading() > 600.0
    //   waitingNow := steamBoilerWaitingReceived()
    //   handshakeDone := readyAnnounced && physicalUnitsReadyReceived()
    //   levelRiskHigh := predictedLevelHigh() > 900.0
    //   otherUnitFault := steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()
    //   steamFault := steamDeviceFailed()
    //   transmissionFault := transmissionFailure()
    //   plantStarted := plantWaiting || steamBoilerWaitingReceived()
    //   pumpControlFault := anyPumpControllerFailed()
    //   rescueEmergency := ((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed()
    //   levelFault := levelDeviceFailed()
    //   pumpFault := anyPumpFailed()
    //   waterHeadingAboveNormal := predictedLevelHigh() > 600.0
    //   initAbort := (successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed()))
    //   levelRiskLow := predictedLevelLow() < 100.0
    //   emergencyNeeded := (successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)
    //   steamNotZero := steamReading() != 0.0
    //   readingBelowNormal := levelReading() < 400.0

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
        readyAnnounced := false;
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(event == TransmissionComplete && ((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionComplete && ((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && !((plantWaiting || steamBoilerWaitingReceived()))) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && !((plantWaiting || steamBoilerWaitingReceived()))) && !(event == TransmissionComplete && ((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) ==> mode == NORMAL
        ensures old(!(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && !((plantWaiting || steamBoilerWaitingReceived()))) && !(event == TransmissionComplete && ((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) ==> mode == DEGRADED
        ensures old(!(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && !((plantWaiting || steamBoilerWaitingReceived()))) && !(event == TransmissionComplete && ((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && !(physicalUnitsReadyReceived())) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && !(physicalUnitsReadyReceived())) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && !((plantWaiting || steamBoilerWaitingReceived()))) && !(event == TransmissionComplete && ((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && !(readyAnnounced) && levelReading() > 600.0) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && !(readyAnnounced) && levelReading() > 600.0) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && !(physicalUnitsReadyReceived())) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && !((plantWaiting || steamBoilerWaitingReceived()))) && !(event == TransmissionComplete && ((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && !(readyAnnounced) && levelReading() < 400.0) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && !(readyAnnounced) && levelReading() < 400.0) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && !(readyAnnounced) && levelReading() > 600.0) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && !(physicalUnitsReadyReceived())) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && !((plantWaiting || steamBoilerWaitingReceived()))) && !(event == TransmissionComplete && ((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && !(readyAnnounced) && !(levelReading() > 600.0) && !(levelReading() < 400.0)) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == TransmissionComplete && ((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && !((plantWaiting || steamBoilerWaitingReceived()))) {
            mode := INITIALIZATION;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && physicalUnitsReadyReceived() && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) {
            mode := NORMAL;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && physicalUnitsReadyReceived() && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) {
            mode := DEGRADED;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && readyAnnounced && !(physicalUnitsReadyReceived())) {
            mode := INITIALIZATION;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && !(readyAnnounced) && levelReading() > 600.0) {
            mode := INITIALIZATION;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && !(readyAnnounced) && levelReading() < 400.0) {
            mode := INITIALIZATION;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || ((plantWaiting || steamBoilerWaitingReceived()) && (transmissionFailure() || (steamReading() != 0.0) || levelDeviceFailed())))) && (plantWaiting || steamBoilerWaitingReceived()) && !(readyAnnounced) && !(levelReading() > 600.0) && !(levelReading() < 400.0)) {
            mode := INITIALIZATION;
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == TransmissionComplete && ((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionComplete && ((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && levelDeviceFailed()) ==> mode == RESCUE
        ensures old(!(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && levelDeviceFailed()) && !(event == TransmissionComplete && ((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) ==> mode == DEGRADED
        ensures old(!(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && levelDeviceFailed()) && !(event == TransmissionComplete && ((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && levelEstimate() < 400.0 && !(predictedLevelHigh() > 600.0)) ==> mode == NORMAL
        ensures old(!(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && levelEstimate() < 400.0 && !(predictedLevelHigh() > 600.0)) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && levelDeviceFailed()) && !(event == TransmissionComplete && ((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && predictedLevelHigh() > 600.0) ==> mode == NORMAL
        ensures old(!(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && predictedLevelHigh() > 600.0) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && levelEstimate() < 400.0 && !(predictedLevelHigh() > 600.0)) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && levelDeviceFailed()) && !(event == TransmissionComplete && ((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(levelEstimate() < 400.0) && !(predictedLevelHigh() > 600.0)) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == TransmissionComplete && ((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && levelDeviceFailed()) {
            mode := RESCUE;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) {
            mode := DEGRADED;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && levelEstimate() < 400.0 && !(predictedLevelHigh() > 600.0)) {
            mode := NORMAL;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && predictedLevelHigh() > 600.0) {
            mode := NORMAL;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(levelEstimate() < 400.0) && !(predictedLevelHigh() > 600.0)) {
            mode := NORMAL;
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == TransmissionComplete && ((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionComplete && ((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && levelDeviceFailed()) ==> mode == RESCUE
        ensures old(!(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && levelDeviceFailed()) && !(event == TransmissionComplete && ((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) ==> mode == NORMAL
        ensures old(!(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && levelDeviceFailed()) && !(event == TransmissionComplete && ((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && levelEstimate() < 400.0 && !(predictedLevelHigh() > 600.0)) ==> mode == DEGRADED
        ensures old(!(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && levelEstimate() < 400.0 && !(predictedLevelHigh() > 600.0)) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && levelDeviceFailed()) && !(event == TransmissionComplete && ((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && predictedLevelHigh() > 600.0) ==> mode == DEGRADED
        ensures old(!(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && predictedLevelHigh() > 600.0) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && levelEstimate() < 400.0 && !(predictedLevelHigh() > 600.0)) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && levelDeviceFailed()) && !(event == TransmissionComplete && ((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && !(levelEstimate() < 400.0) && !(predictedLevelHigh() > 600.0)) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == TransmissionComplete && ((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && levelDeviceFailed()) {
            mode := RESCUE;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) {
            mode := NORMAL;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && levelEstimate() < 400.0 && !(predictedLevelHigh() > 600.0)) {
            mode := DEGRADED;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && predictedLevelHigh() > 600.0) {
            mode := DEGRADED;
        }
        else if (event == TransmissionComplete && !(((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0))) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()) && !(levelEstimate() < 400.0) && !(predictedLevelHigh() > 600.0)) {
            mode := DEGRADED;
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == TransmissionComplete && (((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionComplete && (((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) ==> mode == NORMAL
        ensures old(!(event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == TransmissionComplete && (((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) ==> mode == DEGRADED
        ensures old(!(event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == TransmissionComplete && (((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && levelEstimate() < 400.0 && !(predictedLevelHigh() > 600.0)) ==> mode == RESCUE
        ensures old(!(event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && levelEstimate() < 400.0 && !(predictedLevelHigh() > 600.0)) && !(event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == TransmissionComplete && (((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && predictedLevelHigh() > 600.0) ==> mode == RESCUE
        ensures old(!(event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && predictedLevelHigh() > 600.0) && !(event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && levelEstimate() < 400.0 && !(predictedLevelHigh() > 600.0)) && !(event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) && !(event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) && !(event == TransmissionComplete && (((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && !(levelEstimate() < 400.0) && !(predictedLevelHigh() > 600.0)) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == TransmissionComplete && (((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) {
            mode := EMERGENCY_STOP;
        }
        else if (event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && !((steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed()))) {
            mode := NORMAL;
        }
        else if (event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && !(levelDeviceFailed()) && (steamDeviceFailed() || anyPumpFailed() || anyPumpControllerFailed())) {
            mode := DEGRADED;
        }
        else if (event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && levelEstimate() < 400.0 && !(predictedLevelHigh() > 600.0)) {
            mode := RESCUE;
        }
        else if (event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && predictedLevelHigh() > 600.0) {
            mode := RESCUE;
        }
        else if (event == TransmissionComplete && !((((successiveStops() >= 3) || transmissionFailure() || (predictedLevelLow() < 100.0) || (predictedLevelHigh() > 900.0)) || steamDeviceFailed() || anyPumpControllerFailed())) && levelDeviceFailed() && !(levelEstimate() < 400.0) && !(predictedLevelHigh() > 600.0)) {
            mode := RESCUE;
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
