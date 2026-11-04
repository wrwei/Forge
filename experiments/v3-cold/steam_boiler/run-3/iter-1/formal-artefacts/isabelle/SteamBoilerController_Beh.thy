theory SteamBoilerController_Beh
imports "Z_Machines.Z_Machine"
begin

subsection \<open> Introduction \<close>

text \<open> This theory file is to model the SteamBoilerController state machine in Z Machine notations.\<close>

notation undefined ("???")

subsection \<open> type definition \<close>

datatype ('s, 'e) tag =
  State (ofState: 's) | Event (ofEvent: 'e)

abbreviation "is_Event x \<equiv> \<not> is_State x"

type_synonym ('s, 'e) rctrace = "('s, 'e) tag list"

definition wf_rcstore :: "('s, 'e) rctrace \<Rightarrow> 's \<Rightarrow> 's option \<Rightarrow> bool" where
[z_defs]: "wf_rcstore tr st final = (
     length(tr) > 0 
   \<and> tr ! ((length tr) -1) = State st 
   \<and> (final \<noteq> None \<longrightarrow> (\<forall>i<length tr. tr ! i = State (the final) \<longrightarrow> i= (length tr) -1)) 
   \<and> (filter is_State tr) ! (length (filter is_State tr) -1) = State  st)"
   
enumtype St = INITIALIZATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP | initial 
 


enumtype Evt = transmissionReceived | mode | programReady | valve | closePump | openPump 
 


enumtype FlowObservation = WATER_NOT_FLOWING | WATER_FLOWING

enumtype PumpPosition = PUMP_OFF | PUMP_ON

enumtype ValvePosition = VALVE_CLOSED | VALVE_OPEN

instantiation real :: default
begin
  definition default_real :: "real" where "default_real = 0"
  instance ..
end

instantiation real :: "show"
begin
  instance ..
end
record PumpControlReport = controlledPump :: int observedFlow :: FlowObservation
record_default PumpControlReport
show_record PumpControlReport

record PumpStateReport = pumpNumber :: int reportedPosition :: PumpPosition
record_default PumpStateReport
show_record PumpStateReport


text \<open> function definition \<close>

consts consecutiveStopCount :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stopTransmissionsForEmergency :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts transmissionFailed :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts unitsReadyReceived :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts boilerWaitingReceived :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts steamReading :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelDeviceFailed :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts otherUnitFailed :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelReading :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts normalMaxN2 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts normalMinN1 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelEstimate :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts worstCaseLevelHighPumpsKept :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts limitMaxM2 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts worstCaseLevelLowPumpsClosed :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts limitMinM1 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts worstCaseLevelHighPumpsClosed :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts worstCaseLevelLowPumpsKept :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts steamDeviceFailed :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts pumpControllerFailed :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore SteamBoilerController =
  plantWaiting :: "bool"
  evacuationValve :: "ValvePosition"
  readyAnnounced :: "bool"
  st::"St"
  tr :: "(St, Evt) tag list"
  listens :: "Evt set"   (* FORK: STATIC. The events state `st` listens
                            for. Written by every operation's update to the
                            target state's awaited set; pinned per state by the
                            invariant below. A property OF THE AUTOMATON. *)
  offered :: "Evt set"   (* DYNAMIC. The events the environment offers THIS
                            step. Environment model B (2026-10-20): the
                            invariant pins, per state, offered = {} or
                            offered = {e} for one listened e -- the Java step
                            consumes at most one input event. Every operation
                            sets offered' to a fresh choice `nxt` drawn from the
                            target state's allowed offers. Read by trigger
                            presence conjuncts (C2) and absence conjuncts (C1). *)
  where inv:
    "tr \<noteq> []
                      \<and> (st = INITIALIZATION \<longrightarrow> listens = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived})
                      \<and> (st = NORMAL \<longrightarrow> listens = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived})
                      \<and> (st = DEGRADED \<longrightarrow> listens = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived})
                      \<and> (st = RESCUE \<longrightarrow> listens = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived})
                                                                                                                                                                                                                                                                                                                                                                                              \<and> (st = INITIALIZATION \<longrightarrow> (offered = {} \<or> offered = {transmissionReceived}))
                                                                                                                                                                                                                                                                                                                                                                                              \<and> (st = NORMAL \<longrightarrow> (offered = {} \<or> offered = {transmissionReceived}))
                                                                                                                                                                                                                                                                                                                                                                                              \<and> (st = DEGRADED \<longrightarrow> (offered = {} \<or> offered = {transmissionReceived}))
                                                                                                                                                                                                                                                                                                                                                                                              \<and> (st = RESCUE \<longrightarrow> (offered = {} \<or> offered = {transmissionReceived}))
                                                                                                                                                                                                                                                                                                                                                                                              \<and> (st = EMERGENCY_STOP \<longrightarrow> (offered = {}))
                                                                                                                                                                                                                                                                                                                                                                                              \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToINITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= initial"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr  @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP =
  over SteamBoilerController
  pre "st= INITIALIZATION \<and> consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_1 =
  over SteamBoilerController
  pre "st= INITIALIZATION \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> (transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> \<not>((plantWaiting \<or> boilerWaitingReceived())) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_2 =
  over SteamBoilerController
  pre "st= INITIALIZATION \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> (steamReading() \<noteq> 0.0) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_3 =
  over SteamBoilerController
  pre "st= INITIALIZATION \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> levelDeviceFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> readyAnnounced \<and> unitsReadyReceived() \<and> \<not>otherUnitFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> readyAnnounced \<and> unitsReadyReceived() \<and> otherUnitFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> readyAnnounced \<and> \<not>unitsReadyReceived() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event programReady] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> \<not>readyAnnounced \<and> levelReading()>normalMaxN2() \<and> \<not>evacuationValve= (VALVE_OPEN) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,evacuationValve\<Zprime> = VALVE_OPEN
         ,plantWaiting\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event valve]@ [Event closePump] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_3 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> \<not>readyAnnounced \<and> levelReading()>normalMaxN2() \<and> evacuationValve= (VALVE_OPEN) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,plantWaiting\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_4 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> \<not>readyAnnounced \<and> \<not>levelReading()>normalMaxN2() \<and> levelReading()<normalMinN1() \<and> evacuationValve= (VALVE_OPEN) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,evacuationValve\<Zprime> = VALVE_CLOSED
         ,plantWaiting\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event valve]@ [Event openPump] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_5 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> \<not>readyAnnounced \<and> \<not>levelReading()>normalMaxN2() \<and> levelReading()<normalMinN1() \<and> \<not>evacuationValve= (VALVE_OPEN) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,plantWaiting\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event openPump] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_6 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> \<not>readyAnnounced \<and> \<not>levelReading()>normalMaxN2() \<and> \<not>levelReading()<normalMinN1() \<and> evacuationValve= (VALVE_OPEN) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,evacuationValve\<Zprime> = VALVE_CLOSED
         ,readyAnnounced\<Zprime> = True
         ,plantWaiting\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event valve]@ [Event closePump]@ [Event programReady] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_7 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> \<not>readyAnnounced \<and> \<not>levelReading()>normalMaxN2() \<and> \<not>levelReading()<normalMinN1() \<and> \<not>evacuationValve= (VALVE_OPEN) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,readyAnnounced\<Zprime> = True
         ,plantWaiting\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event closePump]@ [Event programReady] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToEMERGENCY_STOP =
  over SteamBoilerController
  pre "st= NORMAL \<and> consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation NORMALToEMERGENCY_STOP_1 =
  over SteamBoilerController
  pre "st= NORMAL \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived()) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation NORMALToEMERGENCY_STOP_2 =
  over SteamBoilerController
  pre "st= NORMAL \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>(((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<and> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation NORMALToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> levelDeviceFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToRESCUE_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> levelDeviceFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToRESCUE_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> levelDeviceFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToDEGRADED_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToDEGRADED_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP =
  over SteamBoilerController
  pre "st= DEGRADED \<and> consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP_1 =
  over SteamBoilerController
  pre "st= DEGRADED \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived()) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP_2 =
  over SteamBoilerController
  pre "st= DEGRADED \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>(((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<and> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation DEGRADEDToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> levelDeviceFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToRESCUE_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> levelDeviceFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToRESCUE_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> levelDeviceFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToNORMAL_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToNORMAL_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToEMERGENCY_STOP =
  over SteamBoilerController
  pre "st= RESCUE \<and> consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_1 =
  over SteamBoilerController
  pre "st= RESCUE \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived()) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_2 =
  over SteamBoilerController
  pre "st= RESCUE \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>(((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<and> steamDeviceFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_3 =
  over SteamBoilerController
  pre "st= RESCUE \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>(((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<and> \<not>steamDeviceFailed() \<and> pumpControllerFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_4 =
  over SteamBoilerController
  pre "st= RESCUE \<and> \<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>(((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<and> \<not>steamDeviceFailed() \<and> \<not>pumpControllerFailed() \<and> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation RESCUEToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToNORMAL_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToNORMAL_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToDEGRADED_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToDEGRADED_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> levelDeviceFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> levelDeviceFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> levelDeviceFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_INITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}"
  pre "st= INITIALIZATION \<and> \<not>(consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> (transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> \<not>((plantWaiting \<or> boilerWaitingReceived())) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> (steamReading() \<noteq> 0.0) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> levelDeviceFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> readyAnnounced \<and> unitsReadyReceived() \<and> \<not>otherUnitFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> readyAnnounced \<and> unitsReadyReceived() \<and> otherUnitFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> readyAnnounced \<and> \<not>unitsReadyReceived() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> \<not>readyAnnounced \<and> levelReading()>normalMaxN2() \<and> \<not>evacuationValve= (VALVE_OPEN) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> \<not>readyAnnounced \<and> levelReading()>normalMaxN2() \<and> evacuationValve= (VALVE_OPEN) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> \<not>readyAnnounced \<and> \<not>levelReading()>normalMaxN2() \<and> levelReading()<normalMinN1() \<and> evacuationValve= (VALVE_OPEN) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> \<not>readyAnnounced \<and> \<not>levelReading()>normalMaxN2() \<and> levelReading()<normalMinN1() \<and> \<not>evacuationValve= (VALVE_OPEN) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> \<not>readyAnnounced \<and> \<not>levelReading()>normalMaxN2() \<and> \<not>levelReading()<normalMinN1() \<and> evacuationValve= (VALVE_OPEN) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>((transmissionFailed() \<or> unitsReadyReceived() \<and> \<not>readyAnnounced)) \<and> (plantWaiting \<or> boilerWaitingReceived()) \<and> \<not>(steamReading() \<noteq> 0.0) \<and> \<not>levelDeviceFailed() \<and> \<not>readyAnnounced \<and> \<not>levelReading()>normalMaxN2() \<and> \<not>levelReading()<normalMinN1() \<and> \<not>evacuationValve= (VALVE_OPEN) \<and> transmissionReceived \<in> offered)"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_NORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}"
  pre "st= NORMAL \<and> \<not>(consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived()) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>(((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<and> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> levelDeviceFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> levelDeviceFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> levelDeviceFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered)"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_DEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}"
  pre "st= DEGRADED \<and> \<not>(consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived()) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>(((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<and> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> levelDeviceFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> levelDeviceFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> levelDeviceFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()))) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered)"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_RESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}"
  pre "st= RESCUE \<and> \<not>(consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived()) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>(((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<and> steamDeviceFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>(((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<and> \<not>steamDeviceFailed() \<and> pumpControllerFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<and> \<not>(((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<and> \<not>steamDeviceFailed() \<and> \<not>pumpControllerFailed() \<and> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1()) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> levelDeviceFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> levelDeviceFailed() \<and> (levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>(((((consecutiveStopCount()\<ge>stopTransmissionsForEmergency() \<or> ((transmissionFailed() \<or> boilerWaitingReceived()) \<or> unitsReadyReceived())) \<or> ((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2()) \<and> (worstCaseLevelLowPumpsClosed()<limitMinM1() \<or> worstCaseLevelHighPumpsClosed()>limitMaxM2()) \<or> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> worstCaseLevelLowPumpsKept()<limitMinM1())) \<or> steamDeviceFailed()) \<or> pumpControllerFailed())) \<and> levelDeviceFailed() \<and> \<not>((levelEstimate()>normalMaxN2() \<or> worstCaseLevelHighPumpsKept()>limitMaxM2())) \<and> \<not>levelEstimate()<normalMinN1() \<and> transmissionReceived \<in> offered)"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

definition Init :: "SteamBoilerController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ,offered\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine SteamBoilerControllerMachine =
  init Init
  invariant SteamBoilerController_inv
  operations  InitialToINITIALIZATION INITIALIZATIONToEMERGENCY_STOP INITIALIZATIONToEMERGENCY_STOP_1 INITIALIZATIONToINITIALIZATION INITIALIZATIONToEMERGENCY_STOP_2 INITIALIZATIONToEMERGENCY_STOP_3 INITIALIZATIONToNORMAL INITIALIZATIONToDEGRADED INITIALIZATIONToINITIALIZATION_1 INITIALIZATIONToINITIALIZATION_2 INITIALIZATIONToINITIALIZATION_3 INITIALIZATIONToINITIALIZATION_4 INITIALIZATIONToINITIALIZATION_5 INITIALIZATIONToINITIALIZATION_6 INITIALIZATIONToINITIALIZATION_7 NORMALToEMERGENCY_STOP NORMALToEMERGENCY_STOP_1 NORMALToEMERGENCY_STOP_2 NORMALToRESCUE NORMALToRESCUE_1 NORMALToRESCUE_2 NORMALToDEGRADED NORMALToDEGRADED_1 NORMALToDEGRADED_2 NORMALToNORMAL NORMALToNORMAL_1 NORMALToNORMAL_2 DEGRADEDToEMERGENCY_STOP DEGRADEDToEMERGENCY_STOP_1 DEGRADEDToEMERGENCY_STOP_2 DEGRADEDToRESCUE DEGRADEDToRESCUE_1 DEGRADEDToRESCUE_2 DEGRADEDToNORMAL DEGRADEDToNORMAL_1 DEGRADEDToNORMAL_2 DEGRADEDToDEGRADED DEGRADEDToDEGRADED_1 DEGRADEDToDEGRADED_2 RESCUEToEMERGENCY_STOP RESCUEToEMERGENCY_STOP_1 RESCUEToEMERGENCY_STOP_2 RESCUEToEMERGENCY_STOP_3 RESCUEToEMERGENCY_STOP_4 RESCUEToNORMAL RESCUEToNORMAL_1 RESCUEToNORMAL_2 RESCUEToDEGRADED RESCUEToDEGRADED_1 RESCUEToDEGRADED_2 RESCUEToRESCUE RESCUEToRESCUE_1 RESCUEToRESCUE_2 Idle_INITIALIZATION Idle_NORMAL Idle_DEGRADED Idle_RESCUE
  until "st = EMERGENCY_STOP"
(* `until` marks states exempt from deadlock-freedom (deadlock_free_z_machine's
   End parameter). These are states that are the source of no transition, so the
   machine has terminated there and is not required to offer an event. Without this
   clause End defaults to False and the obligation is FALSE at such a state --
   machine-checked, see scripts/test_f17_probe.sh. *)

subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SteamBoilerController_inv"
  by zpog_full

lemma InitialToINITIALIZATION_inv [hoare_lemmas]: "InitialToINITIALIZATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToEMERGENCY_STOP_inv [hoare_lemmas]: "INITIALIZATIONToEMERGENCY_STOP() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma INITIALIZATIONToEMERGENCY_STOP_1_inv [hoare_lemmas]: "INITIALIZATIONToEMERGENCY_STOP_1() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma INITIALIZATIONToINITIALIZATION_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToEMERGENCY_STOP_2_inv [hoare_lemmas]: "INITIALIZATIONToEMERGENCY_STOP_2() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma INITIALIZATIONToEMERGENCY_STOP_3_inv [hoare_lemmas]: "INITIALIZATIONToEMERGENCY_STOP_3() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma INITIALIZATIONToNORMAL_inv [hoare_lemmas]: "INITIALIZATIONToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToDEGRADED_inv [hoare_lemmas]: "INITIALIZATIONToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_1_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_2_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_3_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_3 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_4_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_4 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_5_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_5 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_6_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_6 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_7_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_7 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToEMERGENCY_STOP_inv [hoare_lemmas]: "NORMALToEMERGENCY_STOP() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma NORMALToEMERGENCY_STOP_1_inv [hoare_lemmas]: "NORMALToEMERGENCY_STOP_1() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma NORMALToEMERGENCY_STOP_2_inv [hoare_lemmas]: "NORMALToEMERGENCY_STOP_2() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma NORMALToRESCUE_inv [hoare_lemmas]: "NORMALToRESCUE (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToRESCUE_1_inv [hoare_lemmas]: "NORMALToRESCUE_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToRESCUE_2_inv [hoare_lemmas]: "NORMALToRESCUE_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToDEGRADED_inv [hoare_lemmas]: "NORMALToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToDEGRADED_1_inv [hoare_lemmas]: "NORMALToDEGRADED_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToDEGRADED_2_inv [hoare_lemmas]: "NORMALToDEGRADED_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToNORMAL_inv [hoare_lemmas]: "NORMALToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToNORMAL_1_inv [hoare_lemmas]: "NORMALToNORMAL_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToNORMAL_2_inv [hoare_lemmas]: "NORMALToNORMAL_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToEMERGENCY_STOP_inv [hoare_lemmas]: "DEGRADEDToEMERGENCY_STOP() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma DEGRADEDToEMERGENCY_STOP_1_inv [hoare_lemmas]: "DEGRADEDToEMERGENCY_STOP_1() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma DEGRADEDToEMERGENCY_STOP_2_inv [hoare_lemmas]: "DEGRADEDToEMERGENCY_STOP_2() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma DEGRADEDToRESCUE_inv [hoare_lemmas]: "DEGRADEDToRESCUE (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToRESCUE_1_inv [hoare_lemmas]: "DEGRADEDToRESCUE_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToRESCUE_2_inv [hoare_lemmas]: "DEGRADEDToRESCUE_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToNORMAL_inv [hoare_lemmas]: "DEGRADEDToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToNORMAL_1_inv [hoare_lemmas]: "DEGRADEDToNORMAL_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToNORMAL_2_inv [hoare_lemmas]: "DEGRADEDToNORMAL_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToDEGRADED_inv [hoare_lemmas]: "DEGRADEDToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToDEGRADED_1_inv [hoare_lemmas]: "DEGRADEDToDEGRADED_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToDEGRADED_2_inv [hoare_lemmas]: "DEGRADEDToDEGRADED_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToEMERGENCY_STOP_inv [hoare_lemmas]: "RESCUEToEMERGENCY_STOP() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma RESCUEToEMERGENCY_STOP_1_inv [hoare_lemmas]: "RESCUEToEMERGENCY_STOP_1() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma RESCUEToEMERGENCY_STOP_2_inv [hoare_lemmas]: "RESCUEToEMERGENCY_STOP_2() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma RESCUEToEMERGENCY_STOP_3_inv [hoare_lemmas]: "RESCUEToEMERGENCY_STOP_3() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma RESCUEToEMERGENCY_STOP_4_inv [hoare_lemmas]: "RESCUEToEMERGENCY_STOP_4() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma RESCUEToNORMAL_inv [hoare_lemmas]: "RESCUEToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToNORMAL_1_inv [hoare_lemmas]: "RESCUEToNORMAL_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToNORMAL_2_inv [hoare_lemmas]: "RESCUEToNORMAL_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToDEGRADED_inv [hoare_lemmas]: "RESCUEToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToDEGRADED_1_inv [hoare_lemmas]: "RESCUEToDEGRADED_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToDEGRADED_2_inv [hoare_lemmas]: "RESCUEToDEGRADED_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToRESCUE_inv [hoare_lemmas]: "RESCUEToRESCUE (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToRESCUE_1_inv [hoare_lemmas]: "RESCUEToRESCUE_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToRESCUE_2_inv [hoare_lemmas]: "RESCUEToRESCUE_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_INITIALIZATION_inv [hoare_lemmas]: "Idle_INITIALIZATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_NORMAL_inv [hoare_lemmas]: "Idle_NORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_DEGRADED_inv [hoare_lemmas]: "Idle_DEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_RESCUE_inv [hoare_lemmas]: "Idle_RESCUE (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToINITIALIZATION (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToEMERGENCY_STOP() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToEMERGENCY_STOP_1() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToEMERGENCY_STOP_2() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToEMERGENCY_STOP_3() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_3 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_4 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_5 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_6 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_7 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToEMERGENCY_STOP() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToEMERGENCY_STOP_1() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToEMERGENCY_STOP_2() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToRESCUE (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToRESCUE_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToRESCUE_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToDEGRADED_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToDEGRADED_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToNORMAL_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToNORMAL_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToEMERGENCY_STOP() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToEMERGENCY_STOP_1() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToEMERGENCY_STOP_2() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToRESCUE (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToRESCUE_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToRESCUE_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToNORMAL_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToNORMAL_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToDEGRADED_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToDEGRADED_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToEMERGENCY_STOP() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToEMERGENCY_STOP_1() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToEMERGENCY_STOP_2() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToEMERGENCY_STOP_3() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToEMERGENCY_STOP_4() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToNORMAL_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToNORMAL_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToDEGRADED_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToDEGRADED_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToRESCUE (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToRESCUE_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToRESCUE_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_INITIALIZATION (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_NORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_DEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_RESCUE (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "SteamBoilerController_axioms = True"

(* Terminal state(s), with no outgoing transition: EMERGENCY_STOP.
   The machine's `until` clause exempts them from the deadlock-freedom
   obligation below: the machine has finished there. *)
lemma SteamBoilerController_deadlock_free: "SteamBoilerController_axioms  \<Longrightarrow> deadlock_free SteamBoilerControllerMachine"
  unfolding SteamBoilerControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
