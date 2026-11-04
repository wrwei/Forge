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
   
enumtype St = INITIALIZATION | INITIALIZATION_FILLING | INITIALIZATION_DRAINING | INITIALIZATION_READY | NORMAL | DEGRADED | RESCUE | EMERGENCY_STOP | initial 
 


enumtype Evt = transmission | mode | valve | openPump | programReady | closePump 
 


enumtype OperatingMode = INITIALIZATION_MODE | NORMAL_MODE | DEGRADED_MODE | RESCUE_MODE | EMERGENCY_STOP_MODE

instantiation real :: default
begin
  definition default_real :: "real" where "default_real = 0"
  instance ..
end

instantiation real :: "show"
begin
  instance ..
end
record PumpControllerReading = pump :: int flowing :: bool
record_default PumpControllerReading
show_record PumpControllerReading

record PumpReading = pump :: int open :: bool
record_default PumpReading
show_record PumpReading


text \<open> function definition \<close>

consts successiveStops :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stopRepetitions :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts transmissionFault :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts unitsReady :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts plantWaiting :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts steamRate :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelDeviceFaulty :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts waterLevel :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts normalN2 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts normalN1 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts steamDeviceFaulty :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts pumpFaulty :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts pumpControllerFaulty :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts lowestLevelNextCycle :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts limitM1 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts highestLevelNextCycle :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts limitM2 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelLowerBound :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts levelUpperBound :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore SteamBoilerController =
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
                      \<and> (st = INITIALIZATION \<longrightarrow> listens = {transmission, transmission, transmission, transmission, transmission, transmission, transmission})
                      \<and> (st = INITIALIZATION_FILLING \<longrightarrow> listens = {transmission, transmission, transmission, transmission, transmission, transmission})
                      \<and> (st = INITIALIZATION_DRAINING \<longrightarrow> listens = {transmission, transmission, transmission, transmission, transmission, transmission})
                      \<and> (st = INITIALIZATION_READY \<longrightarrow> listens = {transmission, transmission, transmission, transmission, transmission, transmission})
                      \<and> (st = NORMAL \<longrightarrow> listens = {transmission, transmission, transmission, transmission, transmission, transmission, transmission})
                      \<and> (st = DEGRADED \<longrightarrow> listens = {transmission, transmission, transmission, transmission, transmission, transmission, transmission})
                      \<and> (st = RESCUE \<longrightarrow> listens = {transmission, transmission, transmission, transmission, transmission, transmission, transmission})
                      \<and> (st = EMERGENCY_STOP \<longrightarrow> listens = {transmission})
                                                                                                \<and> (st = INITIALIZATION \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                \<and> (st = INITIALIZATION_FILLING \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                \<and> (st = INITIALIZATION_DRAINING \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                \<and> (st = INITIALIZATION_READY \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                \<and> (st = NORMAL \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                \<and> (st = DEGRADED \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                \<and> (st = RESCUE \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                \<and> (st = EMERGENCY_STOP \<longrightarrow> (offered = {} \<or> offered = {transmission}))
                                                                                                \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToINITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= initial"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr  @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION \<and> successiveStops()\<ge>stopRepetitions() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> (transmissionFault() \<or> unitsReady()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>plantWaiting() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State INITIALIZATION]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToEMERGENCY_STOP_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION \<and> plantWaiting() \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> ((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_DRAINING =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION \<and> plantWaiting() \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> waterLevel()>normalN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION_DRAINING
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode]@ [Event valve] @ [State INITIALIZATION_DRAINING]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_FILLING =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION \<and> plantWaiting() \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> \<not>waterLevel()>normalN2() \<and> waterLevel()<normalN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION_FILLING
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State INITIALIZATION_FILLING]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATIONToINITIALIZATION_READY =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION \<and> plantWaiting() \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> \<not>waterLevel()>normalN2() \<and> \<not>waterLevel()<normalN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION_READY
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode]@ [Event programReady] @ [State INITIALIZATION_READY]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_FILLINGToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_FILLING \<and> successiveStops()\<ge>stopRepetitions() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_FILLINGToEMERGENCY_STOP_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_FILLING \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> (transmissionFault() \<or> unitsReady()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_FILLINGToEMERGENCY_STOP_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_FILLING \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> ((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_FILLINGToINITIALIZATION_DRAINING =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_FILLING \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> waterLevel()>normalN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION_DRAINING
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event valve] @ [State INITIALIZATION_DRAINING]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_FILLINGToINITIALIZATION_FILLING =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_FILLING \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> \<not>waterLevel()>normalN2() \<and> waterLevel()<normalN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION_FILLING
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State INITIALIZATION_FILLING]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_FILLINGToINITIALIZATION_READY =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_FILLING \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> \<not>waterLevel()>normalN2() \<and> \<not>waterLevel()<normalN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION_READY
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event programReady] @ [State INITIALIZATION_READY]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_DRAININGToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_DRAINING \<and> successiveStops()\<ge>stopRepetitions() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_DRAININGToEMERGENCY_STOP_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_DRAINING \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> (transmissionFault() \<or> unitsReady()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_DRAININGToEMERGENCY_STOP_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_DRAINING \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> ((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_DRAININGToINITIALIZATION_DRAINING =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_DRAINING \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> waterLevel()>normalN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION_DRAINING
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State INITIALIZATION_DRAINING]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_DRAININGToINITIALIZATION_FILLING =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_DRAINING \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> \<not>waterLevel()>normalN2() \<and> waterLevel()<normalN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION_FILLING
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode]@ [Event valve]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State INITIALIZATION_FILLING]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_DRAININGToINITIALIZATION_READY =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_DRAINING \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> \<not>waterLevel()>normalN2() \<and> \<not>waterLevel()<normalN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION_READY
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode]@ [Event valve]@ [Event programReady] @ [State INITIALIZATION_READY]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_READYToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_READY \<and> successiveStops()\<ge>stopRepetitions() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_READYToEMERGENCY_STOP_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_READY \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> transmissionFault() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_READYToEMERGENCY_STOP_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_READY \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>transmissionFault() \<and> ((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_READYToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_READY \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>transmissionFault() \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> unitsReady() \<and> \<not>(((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty())) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_READYToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_READY \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>transmissionFault() \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> unitsReady() \<and> ((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation INITIALIZATION_READYToINITIALIZATION_READY =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= INITIALIZATION_READY \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>transmissionFault() \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> \<not>unitsReady() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= INITIALIZATION_READY
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode]@ [Event programReady] @ [State INITIALIZATION_READY]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> successiveStops()\<ge>stopRepetitions() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToEMERGENCY_STOP_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> (((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> levelDeviceFaulty() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> ((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> \<not>(((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty())) \<and> waterLevel()<normalN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> \<not>(((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty())) \<and> \<not>waterLevel()<normalN1() \<and> waterLevel()>normalN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NORMALToNORMAL_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= NORMAL \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> \<not>(((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty())) \<and> \<not>waterLevel()<normalN1() \<and> \<not>waterLevel()>normalN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> successiveStops()\<ge>stopRepetitions() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToEMERGENCY_STOP_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> (((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> levelDeviceFaulty() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> \<not>(((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty())) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> ((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty()) \<and> waterLevel()<normalN1() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> ((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty()) \<and> \<not>waterLevel()<normalN1() \<and> waterLevel()>normalN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation DEGRADEDToDEGRADED_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= DEGRADED \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> ((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty()) \<and> \<not>waterLevel()<normalN1() \<and> \<not>waterLevel()>normalN2() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> successiveStops()\<ge>stopRepetitions() \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToEMERGENCY_STOP_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> (((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<or> steamDeviceFaulty()) \<or> pumpControllerFaulty()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToNORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<or> steamDeviceFaulty()) \<or> pumpControllerFaulty())) \<and> \<not>levelDeviceFaulty() \<and> \<not>(((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty())) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State NORMAL]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToDEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<or> steamDeviceFaulty()) \<or> pumpControllerFaulty())) \<and> \<not>levelDeviceFaulty() \<and> ((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State DEGRADED]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<or> steamDeviceFaulty()) \<or> pumpControllerFaulty())) \<and> levelDeviceFaulty() \<and> (levelLowerBound() + levelUpperBound())<(2.0 * normalN1()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode]@ [Event openPump]@ [Event openPump]@ [Event openPump]@ [Event openPump] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_1 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<or> steamDeviceFaulty()) \<or> pumpControllerFaulty())) \<and> levelDeviceFaulty() \<and> \<not>(levelLowerBound() + levelUpperBound())<(2.0 * normalN1()) \<and> (levelLowerBound() + levelUpperBound())>(2.0 * normalN2()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode]@ [Event closePump]@ [Event closePump]@ [Event closePump]@ [Event closePump] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation RESCUEToRESCUE_2 =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= RESCUE \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<or> steamDeviceFaulty()) \<or> pumpControllerFaulty())) \<and> levelDeviceFaulty() \<and> \<not>(levelLowerBound() + levelUpperBound())<(2.0 * normalN1()) \<and> \<not>(levelLowerBound() + levelUpperBound())>(2.0 * normalN2()) \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State RESCUE]
         ,listens\<Zprime> = {transmission, transmission, transmission, transmission, transmission, transmission, transmission}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation EMERGENCY_STOPToEMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}" 
  pre "st= EMERGENCY_STOP \<and> transmission \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= EMERGENCY_STOP
         ,tr\<Zprime> =tr @ [Event transmission]@ [Event mode] @ [State EMERGENCY_STOP]
         ,listens\<Zprime> = {transmission}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_INITIALIZATION =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= INITIALIZATION \<and> \<not>(successiveStops()\<ge>stopRepetitions() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> (transmissionFault() \<or> unitsReady()) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>plantWaiting() \<and> transmission \<in> offered) \<and> \<not>(plantWaiting() \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> ((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty()) \<and> transmission \<in> offered) \<and> \<not>(plantWaiting() \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> waterLevel()>normalN2() \<and> transmission \<in> offered) \<and> \<not>(plantWaiting() \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> \<not>waterLevel()>normalN2() \<and> waterLevel()<normalN1() \<and> transmission \<in> offered) \<and> \<not>(plantWaiting() \<and> \<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> \<not>waterLevel()>normalN2() \<and> \<not>waterLevel()<normalN1() \<and> transmission \<in> offered)"
  update "[st\<Zprime>= INITIALIZATION
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_INITIALIZATION_FILLING =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= INITIALIZATION_FILLING \<and> \<not>(successiveStops()\<ge>stopRepetitions() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> (transmissionFault() \<or> unitsReady()) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> ((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty()) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> waterLevel()>normalN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> \<not>waterLevel()>normalN2() \<and> waterLevel()<normalN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> \<not>waterLevel()>normalN2() \<and> \<not>waterLevel()<normalN1() \<and> transmission \<in> offered)"
  update "[st\<Zprime>= INITIALIZATION_FILLING
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_INITIALIZATION_DRAINING =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= INITIALIZATION_DRAINING \<and> \<not>(successiveStops()\<ge>stopRepetitions() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> (transmissionFault() \<or> unitsReady()) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> ((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty()) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> waterLevel()>normalN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> \<not>waterLevel()>normalN2() \<and> waterLevel()<normalN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((transmissionFault() \<or> unitsReady())) \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> \<not>waterLevel()>normalN2() \<and> \<not>waterLevel()<normalN1() \<and> transmission \<in> offered)"
  update "[st\<Zprime>= INITIALIZATION_DRAINING
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_INITIALIZATION_READY =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= INITIALIZATION_READY \<and> \<not>(successiveStops()\<ge>stopRepetitions() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> transmissionFault() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>transmissionFault() \<and> ((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty()) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>transmissionFault() \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> unitsReady() \<and> \<not>(((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty())) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>transmissionFault() \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> unitsReady() \<and> ((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty()) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>transmissionFault() \<and> \<not>(((steamRate() \<noteq> 0.0) \<or> levelDeviceFaulty())) \<and> \<not>unitsReady() \<and> transmission \<in> offered)"
  update "[st\<Zprime>= INITIALIZATION_READY
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_NORMAL =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= NORMAL \<and> \<not>(successiveStops()\<ge>stopRepetitions() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> (((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> levelDeviceFaulty() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> ((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty()) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> \<not>(((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty())) \<and> waterLevel()<normalN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> \<not>(((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty())) \<and> \<not>waterLevel()<normalN1() \<and> waterLevel()>normalN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> \<not>(((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty())) \<and> \<not>waterLevel()<normalN1() \<and> \<not>waterLevel()>normalN2() \<and> transmission \<in> offered)"
  update "[st\<Zprime>= NORMAL
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_DEGRADED =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= DEGRADED \<and> \<not>(successiveStops()\<ge>stopRepetitions() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> (((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> levelDeviceFaulty() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> \<not>(((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty())) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> ((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty()) \<and> waterLevel()<normalN1() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> ((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty()) \<and> \<not>waterLevel()<normalN1() \<and> waterLevel()>normalN2() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2()))) \<and> \<not>levelDeviceFaulty() \<and> ((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty()) \<and> \<not>waterLevel()<normalN1() \<and> \<not>waterLevel()>normalN2() \<and> transmission \<in> offered)"
  update "[st\<Zprime>= DEGRADED
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_RESCUE =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= RESCUE \<and> \<not>(successiveStops()\<ge>stopRepetitions() \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> (((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<or> steamDeviceFaulty()) \<or> pumpControllerFaulty()) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<or> steamDeviceFaulty()) \<or> pumpControllerFaulty())) \<and> \<not>levelDeviceFaulty() \<and> \<not>(((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty())) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<or> steamDeviceFaulty()) \<or> pumpControllerFaulty())) \<and> \<not>levelDeviceFaulty() \<and> ((steamDeviceFaulty() \<or> pumpFaulty()) \<or> pumpControllerFaulty()) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<or> steamDeviceFaulty()) \<or> pumpControllerFaulty())) \<and> levelDeviceFaulty() \<and> (levelLowerBound() + levelUpperBound())<(2.0 * normalN1()) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<or> steamDeviceFaulty()) \<or> pumpControllerFaulty())) \<and> levelDeviceFaulty() \<and> \<not>(levelLowerBound() + levelUpperBound())<(2.0 * normalN1()) \<and> (levelLowerBound() + levelUpperBound())>(2.0 * normalN2()) \<and> transmission \<in> offered) \<and> \<not>(\<not>successiveStops()\<ge>stopRepetitions() \<and> \<not>((((((transmissionFault() \<or> plantWaiting()) \<or> unitsReady()) \<or> (lowestLevelNextCycle()<limitM1() \<or> highestLevelNextCycle()>limitM2())) \<or> steamDeviceFaulty()) \<or> pumpControllerFaulty())) \<and> levelDeviceFaulty() \<and> \<not>(levelLowerBound() + levelUpperBound())<(2.0 * normalN1()) \<and> \<not>(levelLowerBound() + levelUpperBound())>(2.0 * normalN2()) \<and> transmission \<in> offered)"
  update "[st\<Zprime>= RESCUE
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_EMERGENCY_STOP =
  over SteamBoilerController
  params nxt \<in> "{{}, {transmission}}"
  pre "st= EMERGENCY_STOP \<and> \<not>(transmission \<in> offered)"
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
  operations  InitialToINITIALIZATION INITIALIZATIONToEMERGENCY_STOP INITIALIZATIONToEMERGENCY_STOP_1 INITIALIZATIONToINITIALIZATION INITIALIZATIONToEMERGENCY_STOP_2 INITIALIZATIONToINITIALIZATION_DRAINING INITIALIZATIONToINITIALIZATION_FILLING INITIALIZATIONToINITIALIZATION_READY INITIALIZATION_FILLINGToEMERGENCY_STOP INITIALIZATION_FILLINGToEMERGENCY_STOP_1 INITIALIZATION_FILLINGToEMERGENCY_STOP_2 INITIALIZATION_FILLINGToINITIALIZATION_DRAINING INITIALIZATION_FILLINGToINITIALIZATION_FILLING INITIALIZATION_FILLINGToINITIALIZATION_READY INITIALIZATION_DRAININGToEMERGENCY_STOP INITIALIZATION_DRAININGToEMERGENCY_STOP_1 INITIALIZATION_DRAININGToEMERGENCY_STOP_2 INITIALIZATION_DRAININGToINITIALIZATION_DRAINING INITIALIZATION_DRAININGToINITIALIZATION_FILLING INITIALIZATION_DRAININGToINITIALIZATION_READY INITIALIZATION_READYToEMERGENCY_STOP INITIALIZATION_READYToEMERGENCY_STOP_1 INITIALIZATION_READYToEMERGENCY_STOP_2 INITIALIZATION_READYToNORMAL INITIALIZATION_READYToDEGRADED INITIALIZATION_READYToINITIALIZATION_READY NORMALToEMERGENCY_STOP NORMALToEMERGENCY_STOP_1 NORMALToRESCUE NORMALToDEGRADED NORMALToNORMAL NORMALToNORMAL_1 NORMALToNORMAL_2 DEGRADEDToEMERGENCY_STOP DEGRADEDToEMERGENCY_STOP_1 DEGRADEDToRESCUE DEGRADEDToNORMAL DEGRADEDToDEGRADED DEGRADEDToDEGRADED_1 DEGRADEDToDEGRADED_2 RESCUEToEMERGENCY_STOP RESCUEToEMERGENCY_STOP_1 RESCUEToNORMAL RESCUEToDEGRADED RESCUEToRESCUE RESCUEToRESCUE_1 RESCUEToRESCUE_2 EMERGENCY_STOPToEMERGENCY_STOP Idle_INITIALIZATION Idle_INITIALIZATION_FILLING Idle_INITIALIZATION_DRAINING Idle_INITIALIZATION_READY Idle_NORMAL Idle_DEGRADED Idle_RESCUE Idle_EMERGENCY_STOP


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SteamBoilerController_inv"
  by zpog_full

lemma InitialToINITIALIZATION_inv [hoare_lemmas]: "InitialToINITIALIZATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToEMERGENCY_STOP_inv [hoare_lemmas]: "INITIALIZATIONToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToEMERGENCY_STOP_1_inv [hoare_lemmas]: "INITIALIZATIONToEMERGENCY_STOP_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToEMERGENCY_STOP_2_inv [hoare_lemmas]: "INITIALIZATIONToEMERGENCY_STOP_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_DRAINING_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_DRAINING (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_FILLING_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_FILLING (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATIONToINITIALIZATION_READY_inv [hoare_lemmas]: "INITIALIZATIONToINITIALIZATION_READY (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_FILLINGToEMERGENCY_STOP_inv [hoare_lemmas]: "INITIALIZATION_FILLINGToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_FILLINGToEMERGENCY_STOP_1_inv [hoare_lemmas]: "INITIALIZATION_FILLINGToEMERGENCY_STOP_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_FILLINGToEMERGENCY_STOP_2_inv [hoare_lemmas]: "INITIALIZATION_FILLINGToEMERGENCY_STOP_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_FILLINGToINITIALIZATION_DRAINING_inv [hoare_lemmas]: "INITIALIZATION_FILLINGToINITIALIZATION_DRAINING (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_FILLINGToINITIALIZATION_FILLING_inv [hoare_lemmas]: "INITIALIZATION_FILLINGToINITIALIZATION_FILLING (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_FILLINGToINITIALIZATION_READY_inv [hoare_lemmas]: "INITIALIZATION_FILLINGToINITIALIZATION_READY (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_DRAININGToEMERGENCY_STOP_inv [hoare_lemmas]: "INITIALIZATION_DRAININGToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_DRAININGToEMERGENCY_STOP_1_inv [hoare_lemmas]: "INITIALIZATION_DRAININGToEMERGENCY_STOP_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_DRAININGToEMERGENCY_STOP_2_inv [hoare_lemmas]: "INITIALIZATION_DRAININGToEMERGENCY_STOP_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_DRAININGToINITIALIZATION_DRAINING_inv [hoare_lemmas]: "INITIALIZATION_DRAININGToINITIALIZATION_DRAINING (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_DRAININGToINITIALIZATION_FILLING_inv [hoare_lemmas]: "INITIALIZATION_DRAININGToINITIALIZATION_FILLING (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_DRAININGToINITIALIZATION_READY_inv [hoare_lemmas]: "INITIALIZATION_DRAININGToINITIALIZATION_READY (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_READYToEMERGENCY_STOP_inv [hoare_lemmas]: "INITIALIZATION_READYToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_READYToEMERGENCY_STOP_1_inv [hoare_lemmas]: "INITIALIZATION_READYToEMERGENCY_STOP_1 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_READYToEMERGENCY_STOP_2_inv [hoare_lemmas]: "INITIALIZATION_READYToEMERGENCY_STOP_2 (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_READYToNORMAL_inv [hoare_lemmas]: "INITIALIZATION_READYToNORMAL (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_READYToDEGRADED_inv [hoare_lemmas]: "INITIALIZATION_READYToDEGRADED (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma INITIALIZATION_READYToINITIALIZATION_READY_inv [hoare_lemmas]: "INITIALIZATION_READYToINITIALIZATION_READY (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToEMERGENCY_STOP_inv [hoare_lemmas]: "NORMALToEMERGENCY_STOP (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NORMALToEMERGENCY_STOP_1_inv [hoare_lemmas]: "NORMALToEMERGENCY_STOP_1 (nxt) preserves SteamBoilerController_inv"
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

lemma Idle_INITIALIZATION_FILLING_inv [hoare_lemmas]: "Idle_INITIALIZATION_FILLING (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_INITIALIZATION_DRAINING_inv [hoare_lemmas]: "Idle_INITIALIZATION_DRAINING (nxt) preserves SteamBoilerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_INITIALIZATION_READY_inv [hoare_lemmas]: "Idle_INITIALIZATION_READY (nxt) preserves SteamBoilerController_inv"
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
  
lemma "INITIALIZATIONToINITIALIZATION (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToEMERGENCY_STOP_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_DRAINING (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_FILLING (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATIONToINITIALIZATION_READY (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_FILLINGToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_FILLINGToEMERGENCY_STOP_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_FILLINGToEMERGENCY_STOP_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_FILLINGToINITIALIZATION_DRAINING (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_FILLINGToINITIALIZATION_FILLING (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_FILLINGToINITIALIZATION_READY (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_DRAININGToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_DRAININGToEMERGENCY_STOP_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_DRAININGToEMERGENCY_STOP_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_DRAININGToINITIALIZATION_DRAINING (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_DRAININGToINITIALIZATION_FILLING (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_DRAININGToINITIALIZATION_READY (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_READYToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_READYToEMERGENCY_STOP_1 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_READYToEMERGENCY_STOP_2 (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_READYToNORMAL (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_READYToDEGRADED (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "INITIALIZATION_READYToINITIALIZATION_READY (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToEMERGENCY_STOP (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "NORMALToEMERGENCY_STOP_1 (nxt) preserves R1 under SteamBoilerController_inv"
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
  
lemma "Idle_INITIALIZATION_FILLING (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_INITIALIZATION_DRAINING (nxt) preserves R1 under SteamBoilerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_INITIALIZATION_READY (nxt) preserves R1 under SteamBoilerController_inv"
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
