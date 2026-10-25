theory Movement_Beh
imports "Z_Machines.Z_Machine"
begin

subsection \<open> Introduction \<close>

text \<open> This theory file is to model the Movement state machine in Z Machine notations.\<close>

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
 


enumtype Evt = randomWalk | stop | flag | turn | resume | obstacle | odometer | shortRandomWalk 
 


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
record Chem = id :: int
record_default Chem
show_record Chem

record Intensity = value_ :: real
record_default Intensity
show_record Intensity

record GasSensor = c :: Chem i :: Intensity
record_default GasSensor
show_record GasSensor

definition A :: "Angle set" where [simp]: "A = UNIV"

definition L :: "Loc set" where [simp]: "L = UNIV"




text \<open> function definition \<close>

consts analysis :: " GasSensor list \<Rightarrow> Status"
consts intensity :: " GasSensor list \<Rightarrow> Intensity"
consts location :: " GasSensor list \<Rightarrow> Angle"
consts insAboveThr :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts since :: "'a \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts T :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckperiod :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckdist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore Movement =
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
                      \<and> (st = Waiting \<longrightarrow> listens = {resume, stop, turn})
                      \<and> (st = Going \<longrightarrow> listens = {resume, obstacle, stop, turn})
                      \<and> (st = Avoiding \<longrightarrow> listens = {resume, turn, stop, odometer})
                      \<and> (st = TryingAgain \<longrightarrow> listens = {obstacle, resume, turn, stop, odometer})
                      \<and> (st = AvoidingAgain \<longrightarrow> listens = {resume, stop})
                      \<and> (st = GettingOut \<longrightarrow> listens = {stop, resume, turn})
                                                                                                  \<and> (st = Waiting \<longrightarrow> (offered = {} \<or> offered = {resume} \<or> offered = {stop} \<or> offered = {turn}))
                                                                                                  \<and> (st = Going \<longrightarrow> (offered = {} \<or> offered = {resume} \<or> offered = {obstacle} \<or> offered = {stop} \<or> offered = {turn}))
                                                                                                  \<and> (st = Avoiding \<longrightarrow> (offered = {} \<or> offered = {resume} \<or> offered = {turn} \<or> offered = {stop} \<or> offered = {odometer}))
                                                                                                  \<and> (st = TryingAgain \<longrightarrow> (offered = {} \<or> offered = {obstacle} \<or> offered = {resume} \<or> offered = {turn} \<or> offered = {stop} \<or> offered = {odometer}))
                                                                                                  \<and> (st = AvoidingAgain \<longrightarrow> (offered = {} \<or> offered = {resume} \<or> offered = {stop}))
                                                                                                  \<and> (st = GettingOut \<longrightarrow> (offered = {} \<or> offered = {stop} \<or> offered = {resume} \<or> offered = {turn}))
                                                                                                  \<and> (st = Found \<longrightarrow> (offered = {}))
                                                                                                  \<and> (st = Final \<longrightarrow> (offered = {}))
                                                                                                  \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToWaiting =
  over Movement
  params nxt \<in> "{{}, {resume}, {stop}, {turn}}" 
  pre "st= initial"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation WaitingToFound =
  over Movement
  pre "st= Waiting \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation WaitingToGoing =
  over Movement
  params a_input \<in> "A" nxt \<in> "{{}, {resume}, {obstacle}, {stop}, {turn}}" 
  pre "st= Waiting \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Going
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State Going]
         ,listens\<Zprime> = {resume, obstacle, stop, turn}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation WaitingToWaiting =
  over Movement
  params nxt \<in> "{{}, {resume}, {stop}, {turn}}" 
  pre "st= Waiting \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GoingToFound =
  over Movement
  pre "st= Going \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation GoingToGoing =
  over Movement
  params a_input \<in> "A" nxt \<in> "{{}, {resume}, {obstacle}, {stop}, {turn}}" 
  pre "st= Going \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Going
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State Going]
         ,listens\<Zprime> = {resume, obstacle, stop, turn}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GoingToAvoiding =
  over Movement
  params l_input \<in> "L" nxt \<in> "{{}, {resume}, {turn}, {stop}, {odometer}}" 
  pre "st= Going \<and> obstacle \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Avoiding
         ,l\<Zprime> =l_input
         ,tr\<Zprime> =tr @ [Event obstacle] @ [State Avoiding]
         ,listens\<Zprime> = {resume, turn, stop, odometer}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GoingToWaiting =
  over Movement
  params nxt \<in> "{{}, {resume}, {stop}, {turn}}" 
  pre "st= Going \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingToFound =
  over Movement
  pre "st= Avoiding \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation AvoidingToTryingAgain =
  over Movement
  params a_input \<in> "A" nxt \<in> "{{}, {obstacle}, {resume}, {turn}, {stop}, {odometer}}" 
  pre "st= Avoiding \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TryingAgain
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State TryingAgain]
         ,listens\<Zprime> = {obstacle, resume, turn, stop, odometer}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingToWaiting =
  over Movement
  params nxt \<in> "{{}, {resume}, {stop}, {turn}}" 
  pre "st= Avoiding \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingToAvoiding =
  over Movement
  params d0_input \<in> "D0" nxt \<in> "{{}, {resume}, {turn}, {stop}, {odometer}}" 
  pre "st= Avoiding \<and> odometer \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Avoiding
         ,d0\<Zprime> =d0_input
         ,tr\<Zprime> =tr @ [Event odometer] @ [State Avoiding]
         ,listens\<Zprime> = {resume, turn, stop, odometer}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TryingAgainToFound =
  over Movement
  pre "st= TryingAgain \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation TryingAgainToTryingAgain =
  over Movement
  params a_input \<in> "A" nxt \<in> "{{}, {obstacle}, {resume}, {turn}, {stop}, {odometer}}" 
  pre "st= TryingAgain \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TryingAgain
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State TryingAgain]
         ,listens\<Zprime> = {obstacle, resume, turn, stop, odometer}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TryingAgainToAvoidingAgain =
  over Movement
  params l_input \<in> "L" nxt \<in> "{{}, {resume}, {stop}}" 
  pre "st= TryingAgain \<and> obstacle \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= AvoidingAgain
         ,l\<Zprime> =l_input
         ,tr\<Zprime> =tr @ [Event obstacle] @ [State AvoidingAgain]
         ,listens\<Zprime> = {resume, stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TryingAgainToWaiting =
  over Movement
  params nxt \<in> "{{}, {resume}, {stop}, {turn}}" 
  pre "st= TryingAgain \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TryingAgainToTryingAgain_1 =
  over Movement
  params d1_input \<in> "D1" nxt \<in> "{{}, {obstacle}, {resume}, {turn}, {stop}, {odometer}}" 
  pre "st= TryingAgain \<and> odometer \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TryingAgain
         ,d1\<Zprime> =d1_input
         ,tr\<Zprime> =tr @ [Event odometer] @ [State TryingAgain]
         ,listens\<Zprime> = {obstacle, resume, turn, stop, odometer}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingAgainToFound =
  over Movement
  pre "st= AvoidingAgain \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation AvoidingAgainToWaiting =
  over Movement
  params nxt \<in> "{{}, {resume}, {stop}, {turn}}" 
  pre "st= AvoidingAgain \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingAgainToAvoiding =
  over Movement
  params nxt \<in> "{{}, {resume}, {turn}, {stop}, {odometer}}" 
  pre "st= AvoidingAgain \<and> (since(T())<stuckperiod() \<or> (d1 - d0)>stuckdist()) \<and> \<not>(stop \<in> offered) \<and> \<not>(resume \<in> offered) \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Avoiding
         ,tr\<Zprime> =tr  @ [State Avoiding]
         ,listens\<Zprime> = {resume, turn, stop, odometer}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingAgainToGettingOut =
  over Movement
  params nxt \<in> "{{}, {stop}, {resume}, {turn}}" 
  pre "st= AvoidingAgain \<and> since(T())\<ge>stuckperiod() \<and> (d1 - d0)\<le>stuckdist() \<and> \<not>((since(T())<stuckperiod() \<or> (d1 - d0)>stuckdist())) \<and> \<not>(stop \<in> offered) \<and> \<not>(resume \<in> offered) \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= GettingOut
         ,tr\<Zprime> =tr @ [Event shortRandomWalk] @ [State GettingOut]
         ,listens\<Zprime> = {stop, resume, turn}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GettingOutToFound =
  over Movement
  pre "st= GettingOut \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation GettingOutToGoing =
  over Movement
  params a_input \<in> "A" nxt \<in> "{{}, {resume}, {obstacle}, {stop}, {turn}}" 
  pre "st= GettingOut \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Going
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State Going]
         ,listens\<Zprime> = {resume, obstacle, stop, turn}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GettingOutToWaiting =
  over Movement
  params nxt \<in> "{{}, {resume}, {stop}, {turn}}" 
  pre "st= GettingOut \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {resume, stop, turn}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation FoundToFinal =
  over Movement
  pre "st= Found"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr  @ [State Final]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        

  
zoperation Idle_Waiting =
  over Movement
  params nxt \<in> "{{}, {resume}, {stop}, {turn}}"
  pre "st= Waiting \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_Going =
  over Movement
  params nxt \<in> "{{}, {resume}, {obstacle}, {stop}, {turn}}"
  pre "st= Going \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(obstacle \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= Going
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_Avoiding =
  over Movement
  params nxt \<in> "{{}, {resume}, {turn}, {stop}, {odometer}}"
  pre "st= Avoiding \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(resume \<in> offered) \<and> \<not>(odometer \<in> offered)"
  update "[st\<Zprime>= Avoiding
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_TryingAgain =
  over Movement
  params nxt \<in> "{{}, {obstacle}, {resume}, {turn}, {stop}, {odometer}}"
  pre "st= TryingAgain \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(obstacle \<in> offered) \<and> \<not>(resume \<in> offered) \<and> \<not>(odometer \<in> offered)"
  update "[st\<Zprime>= TryingAgain
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_AvoidingAgain =
  over Movement
  params nxt \<in> "{{}, {resume}, {stop}}"
  pre "st= AvoidingAgain \<and> \<not>(stop \<in> offered) \<and> \<not>(resume \<in> offered) \<and> \<not>((since(T())<stuckperiod() \<or> (d1 - d0)>stuckdist())) \<and> \<not>(since(T())\<ge>stuckperiod() \<and> (d1 - d0)\<le>stuckdist())"
  update "[st\<Zprime>= AvoidingAgain
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_GettingOut =
  over Movement
  params nxt \<in> "{{}, {stop}, {resume}, {turn}}"
  pre "st= GettingOut \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= GettingOut
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

definition Init :: "Movement subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ,offered\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine MovementMachine =
  init Init
  invariant Movement_inv
  operations  InitialToWaiting WaitingToFound WaitingToGoing WaitingToWaiting GoingToFound GoingToGoing GoingToAvoiding GoingToWaiting AvoidingToFound AvoidingToTryingAgain AvoidingToWaiting AvoidingToAvoiding TryingAgainToFound TryingAgainToTryingAgain TryingAgainToAvoidingAgain TryingAgainToWaiting TryingAgainToTryingAgain_1 AvoidingAgainToFound AvoidingAgainToWaiting AvoidingAgainToAvoiding AvoidingAgainToGettingOut GettingOutToFound GettingOutToGoing GettingOutToWaiting FoundToFinal Idle_Waiting Idle_Going Idle_Avoiding Idle_TryingAgain Idle_AvoidingAgain Idle_GettingOut
  until "st = Final"
(* `until` marks states exempt from deadlock-freedom (deadlock_free_z_machine's
   End parameter). These are states that are the source of no transition, so the
   machine has terminated there and is not required to offer an event. Without this
   clause End defaults to False and the obligation is FALSE at such a state --
   machine-checked, see scripts/test_f17_probe.sh. *)

subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes Movement_inv"
  by zpog_full

lemma InitialToWaiting_inv [hoare_lemmas]: "InitialToWaiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma WaitingToFound_inv [hoare_lemmas]: "WaitingToFound() preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma WaitingToGoing_inv [hoare_lemmas]: "WaitingToGoing (a_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma WaitingToWaiting_inv [hoare_lemmas]: "WaitingToWaiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GoingToFound_inv [hoare_lemmas]: "GoingToFound() preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GoingToGoing_inv [hoare_lemmas]: "GoingToGoing (a_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GoingToAvoiding_inv [hoare_lemmas]: "GoingToAvoiding (l_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GoingToWaiting_inv [hoare_lemmas]: "GoingToWaiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AvoidingToFound_inv [hoare_lemmas]: "AvoidingToFound() preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AvoidingToTryingAgain_inv [hoare_lemmas]: "AvoidingToTryingAgain (a_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AvoidingToWaiting_inv [hoare_lemmas]: "AvoidingToWaiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AvoidingToAvoiding_inv [hoare_lemmas]: "AvoidingToAvoiding (d0_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TryingAgainToFound_inv [hoare_lemmas]: "TryingAgainToFound() preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TryingAgainToTryingAgain_inv [hoare_lemmas]: "TryingAgainToTryingAgain (a_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TryingAgainToAvoidingAgain_inv [hoare_lemmas]: "TryingAgainToAvoidingAgain (l_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TryingAgainToWaiting_inv [hoare_lemmas]: "TryingAgainToWaiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TryingAgainToTryingAgain_1_inv [hoare_lemmas]: "TryingAgainToTryingAgain_1 (d1_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AvoidingAgainToFound_inv [hoare_lemmas]: "AvoidingAgainToFound() preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AvoidingAgainToWaiting_inv [hoare_lemmas]: "AvoidingAgainToWaiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AvoidingAgainToAvoiding_inv [hoare_lemmas]: "AvoidingAgainToAvoiding (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AvoidingAgainToGettingOut_inv [hoare_lemmas]: "AvoidingAgainToGettingOut (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GettingOutToFound_inv [hoare_lemmas]: "GettingOutToFound() preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GettingOutToGoing_inv [hoare_lemmas]: "GettingOutToGoing (a_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GettingOutToWaiting_inv [hoare_lemmas]: "GettingOutToWaiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma FoundToFinal_inv [hoare_lemmas]: "FoundToFinal() preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma Idle_Waiting_inv [hoare_lemmas]: "Idle_Waiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Going_inv [hoare_lemmas]: "Idle_Going (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Avoiding_inv [hoare_lemmas]: "Idle_Avoiding (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_TryingAgain_inv [hoare_lemmas]: "Idle_TryingAgain (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_AvoidingAgain_inv [hoare_lemmas]: "Idle_AvoidingAgain (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_GettingOut_inv [hoare_lemmas]: "Idle_GettingOut (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToWaiting (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "WaitingToFound() preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "WaitingToGoing (a_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "WaitingToWaiting (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "GoingToFound() preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "GoingToGoing (a_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "GoingToAvoiding (l_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "GoingToWaiting (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "AvoidingToFound() preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "AvoidingToTryingAgain (a_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "AvoidingToWaiting (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "AvoidingToAvoiding (d0_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToFound() preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToTryingAgain (a_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToAvoidingAgain (l_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToWaiting (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToTryingAgain_1 (d1_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToFound() preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToWaiting (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToAvoiding (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToGettingOut (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "GettingOutToFound() preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "GettingOutToGoing (a_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "GettingOutToWaiting (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "FoundToFinal() preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "Idle_Waiting (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "Idle_Going (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "Idle_Avoiding (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "Idle_TryingAgain (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "Idle_AvoidingAgain (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "Idle_GettingOut (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "Movement_axioms = True"

(* WARNING -- THIS OBLIGATION IS FALSE AS EMITTED.
   State(s) with no outgoing transition: Final.
   Under `loop` semantics such a state offers Vis {} = deadlock, so
   deadlock_free does not hold. This is a MODEL defect (a missing tick-triggered
   self-loop in the Java), not a proof-search problem: no tactic can close it.
   Measured on chemical_detector v2 -- auto OOM 978s, metis 1825s, blast 922s,
   fastforce 923s; refuted by probe_Y3_obligation_refuted with witness
   st = Final.
   Fix the Java per forge.assets/prompts/java_codegen_rules.txt ("EVERY MODE
   MUST HAVE AN OUTGOING TRANSITION"), re-extract, and this marker disappears. *)
lemma Movement_deadlock_free: "Movement_axioms  \<Longrightarrow> deadlock_free MovementMachine"
  unfolding MovementMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
