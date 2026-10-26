theory MovementController_Beh
imports "Z_Machines.Z_Machine"
begin

subsection \<open> Introduction \<close>

text \<open> This theory file is to model the MovementController state machine in Z Machine notations.\<close>

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
   
enumtype St = Waiting | Going | Avoiding | TryingAgain | AvoidingAgain | GettingOut | Found | Final | initial 
 


enumtype Evt = randomWalk | stop | flag | turn | resume | obstacle | compute | shortRandomWalk | tick 
 


enumtype Angle = Left | Right | Back | Front

enumtype Loc = left | right | front

enumtype Status = noGas | gasD

instantiation real :: default
begin
  definition default_real :: "real" where "default_real = 0"
  instance ..
end

instantiation real :: "show"
begin
  instance ..
end
record GasSensor = chemId :: int intensityValue :: real
record_default GasSensor
show_record GasSensor

record Intensity = value_ :: real
record_default Intensity
show_record Intensity


text \<open> function definition \<close>

consts peakIntensity :: " GasSensor list \<Rightarrow> real"
consts location :: " GasSensor list \<Rightarrow> Angle"
consts analysis :: " GasSensor list \<Rightarrow> Status"
consts since :: "'a \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts clockResetTime :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckperiod :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckdist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore MovementController =
  a :: "Angle"
  d0 :: "real"
  d1 :: "real"
  l :: "Loc"
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
                      \<and> (st = Waiting \<longrightarrow> listens = {stop, turn, resume})
                      \<and> (st = Going \<longrightarrow> listens = {stop, obstacle, turn, resume})
                      \<and> (st = Avoiding \<longrightarrow> listens = {resume, turn, stop})
                      \<and> (st = TryingAgain \<longrightarrow> listens = {turn, resume, stop, obstacle})
                      \<and> (st = AvoidingAgain \<longrightarrow> listens = {stop, resume})
                      \<and> (st = GettingOut \<longrightarrow> listens = {stop, turn, resume})
                      \<and> (st = Final \<longrightarrow> listens = {tick})
                                                                               \<and> (st = Waiting \<longrightarrow> (offered = {} \<or> offered = {stop} \<or> offered = {turn} \<or> offered = {resume}))
                                                                               \<and> (st = Going \<longrightarrow> (offered = {} \<or> offered = {stop} \<or> offered = {obstacle} \<or> offered = {turn} \<or> offered = {resume}))
                                                                               \<and> (st = Avoiding \<longrightarrow> (offered = {} \<or> offered = {resume} \<or> offered = {turn} \<or> offered = {stop}))
                                                                               \<and> (st = TryingAgain \<longrightarrow> (offered = {} \<or> offered = {turn} \<or> offered = {resume} \<or> offered = {stop} \<or> offered = {obstacle}))
                                                                               \<and> (st = AvoidingAgain \<longrightarrow> (offered = {} \<or> offered = {stop} \<or> offered = {resume}))
                                                                               \<and> (st = GettingOut \<longrightarrow> (offered = {} \<or> offered = {stop} \<or> offered = {turn} \<or> offered = {resume}))
                                                                               \<and> (st = Found \<longrightarrow> (offered = {}))
                                                                               \<and> (st = Final \<longrightarrow> (offered = {} \<or> offered = {tick}))
                                                                               \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToWaiting =
  over MovementController
  params nxt \<in> "{{}, {stop}, {turn}, {resume}}" 
  pre "st= initial"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {stop, turn, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation WaitingToFound =
  over MovementController
  pre "st= Waiting \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation WaitingToGoing =
  over MovementController
  params nxt \<in> "{{}, {stop}, {obstacle}, {turn}, {resume}}" 
  pre "st= Waiting \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Going
         ,tr\<Zprime> =tr @ [Event turn] @ [State Going]
         ,listens\<Zprime> = {stop, obstacle, turn, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation WaitingToWaiting =
  over MovementController
  params nxt \<in> "{{}, {stop}, {turn}, {resume}}" 
  pre "st= Waiting \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {stop, turn, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GoingToFound =
  over MovementController
  pre "st= Going \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation GoingToGoing =
  over MovementController
  params nxt \<in> "{{}, {stop}, {obstacle}, {turn}, {resume}}" 
  pre "st= Going \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Going
         ,tr\<Zprime> =tr @ [Event turn] @ [State Going]
         ,listens\<Zprime> = {stop, obstacle, turn, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GoingToAvoiding =
  over MovementController
  params nxt \<in> "{{}, {resume}, {turn}, {stop}}" 
  pre "st= Going \<and> obstacle \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Avoiding
         ,d0\<Zprime> = 0.0
         ,tr\<Zprime> =tr @ [Event obstacle]@ [Event compute] @ [State Avoiding]
         ,listens\<Zprime> = {resume, turn, stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GoingToWaiting =
  over MovementController
  params nxt \<in> "{{}, {stop}, {turn}, {resume}}" 
  pre "st= Going \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {stop, turn, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation FoundToFinal =
  over MovementController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= Found"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr  @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingToFound =
  over MovementController
  pre "st= Avoiding \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation AvoidingToTryingAgain =
  over MovementController
  params nxt \<in> "{{}, {turn}, {resume}, {stop}, {obstacle}}" 
  pre "st= Avoiding \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TryingAgain
         ,tr\<Zprime> =tr @ [Event turn] @ [State TryingAgain]
         ,listens\<Zprime> = {turn, resume, stop, obstacle}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingToWaiting =
  over MovementController
  params nxt \<in> "{{}, {stop}, {turn}, {resume}}" 
  pre "st= Avoiding \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {stop, turn, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TryingAgainToFound =
  over MovementController
  pre "st= TryingAgain \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation TryingAgainToTryingAgain =
  over MovementController
  params nxt \<in> "{{}, {turn}, {resume}, {stop}, {obstacle}}" 
  pre "st= TryingAgain \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TryingAgain
         ,tr\<Zprime> =tr @ [Event turn] @ [State TryingAgain]
         ,listens\<Zprime> = {turn, resume, stop, obstacle}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TryingAgainToAvoidingAgain =
  over MovementController
  params nxt \<in> "{{}, {stop}, {resume}}" 
  pre "st= TryingAgain \<and> obstacle \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= AvoidingAgain
         ,d1\<Zprime> = 0.0
         ,tr\<Zprime> =tr @ [Event obstacle] @ [State AvoidingAgain]
         ,listens\<Zprime> = {stop, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TryingAgainToWaiting =
  over MovementController
  params nxt \<in> "{{}, {stop}, {turn}, {resume}}" 
  pre "st= TryingAgain \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {stop, turn, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingAgainToFound =
  over MovementController
  pre "st= AvoidingAgain \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation AvoidingAgainToWaiting =
  over MovementController
  params nxt \<in> "{{}, {stop}, {turn}, {resume}}" 
  pre "st= AvoidingAgain \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {stop, turn, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingAgainToAvoiding =
  over MovementController
  params nxt \<in> "{{}, {resume}, {turn}, {stop}}" 
  pre "st= AvoidingAgain \<and> (since(clockResetTime())<stuckperiod() \<or> (d1 - d0)>stuckdist()) \<and> \<not>(stop \<in> offered) \<and> \<not>(resume \<in> offered) \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Avoiding
         ,d0\<Zprime> = 0.0
         ,tr\<Zprime> =tr @ [Event compute] @ [State Avoiding]
         ,listens\<Zprime> = {resume, turn, stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingAgainToGettingOut =
  over MovementController
  params nxt \<in> "{{}, {stop}, {turn}, {resume}}" 
  pre "st= AvoidingAgain \<and> since(clockResetTime())\<ge>stuckperiod() \<and> (d1 - d0)\<le>stuckdist() \<and> \<not>((since(clockResetTime())<stuckperiod() \<or> (d1 - d0)>stuckdist())) \<and> \<not>(stop \<in> offered) \<and> \<not>(resume \<in> offered) \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= GettingOut
         ,tr\<Zprime> =tr @ [Event shortRandomWalk] @ [State GettingOut]
         ,listens\<Zprime> = {stop, turn, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GettingOutToFound =
  over MovementController
  pre "st= GettingOut \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation GettingOutToGoing =
  over MovementController
  params nxt \<in> "{{}, {stop}, {obstacle}, {turn}, {resume}}" 
  pre "st= GettingOut \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Going
         ,tr\<Zprime> =tr @ [Event turn] @ [State Going]
         ,listens\<Zprime> = {stop, obstacle, turn, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GettingOutToWaiting =
  over MovementController
  params nxt \<in> "{{}, {stop}, {turn}, {resume}}" 
  pre "st= GettingOut \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {stop, turn, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation FinalToFinal =
  over MovementController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= Final \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event tick] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_Waiting =
  over MovementController
  params nxt \<in> "{{}, {stop}, {turn}, {resume}}"
  pre "st= Waiting \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_Going =
  over MovementController
  params nxt \<in> "{{}, {stop}, {obstacle}, {turn}, {resume}}"
  pre "st= Going \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(obstacle \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= Going
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_Avoiding =
  over MovementController
  params nxt \<in> "{{}, {resume}, {turn}, {stop}}"
  pre "st= Avoiding \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= Avoiding
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_TryingAgain =
  over MovementController
  params nxt \<in> "{{}, {turn}, {resume}, {stop}, {obstacle}}"
  pre "st= TryingAgain \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(obstacle \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= TryingAgain
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_AvoidingAgain =
  over MovementController
  params nxt \<in> "{{}, {stop}, {resume}}"
  pre "st= AvoidingAgain \<and> \<not>(stop \<in> offered) \<and> \<not>(resume \<in> offered) \<and> \<not>((since(clockResetTime())<stuckperiod() \<or> (d1 - d0)>stuckdist())) \<and> \<not>(since(clockResetTime())\<ge>stuckperiod() \<and> (d1 - d0)\<le>stuckdist())"
  update "[st\<Zprime>= AvoidingAgain
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_GettingOut =
  over MovementController
  params nxt \<in> "{{}, {stop}, {turn}, {resume}}"
  pre "st= GettingOut \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= GettingOut
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_Final =
  over MovementController
  params nxt \<in> "{{}, {tick}}"
  pre "st= Final \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

definition Init :: "MovementController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ,offered\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine MovementControllerMachine =
  init Init
  invariant MovementController_inv
  operations  InitialToWaiting WaitingToFound WaitingToGoing WaitingToWaiting GoingToFound GoingToGoing GoingToAvoiding GoingToWaiting FoundToFinal AvoidingToFound AvoidingToTryingAgain AvoidingToWaiting TryingAgainToFound TryingAgainToTryingAgain TryingAgainToAvoidingAgain TryingAgainToWaiting AvoidingAgainToFound AvoidingAgainToWaiting AvoidingAgainToAvoiding AvoidingAgainToGettingOut GettingOutToFound GettingOutToGoing GettingOutToWaiting FinalToFinal Idle_Waiting Idle_Going Idle_Avoiding Idle_TryingAgain Idle_AvoidingAgain Idle_GettingOut Idle_Final


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes MovementController_inv"
  by zpog_full

lemma InitialToWaiting_inv [hoare_lemmas]: "InitialToWaiting (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma WaitingToFound_inv [hoare_lemmas]: "WaitingToFound() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma WaitingToGoing_inv [hoare_lemmas]: "WaitingToGoing (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma WaitingToWaiting_inv [hoare_lemmas]: "WaitingToWaiting (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GoingToFound_inv [hoare_lemmas]: "GoingToFound() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GoingToGoing_inv [hoare_lemmas]: "GoingToGoing (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GoingToAvoiding_inv [hoare_lemmas]: "GoingToAvoiding (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GoingToWaiting_inv [hoare_lemmas]: "GoingToWaiting (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma FoundToFinal_inv [hoare_lemmas]: "FoundToFinal (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AvoidingToFound_inv [hoare_lemmas]: "AvoidingToFound() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AvoidingToTryingAgain_inv [hoare_lemmas]: "AvoidingToTryingAgain (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AvoidingToWaiting_inv [hoare_lemmas]: "AvoidingToWaiting (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TryingAgainToFound_inv [hoare_lemmas]: "TryingAgainToFound() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TryingAgainToTryingAgain_inv [hoare_lemmas]: "TryingAgainToTryingAgain (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TryingAgainToAvoidingAgain_inv [hoare_lemmas]: "TryingAgainToAvoidingAgain (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TryingAgainToWaiting_inv [hoare_lemmas]: "TryingAgainToWaiting (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AvoidingAgainToFound_inv [hoare_lemmas]: "AvoidingAgainToFound() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AvoidingAgainToWaiting_inv [hoare_lemmas]: "AvoidingAgainToWaiting (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AvoidingAgainToAvoiding_inv [hoare_lemmas]: "AvoidingAgainToAvoiding (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AvoidingAgainToGettingOut_inv [hoare_lemmas]: "AvoidingAgainToGettingOut (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GettingOutToFound_inv [hoare_lemmas]: "GettingOutToFound() preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GettingOutToGoing_inv [hoare_lemmas]: "GettingOutToGoing (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GettingOutToWaiting_inv [hoare_lemmas]: "GettingOutToWaiting (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma FinalToFinal_inv [hoare_lemmas]: "FinalToFinal (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Waiting_inv [hoare_lemmas]: "Idle_Waiting (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Going_inv [hoare_lemmas]: "Idle_Going (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Avoiding_inv [hoare_lemmas]: "Idle_Avoiding (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_TryingAgain_inv [hoare_lemmas]: "Idle_TryingAgain (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_AvoidingAgain_inv [hoare_lemmas]: "Idle_AvoidingAgain (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_GettingOut_inv [hoare_lemmas]: "Idle_GettingOut (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Final_inv [hoare_lemmas]: "Idle_Final (nxt) preserves MovementController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToWaiting (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "WaitingToFound() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "WaitingToGoing (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "WaitingToWaiting (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GoingToFound() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GoingToGoing (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GoingToAvoiding (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GoingToWaiting (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "FoundToFinal (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AvoidingToFound() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AvoidingToTryingAgain (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AvoidingToWaiting (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToFound() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToTryingAgain (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToAvoidingAgain (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToWaiting (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToFound() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToWaiting (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToAvoiding (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToGettingOut (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GettingOutToFound() preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GettingOutToGoing (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "GettingOutToWaiting (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "FinalToFinal (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "Idle_Waiting (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "Idle_Going (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "Idle_Avoiding (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "Idle_TryingAgain (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "Idle_AvoidingAgain (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "Idle_GettingOut (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  
lemma "Idle_Final (nxt) preserves R1 under MovementController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "MovementController_axioms = True"

lemma MovementController_deadlock_free: "MovementController_axioms  \<Longrightarrow> deadlock_free MovementControllerMachine"
  unfolding MovementControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
