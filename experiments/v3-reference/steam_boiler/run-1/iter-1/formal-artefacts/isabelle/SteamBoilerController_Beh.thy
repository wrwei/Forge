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
   
enumtype St = INITIALISATION | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP | initial 
 


enumtype Evt = transmission | mode | valve | openPump | closePump | programReady 
 


enumtype PumpPosition = PUMP_OFF | PUMP_ON

enumtype WaterFlow = NO_FLOW | FLOW

enumtype OperatingMode = INITIALISATION_MODE | NORMAL_MODE | DEGRADED_MODE | RESCUE_MODE | EMERGENCY_STOP_MODE

instantiation real :: default
begin
  definition default_real :: "real" where "default_real = 0"
  instance ..
end

instantiation real :: "show"
begin
  instance ..
end
record PumpControlReading = controllerIndex :: int observation :: WaterFlow
record_default PumpControlReading
show_record PumpControlReading

record PumpReading = pumpIndex :: int position :: PumpPosition
record_default PumpReading
show_record PumpReading


text \<open> function definition \<close>

consts stopCount :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stopLimit :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts transmissionFailure :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts unitsReadyMessage :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts waitingMessage :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts steamReading :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelFailure :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelReading :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts normalN2 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts normalN1 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts steamFailure :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts pumpFailure :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts pumpControlFailure :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts predictedMinimum :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts limitM1 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts predictedMaximumWithoutPumps :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts limitM2 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts predictedMaximum :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts estimatedLevel :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore SteamBoilerController =
  plantWaiting :: "bool"
  valveOpen :: "bool"
  programReadySent :: "bool"
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
                      \<and> (st = INITIALISATION \<longrightarrow> listens = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission})
                      \<and> (st = NORMAL \<longrightarrow> listens = {transmission, transmission, transmission, transmission, transmission, transmission})
                      \<and> (st = DEGRADED \<longrightarrow> listens = {transmission, transmission, transmission, transmission, transmission, transmission})
                      \<and> (st = RESCUE \<longrightarrow> listens = {transmission, transmission, transmission, transmission, transmission, transmission})
                                                                                                                                                              \<and> (st = INITIALISATION \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                                                                              \<and> (st = NORMAL \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                                                                              \<and> (st = DEGRADED \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                                                                              \<and> (st = RESCUE \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                                                                              \<and> (st = EMERGENCY_STOP \<longrightarrow> (offered = {}))
                                                                                                                                                              \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToINITIALISATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= initial"
  update "[st\<Zprime>= INITIALISATION
         ,tr\<Zprime> =tr  @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToEMERGENCY_STOP =
  over SteamBoilerController
  pre "st= INITIALISATION \<and> (((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure())) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation INITIALISATIONToINITIALISATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> \<not>((plantWaiting \<or> waitingMessage())) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToINITIALISATION_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> \<not>programReadySent \<and> levelReading()>normalN2() \<and> \<not>valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,plantWaiting\<Zprime> = True
         ,valveOpen\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event valve]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToINITIALISATION_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> \<not>programReadySent \<and> levelReading()>normalN2() \<and> valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,plantWaiting\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToINITIALISATION_3 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> \<not>programReadySent \<and> levelReading()<normalN1() \<and> valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,plantWaiting\<Zprime> = True
         ,valveOpen\<Zprime> = False
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event valve]@ [Event openPump]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToINITIALISATION_4 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> \<not>programReadySent \<and> levelReading()<normalN1() \<and> \<not>valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,plantWaiting\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event openPump]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToINITIALISATION_5 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> \<not>programReadySent \<and> \<not>levelReading()<normalN1() \<and> \<not>levelReading()>normalN2() \<and> valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,plantWaiting\<Zprime> = True
         ,valveOpen\<Zprime> = False
         ,programReadySent\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event valve]@ [Event closePump]@ [Event programReady]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToINITIALISATION_6 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> \<not>programReadySent \<and> \<not>levelReading()<normalN1() \<and> \<not>levelReading()>normalN2() \<and> \<not>valveOpen \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,plantWaiting\<Zprime> = True
         ,programReadySent\<Zprime> = True
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event closePump]@ [Event programReady]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToINITIALISATION_7 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> programReadySent \<and> \<not>unitsReadyMessage() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALISATION
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event programReady]@ [Event mode] @ [State INITIALISATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> programReadySent \<and> unitsReadyMessage() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALISATIONToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALISATION \<and> \<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> programReadySent \<and> unitsReadyMessage() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToEMERGENCY_STOP =
  over SteamBoilerController
  pre "st= NORMAL \<and> ((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation NORMALToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> levelFailure() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()<normalN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> \<not>levelReading()<normalN1() \<and> \<not>levelReading()>normalN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP =
  over SteamBoilerController
  pre "st= DEGRADED \<and> ((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation DEGRADEDToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> levelFailure() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()<normalN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> \<not>levelReading()<normalN1() \<and> \<not>levelReading()>normalN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToEMERGENCY_STOP =
  over SteamBoilerController
  pre "st= RESCUE \<and> ((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> (steamFailure() \<or> pumpControlFailure())) \<or> levelFailure() \<and> estimatedLevel()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(levelFailure() \<and> estimatedLevel()>normalN2()) \<and> predictedMaximum()\<ge>limitM2()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation RESCUEToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> (steamFailure() \<or> pumpControlFailure())) \<or> levelFailure() \<and> estimatedLevel()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(levelFailure() \<and> estimatedLevel()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> (steamFailure() \<or> pumpControlFailure())) \<or> levelFailure() \<and> estimatedLevel()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(levelFailure() \<and> estimatedLevel()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> (steamFailure() \<or> pumpControlFailure())) \<or> levelFailure() \<and> estimatedLevel()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(levelFailure() \<and> estimatedLevel()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> levelFailure() \<and> estimatedLevel()<normalN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> (steamFailure() \<or> pumpControlFailure())) \<or> levelFailure() \<and> estimatedLevel()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(levelFailure() \<and> estimatedLevel()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> levelFailure() \<and> estimatedLevel()>normalN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> (steamFailure() \<or> pumpControlFailure())) \<or> levelFailure() \<and> estimatedLevel()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(levelFailure() \<and> estimatedLevel()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> levelFailure() \<and> \<not>estimatedLevel()<normalN1() \<and> \<not>estimatedLevel()>normalN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_INITIALISATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= INITIALISATION \<and> \<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure())) \<and> transmission \<in> offered) \<and> \<not>(\<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> \<not>((plantWaiting \<or> waitingMessage())) \<and> transmission \<in> offered) \<and> \<not>(\<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> \<not>programReadySent \<and> levelReading()>normalN2() \<and> \<not>valveOpen \<and> transmission \<in> offered) \<and> \<not>(\<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> \<not>programReadySent \<and> levelReading()>normalN2() \<and> valveOpen \<and> transmission \<in> offered) \<and> \<not>(\<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> \<not>programReadySent \<and> levelReading()<normalN1() \<and> valveOpen \<and> transmission \<in> offered) \<and> \<not>(\<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> \<not>programReadySent \<and> levelReading()<normalN1() \<and> \<not>valveOpen \<and> transmission \<in> offered) \<and> \<not>(\<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> \<not>programReadySent \<and> \<not>levelReading()<normalN1() \<and> \<not>levelReading()>normalN2() \<and> valveOpen \<and> transmission \<in> offered) \<and> \<not>(\<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> \<not>programReadySent \<and> \<not>levelReading()<normalN1() \<and> \<not>levelReading()>normalN2() \<and> \<not>valveOpen \<and> transmission \<in> offered) \<and> \<not>(\<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> programReadySent \<and> \<not>unitsReadyMessage() \<and> transmission \<in> offered) \<and> \<not>(\<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> programReadySent \<and> unitsReadyMessage() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> transmission \<in> offered) \<and> \<not>(\<not>((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> unitsReadyMessage() \<and> \<not>programReadySent) \<or> (plantWaiting \<or> waitingMessage()) \<and> ((steamReading() \<noteq> 0.0) \<or> levelFailure()))) \<and> (plantWaiting \<or> waitingMessage()) \<and> programReadySent \<and> unitsReadyMessage() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> transmission \<in> offered)"
  update "[st\<Zprime>= INITIALISATION
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_NORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= NORMAL \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2()) \<and> transmission \<in> offered) \<and> \<not>(\<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> levelFailure() \<and> transmission \<in> offered) \<and> \<not>(\<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> transmission \<in> offered) \<and> \<not>(\<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()<normalN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> \<not>levelReading()<normalN1() \<and> \<not>levelReading()>normalN2() \<and> transmission \<in> offered)"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_DEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= DEGRADED \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2()) \<and> transmission \<in> offered) \<and> \<not>(\<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> levelFailure() \<and> transmission \<in> offered) \<and> \<not>(\<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> transmission \<in> offered) \<and> \<not>(\<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()<normalN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> levelFailure() \<and> (steamFailure() \<or> pumpControlFailure())) \<or> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(\<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> levelReading()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> \<not>levelReading()<normalN1() \<and> \<not>levelReading()>normalN2() \<and> transmission \<in> offered)"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_RESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= RESCUE \<and> \<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> (steamFailure() \<or> pumpControlFailure())) \<or> levelFailure() \<and> estimatedLevel()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(levelFailure() \<and> estimatedLevel()>normalN2()) \<and> predictedMaximum()\<ge>limitM2()) \<and> transmission \<in> offered) \<and> \<not>(\<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> (steamFailure() \<or> pumpControlFailure())) \<or> levelFailure() \<and> estimatedLevel()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(levelFailure() \<and> estimatedLevel()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> \<not>(((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure())) \<and> transmission \<in> offered) \<and> \<not>(\<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> (steamFailure() \<or> pumpControlFailure())) \<or> levelFailure() \<and> estimatedLevel()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(levelFailure() \<and> estimatedLevel()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> \<not>levelFailure() \<and> ((steamFailure() \<or> pumpFailure()) \<or> pumpControlFailure()) \<and> transmission \<in> offered) \<and> \<not>(\<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> (steamFailure() \<or> pumpControlFailure())) \<or> levelFailure() \<and> estimatedLevel()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(levelFailure() \<and> estimatedLevel()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> levelFailure() \<and> estimatedLevel()<normalN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> (steamFailure() \<or> pumpControlFailure())) \<or> levelFailure() \<and> estimatedLevel()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(levelFailure() \<and> estimatedLevel()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> levelFailure() \<and> estimatedLevel()>normalN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>(((((((stopCount()\<ge>stopLimit() \<or> transmissionFailure()) \<or> (waitingMessage() \<or> unitsReadyMessage())) \<or> predictedMinimum()\<le>limitM1()) \<or> (steamFailure() \<or> pumpControlFailure())) \<or> levelFailure() \<and> estimatedLevel()>normalN2() \<and> predictedMaximumWithoutPumps()\<ge>limitM2()) \<or> \<not>(levelFailure() \<and> estimatedLevel()>normalN2()) \<and> predictedMaximum()\<ge>limitM2())) \<and> levelFailure() \<and> \<not>estimatedLevel()<normalN1() \<and> \<not>estimatedLevel()>normalN2() \<and> transmission \<in> offered)"
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
  operations  InitialToINITIALISATION INITIALISATIONToEMERGENCY_STOP INITIALISATIONToINITIALISATION INITIALISATIONToINITIALISATION_1 INITIALISATIONToINITIALISATION_2 INITIALISATIONToINITIALISATION_3 INITIALISATIONToINITIALISATION_4 INITIALISATIONToINITIALISATION_5 INITIALISATIONToINITIALISATION_6 INITIALISATIONToINITIALISATION_7 INITIALISATIONToNORMAL INITIALISATIONToDEGRADED NORMALToEMERGENCY_STOP NORMALToRESCUE NORMALToDEGRADED NORMALToNORMAL NORMALToNORMAL_1 NORMALToNORMAL_2 DEGRADEDToEMERGENCY_STOP DEGRADEDToRESCUE DEGRADEDToNORMAL DEGRADEDToDEGRADED DEGRADEDToDEGRADED_1 DEGRADEDToDEGRADED_2 RESCUEToEMERGENCY_STOP RESCUEToNORMAL RESCUEToDEGRADED RESCUEToRESCUE RESCUEToRESCUE_1 RESCUEToRESCUE_2 Idle_INITIALISATION Idle_NORMAL Idle_DEGRADED Idle_RESCUE
  until "st = EMERGENCY_STOP"
(* `until` marks states exempt from deadlock-freedom (deadlock_free_z_machine's
   End parameter). These are states that are the source of no transition, so the
   machine has terminated there and is not required to offer an event. Without this
   clause End defaults to False and the obligation is FALSE at such a state --
   machine-checked, see scripts/test_f17_probe.sh. *)

subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SteamBoilerController_inv"
  by zpog_full

lemma InitialToINITIALISATION_inv [hoare_lemmas]: "InitialToINITIALISATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToEMERGENCY_STOP_inv [hoare_lemmas]: "INITIALISATIONToEMERGENCY_STOP() preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma INITIALISATIONToINITIALISATION_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToINITIALISATION_1_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToINITIALISATION_2_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToINITIALISATION_3_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION_3 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToINITIALISATION_4_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION_4 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToINITIALISATION_5_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION_5 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToINITIALISATION_6_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION_6 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToINITIALISATION_7_inv [hoare_lemmas]: "INITIALISATIONToINITIALISATION_7 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToNORMAL_inv [hoare_lemmas]: "INITIALISATIONToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALISATIONToDEGRADED_inv [hoare_lemmas]: "INITIALISATIONToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToEMERGENCY_STOP_inv [hoare_lemmas]: "NORMALToEMERGENCY_STOP() preserves SteamBoilerController_inv"
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

lemma Idle_INITIALISATION_inv [hoare_lemmas]: "Idle_INITIALISATION (nxt) preserves SteamBoilerController_inv"
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

lemma "InitialToINITIALISATION (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToEMERGENCY_STOP() preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION_3 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION_4 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION_5 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION_6 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToINITIALISATION_7 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALISATIONToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToEMERGENCY_STOP() preserves R1 under SteamBoilerController_inv"
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
  
lemma "Idle_INITIALISATION (nxt) preserves R1 under SteamBoilerController_inv"
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
