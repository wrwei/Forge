// Auto-generated Dafny verification code from Java source
// Source: SteamBoilerController.java (Spoon EMF model)

datatype Mode = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP

datatype OperatingMode = MODE_INITIALIZATION | MODE_NORMAL | MODE_DEGRADED | MODE_RESCUE | MODE_EMERGENCY_STOP
datatype PumpId = PUMP1 | PUMP2 | PUMP3 | PUMP4
datatype PumpSwitch = ALL_ON | ALL_OFF
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
    var valveOpen: bool
    var programReadySent: bool

    // Abstracted functions (from dependency objects -- uninterpreted)
    function levelBelowNormalBand(): bool
        reads this
    function levelAboveNormalBand(): bool
        reads this
    function boilerWaitingSeen(): bool
        reads this
    function levelDeviceSound(): bool
        reads this
    function otherUnitsSound(): bool
        reads this
    function physicalUnitsReadyReceived(): bool
        reads this
    function emergencyDemanded(): bool
        reads this

    // Guard predicates (inlined from Java local variables in step())
    //   initAdjusting := (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen()
    //   levelFailureNoEmergency := !(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()
    //   readyAberrant := physicalUnitsReadyReceived() && !programReadySent
    //   operatingEmergency := emergencyDemanded() || physicalUnitsReadyReceived()
    //   soundNoEmergency := !(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound())
    //   levelInBand := !levelBelowNormalBand() && !levelAboveNormalBand()
    //   unitsReadyAllSound := (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())
    //   levelBelowN1 := !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())
    //   unitsAllSound := levelDeviceSound() && otherUnitsSound()
    //   levelAboveN2 := levelAboveNormalBand() && !levelBelowNormalBand()
    //   unitsReadySomeDefective := (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())
    //   initCalm := !emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)
    //   unitsReadyAwaited := (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()
    //   unitFailureNoEmergency := !(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()
    //   initIdle := (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()

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
        valveOpen := false;
        programReadySent := false;
    }

    method transitionFromINITIALIZATION(event: InputEvent)
        requires mode == INITIALIZATION
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && valveOpen) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && !(valveOpen)) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && !(valveOpen)) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && valveOpen) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && !levelBelowNormalBand() && !levelAboveNormalBand() && valveOpen) ==> mode == INITIALIZATION
        ensures old(!(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && !levelBelowNormalBand() && !levelAboveNormalBand() && valveOpen) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && !(valveOpen)) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && valveOpen) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) && !(event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) && event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && !levelBelowNormalBand() && !levelAboveNormalBand() && !(valveOpen)) ==> mode == INITIALIZATION
        ensures Valid()
    {
        if (event == MessagesReceived && !(!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent))) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && (levelDeviceSound() && otherUnitsSound())) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && physicalUnitsReadyReceived() && !(levelDeviceSound() && otherUnitsSound())) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && programReadySent && !physicalUnitsReadyReceived()) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && !boilerWaitingSeen()) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelAboveNormalBand() && !levelBelowNormalBand() && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && levelAboveNormalBand() && !levelBelowNormalBand() && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand()) && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && !levelBelowNormalBand() && !levelAboveNormalBand() && valveOpen) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
        else if (event == MessagesReceived && (!emergencyDemanded() && !(physicalUnitsReadyReceived() && !programReadySent)) && !programReadySent && boilerWaitingSeen() && !levelBelowNormalBand() && !levelAboveNormalBand() && !(valveOpen)) {
            mode := INITIALIZATION;
            // action: ModeMessage(MODE_INITIALIZATION)
        }
    }

    method transitionFromNORMAL(event: InputEvent)
        requires mode == NORMAL
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound())) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound())) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound())) && levelAboveNormalBand() && !levelBelowNormalBand()) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound())) && levelAboveNormalBand() && !levelBelowNormalBand()) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound())) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound())) && !levelBelowNormalBand() && !levelAboveNormalBand()) ==> mode == NORMAL
        ensures Valid()
    {
        if (event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound())) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound())) && levelAboveNormalBand() && !levelBelowNormalBand()) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound())) && !levelBelowNormalBand() && !levelAboveNormalBand()) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
    }

    method transitionFromDEGRADED(event: InputEvent)
        requires mode == DEGRADED
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound()))) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound()))) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound()))) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && levelAboveNormalBand() && !levelBelowNormalBand()) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && levelAboveNormalBand() && !levelBelowNormalBand()) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound()))) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && !levelBelowNormalBand() && !levelAboveNormalBand()) ==> mode == DEGRADED
        ensures Valid()
    {
        if (event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound())) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound()))) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && levelAboveNormalBand() && !levelBelowNormalBand()) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound()) && !levelBelowNormalBand() && !levelAboveNormalBand()) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
    }

    method transitionFromRESCUE(event: InputEvent)
        requires mode == RESCUE
        requires Valid()
        modifies this
        ensures old(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) ==> mode == EMERGENCY_STOP
        ensures old(!(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound()))) ==> mode == NORMAL
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound()))) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) ==> mode == DEGRADED
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound()))) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound()))) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && levelAboveNormalBand() && !levelBelowNormalBand()) ==> mode == RESCUE
        ensures old(!(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && levelAboveNormalBand() && !levelBelowNormalBand()) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) && !(event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound()))) && !(event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) && event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && !levelBelowNormalBand() && !levelAboveNormalBand()) ==> mode == RESCUE
        ensures Valid()
    {
        if (event == MessagesReceived && (emergencyDemanded() || physicalUnitsReadyReceived())) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && (levelDeviceSound() && otherUnitsSound()))) {
            mode := NORMAL;
            // action: ModeMessage(MODE_NORMAL)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && levelDeviceSound() && !otherUnitsSound())) {
            mode := DEGRADED;
            // action: ModeMessage(MODE_DEGRADED)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && !(levelAboveNormalBand() && !levelBelowNormalBand()) && !(!levelBelowNormalBand() && !levelAboveNormalBand())) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && levelAboveNormalBand() && !levelBelowNormalBand()) {
            mode := RESCUE;
            // action: ModeMessage(MODE_RESCUE)
        }
        else if (event == MessagesReceived && (!(emergencyDemanded() || physicalUnitsReadyReceived()) && !levelDeviceSound()) && !levelBelowNormalBand() && !levelAboveNormalBand()) {
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
        if (event == MessagesReceived) {
            mode := EMERGENCY_STOP;
            // action: ModeMessage(MODE_EMERGENCY_STOP)
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
