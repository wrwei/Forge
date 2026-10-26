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
 


enumtype Evt = endTask | move | obstacle | tick 
 


type_synonym move_Type_lv_double_av_double= "nat"
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
                      \<and> (st = MOVING \<longrightarrow> listens = {tick, obstacle, endTask})
                      \<and> (st = TURNING \<longrightarrow> listens = {tick, endTask})
                                                                                          \<and> (st = MOVING \<longrightarrow> (offered = {} \<or> offered = {tick} \<or> offered = {obstacle} \<or> offered = {endTask}))
                                                                                          \<and> (st = TURNING \<longrightarrow> (offered = {} \<or> offered = {tick} \<or> offered = {endTask}))
                                                                                          \<and> (st = FINAL \<longrightarrow> (offered = {}))
                                                                                          \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToMOVING =
  over SRangerController
  params nxt \<in> "{{}, {tick}, {obstacle}, {endTask}}" 
  pre "st= initial"
  update "[st\<Zprime>= MOVING
         ,tr\<Zprime> =tr  @ [State MOVING]
         ,listens\<Zprime> = {tick, obstacle, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation MOVINGToFINAL =
  over SRangerController
  pre "st= MOVING \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= FINAL
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State FINAL]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation MOVINGToTURNING =
  over SRangerController
  params nxt \<in> "{{}, {tick}, {endTask}}" 
  pre "st= MOVING \<and> distance()\<le>obstacleThreshold() \<and> obstacle \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TURNING
         ,tr\<Zprime> =tr @ [Event obstacle]@ [Event move] @ [State TURNING]
         ,listens\<Zprime> = {tick, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation MOVINGToMOVING =
  over SRangerController
  params nxt \<in> "{{}, {tick}, {obstacle}, {endTask}}" 
  pre "st= MOVING \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= MOVING
         ,tr\<Zprime> =tr @ [Event tick] @ [State MOVING]
         ,listens\<Zprime> = {tick, obstacle, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TURNINGToFINAL =
  over SRangerController
  pre "st= TURNING \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= FINAL
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State FINAL]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation TURNINGToMOVING =
  over SRangerController
  params nxt \<in> "{{}, {tick}, {obstacle}, {endTask}}" 
  pre "st= TURNING \<and> since(clockResetTime())\<ge>turnDuration() \<and> \<not>(endTask \<in> offered) \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= MOVING
         ,tr\<Zprime> =tr @ [Event move] @ [State MOVING]
         ,listens\<Zprime> = {tick, obstacle, endTask}
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
        

  
zoperation Idle_MOVING =
  over SRangerController
  params nxt \<in> "{{}, {tick}, {obstacle}, {endTask}}"
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
  operations  InitialToMOVING MOVINGToFINAL MOVINGToTURNING MOVINGToMOVING TURNINGToFINAL TURNINGToMOVING TURNINGToTURNING Idle_MOVING Idle_TURNING
  until "st = FINAL"
(* `until` marks states exempt from deadlock-freedom (deadlock_free_z_machine's
   End parameter). These are states that are the source of no transition, so the
   machine has terminated there and is not required to offer an event. Without this
   clause End defaults to False and the obligation is FALSE at such a state --
   machine-checked, see scripts/test_f17_probe.sh. *)

subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SRangerController_inv"
  by zpog_full

lemma InitialToMOVING_inv [hoare_lemmas]: "InitialToMOVING (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MOVINGToFINAL_inv [hoare_lemmas]: "MOVINGToFINAL() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOVINGToTURNING_inv [hoare_lemmas]: "MOVINGToTURNING (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma MOVINGToMOVING_inv [hoare_lemmas]: "MOVINGToMOVING (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TURNINGToFINAL_inv [hoare_lemmas]: "TURNINGToFINAL() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TURNINGToMOVING_inv [hoare_lemmas]: "TURNINGToMOVING (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TURNINGToTURNING_inv [hoare_lemmas]: "TURNINGToTURNING (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_MOVING_inv [hoare_lemmas]: "Idle_MOVING (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_TURNING_inv [hoare_lemmas]: "Idle_TURNING (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToMOVING (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MOVINGToFINAL() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MOVINGToTURNING (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MOVINGToMOVING (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TURNINGToFINAL() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TURNINGToMOVING (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TURNINGToTURNING (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_MOVING (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_TURNING (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "SRangerController_axioms = True"

(* WARNING -- THIS OBLIGATION IS FALSE AS EMITTED.
   State(s) with no outgoing transition: FINAL.
   Under `loop` semantics such a state offers Vis {} = deadlock, so
   deadlock_free does not hold. This is a MODEL defect (a missing tick-triggered
   self-loop in the Java), not a proof-search problem: no tactic can close it.
   Measured on chemical_detector v2 -- auto OOM 978s, metis 1825s, blast 922s,
   fastforce 923s; refuted by probe_Y3_obligation_refuted with witness
   st = FINAL.
   Fix the Java per forge.assets/prompts/java_codegen_rules.txt ("EVERY MODE
   MUST HAVE AN OUTGOING TRANSITION"), re-extract, and this marker disappears. *)
lemma SRangerController_deadlock_free: "SRangerController_axioms  \<Longrightarrow> deadlock_free SRangerControllerMachine"
  unfolding SRangerControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
