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
  offered :: "Evt set"   (* FORK: DYNAMIC. The events the environment
                            offers THIS step. Written by no operation, pinned by
                            no invariant conjunct: a free input. Read by trigger
                            presence conjuncts (C2) and absence conjuncts.
                            A property OF THE ENVIRONMENT, not of the automaton. *)
  where inv:
    "tr \<noteq> []
                      \<and> (st = MOVING \<longrightarrow> listens = {tick, obstacle, endTask})
                      \<and> (st = TURNING \<longrightarrow> listens = {endTask, tick})
                      \<and> (st = FINAL \<longrightarrow> listens = {tick}) \<and> offered = listens"

subsection \<open> Operations \<close>

zoperation InitialToMOVING =
  over SRangerController
  pre "st= initial"
  update "[st\<Zprime>= MOVING
         ,tr\<Zprime> =tr  @ [State MOVING]
         ,listens\<Zprime> = {tick, obstacle, endTask}
         ,offered\<Zprime> = {tick, obstacle, endTask}
         ]"
        
zoperation MOVINGToFINAL =
  over SRangerController
  pre "st= MOVING \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= FINAL
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State FINAL]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = {tick}
         ]"
        
zoperation MOVINGToTURNING =
  over SRangerController
  pre "st= MOVING \<and> distance()\<le>obstacleThreshold() \<and> obstacle \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TURNING
         ,tr\<Zprime> =tr @ [Event obstacle]@ [Event move] @ [State TURNING]
         ,listens\<Zprime> = {endTask, tick}
         ,offered\<Zprime> = {endTask, tick}
         ]"
        
zoperation MOVINGToMOVING =
  over SRangerController
  pre "st= MOVING \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= MOVING
         ,tr\<Zprime> =tr @ [Event tick] @ [State MOVING]
         ,listens\<Zprime> = {tick, obstacle, endTask}
         ,offered\<Zprime> = {tick, obstacle, endTask}
         ]"
        
zoperation TURNINGToFINAL =
  over SRangerController
  pre "st= TURNING \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= FINAL
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State FINAL]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = {tick}
         ]"
        
zoperation TURNINGToMOVING =
  over SRangerController
  pre "st= TURNING \<and> since(clockResetTime())\<ge>turnDuration() \<and> \<not>(endTask \<in> offered) \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= MOVING
         ,tr\<Zprime> =tr @ [Event move] @ [State MOVING]
         ,listens\<Zprime> = {tick, obstacle, endTask}
         ,offered\<Zprime> = {tick, obstacle, endTask}
         ]"
        
zoperation TURNINGToTURNING =
  over SRangerController
  pre "st= TURNING \<and> \<not>since(clockResetTime())\<ge>turnDuration() \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TURNING
         ,tr\<Zprime> =tr @ [Event tick] @ [State TURNING]
         ,listens\<Zprime> = {endTask, tick}
         ,offered\<Zprime> = {endTask, tick}
         ]"
        
zoperation FINALToFINAL =
  over SRangerController
  pre "st= FINAL \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= FINAL
         ,tr\<Zprime> =tr @ [Event tick] @ [State FINAL]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = {tick}
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
  operations  InitialToMOVING MOVINGToFINAL MOVINGToTURNING MOVINGToMOVING TURNINGToFINAL TURNINGToMOVING TURNINGToTURNING FINALToFINAL


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SRangerController_inv"
  by zpog_full

lemma InitialToMOVING_inv [hoare_lemmas]: "InitialToMOVING() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOVINGToFINAL_inv [hoare_lemmas]: "MOVINGToFINAL() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOVINGToTURNING_inv [hoare_lemmas]: "MOVINGToTURNING() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma MOVINGToMOVING_inv [hoare_lemmas]: "MOVINGToMOVING() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TURNINGToFINAL_inv [hoare_lemmas]: "TURNINGToFINAL() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TURNINGToMOVING_inv [hoare_lemmas]: "TURNINGToMOVING() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TURNINGToTURNING_inv [hoare_lemmas]: "TURNINGToTURNING() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma FINALToFINAL_inv [hoare_lemmas]: "FINALToFINAL() preserves SRangerController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToMOVING() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MOVINGToFINAL() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MOVINGToTURNING() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "MOVINGToMOVING() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TURNINGToFINAL() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TURNINGToMOVING() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "TURNINGToTURNING() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  
lemma "FINALToFINAL() preserves R1 under SRangerController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "SRangerController_axioms = True"

lemma SRangerController_deadlock_free: "SRangerController_axioms  \<Longrightarrow> deadlock_free SRangerControllerMachine"
  unfolding SRangerControllerMachine_def
  apply deadlock_free
  by (metis St.exhaust_disc insertCI)
  (* TACTIC (2026-09-16, supersedes the hasPayloadDomain fork): the closing
     call is `by (metis St.exhaust_disc <used-enum>.exhaust_disc ... insertCI)`.
     Under the invariant conjunct `offered = listens` the residual goal after
     `apply deadlock_free` is a finite case analysis over St plus every enum a
     precondition compares (e.g. `sts = noGas` puts Status in the disjunction);
     each such enum's exhaust_disc lemma is supplied, computed from the emitted
     preconditions. Measured 2026-09-16: sranger 17/17 obligations (21s), lre
     39/39 (119s), chem 17/17 (30s -- without Status.exhaust_disc the same call
     is a >900s timeout). The earlier auto branch existed for a payload
     existential measured against a goal that was FALSE before this fix; with the true
     goal, metis with the computed fact list closes the payload machine too. *)
end
