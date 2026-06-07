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
   
enumtype St = Moving | Turning | Halted | initial 
 


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
consts clockResetTime :: "unit \<Rightarrow> nat"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts turnDuration :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore SRangerController =
  st::"St"
  tr :: "(St, Evt) tag list"
  triggers:: "Evt set"
  where inv:
    "tr \<noteq> []"

subsection \<open> Operations \<close>

zoperation InitialToMoving =
  over SRangerController
  pre "st= initial"
  update "[st\<Zprime>= Moving
         ,tr\<Zprime> =tr  @ [State Moving]
         ,triggers\<Zprime> = {tick, endTask, obstacle}
         ]"
        
zoperation MovingToHalted =
  over SRangerController
  pre "st= Moving"
  update "[st\<Zprime>= Halted
         ,tr\<Zprime> =tr @ [Event endTask] @ [State Halted]
         ,triggers\<Zprime> = {tick}
         ]"
        
zoperation MovingToTurning =
  over SRangerController
  pre "st= Moving \<and> distance()\<le>obstacleThreshold()"
  update "[st\<Zprime>= Turning
         ,tr\<Zprime> =tr @ [Event obstacle] @ [State Turning]
         ,triggers\<Zprime> = {endTask, tick}
         ]"
        
zoperation MovingToMoving =
  over SRangerController
  pre "st= Moving"
  update "[st\<Zprime>= Moving
         ,tr\<Zprime> =tr @ [Event tick] @ [State Moving]
         ,triggers\<Zprime> = {tick, endTask, obstacle}
         ]"
        
zoperation TurningToHalted =
  over SRangerController
  pre "st= Turning"
  update "[st\<Zprime>= Halted
         ,tr\<Zprime> =tr @ [Event endTask] @ [State Halted]
         ,triggers\<Zprime> = {tick}
         ]"
        
zoperation TurningToMoving =
  over SRangerController
  pre "st= Turning \<and> since(clockResetTime())\<ge>turnDuration()"
  update "[st\<Zprime>= Moving
         ,tr\<Zprime> =tr  @ [State Moving]
         ,triggers\<Zprime> = {tick, endTask, obstacle}
         ]"
        
zoperation TurningToTurning =
  over SRangerController
  pre "st= Turning"
  update "[st\<Zprime>= Turning
         ,tr\<Zprime> =tr @ [Event tick] @ [State Turning]
         ,triggers\<Zprime> = {endTask, tick}
         ]"
        
zoperation HaltedToHalted =
  over SRangerController
  pre "st= Halted"
  update "[st\<Zprime>= Halted
         ,tr\<Zprime> =tr @ [Event tick] @ [State Halted]
         ,triggers\<Zprime> = {tick}
         ]"
        

  
definition Init :: "SRangerController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,triggers\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine SRangerControllerMachine =
  init Init
  invariant SRangerController_inv
  operations  InitialToMoving MovingToHalted MovingToTurning MovingToMoving TurningToHalted TurningToMoving TurningToTurning HaltedToHalted 


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SRangerController_inv"
  by zpog_full

lemma InitialToMoving_inv [hoare_lemmas]: "InitialToMoving() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MovingToHalted_inv [hoare_lemmas]: "MovingToHalted() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MovingToTurning_inv [hoare_lemmas]: "MovingToTurning() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MovingToMoving_inv [hoare_lemmas]: "MovingToMoving() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TurningToHalted_inv [hoare_lemmas]: "TurningToHalted() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TurningToMoving_inv [hoare_lemmas]: "TurningToMoving() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TurningToTurning_inv [hoare_lemmas]: "TurningToTurning() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma HaltedToHalted_inv [hoare_lemmas]: "HaltedToHalted() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToMoving() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MovingToHalted() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MovingToTurning() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MovingToMoving() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TurningToHalted() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TurningToMoving() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TurningToTurning() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "HaltedToHalted() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "SRangerController_axioms = True"

lemma SRangerController_deadlock_free: "SRangerController_axioms  \<Longrightarrow> deadlock_free SRangerControllerMachine"
  unfolding SRangerControllerMachine_def
  apply deadlock_free
  by (metis St.exhaust_disc)
  (* FORK: the deadlock_free closing tactic is selected by `hasPayloadDomain`
     (set in PART 2 when this machine emits a typed-payload domain set such as
     `SeqGs`). The residual goal after `apply deadlock_free` is
       \<And>st. st = M\<^sub>1 \<or> ... \<or> guarded-disjuncts
     and St.exhaust_disc is the enumtype discriminator exhaustion lemma that
     matches the bare `st = X` disjunct for each state. Two cases:
       * has payload domain -> `using St.exhaust_disc by auto`. metis HANGS
         (>10 min) when a state's ONLY transition consumes a typed payload
         (e.g. gas-analysis Reading: `params gs_input \<in> SeqGs`), because the
         enabledness disjunct is `\<exists>gs_input \<in> SeqGs. ...`. With the set
         emitted as a [simp] `= UNIV` definition (PART 2), `auto` discharges the
         existential and closes via the bare disjunct. Verified on
         chemical_detector (gas-analysis, ~15s).
       * no payload domain -> `by (metis St.exhaust_disc)` (the ICECCS2023 LRE
         tactic). `auto` TIMES OUT (>6 min) on LRE's real-arithmetic guards --
         confirmed by regression -- so LRE-style machines keep metis.
     Tried and rejected: `(cases st; simp_all)` / `(cases st; auto)` leave the
     guarded disjunct (e.g. `thr() \<le> ins`) open because the split selects the
     guarded disjunct instead of the bare one. *)
end
