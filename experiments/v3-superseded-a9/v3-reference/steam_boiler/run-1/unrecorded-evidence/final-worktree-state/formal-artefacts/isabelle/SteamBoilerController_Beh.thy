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
 


enumtype Evt = transmissionReceived | sendMode | setValve | stopAllPumps | sendUnitMessages | startFillPump | sendProgramReady | startAllPumps 
 


enumtype FlowObservation = NO_FLOW | WATER_FLOWING

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
record FlowReading = pump :: int flow :: FlowObservation
record_default FlowReading
show_record FlowReading

record PumpReading = pump :: int position :: PumpPosition
record_default PumpReading
show_record PumpReading


text \<open> function definition \<close>

consts successiveStops :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stopLimit :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts transmissionFault :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts plantWaiting :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts steamNonZero :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelFailed :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelAboveN2 :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelBelowN1 :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts unitsReady :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts otherFailed :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts limitRisk :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts estimateUnreliable :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts headingBelowN1 :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts headingAboveN2 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore SteamBoilerController =
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
                      \<and> (st = INITIALIZATION \<longrightarrow> listens = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived})
                      \<and> (st = NORMAL \<longrightarrow> listens = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived})
                      \<and> (st = DEGRADED \<longrightarrow> listens = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived})
                      \<and> (st = RESCUE \<longrightarrow> listens = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived})
                      \<and> (st = EMERGENCY_STOP \<longrightarrow> listens = {transmissionReceived})
                                                                                                        \<and> (st = INITIALIZATION \<longrightarrow> (offered = {} \<or> offered = {transmissionReceived}))
                                                                                                        \<and> (st = NORMAL \<longrightarrow> (offered = {} \<or> offered = {transmissionReceived}))
                                                                                                        \<and> (st = DEGRADED \<longrightarrow> (offered = {} \<or> offered = {transmissionReceived}))
                                                                                                        \<and> (st = RESCUE \<longrightarrow> (offered = {} \<or> offered = {transmissionReceived}))
                                                                                                        \<and> (st = EMERGENCY_STOP \<longrightarrow> (offered = {} \<or> offered = {transmissionReceived}))
                                                                                                        \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToINITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= initial"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr  @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> successiveStops()\<ge>stopLimit() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>successiveStops()\<ge>stopLimit() \<and> transmissionFault() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> steamNonZero() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_3 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> \<not>steamNonZero() \<and> levelFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> \<not>steamNonZero() \<and> \<not>levelFailed() \<and> \<not>readyAnnounced \<and> levelAboveN2() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event setValve]@ [Event stopAllPumps]@ [Event sendUnitMessages] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> \<not>steamNonZero() \<and> \<not>levelFailed() \<and> \<not>readyAnnounced \<and> \<not>levelAboveN2() \<and> levelBelowN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event setValve]@ [Event startFillPump]@ [Event sendUnitMessages] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> \<not>steamNonZero() \<and> \<not>levelFailed() \<and> \<not>readyAnnounced \<and> \<not>levelAboveN2() \<and> \<not>levelBelowN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,readyAnnounced\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event setValve]@ [Event stopAllPumps]@ [Event sendProgramReady]@ [Event sendUnitMessages] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> \<not>steamNonZero() \<and> \<not>levelFailed() \<and> readyAnnounced \<and> unitsReady() \<and> \<not>otherFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event sendUnitMessages] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> \<not>steamNonZero() \<and> \<not>levelFailed() \<and> readyAnnounced \<and> unitsReady() \<and> otherFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event sendUnitMessages] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_3 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> \<not>steamNonZero() \<and> \<not>levelFailed() \<and> readyAnnounced \<and> \<not>unitsReady() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event setValve]@ [Event stopAllPumps]@ [Event sendProgramReady]@ [Event sendUnitMessages] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_4 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= INITIALIZATION \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> successiveStops()\<ge>stopLimit() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToEMERGENCY_STOP_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>successiveStops()\<ge>stopLimit() \<and> transmissionFault() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToEMERGENCY_STOP_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> limitRisk() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToEMERGENCY_STOP_3 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> levelFailed() \<and> estimateUnreliable() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> levelFailed() \<and> \<not>estimateUnreliable() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event sendUnitMessages] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> otherFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event sendUnitMessages] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> \<not>otherFailed() \<and> headingBelowN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event startAllPumps]@ [Event sendUnitMessages] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> \<not>otherFailed() \<and> \<not>headingBelowN1() \<and> headingAboveN2() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event stopAllPumps]@ [Event sendUnitMessages] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= NORMAL \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event sendUnitMessages] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> successiveStops()\<ge>stopLimit() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>successiveStops()\<ge>stopLimit() \<and> transmissionFault() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> limitRisk() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP_3 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> levelFailed() \<and> estimateUnreliable() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> levelFailed() \<and> \<not>estimateUnreliable() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event sendUnitMessages] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> \<not>otherFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event sendUnitMessages] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> otherFailed() \<and> headingBelowN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event startAllPumps]@ [Event sendUnitMessages] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> otherFailed() \<and> \<not>headingBelowN1() \<and> headingAboveN2() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event stopAllPumps]@ [Event sendUnitMessages] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= DEGRADED \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event sendUnitMessages] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> successiveStops()\<ge>stopLimit() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>successiveStops()\<ge>stopLimit() \<and> transmissionFault() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> estimateUnreliable() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_3 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>estimateUnreliable() \<and> limitRisk() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>estimateUnreliable() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> \<not>otherFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event sendUnitMessages] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>estimateUnreliable() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> otherFailed() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event sendUnitMessages] @ [State DEGRADED]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>estimateUnreliable() \<and> \<not>limitRisk() \<and> levelFailed() \<and> headingBelowN1() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event startAllPumps]@ [Event sendUnitMessages] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> \<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>estimateUnreliable() \<and> \<not>limitRisk() \<and> levelFailed() \<and> \<not>headingBelowN1() \<and> headingAboveN2() \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event stopAllPumps]@ [Event sendUnitMessages] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= RESCUE \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode]@ [Event sendUnitMessages] @ [State RESCUE]
         ,listens\<Zprime> = {transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived, transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation EMERGENCY_STOPToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}" 
  pre "st= EMERGENCY_STOP \<and> transmissionReceived \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionReceived]@ [Event sendMode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmissionReceived}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_INITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}"
  pre "st= INITIALIZATION \<and> \<not>(successiveStops()\<ge>stopLimit() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> transmissionFault() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> steamNonZero() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> \<not>steamNonZero() \<and> levelFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> \<not>steamNonZero() \<and> \<not>levelFailed() \<and> \<not>readyAnnounced \<and> levelAboveN2() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> \<not>steamNonZero() \<and> \<not>levelFailed() \<and> \<not>readyAnnounced \<and> \<not>levelAboveN2() \<and> levelBelowN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> \<not>steamNonZero() \<and> \<not>levelFailed() \<and> \<not>readyAnnounced \<and> \<not>levelAboveN2() \<and> \<not>levelBelowN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> \<not>steamNonZero() \<and> \<not>levelFailed() \<and> readyAnnounced \<and> unitsReady() \<and> \<not>otherFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> \<not>steamNonZero() \<and> \<not>levelFailed() \<and> readyAnnounced \<and> unitsReady() \<and> otherFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> plantWaiting() \<and> \<not>steamNonZero() \<and> \<not>levelFailed() \<and> readyAnnounced \<and> \<not>unitsReady() \<and> transmissionReceived \<in> offered) \<and> \<not>(transmissionReceived \<in> offered)"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_NORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}"
  pre "st= NORMAL \<and> \<not>(successiveStops()\<ge>stopLimit() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> transmissionFault() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> limitRisk() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> levelFailed() \<and> estimateUnreliable() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> levelFailed() \<and> \<not>estimateUnreliable() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> otherFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> \<not>otherFailed() \<and> headingBelowN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> \<not>otherFailed() \<and> \<not>headingBelowN1() \<and> headingAboveN2() \<and> transmissionReceived \<in> offered) \<and> \<not>(transmissionReceived \<in> offered)"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_DEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}"
  pre "st= DEGRADED \<and> \<not>(successiveStops()\<ge>stopLimit() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> transmissionFault() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> limitRisk() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> levelFailed() \<and> estimateUnreliable() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> levelFailed() \<and> \<not>estimateUnreliable() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> \<not>otherFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> otherFailed() \<and> headingBelowN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> otherFailed() \<and> \<not>headingBelowN1() \<and> headingAboveN2() \<and> transmissionReceived \<in> offered) \<and> \<not>(transmissionReceived \<in> offered)"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_RESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}"
  pre "st= RESCUE \<and> \<not>(successiveStops()\<ge>stopLimit() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> transmissionFault() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> estimateUnreliable() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>estimateUnreliable() \<and> limitRisk() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>estimateUnreliable() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> \<not>otherFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>estimateUnreliable() \<and> \<not>limitRisk() \<and> \<not>levelFailed() \<and> otherFailed() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>estimateUnreliable() \<and> \<not>limitRisk() \<and> levelFailed() \<and> headingBelowN1() \<and> transmissionReceived \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopLimit() \<and> \<not>transmissionFault() \<and> \<not>estimateUnreliable() \<and> \<not>limitRisk() \<and> levelFailed() \<and> \<not>headingBelowN1() \<and> headingAboveN2() \<and> transmissionReceived \<in> offered) \<and> \<not>(transmissionReceived \<in> offered)"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_EMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionReceived}}"
  pre "st= EMERGENCY_STOP \<and> \<not>(transmissionReceived \<in> offered)"
  update "[st\<Zprime>= EMERGENCY_STOP
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
  operations  InitialToINITIALIZATION INITIALIZATIONToEMERGENCY_STOP INITIALIZATIONToEMERGENCY_STOP_1 INITIALIZATIONToEMERGENCY_STOP_2 INITIALIZATIONToEMERGENCY_STOP_3 INITIALIZATIONToINITIALIZATION INITIALIZATIONToINITIALIZATION_1 INITIALIZATIONToINITIALIZATION_2 INITIALIZATIONToNORMAL INITIALIZATIONToDEGRADED INITIALIZATIONToINITIALIZATION_3 INITIALIZATIONToINITIALIZATION_4 NORMALToEMERGENCY_STOP NORMALToEMERGENCY_STOP_1 NORMALToEMERGENCY_STOP_2 NORMALToEMERGENCY_STOP_3 NORMALToRESCUE NORMALToDEGRADED NORMALToNORMAL NORMALToNORMAL_1 NORMALToNORMAL_2 DEGRADEDToEMERGENCY_STOP DEGRADEDToEMERGENCY_STOP_1 DEGRADEDToEMERGENCY_STOP_2 DEGRADEDToEMERGENCY_STOP_3 DEGRADEDToRESCUE DEGRADEDToNORMAL DEGRADEDToDEGRADED DEGRADEDToDEGRADED_1 DEGRADEDToDEGRADED_2 RESCUEToEMERGENCY_STOP RESCUEToEMERGENCY_STOP_1 RESCUEToEMERGENCY_STOP_2 RESCUEToEMERGENCY_STOP_3 RESCUEToNORMAL RESCUEToDEGRADED RESCUEToRESCUE RESCUEToRESCUE_1 RESCUEToRESCUE_2 EMERGENCY_STOPToEMERGENCY_STOP Idle_INITIALIZATION Idle_NORMAL Idle_DEGRADED Idle_RESCUE Idle_EMERGENCY_STOP


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SteamBoilerController_inv"
  by zpog_full

lemma InitialToINITIALIZATION_inv [hoare_lemmas]: "InitialToINITIALIZATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToEMERGENCY_STOP_inv [hoare_lemmas]: "INITIALIZATIONToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToEMERGENCY_STOP_1_inv [hoare_lemmas]: "INITIALIZATIONToEMERGENCY_STOP_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToEMERGENCY_STOP_2_inv [hoare_lemmas]: "INITIALIZATIONToEMERGENCY_STOP_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToEMERGENCY_STOP_3_inv [hoare_lemmas]: "INITIALIZATIONToEMERGENCY_STOP_3 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_1_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_2_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToNORMAL_inv [hoare_lemmas]: "INITIALIZATIONToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToDEGRADED_inv [hoare_lemmas]: "INITIALIZATIONToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_3_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_3 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_4_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_4 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToEMERGENCY_STOP_inv [hoare_lemmas]: "NORMALToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToEMERGENCY_STOP_1_inv [hoare_lemmas]: "NORMALToEMERGENCY_STOP_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToEMERGENCY_STOP_2_inv [hoare_lemmas]: "NORMALToEMERGENCY_STOP_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToEMERGENCY_STOP_3_inv [hoare_lemmas]: "NORMALToEMERGENCY_STOP_3 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToRESCUE_inv [hoare_lemmas]: "NORMALToRESCUE (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToDEGRADED_inv [hoare_lemmas]: "NORMALToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToNORMAL_inv [hoare_lemmas]: "NORMALToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToNORMAL_1_inv [hoare_lemmas]: "NORMALToNORMAL_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToNORMAL_2_inv [hoare_lemmas]: "NORMALToNORMAL_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToEMERGENCY_STOP_inv [hoare_lemmas]: "DEGRADEDToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToEMERGENCY_STOP_1_inv [hoare_lemmas]: "DEGRADEDToEMERGENCY_STOP_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToEMERGENCY_STOP_2_inv [hoare_lemmas]: "DEGRADEDToEMERGENCY_STOP_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToEMERGENCY_STOP_3_inv [hoare_lemmas]: "DEGRADEDToEMERGENCY_STOP_3 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToRESCUE_inv [hoare_lemmas]: "DEGRADEDToRESCUE (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToNORMAL_inv [hoare_lemmas]: "DEGRADEDToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToDEGRADED_inv [hoare_lemmas]: "DEGRADEDToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToDEGRADED_1_inv [hoare_lemmas]: "DEGRADEDToDEGRADED_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma DEGRADEDToDEGRADED_2_inv [hoare_lemmas]: "DEGRADEDToDEGRADED_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToEMERGENCY_STOP_inv [hoare_lemmas]: "RESCUEToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToEMERGENCY_STOP_1_inv [hoare_lemmas]: "RESCUEToEMERGENCY_STOP_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToEMERGENCY_STOP_2_inv [hoare_lemmas]: "RESCUEToEMERGENCY_STOP_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToEMERGENCY_STOP_3_inv [hoare_lemmas]: "RESCUEToEMERGENCY_STOP_3 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToNORMAL_inv [hoare_lemmas]: "RESCUEToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToDEGRADED_inv [hoare_lemmas]: "RESCUEToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToRESCUE_inv [hoare_lemmas]: "RESCUEToRESCUE (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToRESCUE_1_inv [hoare_lemmas]: "RESCUEToRESCUE_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma RESCUEToRESCUE_2_inv [hoare_lemmas]: "RESCUEToRESCUE_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma EMERGENCY_STOPToEMERGENCY_STOP_inv [hoare_lemmas]: "EMERGENCY_STOPToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_INITIALIZATION_inv [hoare_lemmas]: "Idle_INITIALIZATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_NORMAL_inv [hoare_lemmas]: "Idle_NORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_DEGRADED_inv [hoare_lemmas]: "Idle_DEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_RESCUE_inv [hoare_lemmas]: "Idle_RESCUE (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_EMERGENCY_STOP_inv [hoare_lemmas]: "Idle_EMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToINITIALIZATION (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToEMERGENCY_STOP_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToEMERGENCY_STOP_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToEMERGENCY_STOP_3 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_3 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_4 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToEMERGENCY_STOP_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToEMERGENCY_STOP_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToEMERGENCY_STOP_3 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToRESCUE (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToNORMAL_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToNORMAL_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToEMERGENCY_STOP_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToEMERGENCY_STOP_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToEMERGENCY_STOP_3 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToRESCUE (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToDEGRADED_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "DEGRADEDToDEGRADED_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToEMERGENCY_STOP_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToEMERGENCY_STOP_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToEMERGENCY_STOP_3 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToRESCUE (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToRESCUE_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "RESCUEToRESCUE_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "EMERGENCY_STOPToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_INITIALIZATION (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_NORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_DEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_RESCUE (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_EMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "SteamBoilerController_axioms = True"

lemma SteamBoilerController_deadlock_free: "SteamBoilerController_axioms  \<Longrightarrow> deadlock_free SteamBoilerControllerMachine"
  unfolding SteamBoilerControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
