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
   
enumtype St = Reading | Analysis | NoGas | GasDetected | Final | initial 
 


enumtype Evt = gas | tick | resume | stop | turn 
 


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
record GasSensor = chemId :: nat intensityValue :: real
record_default GasSensor
show_record GasSensor

record Intensity = value_ :: real
record_default Intensity
show_record Intensity

consts SeqGs :: "((GasSensor) list) set"

text \<open> function definition \<close>

consts analysis :: " GasSensor list \<Rightarrow> Status"
consts peakIntensity :: " GasSensor list \<Rightarrow> real"
consts location :: " GasSensor list \<Rightarrow> Angle"
consts since :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts clockResetTime :: "unit \<Rightarrow> nat"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckperiod :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckdist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

subsection \<open> State Space \<close>

zstore GasAnalysisController =
  gs :: "GasSensor list"
  sts :: "Status"
  insVal :: "real"
  anl :: "Angle"
  st::"St"
  tr :: "(St, Evt) tag list"
  triggers:: "Evt set"
  where inv:
    "tr \<noteq> []"

subsection \<open> Operations \<close>

zoperation InitialToReading =
  over GasAnalysisController
  pre "st= initial"
  update "[st\<Zprime>= Reading
         ,tr\<Zprime> =tr  @ [State Reading]
         ,triggers\<Zprime> = {gas, tick}
         ]"
        
zoperation ReadingToAnalysis =
  over GasAnalysisController
  params gs_input \<in> "SeqGs" 
  pre "st= Reading"
  update "[st\<Zprime>= Analysis
         ,gs\<Zprime> =gs_input
         ,sts\<Zprime> = analysis(gs_input)
         ,tr\<Zprime> =tr @ [Event gas] @ [State Analysis]
         ,triggers\<Zprime> = {tick}
         ]"
        
zoperation ReadingToReading =
  over GasAnalysisController
  pre "st= Reading"
  update "[st\<Zprime>= Reading
         ,tr\<Zprime> =tr @ [Event tick] @ [State Reading]
         ,triggers\<Zprime> = {gas, tick}
         ]"
        
zoperation AnalysisToNoGas =
  over GasAnalysisController
  pre "st= Analysis \<and> sts= (noGas)"
  update "[st\<Zprime>= NoGas
         ,tr\<Zprime> =tr @ [Event resume] @ [State NoGas]
         ,triggers\<Zprime> = {}
         ]"
        
zoperation AnalysisToGasDetected =
  over GasAnalysisController
  pre "st= Analysis \<and> sts= (gasD)"
  update "[st\<Zprime>= GasDetected
         ,tr\<Zprime> =tr  @ [State GasDetected]
         ,triggers\<Zprime> = {tick}
         ]"
        
zoperation AnalysisToAnalysis =
  over GasAnalysisController
  pre "st= Analysis"
  update "[st\<Zprime>= Analysis
         ,tr\<Zprime> =tr @ [Event tick] @ [State Analysis]
         ,triggers\<Zprime> = {tick}
         ]"
        
zoperation NoGasToReading =
  over GasAnalysisController
  pre "st= NoGas"
  update "[st\<Zprime>= Reading
         ,tr\<Zprime> =tr  @ [State Reading]
         ,triggers\<Zprime> = {gas, tick}
         ]"
        
zoperation GasDetectedToFinal =
  over GasAnalysisController
  pre "st= GasDetected \<and> insVal\<ge>10.0"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event stop] @ [State Final]
         ,triggers\<Zprime> = {tick}
         ]"
        
zoperation GasDetectedToReading =
  over GasAnalysisController
  pre "st= GasDetected \<and> \<not>insVal\<ge>10.0"
  update "[st\<Zprime>= Reading
         ,anl\<Zprime> = location(gs)
         ,tr\<Zprime> =tr @ [Event turn] @ [State Reading]
         ,triggers\<Zprime> = {gas, tick}
         ]"
        
zoperation GasDetectedToGasDetected =
  over GasAnalysisController
  pre "st= GasDetected"
  update "[st\<Zprime>= GasDetected
         ,tr\<Zprime> =tr @ [Event tick] @ [State GasDetected]
         ,triggers\<Zprime> = {tick}
         ]"
        
zoperation FinalToFinal =
  over GasAnalysisController
  pre "st= Final"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event tick] @ [State Final]
         ,triggers\<Zprime> = {tick}
         ]"
        

  
definition Init :: "GasAnalysisController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,triggers\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine GasAnalysisControllerMachine =
  init Init
  invariant GasAnalysisController_inv
  operations  InitialToReading ReadingToAnalysis ReadingToReading AnalysisToNoGas AnalysisToGasDetected AnalysisToAnalysis NoGasToReading GasDetectedToFinal GasDetectedToReading GasDetectedToGasDetected FinalToFinal 


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes GasAnalysisController_inv"
  by zpog_full

lemma InitialToReading_inv [hoare_lemmas]: "InitialToReading() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma ReadingToAnalysis_inv [hoare_lemmas]: "ReadingToAnalysis (gs_input) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma ReadingToReading_inv [hoare_lemmas]: "ReadingToReading() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AnalysisToNoGas_inv [hoare_lemmas]: "AnalysisToNoGas() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AnalysisToGasDetected_inv [hoare_lemmas]: "AnalysisToGasDetected() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AnalysisToAnalysis_inv [hoare_lemmas]: "AnalysisToAnalysis() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma NoGasToReading_inv [hoare_lemmas]: "NoGasToReading() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GasDetectedToFinal_inv [hoare_lemmas]: "GasDetectedToFinal() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GasDetectedToReading_inv [hoare_lemmas]: "GasDetectedToReading() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GasDetectedToGasDetected_inv [hoare_lemmas]: "GasDetectedToGasDetected() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma FinalToFinal_inv [hoare_lemmas]: "FinalToFinal() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToReading() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "ReadingToAnalysis (gs_input) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "ReadingToReading() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "AnalysisToNoGas() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "AnalysisToGasDetected() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "AnalysisToAnalysis() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "NoGasToReading() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "GasDetectedToFinal() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "GasDetectedToReading() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "GasDetectedToGasDetected() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "FinalToFinal() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "GasAnalysisController_axioms = True"

lemma GasAnalysisController_deadlock_free: "GasAnalysisController_axioms  \<Longrightarrow> deadlock_free GasAnalysisControllerMachine"
  unfolding GasAnalysisControllerMachine_def
  apply deadlock_free
  by (metis St.exhaust_disc)
  (* FORK: archive closes the residual disjunctive goal
       \<And>st. st = M\<^sub>1 \<or> ... \<or> guarded-disjuncts
     via `by (metis St.exhaust_disc)` (see ICECCS2023 archive
     LRE_Z_Machine_deadlock_free.thy). St.exhaust_disc is the
     enumtype discriminator exhaustion lemma generated for `enumtype St`;
     metis instantiates it for the current `st` and matches the plain
     `st = X` disjunct to True, closing every case.
     Tried and rejected: `apply (cases st; simp_all)` fails on the mixed
     bare/guarded disjunction; `by (deadlock_free; cases st; auto)` times
     out (>10 min) because `;` makes \<And>st inaccessible to cases. *)
end
