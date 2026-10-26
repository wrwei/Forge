theory SRangerController_Beh
imports "Z_Machines.Z_Machine"
begin

subsection \<open> Introduction \<close>

text \<open> This theory file is to model the SRangerController state machine in Z Machine notations.\<close>

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
   
enumtype St = MOVING | TURNING | FINAL | initial 
 


enumtype Evt = endTask | obstacle | tick 
 


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

consts distance :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts obstacleThreshold :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts since :: "'a \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts clockResetTime :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts turnDuration :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore SRangerController =
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
                      \<and> (st = MOVING \<longrightarrow> listens = {endTask, tick, obstacle})
                      \<and> (st = TURNING \<longrightarrow> listens = {tick, endTask})
                      \<and> (st = FINAL \<longrightarrow> listens = {tick})
                                                                               \<and> (st = MOVING \<longrightarrow> (offered = {} \<or> offered = {endTask} \<or> offered = {tick} \<or> offered = {obstacle}))
                                                                               \<and> (st = TURNING \<longrightarrow> (offered = {} \<or> offered = {tick} \<or> offered = {endTask}))
                                                                               \<and> (st = FINAL \<longrightarrow> (offered = {} \<or> offered = {tick}))
                                                                               \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToMOVING =
  over SRangerController
  params nxt \<in> "{{}, {endTask}, {tick}, {obstacle}}" 
  pre "st= initial"
  update "[st\<Zprime>= MOVING
         ,tr\<Zprime> =tr  @ [State MOVING]
         ,listens\<Zprime> = {endTask, tick, obstacle}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation MOVINGToFINAL =
  over SRangerController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= MOVING \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= FINAL
         ,tr\<Zprime> =tr @ [Event endTask] @ [State FINAL]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation MOVINGToTURNING =
  over SRangerController
  params nxt \<in> "{{}, {tick}, {endTask}}" 
  pre "st= MOVING \<and> distance()\<le>obstacleThreshold() \<and> obstacle \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TURNING
         ,tr\<Zprime> =tr @ [Event obstacle] @ [State TURNING]
         ,listens\<Zprime> = {tick, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation MOVINGToMOVING =
  over SRangerController
  params nxt \<in> "{{}, {endTask}, {tick}, {obstacle}}" 
  pre "st= MOVING \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= MOVING
         ,tr\<Zprime> =tr @ [Event tick] @ [State MOVING]
         ,listens\<Zprime> = {endTask, tick, obstacle}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TURNINGToFINAL =
  over SRangerController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= TURNING \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= FINAL
         ,tr\<Zprime> =tr @ [Event endTask] @ [State FINAL]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TURNINGToMOVING =
  over SRangerController
  params nxt \<in> "{{}, {endTask}, {tick}, {obstacle}}" 
  pre "st= TURNING \<and> since(clockResetTime())\<ge>turnDuration() \<and> \<not>(endTask \<in> offered) \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= MOVING
         ,tr\<Zprime> =tr  @ [State MOVING]
         ,listens\<Zprime> = {endTask, tick, obstacle}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TURNINGToTURNING =
  over SRangerController
  params nxt \<in> "{{}, {tick}, {endTask}}" 
  pre "st= TURNING \<and> \<not>since(clockResetTime())\<ge>turnDuration() \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TURNING
         ,tr\<Zprime> =tr @ [Event tick] @ [State TURNING]
         ,listens\<Zprime> = {tick, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation FINALToFINAL =
  over SRangerController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= FINAL \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= FINAL
         ,tr\<Zprime> =tr @ [Event tick] @ [State FINAL]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_MOVING =
  over SRangerController
  params nxt \<in> "{{}, {endTask}, {tick}, {obstacle}}"
  pre "st= MOVING \<and> \<not>(endTask \<in> offered) \<and> \<not>(distance()\<le>obstacleThreshold() \<and> obstacle \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= MOVING
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_TURNING =
  over SRangerController
  params nxt \<in> "{{}, {tick}, {endTask}}"
  pre "st= TURNING \<and> \<not>(endTask \<in> offered) \<and> \<not>(since(clockResetTime())\<ge>turnDuration()) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= TURNING
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_FINAL =
  over SRangerController
  params nxt \<in> "{{}, {tick}}"
  pre "st= FINAL \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= FINAL
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

definition Init :: "SRangerController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ,offered\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine SRangerControllerMachine =
  init Init
  invariant SRangerController_inv
  operations  InitialToMOVING MOVINGToFINAL MOVINGToTURNING MOVINGToMOVING TURNINGToFINAL TURNINGToMOVING TURNINGToTURNING FINALToFINAL Idle_MOVING Idle_TURNING Idle_FINAL


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SRangerController_inv"
  by zpog_full

lemma InitialToMOVING_inv [hoare_lemmas]: "InitialToMOVING (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MOVINGToFINAL_inv [hoare_lemmas]: "MOVINGToFINAL (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MOVINGToTURNING_inv [hoare_lemmas]: "MOVINGToTURNING (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MOVINGToMOVING_inv [hoare_lemmas]: "MOVINGToMOVING (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TURNINGToFINAL_inv [hoare_lemmas]: "TURNINGToFINAL (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TURNINGToMOVING_inv [hoare_lemmas]: "TURNINGToMOVING (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TURNINGToTURNING_inv [hoare_lemmas]: "TURNINGToTURNING (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma FINALToFINAL_inv [hoare_lemmas]: "FINALToFINAL (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_MOVING_inv [hoare_lemmas]: "Idle_MOVING (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_TURNING_inv [hoare_lemmas]: "Idle_TURNING (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_FINAL_inv [hoare_lemmas]: "Idle_FINAL (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToMOVING (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MOVINGToFINAL (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MOVINGToTURNING (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MOVINGToMOVING (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TURNINGToFINAL (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TURNINGToMOVING (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TURNINGToTURNING (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "FINALToFINAL (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_MOVING (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_TURNING (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_FINAL (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "SRangerController_axioms = True"

lemma SRangerController_deadlock_free: "SRangerController_axioms  \<Longrightarrow> deadlock_free SRangerControllerMachine"
  unfolding SRangerControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
