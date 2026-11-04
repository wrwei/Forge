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
 


enumtype Evt = stopConfirmed | modeMessage | transmissionFault | steamBoilerWaiting | physicalUnitsReady | transmission | programReady | valve | closePump | openPump | levelRisk 
 


enumtype ModeValue = INITIALIZATION_MODE | NORMAL_MODE | DEGRADED_MODE | RESCUE_MODE | EMERGENCY_STOP_MODE

enumtype WaterFlow = FLOWING | NOT_FLOWING

instantiation real :: default
begin
  definition default_real :: "real" where "default_real = 0"
  instance ..
end

instantiation real :: "show"
begin
  instance ..
end

text \<open> function definition \<close>

consts levelDeviceFailed :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts steamDeviceFailed :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts otherUnitFailed :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelAboveN2 :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelBelowN1 :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelMayFallBelowN1 :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelMayRiseAboveN2 :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts pumpControllerFailed :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore SteamBoilerController =
  steamBoilerWaitingSeen :: "bool"
  readyAnnounced :: "bool"
  valveOpen :: "bool"
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
                      \<and> (st = INITIALIZATION \<longrightarrow> listens = {physicalUnitsReady, stopConfirmed, transmission, transmission, steamBoilerWaiting, physicalUnitsReady, transmissionFault, transmission, transmission, transmission, transmission, transmission, transmission, physicalUnitsReady, physicalUnitsReady, transmission})
                      \<and> (st = NORMAL \<longrightarrow> listens = {transmissionFault, transmission, levelRisk, transmission, stopConfirmed, transmission, transmission, transmission})
                      \<and> (st = DEGRADED \<longrightarrow> listens = {levelRisk, transmissionFault, transmission, transmission, stopConfirmed, transmission, transmission, transmission})
                      \<and> (st = RESCUE \<longrightarrow> listens = {transmission, transmission, stopConfirmed, levelRisk, transmission, transmission, transmissionFault, transmission, transmission, transmission})
                                                                                                                                                                                                                         \<and> (st = INITIALIZATION \<longrightarrow> (offered = {} \<or> offered = {physicalUnitsReady} \<or> offered = {stopConfirmed} \<or> offered = {transmission} \<or> offered = {steamBoilerWaiting} \<or> offered = {transmissionFault}))
                                                                                                                                                                                                                         \<and> (st = NORMAL \<longrightarrow> (offered = {} \<or> offered = {transmissionFault} \<or> offered = {transmission} \<or> offered = {levelRisk} \<or> offered = {stopConfirmed}))
                                                                                                                                                                                                                         \<and> (st = DEGRADED \<longrightarrow> (offered = {} \<or> offered = {levelRisk} \<or> offered = {transmissionFault} \<or> offered = {transmission} \<or> offered = {stopConfirmed}))
                                                                                                                                                                                                                         \<and> (st = RESCUE \<longrightarrow> (offered = {} \<or> offered = {transmission} \<or> offered = {stopConfirmed} \<or> offered = {levelRisk} \<or> offered = {transmissionFault}))
                                                                                                                                                                                                                         \<and> (st = EMERGENCY_STOP \<longrightarrow> (offered = {}))
                                                                                                                                                                                                                         \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToINITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {physicalUnitsReady}, {stopConfirmed}, {transmission}, {steamBoilerWaiting}, {transmissionFault}}" 
  pre "st= initial"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr  @ [State INITIALIZATION]
         ,listens\<Zprime> = {physicalUnitsReady, stopConfirmed, transmission, transmission, steamBoilerWaiting, physicalUnitsReady, transmissionFault, transmission, transmission, transmission, transmission, transmission, transmission, physicalUnitsReady, physicalUnitsReady, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP =
  over SteamBoilerController
  pre "st= INITIALIZATION \<and> stopConfirmed \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event stopConfirmed]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_1 =
  over SteamBoilerController
  pre "st= INITIALIZATION \<and> transmissionFault \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionFault]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {physicalUnitsReady}, {stopConfirmed}, {transmission}, {steamBoilerWaiting}, {transmissionFault}}" 
  pre "st= INITIALIZATION \<and> \<not>steamBoilerWaitingSeen \<and> steamBoilerWaiting \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,steamBoilerWaitingSeen\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event steamBoilerWaiting]@ [Event modeMessage] @ [State INITIALIZATION]
         ,listens\<Zprime> = {physicalUnitsReady, stopConfirmed, transmission, transmission, steamBoilerWaiting, physicalUnitsReady, transmissionFault, transmission, transmission, transmission, transmission, transmission, transmission, physicalUnitsReady, physicalUnitsReady, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_2 =
  over SteamBoilerController
  pre "st= INITIALIZATION \<and> readyAnnounced \<and> levelDeviceFailed() \<and> physicalUnitsReady \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event physicalUnitsReady]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_3 =
  over SteamBoilerController
  pre "st= INITIALIZATION \<and> readyAnnounced \<and> \<not>levelDeviceFailed() \<and> steamDeviceFailed() \<and> physicalUnitsReady \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event physicalUnitsReady]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionFault}, {transmission}, {levelRisk}, {stopConfirmed}}" 
  pre "st= INITIALIZATION \<and> readyAnnounced \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> \<not>otherUnitFailed() \<and> physicalUnitsReady \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event physicalUnitsReady]@ [Event modeMessage] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionFault, transmission, levelRisk, transmission, stopConfirmed, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {levelRisk}, {transmissionFault}, {transmission}, {stopConfirmed}}" 
  pre "st= INITIALIZATION \<and> readyAnnounced \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> otherUnitFailed() \<and> physicalUnitsReady \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event physicalUnitsReady]@ [Event modeMessage] @ [State DEGRADED]
         ,listens\<Zprime> = {levelRisk, transmissionFault, transmission, transmission, stopConfirmed, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {physicalUnitsReady}, {stopConfirmed}, {transmission}, {steamBoilerWaiting}, {transmissionFault}}" 
  pre "st= INITIALIZATION \<and> \<not>steamBoilerWaitingSeen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage] @ [State INITIALIZATION]
         ,listens\<Zprime> = {physicalUnitsReady, stopConfirmed, transmission, transmission, steamBoilerWaiting, physicalUnitsReady, transmissionFault, transmission, transmission, transmission, transmission, transmission, transmission, physicalUnitsReady, physicalUnitsReady, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_4 =
  over SteamBoilerController
  pre "st= INITIALIZATION \<and> steamBoilerWaitingSeen \<and> levelDeviceFailed() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_5 =
  over SteamBoilerController
  pre "st= INITIALIZATION \<and> steamBoilerWaitingSeen \<and> \<not>levelDeviceFailed() \<and> steamDeviceFailed() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {physicalUnitsReady}, {stopConfirmed}, {transmission}, {steamBoilerWaiting}, {transmissionFault}}" 
  pre "st= INITIALIZATION \<and> steamBoilerWaitingSeen \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> readyAnnounced \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage]@ [Event programReady] @ [State INITIALIZATION]
         ,listens\<Zprime> = {physicalUnitsReady, stopConfirmed, transmission, transmission, steamBoilerWaiting, physicalUnitsReady, transmissionFault, transmission, transmission, transmission, transmission, transmission, transmission, physicalUnitsReady, physicalUnitsReady, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_3 =
  over SteamBoilerController
  params nxt \<in> "{{}, {physicalUnitsReady}, {stopConfirmed}, {transmission}, {steamBoilerWaiting}, {transmissionFault}}" 
  pre "st= INITIALIZATION \<and> steamBoilerWaitingSeen \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> \<not>readyAnnounced \<and> levelAboveN2() \<and> \<not>valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,valveOpen\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage]@ [Event valve]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State INITIALIZATION]
         ,listens\<Zprime> = {physicalUnitsReady, stopConfirmed, transmission, transmission, steamBoilerWaiting, physicalUnitsReady, transmissionFault, transmission, transmission, transmission, transmission, transmission, transmission, physicalUnitsReady, physicalUnitsReady, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_4 =
  over SteamBoilerController
  params nxt \<in> "{{}, {physicalUnitsReady}, {stopConfirmed}, {transmission}, {steamBoilerWaiting}, {transmissionFault}}" 
  pre "st= INITIALIZATION \<and> steamBoilerWaitingSeen \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> \<not>readyAnnounced \<and> levelAboveN2() \<and> valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage] @ [State INITIALIZATION]
         ,listens\<Zprime> = {physicalUnitsReady, stopConfirmed, transmission, transmission, steamBoilerWaiting, physicalUnitsReady, transmissionFault, transmission, transmission, transmission, transmission, transmission, transmission, physicalUnitsReady, physicalUnitsReady, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_5 =
  over SteamBoilerController
  params nxt \<in> "{{}, {physicalUnitsReady}, {stopConfirmed}, {transmission}, {steamBoilerWaiting}, {transmissionFault}}" 
  pre "st= INITIALIZATION \<and> steamBoilerWaitingSeen \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> \<not>readyAnnounced \<and> \<not>levelAboveN2() \<and> valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,valveOpen\<Zprime> = False
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage]@ [Event valve] @ [State INITIALIZATION]
         ,listens\<Zprime> = {physicalUnitsReady, stopConfirmed, transmission, transmission, steamBoilerWaiting, physicalUnitsReady, transmissionFault, transmission, transmission, transmission, transmission, transmission, transmission, physicalUnitsReady, physicalUnitsReady, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_6 =
  over SteamBoilerController
  params nxt \<in> "{{}, {physicalUnitsReady}, {stopConfirmed}, {transmission}, {steamBoilerWaiting}, {transmissionFault}}" 
  pre "st= INITIALIZATION \<and> steamBoilerWaitingSeen \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> \<not>readyAnnounced \<and> \<not>levelAboveN2() \<and> levelBelowN1() \<and> \<not>valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State INITIALIZATION]
         ,listens\<Zprime> = {physicalUnitsReady, stopConfirmed, transmission, transmission, steamBoilerWaiting, physicalUnitsReady, transmissionFault, transmission, transmission, transmission, transmission, transmission, transmission, physicalUnitsReady, physicalUnitsReady, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_7 =
  over SteamBoilerController
  params nxt \<in> "{{}, {physicalUnitsReady}, {stopConfirmed}, {transmission}, {steamBoilerWaiting}, {transmissionFault}}" 
  pre "st= INITIALIZATION \<and> steamBoilerWaitingSeen \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> \<not>readyAnnounced \<and> \<not>levelAboveN2() \<and> \<not>levelBelowN1() \<and> \<not>valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,readyAnnounced\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event programReady] @ [State INITIALIZATION]
         ,listens\<Zprime> = {physicalUnitsReady, stopConfirmed, transmission, transmission, steamBoilerWaiting, physicalUnitsReady, transmissionFault, transmission, transmission, transmission, transmission, transmission, transmission, physicalUnitsReady, physicalUnitsReady, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToEMERGENCY_STOP =
  over SteamBoilerController
  pre "st= NORMAL \<and> stopConfirmed \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event stopConfirmed]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation NORMALToEMERGENCY_STOP_1 =
  over SteamBoilerController
  pre "st= NORMAL \<and> transmissionFault \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionFault]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation NORMALToEMERGENCY_STOP_2 =
  over SteamBoilerController
  pre "st= NORMAL \<and> levelRisk \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event levelRisk]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation NORMALToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}, {stopConfirmed}, {levelRisk}, {transmissionFault}}" 
  pre "st= NORMAL \<and> levelDeviceFailed() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, stopConfirmed, levelRisk, transmission, transmission, transmissionFault, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {levelRisk}, {transmissionFault}, {transmission}, {stopConfirmed}}" 
  pre "st= NORMAL \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage] @ [State DEGRADED]
         ,listens\<Zprime> = {levelRisk, transmissionFault, transmission, transmission, stopConfirmed, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionFault}, {transmission}, {levelRisk}, {stopConfirmed}}" 
  pre "st= NORMAL \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> levelMayFallBelowN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionFault, transmission, levelRisk, transmission, stopConfirmed, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionFault}, {transmission}, {levelRisk}, {stopConfirmed}}" 
  pre "st= NORMAL \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>levelMayFallBelowN1() \<and> levelMayRiseAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionFault, transmission, levelRisk, transmission, stopConfirmed, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionFault}, {transmission}, {levelRisk}, {stopConfirmed}}" 
  pre "st= NORMAL \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>levelMayFallBelowN1() \<and> \<not>levelMayRiseAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionFault, transmission, levelRisk, transmission, stopConfirmed, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP =
  over SteamBoilerController
  pre "st= DEGRADED \<and> stopConfirmed \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event stopConfirmed]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP_1 =
  over SteamBoilerController
  pre "st= DEGRADED \<and> transmissionFault \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionFault]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP_2 =
  over SteamBoilerController
  pre "st= DEGRADED \<and> levelRisk \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event levelRisk]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation DEGRADEDToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}, {stopConfirmed}, {levelRisk}, {transmissionFault}}" 
  pre "st= DEGRADED \<and> levelDeviceFailed() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, stopConfirmed, levelRisk, transmission, transmission, transmissionFault, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionFault}, {transmission}, {levelRisk}, {stopConfirmed}}" 
  pre "st= DEGRADED \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionFault, transmission, levelRisk, transmission, stopConfirmed, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {levelRisk}, {transmissionFault}, {transmission}, {stopConfirmed}}" 
  pre "st= DEGRADED \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> levelMayFallBelowN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State DEGRADED]
         ,listens\<Zprime> = {levelRisk, transmissionFault, transmission, transmission, stopConfirmed, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {levelRisk}, {transmissionFault}, {transmission}, {stopConfirmed}}" 
  pre "st= DEGRADED \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>levelMayFallBelowN1() \<and> levelMayRiseAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State DEGRADED]
         ,listens\<Zprime> = {levelRisk, transmissionFault, transmission, transmission, stopConfirmed, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {levelRisk}, {transmissionFault}, {transmission}, {stopConfirmed}}" 
  pre "st= DEGRADED \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>levelMayFallBelowN1() \<and> \<not>levelMayRiseAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage] @ [State DEGRADED]
         ,listens\<Zprime> = {levelRisk, transmissionFault, transmission, transmission, stopConfirmed, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToEMERGENCY_STOP =
  over SteamBoilerController
  pre "st= RESCUE \<and> stopConfirmed \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event stopConfirmed]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_1 =
  over SteamBoilerController
  pre "st= RESCUE \<and> transmissionFault \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmissionFault]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_2 =
  over SteamBoilerController
  pre "st= RESCUE \<and> levelRisk \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event levelRisk]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_3 =
  over SteamBoilerController
  pre "st= RESCUE \<and> steamDeviceFailed() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_4 =
  over SteamBoilerController
  pre "st= RESCUE \<and> \<not>steamDeviceFailed() \<and> pumpControllerFailed() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation RESCUEToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionFault}, {transmission}, {levelRisk}, {stopConfirmed}}" 
  pre "st= RESCUE \<and> \<not>steamDeviceFailed() \<and> \<not>pumpControllerFailed() \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage] @ [State NORMAL]
         ,listens\<Zprime> = {transmissionFault, transmission, levelRisk, transmission, stopConfirmed, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {levelRisk}, {transmissionFault}, {transmission}, {stopConfirmed}}" 
  pre "st= RESCUE \<and> \<not>steamDeviceFailed() \<and> \<not>pumpControllerFailed() \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage] @ [State DEGRADED]
         ,listens\<Zprime> = {levelRisk, transmissionFault, transmission, transmission, stopConfirmed, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}, {stopConfirmed}, {levelRisk}, {transmissionFault}}" 
  pre "st= RESCUE \<and> \<not>steamDeviceFailed() \<and> \<not>pumpControllerFailed() \<and> levelDeviceFailed() \<and> levelMayFallBelowN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, stopConfirmed, levelRisk, transmission, transmission, transmissionFault, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}, {stopConfirmed}, {levelRisk}, {transmissionFault}}" 
  pre "st= RESCUE \<and> \<not>steamDeviceFailed() \<and> \<not>pumpControllerFailed() \<and> levelDeviceFailed() \<and> \<not>levelMayFallBelowN1() \<and> levelMayRiseAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, stopConfirmed, levelRisk, transmission, transmission, transmissionFault, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}, {stopConfirmed}, {levelRisk}, {transmissionFault}}" 
  pre "st= RESCUE \<and> \<not>steamDeviceFailed() \<and> \<not>pumpControllerFailed() \<and> levelDeviceFailed() \<and> \<not>levelMayFallBelowN1() \<and> \<not>levelMayRiseAboveN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event modeMessage] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, stopConfirmed, levelRisk, transmission, transmission, transmissionFault, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_INITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {physicalUnitsReady}, {stopConfirmed}, {transmission}, {steamBoilerWaiting}, {transmissionFault}}"
  pre "st= INITIALIZATION \<and> \<not>(stopConfirmed \<in> offered) \<and> \<not>(transmissionFault \<in> offered) \<and> \<not>(\<not>steamBoilerWaitingSeen \<and> steamBoilerWaiting \<in> offered) \<and> \<not>(readyAnnounced \<and> levelDeviceFailed() \<and> physicalUnitsReady \<in> offered) \<and> \<not>(readyAnnounced \<and> \<not>levelDeviceFailed() \<and> steamDeviceFailed() \<and> physicalUnitsReady \<in> offered) \<and> \<not>(readyAnnounced \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> \<not>otherUnitFailed() \<and> physicalUnitsReady \<in> offered) \<and> \<not>(readyAnnounced \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> otherUnitFailed() \<and> physicalUnitsReady \<in> offered) \<and> \<not>(\<not>steamBoilerWaitingSeen \<and> transmission \<in> offered) \<and> \<not>(steamBoilerWaitingSeen \<and> levelDeviceFailed() \<and> transmission \<in> offered) \<and> \<not>(steamBoilerWaitingSeen \<and> \<not>levelDeviceFailed() \<and> steamDeviceFailed() \<and> transmission \<in> offered) \<and> \<not>(steamBoilerWaitingSeen \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> readyAnnounced \<and> transmission \<in> offered) \<and> \<not>(steamBoilerWaitingSeen \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> \<not>readyAnnounced \<and> levelAboveN2() \<and> \<not>valveOpen \<and> transmission \<in> offered) \<and> \<not>(steamBoilerWaitingSeen \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> \<not>readyAnnounced \<and> levelAboveN2() \<and> valveOpen \<and> transmission \<in> offered) \<and> \<not>(steamBoilerWaitingSeen \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> \<not>readyAnnounced \<and> \<not>levelAboveN2() \<and> valveOpen \<and> transmission \<in> offered) \<and> \<not>(steamBoilerWaitingSeen \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> \<not>readyAnnounced \<and> \<not>levelAboveN2() \<and> levelBelowN1() \<and> \<not>valveOpen \<and> transmission \<in> offered) \<and> \<not>(steamBoilerWaitingSeen \<and> \<not>levelDeviceFailed() \<and> \<not>steamDeviceFailed() \<and> \<not>readyAnnounced \<and> \<not>levelAboveN2() \<and> \<not>levelBelowN1() \<and> \<not>valveOpen \<and> transmission \<in> offered)"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_NORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmissionFault}, {transmission}, {levelRisk}, {stopConfirmed}}"
  pre "st= NORMAL \<and> \<not>(stopConfirmed \<in> offered) \<and> \<not>(transmissionFault \<in> offered) \<and> \<not>(levelRisk \<in> offered) \<and> \<not>(levelDeviceFailed() \<and> transmission \<in> offered) \<and> \<not>(\<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> transmission \<in> offered) \<and> \<not>(\<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> levelMayFallBelowN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>levelMayFallBelowN1() \<and> levelMayRiseAboveN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> \<not>levelMayFallBelowN1() \<and> \<not>levelMayRiseAboveN2() \<and> transmission \<in> offered)"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_DEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {levelRisk}, {transmissionFault}, {transmission}, {stopConfirmed}}"
  pre "st= DEGRADED \<and> \<not>(stopConfirmed \<in> offered) \<and> \<not>(transmissionFault \<in> offered) \<and> \<not>(levelRisk \<in> offered) \<and> \<not>(levelDeviceFailed() \<and> transmission \<in> offered) \<and> \<not>(\<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> transmission \<in> offered) \<and> \<not>(\<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> levelMayFallBelowN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>levelMayFallBelowN1() \<and> levelMayRiseAboveN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> \<not>levelMayFallBelowN1() \<and> \<not>levelMayRiseAboveN2() \<and> transmission \<in> offered)"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_RESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}, {stopConfirmed}, {levelRisk}, {transmissionFault}}"
  pre "st= RESCUE \<and> \<not>(stopConfirmed \<in> offered) \<and> \<not>(transmissionFault \<in> offered) \<and> \<not>(levelRisk \<in> offered) \<and> \<not>(steamDeviceFailed() \<and> transmission \<in> offered) \<and> \<not>(\<not>steamDeviceFailed() \<and> pumpControllerFailed() \<and> transmission \<in> offered) \<and> \<not>(\<not>steamDeviceFailed() \<and> \<not>pumpControllerFailed() \<and> \<not>levelDeviceFailed() \<and> \<not>otherUnitFailed() \<and> transmission \<in> offered) \<and> \<not>(\<not>steamDeviceFailed() \<and> \<not>pumpControllerFailed() \<and> \<not>levelDeviceFailed() \<and> otherUnitFailed() \<and> transmission \<in> offered) \<and> \<not>(\<not>steamDeviceFailed() \<and> \<not>pumpControllerFailed() \<and> levelDeviceFailed() \<and> levelMayFallBelowN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>steamDeviceFailed() \<and> \<not>pumpControllerFailed() \<and> levelDeviceFailed() \<and> \<not>levelMayFallBelowN1() \<and> levelMayRiseAboveN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>steamDeviceFailed() \<and> \<not>pumpControllerFailed() \<and> levelDeviceFailed() \<and> \<not>levelMayFallBelowN1() \<and> \<not>levelMayRiseAboveN2() \<and> transmission \<in> offered)"
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
  operations  InitialToINITIALIZATION INITIALIZATIONToEMERGENCY_STOP INITIALIZATIONToEMERGENCY_STOP_1 INITIALIZATIONToINITIALIZATION INITIALIZATIONToEMERGENCY_STOP_2 INITIALIZATIONToEMERGENCY_STOP_3 INITIALIZATIONToNORMAL INITIALIZATIONToDEGRADED INITIALIZATIONToINITIALIZATION_1 INITIALIZATIONToEMERGENCY_STOP_4 INITIALIZATIONToEMERGENCY_STOP_5 INITIALIZATIONToINITIALIZATION_2 INITIALIZATIONToINITIALIZATION_3 INITIALIZATIONToINITIALIZATION_4 INITIALIZATIONToINITIALIZATION_5 INITIALIZATIONToINITIALIZATION_6 INITIALIZATIONToINITIALIZATION_7 NORMALToEMERGENCY_STOP NORMALToEMERGENCY_STOP_1 NORMALToEMERGENCY_STOP_2 NORMALToRESCUE NORMALToDEGRADED NORMALToNORMAL NORMALToNORMAL_1 NORMALToNORMAL_2 DEGRADEDToEMERGENCY_STOP DEGRADEDToEMERGENCY_STOP_1 DEGRADEDToEMERGENCY_STOP_2 DEGRADEDToRESCUE DEGRADEDToNORMAL DEGRADEDToDEGRADED DEGRADEDToDEGRADED_1 DEGRADEDToDEGRADED_2 RESCUEToEMERGENCY_STOP RESCUEToEMERGENCY_STOP_1 RESCUEToEMERGENCY_STOP_2 RESCUEToEMERGENCY_STOP_3 RESCUEToEMERGENCY_STOP_4 RESCUEToNORMAL RESCUEToDEGRADED RESCUEToRESCUE RESCUEToRESCUE_1 RESCUEToRESCUE_2 Idle_INITIALIZATION Idle_NORMAL Idle_DEGRADED Idle_RESCUE
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

lemma INITIALIZATIONToEMERGENCY_STOP_4_inv [hoare_lemmas]: "INITIALIZATIONToEMERGENCY_STOP_4() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma INITIALIZATIONToEMERGENCY_STOP_5_inv [hoare_lemmas]: "INITIALIZATIONToEMERGENCY_STOP_5() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

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

lemma NORMALToDEGRADED_inv [hoare_lemmas]: "NORMALToDEGRADED (nxt) preserves SteamBoilerController_inv"
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

lemma DEGRADEDToNORMAL_inv [hoare_lemmas]: "DEGRADEDToNORMAL (nxt) preserves SteamBoilerController_inv"
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

lemma RESCUEToDEGRADED_inv [hoare_lemmas]: "RESCUEToDEGRADED (nxt) preserves SteamBoilerController_inv"
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
  
lemma "INITIALIZATIONToEMERGENCY_STOP_4() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToEMERGENCY_STOP_5() preserves R1 under SteamBoilerController_inv"
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
  
lemma "NORMALToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
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
  
lemma "DEGRADEDToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
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
  
lemma "RESCUEToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
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
