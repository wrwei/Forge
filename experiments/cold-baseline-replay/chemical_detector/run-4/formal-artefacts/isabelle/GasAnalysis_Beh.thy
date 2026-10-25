theory GasAnalysis_Beh
imports "Z_Machines.Z_Machine"
begin

subsection \<open> Introduction \<close>

text \<open> This theory file is to model the GasAnalysis state machine in Z Machine notations.\<close>

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
   
enumtype St = Reading | Analysis | NoGas | GasDetected | Final | initial 
 


enumtype Evt = gas | resume | stop | turn 
 


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

definition SeqGs :: "((GasSensor) list) set" where [simp]: "SeqGs = UNIV"

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

zstore GasAnalysis =
  gs :: "GasSensor list"
  sts :: "Status"
  anl :: "Angle"
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
                      \<and> (st = Reading \<longrightarrow> listens = {gas})
                                                                                \<and> (st = Reading \<longrightarrow> (offered = {} \<or> offered = {gas}))
                                                                                \<and> (st = Analysis \<longrightarrow> (offered = {}))
                                                                                \<and> (st = NoGas \<longrightarrow> (offered = {}))
                                                                                \<and> (st = GasDetected \<longrightarrow> (offered = {}))
                                                                                \<and> (st = Final \<longrightarrow> (offered = {}))
                                                                                \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToReading =
  over GasAnalysis
  params nxt \<in> "{{}, {gas}}" 
  pre "st= initial"
  update "[st\<Zprime>= Reading
         ,tr\<Zprime> =tr  @ [State Reading]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation ReadingToAnalysis =
  over GasAnalysis
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
  over GasAnalysis
  pre "st= Analysis \<and> sts= (noGas)"
  update "[st\<Zprime>= NoGas
         ,tr\<Zprime> =tr @ [Event resume] @ [State NoGas]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation AnalysisToGasDetected =
  over GasAnalysis
  pre "st= Analysis \<and> sts= (gasD) \<and> \<not>sts= (noGas)"
  update "[st\<Zprime>= GasDetected
         ,tr\<Zprime> =tr  @ [State GasDetected]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation NoGasToReading =
  over GasAnalysis
  params nxt \<in> "{{}, {gas}}" 
  pre "st= NoGas"
  update "[st\<Zprime>= Reading
         ,tr\<Zprime> =tr  @ [State Reading]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GasDetectedToFinal =
  over GasAnalysis
  pre "st= GasDetected \<and> insAboveThr()"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event stop] @ [State Final]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"
        
zoperation GasDetectedToReading =
  over GasAnalysis
  params nxt \<in> "{{}, {gas}}" 
  pre "st= GasDetected \<and> \<not>insAboveThr()"
  update "[st\<Zprime>= Reading
         ,anl\<Zprime> = location(gs)
         ,tr\<Zprime> =tr @ [Event turn] @ [State Reading]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_Reading =
  over GasAnalysis
  params nxt \<in> "{{}, {gas}}"
  pre "st= Reading \<and> \<not>(gas \<in> offered)"
  update "[st\<Zprime>= Reading
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_Analysis =
  over GasAnalysis
  pre "st= Analysis \<and> \<not>(sts= (noGas)) \<and> \<not>(sts= (gasD))"
  update "[st\<Zprime>= Analysis
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = {}
         ]"

definition Init :: "GasAnalysis subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ,offered\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine GasAnalysisMachine =
  init Init
  invariant GasAnalysis_inv
  operations  InitialToReading ReadingToAnalysis AnalysisToNoGas AnalysisToGasDetected NoGasToReading GasDetectedToFinal GasDetectedToReading Idle_Reading Idle_Analysis
  until "st = Final"
(* `until` marks states exempt from deadlock-freedom (deadlock_free_z_machine's
   End parameter). These are states that are the source of no transition, so the
   machine has terminated there and is not required to offer an event. Without this
   clause End defaults to False and the obligation is FALSE at such a state --
   machine-checked, see scripts/test_f17_probe.sh. *)

subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes GasAnalysis_inv"
  by zpog_full

lemma InitialToReading_inv [hoare_lemmas]: "InitialToReading (nxt) preserves GasAnalysis_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma ReadingToAnalysis_inv [hoare_lemmas]: "ReadingToAnalysis (gs_input) preserves GasAnalysis_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AnalysisToNoGas_inv [hoare_lemmas]: "AnalysisToNoGas() preserves GasAnalysis_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AnalysisToGasDetected_inv [hoare_lemmas]: "AnalysisToGasDetected() preserves GasAnalysis_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma NoGasToReading_inv [hoare_lemmas]: "NoGasToReading (nxt) preserves GasAnalysis_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GasDetectedToFinal_inv [hoare_lemmas]: "GasDetectedToFinal() preserves GasAnalysis_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GasDetectedToReading_inv [hoare_lemmas]: "GasDetectedToReading (nxt) preserves GasAnalysis_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Reading_inv [hoare_lemmas]: "Idle_Reading (nxt) preserves GasAnalysis_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Analysis_inv [hoare_lemmas]: "Idle_Analysis() preserves GasAnalysis_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToReading (nxt) preserves R1 under GasAnalysis_inv"
  by (zpog_full; auto)
  
lemma "ReadingToAnalysis (gs_input) preserves R1 under GasAnalysis_inv"
  by (zpog_full; auto)
  
lemma "AnalysisToNoGas() preserves R1 under GasAnalysis_inv"
  by (zpog_full; auto)
  
lemma "AnalysisToGasDetected() preserves R1 under GasAnalysis_inv"
  by (zpog_full; auto)
  
lemma "NoGasToReading (nxt) preserves R1 under GasAnalysis_inv"
  by (zpog_full; auto)
  
lemma "GasDetectedToFinal() preserves R1 under GasAnalysis_inv"
  by (zpog_full; auto)
  
lemma "GasDetectedToReading (nxt) preserves R1 under GasAnalysis_inv"
  by (zpog_full; auto)
  
lemma "Idle_Reading (nxt) preserves R1 under GasAnalysis_inv"
  by (zpog_full; auto)
  
lemma "Idle_Analysis() preserves R1 under GasAnalysis_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "GasAnalysis_axioms = True"

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
lemma GasAnalysis_deadlock_free: "GasAnalysis_axioms  \<Longrightarrow> deadlock_free GasAnalysisMachine"
  unfolding GasAnalysisMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
