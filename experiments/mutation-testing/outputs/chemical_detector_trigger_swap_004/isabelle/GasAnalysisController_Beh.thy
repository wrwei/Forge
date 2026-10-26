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

zstore GasAnalysisController =
  gs :: "GasSensor list"
  sts :: "Status"
  insVal :: "real"
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
                      \<and> (st = Reading \<longrightarrow> listens = {gas, tick})
                      \<and> (st = Analysis \<longrightarrow> listens = {tick})
                      \<and> (st = GasDetected \<longrightarrow> listens = {tick})
                      \<and> (st = Final \<longrightarrow> listens = {gas})
                                                                              \<and> (st = Reading \<longrightarrow> (offered = {} \<or> offered = {gas} \<or> offered = {tick}))
                                                                              \<and> (st = Analysis \<longrightarrow> (offered = {} \<or> offered = {tick}))
                                                                              \<and> (st = NoGas \<longrightarrow> (offered = {}))
                                                                              \<and> (st = GasDetected \<longrightarrow> (offered = {} \<or> offered = {tick}))
                                                                              \<and> (st = Final \<longrightarrow> (offered = {} \<or> offered = {gas}))
                                                                              \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToReading =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}, {tick}}" 
  pre "st= initial"
  update "[st\<Zprime>= Reading
         ,tr\<Zprime> =tr  @ [State Reading]
         ,listens\<Zprime> = {gas, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation ReadingToAnalysis =
  over GasAnalysisController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= Reading \<and> gas \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Analysis
         ,sts\<Zprime> = analysis(gs)
         ,tr\<Zprime> =tr @ [Event gas] @ [State Analysis]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation ReadingToReading =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}, {tick}}" 
  pre "st= Reading \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Reading
         ,tr\<Zprime> =tr @ [Event tick] @ [State Reading]
         ,listens\<Zprime> = {gas, tick}
         ,offered\<Zprime> = nxt
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
  params nxt \<in> "{{}, {tick}}" 
  pre "st= Analysis \<and> sts= (gasD) \<and> \<not>sts= (noGas)"
  update "[st\<Zprime>= GasDetected
         ,insVal\<Zprime> = peakIntensity(gs)
         ,tr\<Zprime> =tr  @ [State GasDetected]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AnalysisToAnalysis =
  over GasAnalysisController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= Analysis \<and> \<not>sts= (noGas) \<and> \<not>sts= (gasD) \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Analysis
         ,tr\<Zprime> =tr @ [Event tick] @ [State Analysis]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation NoGasToReading =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}, {tick}}" 
  pre "st= NoGas"
  update "[st\<Zprime>= Reading
         ,tr\<Zprime> =tr  @ [State Reading]
         ,listens\<Zprime> = {gas, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GasDetectedToFinal =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}}" 
  pre "st= GasDetected \<and> insVal\<ge>10.0"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event stop] @ [State Final]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GasDetectedToReading =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}, {tick}}" 
  pre "st= GasDetected \<and> \<not>insVal\<ge>10.0"
  update "[st\<Zprime>= Reading
         ,anl\<Zprime> = location(gs)
         ,tr\<Zprime> =tr @ [Event turn] @ [State Reading]
         ,listens\<Zprime> = {gas, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GasDetectedToGasDetected =
  over GasAnalysisController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= GasDetected \<and> \<not>insVal\<ge>10.0 \<and> insVal\<ge>10.0 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= GasDetected
         ,tr\<Zprime> =tr @ [Event tick] @ [State GasDetected]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation FinalToFinal =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}}" 
  pre "st= Final \<and> gas \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event gas] @ [State Final]
         ,listens\<Zprime> = {gas}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_Reading =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}, {tick}}"
  pre "st= Reading \<and> \<not>(gas \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= Reading
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_Analysis =
  over GasAnalysisController
  params nxt \<in> "{{}, {tick}}"
  pre "st= Analysis \<and> \<not>(sts= (noGas)) \<and> \<not>(sts= (gasD)) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= Analysis
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_Final =
  over GasAnalysisController
  params nxt \<in> "{{}, {gas}}"
  pre "st= Final \<and> \<not>(gas \<in> offered)"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
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
  operations  InitialToReading ReadingToAnalysis ReadingToReading AnalysisToNoGas AnalysisToGasDetected AnalysisToAnalysis NoGasToReading GasDetectedToFinal GasDetectedToReading GasDetectedToGasDetected FinalToFinal Idle_Reading Idle_Analysis Idle_Final


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes GasAnalysisController_inv"
  by zpog_full

lemma InitialToReading_inv [hoare_lemmas]: "InitialToReading (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma ReadingToAnalysis_inv [hoare_lemmas]: "ReadingToAnalysis (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma ReadingToReading_inv [hoare_lemmas]: "ReadingToReading (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AnalysisToNoGas_inv [hoare_lemmas]: "AnalysisToNoGas() preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AnalysisToGasDetected_inv [hoare_lemmas]: "AnalysisToGasDetected (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AnalysisToAnalysis_inv [hoare_lemmas]: "AnalysisToAnalysis (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma NoGasToReading_inv [hoare_lemmas]: "NoGasToReading (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GasDetectedToFinal_inv [hoare_lemmas]: "GasDetectedToFinal (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GasDetectedToReading_inv [hoare_lemmas]: "GasDetectedToReading (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GasDetectedToGasDetected_inv [hoare_lemmas]: "GasDetectedToGasDetected (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma FinalToFinal_inv [hoare_lemmas]: "FinalToFinal (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Reading_inv [hoare_lemmas]: "Idle_Reading (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Analysis_inv [hoare_lemmas]: "Idle_Analysis (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Final_inv [hoare_lemmas]: "Idle_Final (nxt) preserves GasAnalysisController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToReading (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "ReadingToAnalysis (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "ReadingToReading (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "AnalysisToNoGas() preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "AnalysisToGasDetected (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "AnalysisToAnalysis (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "NoGasToReading (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "GasDetectedToFinal (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "GasDetectedToReading (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "GasDetectedToGasDetected (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "FinalToFinal (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "Idle_Reading (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "Idle_Analysis (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  
lemma "Idle_Final (nxt) preserves R1 under GasAnalysisController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "GasAnalysisController_axioms = True"

lemma GasAnalysisController_deadlock_free: "GasAnalysisController_axioms  \<Longrightarrow> deadlock_free GasAnalysisControllerMachine"
  unfolding GasAnalysisControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
