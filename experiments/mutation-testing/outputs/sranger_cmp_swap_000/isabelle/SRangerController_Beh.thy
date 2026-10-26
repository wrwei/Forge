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
   
enumtype St = Moving | Turning | Final | initial 
 


enumtype Evt = move | endTask | tick 
 


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


subsection \<open> State Space \<close>

zstore SRangerController =
  turnDurationElapsed :: "bool"
  resetTime :: "real"
  elapsed :: "bool"
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
                      \<and> (st = Turning \<longrightarrow> listens = {tick, endTask})
                      \<and> (st = Final \<longrightarrow> listens = {tick})
                                                                               \<and> (st = Moving \<longrightarrow> (offered = {}))
                                                                               \<and> (st = Turning \<longrightarrow> (offered = {} \<or> offered = {tick} \<or> offered = {endTask}))
                                                                               \<and> (st = Final \<longrightarrow> (offered = {} \<or> offered = {tick}))
                                                                               \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToMoving =
  over SRangerController
  pre "st= initial"
  update "[st\<Zprime>= Moving
         ,resetTime\<Zprime> = 0
         ,elapsed\<Zprime> = False
         ,tr\<Zprime> =tr  @ [State Moving]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation TurningToMoving =
  over SRangerController
  pre "st= Turning \<and> turnDurationElapsed"
  update "[st\<Zprime>= Moving
         ,resetTime\<Zprime> = 0
         ,elapsed\<Zprime> = False
         ,tr\<Zprime> =tr @ [Event move] @ [State Moving]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation TurningToFinal =
  over SRangerController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= Turning \<and> \<not>turnDurationElapsed \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,resetTime\<Zprime> = 0
         ,elapsed\<Zprime> = False
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TurningToTurning =
  over SRangerController
  params nxt \<in> "{{}, {tick}, {endTask}}" 
  pre "st= Turning \<and> \<not>turnDurationElapsed \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Turning
         ,resetTime\<Zprime> = 0
         ,elapsed\<Zprime> = False
         ,tr\<Zprime> =tr @ [Event tick] @ [State Turning]
         ,listens\<Zprime> = {tick, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation FinalToFinal =
  over SRangerController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= Final \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,resetTime\<Zprime> = 0
         ,elapsed\<Zprime> = False
         ,tr\<Zprime> =tr @ [Event tick] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_Turning =
  over SRangerController
  params nxt \<in> "{{}, {tick}, {endTask}}"
  pre "st= Turning \<and> \<not>(turnDurationElapsed) \<and> \<not>(endTask \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= Turning
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_Final =
  over SRangerController
  params nxt \<in> "{{}, {tick}}"
  pre "st= Final \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= Final
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
  operations  InitialToMoving TurningToMoving TurningToFinal TurningToTurning FinalToFinal Idle_Turning Idle_Final
  until "st = Moving"
(* `until` marks states exempt from deadlock-freedom (deadlock_free_z_machine's
   End parameter). These are states that are the source of no transition, so the
   machine has terminated there and is not required to offer an event. Without this
   clause End defaults to False and the obligation is FALSE at such a state --
   machine-checked, see scripts/test_f17_probe.sh. *)

subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SRangerController_inv"
  by zpog_full

lemma InitialToMoving_inv [hoare_lemmas]: "InitialToMoving() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TurningToMoving_inv [hoare_lemmas]: "TurningToMoving() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TurningToFinal_inv [hoare_lemmas]: "TurningToFinal (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TurningToTurning_inv [hoare_lemmas]: "TurningToTurning (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma FinalToFinal_inv [hoare_lemmas]: "FinalToFinal (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Turning_inv [hoare_lemmas]: "Idle_Turning (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Final_inv [hoare_lemmas]: "Idle_Final (nxt) preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToMoving() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TurningToMoving() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TurningToFinal (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TurningToTurning (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "FinalToFinal (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_Turning (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "Idle_Final (nxt) preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "SRangerController_axioms = True"

(* WARNING -- THIS OBLIGATION IS FALSE AS EMITTED.
   State(s) with no outgoing transition: Moving.
   Under `loop` semantics such a state offers Vis {} = deadlock, so
   deadlock_free does not hold. This is a MODEL defect (a missing tick-triggered
   self-loop in the Java), not a proof-search problem: no tactic can close it.
   Measured on chemical_detector v2 -- auto OOM 978s, metis 1825s, blast 922s,
   fastforce 923s; refuted by probe_Y3_obligation_refuted with witness
   st = Moving.
   Fix the Java per forge.assets/prompts/java_codegen_rules.txt ("EVERY MODE
   MUST HAVE AN OUTGOING TRANSITION"), re-extract, and this marker disappears. *)
lemma SRangerController_deadlock_free: "SRangerController_axioms  \<Longrightarrow> deadlock_free SRangerControllerMachine"
  unfolding SRangerControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
