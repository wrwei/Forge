// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype ValvePosition = VALVE_CLOSED | VALVE_OPEN
datatype FlowObservation = NO_WATER_FLOW | WATER_FLOWING
datatype ModeValue = INITIALIZATION_MODE | NORMAL_MODE | DEGRADED_MODE | RESCUE_MODE | EMERGENCY_STOP_MODE
datatype InputEvent = NoEvent | TransmissionReceived

// Static constants
const PUMPS: int := 4
const NORMAL_N1: real := 400.0
const NORMAL_N2: real := 600.0
const PUMP_CAPACITY_P: real := 10.0
const GRADIENT_U1: real := 2.0
const P: real := 10.0
const CAPACITY_C: real := 1000.0
const PUMP_COUNT: int := 4
const T: real := 5.0
const GRADIENT_U2: real := 2.0
const W: real := 25.0
const LIMIT_M2: real := 800.0
const STEAM_MAX_W: real := 25.0
const LIMIT_M1: real := 150.0
const CYCLE_PERIOD: real := 5.0
const STOP_REPETITIONS: int := 3
const U1: real := 2.0
const U2: real := 2.0

class SteamBoilerController {
    var mode: Mode

    // Abstracted functions (from dependency objects -- uninterpreted)
    function steamBoilerWaitingReceived(): bool
        reads this
    function steamDeviceFailed(): bool
        reads this
    function programReadyAnnounced(): bool
        reads this
    function stopRequestedThrice(): bool
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this
    function levelDeviceFailed(): bool
        reads this
    function plantWaitingSignalled(): bool
        reads this
    function otherUnitFailed(): bool
        reads this
    function levelRisk(): bool
        reads this
    function pumpControlFailed(): bool
        reads this
    function steamRate(): real
        reads this
    function transmissionFailure(): bool
        reads this
    function waterLevel(): real
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   startupFault := transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed()
    //   unitsReady := physicalUnitsReadyReceived()
    //   estimateUnreliable := steamDeviceFailed() || pumpControlFailed()
    //   transmissionLost := transmissionFailure()
    //   initialChecksPassed := plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())
    //   nonLevelUnitFailed := otherUnitFailed()
    //   startMessageOutOfPlace := steamBoilerWaitingReceived() || physicalUnitsReadyReceived()
    //   levelBelowN1 := waterLevel() < 400.0
    //   runningChecksPassed := !(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()
    //   linkFailed := transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())
    //   steamUnitFailed := steamDeviceFailed()
    //   levelAboveN2 := waterLevel() > 600.0
    //   readyAnnounced := programReadyAnnounced()
    //   operatorStopRequested := stopRequestedThrice()
    //   steamNotZero := steamRate() != 0.0
    //   levelUnitFailed := levelDeviceFailed()
    //   plantWaiting := plantWaitingSignalled()
    //   pumpControlUnitFailed := pumpControlFailed()
    //   levelNearLimit := levelRisk()

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
        ensures old(event == TransmissionReceived && stopRequestedThrice()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && !(plantWaitingSignalled())) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && !(plantWaitingSignalled())) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && transmissionFailure()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && transmissionFailure()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && !(plantWaitingSignalled())) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && steamRate() != 0.0) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && steamRate() != 0.0) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && transmissionFailure()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && !(plantWaitingSignalled())) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && !(steamRate() != 0.0) && levelDeviceFailed()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && !(steamRate() != 0.0) && levelDeviceFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && steamRate() != 0.0) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && transmissionFailure()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && !(plantWaitingSignalled())) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && physicalUnitsReadyReceived() && !(otherUnitFailed())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && physicalUnitsReadyReceived() && !(otherUnitFailed())) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && !(steamRate() != 0.0) && levelDeviceFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && steamRate() != 0.0) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && transmissionFailure()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && !(plantWaitingSignalled())) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && physicalUnitsReadyReceived() && otherUnitFailed()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && physicalUnitsReadyReceived() && otherUnitFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && physicalUnitsReadyReceived() && !(otherUnitFailed())) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && !(steamRate() != 0.0) && levelDeviceFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && steamRate() != 0.0) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && transmissionFailure()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && !(plantWaitingSignalled())) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && !(physicalUnitsReadyReceived()) && programReadyAnnounced()) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && !(physicalUnitsReadyReceived()) && programReadyAnnounced()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && physicalUnitsReadyReceived() && otherUnitFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && physicalUnitsReadyReceived() && !(otherUnitFailed())) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && !(steamRate() != 0.0) && levelDeviceFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && steamRate() != 0.0) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && transmissionFailure()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && !(plantWaitingSignalled())) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && !(physicalUnitsReadyReceived()) && !(programReadyAnnounced()) && waterLevel() > 600.0) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && !(physicalUnitsReadyReceived()) && !(programReadyAnnounced()) && waterLevel() > 600.0) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && !(physicalUnitsReadyReceived()) && programReadyAnnounced()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && physicalUnitsReadyReceived() && otherUnitFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && physicalUnitsReadyReceived() && !(otherUnitFailed())) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && !(steamRate() != 0.0) && levelDeviceFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && steamRate() != 0.0) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && transmissionFailure()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && !(plantWaitingSignalled())) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && !(physicalUnitsReadyReceived()) && !(programReadyAnnounced()) && !(waterLevel() > 600.0) && waterLevel() < 400.0) ==> mode == INITIALIZATION
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && !(physicalUnitsReadyReceived()) && !(programReadyAnnounced()) && !(waterLevel() > 600.0) && waterLevel() < 400.0) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && !(physicalUnitsReadyReceived()) && !(programReadyAnnounced()) && waterLevel() > 600.0) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && !(physicalUnitsReadyReceived()) && programReadyAnnounced()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && physicalUnitsReadyReceived() && otherUnitFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && physicalUnitsReadyReceived() && !(otherUnitFailed())) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && !(steamRate() != 0.0) && levelDeviceFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && steamRate() != 0.0) && !(event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && transmissionFailure()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && !(plantWaitingSignalled())) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && !(physicalUnitsReadyReceived()) && !(programReadyAnnounced()) && !(waterLevel() > 600.0) && !(waterLevel() < 400.0)) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == TransmissionReceived && stopRequestedThrice()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && !(plantWaitingSignalled())) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && transmissionFailure()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && steamRate() != 0.0) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && plantWaitingSignalled() && !(transmissionFailure()) && !(steamRate() != 0.0) && levelDeviceFailed()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && physicalUnitsReadyReceived() && !(otherUnitFailed())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && physicalUnitsReadyReceived() && otherUnitFailed()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && !(physicalUnitsReadyReceived()) && programReadyAnnounced()) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && !(physicalUnitsReadyReceived()) && !(programReadyAnnounced()) && waterLevel() > 600.0) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && !(physicalUnitsReadyReceived()) && !(programReadyAnnounced()) && !(waterLevel() > 600.0) && waterLevel() < 400.0) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (plantWaitingSignalled() && !(transmissionFailure() || (steamRate() != 0.0) || levelDeviceFailed())) && !(physicalUnitsReadyReceived()) && !(programReadyAnnounced()) && !(waterLevel() > 600.0) && !(waterLevel() < 400.0)) {
            mode := INITIALIZATION;
            // action: ModeMessage(INITIALIZATION_MODE)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && stopRequestedThrice()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && levelDeviceFailed()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && levelDeviceFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !(levelDeviceFailed()) && otherUnitFailed()) ==> mode == DEGRADED
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !(levelDeviceFailed()) && otherUnitFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && levelDeviceFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !(levelDeviceFailed()) && !(otherUnitFailed())) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == TransmissionReceived && stopRequestedThrice()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && levelDeviceFailed()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !(levelDeviceFailed()) && otherUnitFailed()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !(levelDeviceFailed()) && !(otherUnitFailed())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && stopRequestedThrice()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && levelDeviceFailed()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && levelDeviceFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !(levelDeviceFailed()) && !(otherUnitFailed())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !(levelDeviceFailed()) && !(otherUnitFailed())) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && levelDeviceFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !(levelDeviceFailed()) && otherUnitFailed()) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == TransmissionReceived && stopRequestedThrice()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && levelDeviceFailed()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !(levelDeviceFailed()) && !(otherUnitFailed())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !(levelDeviceFailed()) && otherUnitFailed()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == TransmissionReceived && stopRequestedThrice()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && (steamDeviceFailed() || pumpControlFailed())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && (steamDeviceFailed() || pumpControlFailed())) && !(event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !((steamDeviceFailed() || pumpControlFailed())) && levelDeviceFailed()) ==> mode == RESCUE
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !((steamDeviceFailed() || pumpControlFailed())) && levelDeviceFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && (steamDeviceFailed() || pumpControlFailed())) && !(event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !((steamDeviceFailed() || pumpControlFailed())) && !(levelDeviceFailed()) && !(otherUnitFailed())) ==> mode == NORMAL
        ensures old(!(event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !((steamDeviceFailed() || pumpControlFailed())) && !(levelDeviceFailed()) && !(otherUnitFailed())) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !((steamDeviceFailed() || pumpControlFailed())) && levelDeviceFailed()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && (steamDeviceFailed() || pumpControlFailed())) && !(event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) && !(event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && !(event == TransmissionReceived && stopRequestedThrice()) && event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !((steamDeviceFailed() || pumpControlFailed())) && !(levelDeviceFailed()) && otherUnitFailed()) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == TransmissionReceived && stopRequestedThrice()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && !((transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived()))) && levelRisk()) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && (steamDeviceFailed() || pumpControlFailed())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(EMERGENCY_STOP_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !((steamDeviceFailed() || pumpControlFailed())) && levelDeviceFailed()) {
            mode := RESCUE;
            // action: ModeMessage(RESCUE_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !((steamDeviceFailed() || pumpControlFailed())) && !(levelDeviceFailed()) && !(otherUnitFailed())) {
            mode := NORMAL;
            // action: ModeMessage(NORMAL_MODE)
        }
        else if (event == TransmissionReceived && !(stopRequestedThrice()) && (!(transmissionFailure() || (steamBoilerWaitingReceived() || physicalUnitsReadyReceived())) && !levelRisk()) && !((steamDeviceFailed() || pumpControlFailed())) && !(levelDeviceFailed()) && otherUnitFailed()) {
            mode := DEGRADED;
            // action: ModeMessage(DEGRADED_MODE)
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
