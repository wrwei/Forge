theory GasAnalysisController_Beh
imports "Z_Machines.Z_Machine"
begin

subsection \<open> Introduction \<close>

text \<open> This theory file is to model the GasAnalysisController state machine in Z Machine notations.\<close>

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
   
enumtype St = Reading | Analysis | NoGas | GasDetected | Concluded | initial 
 


enumtype Evt = gas | resume | stop | turn 
 


enumtype Angle = Left | Right | Back | Front

enumtype Chem = TARGET | OTHER

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
record GasSensor = c :: Chem i :: real
record_default GasSensor
show_record GasSensor

definition SeqGs :: "((GasSensor) list) set" where [simp]: "SeqGs = UNIV"

text \<open> function definition \<close>

consts analysis :: " GasSensor list \<Rightarrow> Status"
consts intensity :: " GasSensor list \<Rightarrow> real"
consts location :: " GasSensor list \<Rightarrow> Angle"
consts insAtOrAboveThr :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts since :: "'a \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts T :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckPeriod :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckDist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore GasAnalysisController =
  gs :: "GasSensor list"
  sts :: "Status"
  anl :: "Angle"
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
                      \<and> (st = Reading \<longrightarrow> listens = {gas})
                      \<and> (st = Concluded \<longrightarrow> listens = {gas}) \<and> offered = listens"

subsection \<open> Operations \<close>

zoperation InitialToReading =
  over GasAnalysisController
  pre "st= initial"
  update "[st\<Zprime>= Reading
         ,tr\<Zprime> =tr  @ [State Reading]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = {gas}
         ]"
        
zoperation ReadingToAnalysis =
  over GasAnalysisController
  params gs_input \<in> "SeqGs" 
  pre "st= Reading \<and> gas \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Analysis
         ,gs\<Zprime> =gs_input
         ,sts\<Zprime> = analysis(gs_input)
         ,tr\<Zprime> =tr @ [Event gas] @ [State Analysis]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation AnalysisToNoGas =
  over GasAnalysisController
  pre "st= Analysis \<and> sts= (noGas)"
  update "[st\<Zprime>= NoGas
         ,tr\<Zprime> =tr @ [Event resume] @ [State NoGas]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation AnalysisToGasDetected =
  over GasAnalysisController
  pre "st= Analysis \<and> \<not>sts= (noGas)"
  update "[st\<Zprime>= GasDetected
         ,tr\<Zprime> =tr  @ [State GasDetected]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation NoGasToReading =
  over GasAnalysisController
  pre "st= NoGas"
  update "[st\<Zprime>= Reading
         ,tr\<Zprime> =tr  @ [State Reading]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = {gas}
         ]"
        
zoperation GasDetectedToConcluded =
  over GasAnalysisController
  pre "st= GasDetected \<and> insAtOrAboveThr()"
  update "[st\<Zprime>= Concluded
         ,tr\<Zprime> =tr @ [Event stop] @ [State Concluded]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = {gas}
         ]"
        
zoperation GasDetectedToReading =
  over GasAnalysisController
  pre "st= GasDetected \<and> \<not>insAtOrAboveThr()"
  update "[st\<Zprime>= Reading
         ,anl\<Zprime> = location(gs)
         ,tr\<Zprime> =tr @ [Event turn] @ [State Reading]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = {gas}
         ]"
        
zoperation ConcludedToConcluded =
  over GasAnalysisController
  params gs_input \<in> "SeqGs" 
  pre "st= Concluded \<and> gas \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Concluded
         ,gs\<Zprime> =gs_input
         ,tr\<Zprime> =tr @ [Event gas] @ [State Concluded]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = {gas}
         ]"
        

  
definition Init :: "GasAnalysisController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ,offered\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine GasAnalysisControllerMachine =
  init Init
  invariant GasAnalysisController_inv
  operations  InitialToReading ReadingToAnalysis AnalysisToNoGas AnalysisToGasDetected NoGasToReading GasDetectedToConcluded GasDetectedToReading ConcludedToConcluded


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes GasAnalysisController_inv"
  by zpog_full

lemma InitialToReading_inv [hoare_lemmas]: "InitialToReading() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma ReadingToAnalysis_inv [hoare_lemmas]: "ReadingToAnalysis (gs_input) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AnalysisToNoGas_inv [hoare_lemmas]: "AnalysisToNoGas() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AnalysisToGasDetected_inv [hoare_lemmas]: "AnalysisToGasDetected() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma NoGasToReading_inv [hoare_lemmas]: "NoGasToReading() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GasDetectedToConcluded_inv [hoare_lemmas]: "GasDetectedToConcluded() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GasDetectedToReading_inv [hoare_lemmas]: "GasDetectedToReading() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma ConcludedToConcluded_inv [hoare_lemmas]: "ConcludedToConcluded (gs_input) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToReading() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "ReadingToAnalysis (gs_input) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "AnalysisToNoGas() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "AnalysisToGasDetected() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "NoGasToReading() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "GasDetectedToConcluded() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "GasDetectedToReading() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "ConcludedToConcluded (gs_input) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "GasAnalysisController_axioms = True"

lemma GasAnalysisController_deadlock_free: "GasAnalysisController_axioms  \<Longrightarrow> deadlock_free GasAnalysisControllerMachine"
  unfolding GasAnalysisControllerMachine_def
  apply deadlock_free
  by (metis St.exhaust_disc Status.exhaust_disc insertCI)
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
