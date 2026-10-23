theory Movement_Beh_B3_S1
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
   
enumtype St = Waiting | Going | Found | Avoiding | TryingAgain | AvoidingAgain | GettingOut | initial 
 


enumtype Evt = randomWalk | stop | flag | turn | resume | obstacle | changeDirection | shortRandomWalk 
 


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

record GasSample = c :: Chem i :: real
record_default GasSample
show_record GasSample

definition A :: "Angle set" where [simp]: "A = UNIV"

definition L :: "Loc set" where [simp]: "L = UNIV"





text \<open> function definition \<close>

consts analysis :: " GasSample list \<Rightarrow> Status"
consts intensity :: " GasSample list \<Rightarrow> real"
consts location :: " GasSample list \<Rightarrow> Angle"
consts peakAtOrAboveThr :: "unit \<Rightarrow> bool"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts since :: "'a \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts evasionStart :: "unit \<Rightarrow> int"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckPeriod :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts stuckDist :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
consts odometer :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)

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
  offered :: "Evt set"   (* FORK: DYNAMIC. The events the environment
                            offers THIS step. Written by no operation, pinned by
                            no invariant conjunct: a free input. Read by trigger
                            presence conjuncts (C2) and absence conjuncts.
                            A property OF THE ENVIRONMENT, not of the automaton. *)
  where inv:
    "tr \<noteq> []
                      \<and> (st = Waiting \<longrightarrow> listens = {turn, stop, resume})
                      \<and> (st = Going \<longrightarrow> listens = {resume, obstacle, turn, stop})
                      \<and> (st = Found \<longrightarrow> listens = {stop})
                      \<and> (st = Avoiding \<longrightarrow> listens = {turn, resume, stop})
                      \<and> (st = TryingAgain \<longrightarrow> listens = {turn, stop, obstacle, resume})
                      \<and> (st = AvoidingAgain \<longrightarrow> listens = {resume, stop})
                      \<and> (st = GettingOut \<longrightarrow> listens = {stop, resume, turn}) 
                      \<and> (st = Waiting \<longrightarrow> (offered = {} \<or> offered = {turn} \<or> offered = {stop} \<or> offered = {resume}))
                      \<and> (st = Going \<longrightarrow> (offered = {} \<or> offered = {resume} \<or> offered = {obstacle} \<or> offered = {turn} \<or> offered = {stop}))
                      \<and> (st = Found \<longrightarrow> (offered = {} \<or> offered = {stop}))
                      \<and> (st = Avoiding \<longrightarrow> (offered = {} \<or> offered = {turn} \<or> offered = {resume} \<or> offered = {stop}))
                      \<and> (st = TryingAgain \<longrightarrow> (offered = {} \<or> offered = {turn} \<or> offered = {stop} \<or> offered = {obstacle} \<or> offered = {resume}))
                      \<and> (st = AvoidingAgain \<longrightarrow> (offered = {} \<or> offered = {resume} \<or> offered = {stop}))
                      \<and> (st = GettingOut \<longrightarrow> (offered = {} \<or> offered = {stop} \<or> offered = {resume} \<or> offered = {turn}))
                      \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToWaiting =
  over Movement
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}"
  pre "st= initial"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {turn, stop, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation WaitingToFound =
  over Movement
  params nxt \<in> "{{}, {stop}}"
  pre "st= Waiting \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation WaitingToGoing =
  over Movement
  params a_input \<in> "A"  nxt \<in> "{{}, {resume}, {obstacle}, {turn}, {stop}}"
  pre "st= Waiting \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Going
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State Going]
         ,listens\<Zprime> = {resume, obstacle, turn, stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation WaitingToWaiting =
  over Movement
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}"
  pre "st= Waiting \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {turn, stop, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GoingToFound =
  over Movement
  params nxt \<in> "{{}, {stop}}"
  pre "st= Going \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GoingToGoing =
  over Movement
  params a_input \<in> "A"  nxt \<in> "{{}, {resume}, {obstacle}, {turn}, {stop}}"
  pre "st= Going \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Going
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State Going]
         ,listens\<Zprime> = {resume, obstacle, turn, stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GoingToAvoiding =
  over Movement
  params l_input \<in> "L"  nxt \<in> "{{}, {turn}, {resume}, {stop}}"
  pre "st= Going \<and> obstacle \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Avoiding
         ,l\<Zprime> =l_input
         ,d0\<Zprime> = odometer()
         ,tr\<Zprime> =tr @ [Event obstacle]@ [Event changeDirection] @ [State Avoiding]
         ,listens\<Zprime> = {turn, resume, stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GoingToWaiting =
  over Movement
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}"
  pre "st= Going \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {turn, stop, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation FoundToFound =
  over Movement
  params nxt \<in> "{{}, {stop}}"
  pre "st= Found \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop] @ [State Found]
         ,listens\<Zprime> = {stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingToFound =
  over Movement
  params nxt \<in> "{{}, {stop}}"
  pre "st= Avoiding \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingToTryingAgain =
  over Movement
  params a_input \<in> "A"  nxt \<in> "{{}, {turn}, {stop}, {obstacle}, {resume}}"
  pre "st= Avoiding \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TryingAgain
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State TryingAgain]
         ,listens\<Zprime> = {turn, stop, obstacle, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingToWaiting =
  over Movement
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}"
  pre "st= Avoiding \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {turn, stop, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TryingAgainToFound =
  over Movement
  params nxt \<in> "{{}, {stop}}"
  pre "st= TryingAgain \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TryingAgainToTryingAgain =
  over Movement
  params a_input \<in> "A"  nxt \<in> "{{}, {turn}, {stop}, {obstacle}, {resume}}"
  pre "st= TryingAgain \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= TryingAgain
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State TryingAgain]
         ,listens\<Zprime> = {turn, stop, obstacle, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TryingAgainToAvoidingAgain =
  over Movement
  params l_input \<in> "L"  nxt \<in> "{{}, {resume}, {stop}}"
  pre "st= TryingAgain \<and> obstacle \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= AvoidingAgain
         ,l\<Zprime> =l_input
         ,d1\<Zprime> = odometer()
         ,tr\<Zprime> =tr @ [Event obstacle] @ [State AvoidingAgain]
         ,listens\<Zprime> = {resume, stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation TryingAgainToWaiting =
  over Movement
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}"
  pre "st= TryingAgain \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {turn, stop, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingAgainToFound =
  over Movement
  params nxt \<in> "{{}, {stop}}"
  pre "st= AvoidingAgain \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingAgainToWaiting =
  over Movement
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}"
  pre "st= AvoidingAgain \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {turn, stop, resume}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingAgainToAvoiding =
  over Movement
  params nxt \<in> "{{}, {turn}, {resume}, {stop}}"
  pre "st= AvoidingAgain \<and> (since(evasionStart())<stuckPeriod() \<or> (d1 - d0)>stuckDist()) \<and> \<not>(stop \<in> offered) \<and> \<not>(resume \<in> offered) \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Avoiding
         ,d0\<Zprime> = odometer()
         ,tr\<Zprime> =tr @ [Event changeDirection] @ [State Avoiding]
         ,listens\<Zprime> = {turn, resume, stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation AvoidingAgainToGettingOut =
  over Movement
  params nxt \<in> "{{}, {stop}, {resume}, {turn}}"
  pre "st= AvoidingAgain \<and> \<not>since(evasionStart())<stuckPeriod() \<and> \<not>(d1 - d0)>stuckDist() \<and> \<not>((since(evasionStart())<stuckPeriod() \<or> (d1 - d0)>stuckDist())) \<and> \<not>(stop \<in> offered) \<and> \<not>(resume \<in> offered) \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= GettingOut
         ,tr\<Zprime> =tr @ [Event shortRandomWalk] @ [State GettingOut]
         ,listens\<Zprime> = {stop, resume, turn}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GettingOutToFound =
  over Movement
  params nxt \<in> "{{}, {stop}}"
  pre "st= GettingOut \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Found
         ,tr\<Zprime> =tr @ [Event stop]@ [Event flag] @ [State Found]
         ,listens\<Zprime> = {stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GettingOutToGoing =
  over Movement
  params a_input \<in> "A"  nxt \<in> "{{}, {resume}, {obstacle}, {turn}, {stop}}"
  pre "st= GettingOut \<and> turn \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Going
         ,a\<Zprime> =a_input
         ,tr\<Zprime> =tr @ [Event turn] @ [State Going]
         ,listens\<Zprime> = {resume, obstacle, turn, stop}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation GettingOutToWaiting =
  over Movement
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}"
  pre "st= GettingOut \<and> resume \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Waiting
         ,tr\<Zprime> =tr @ [Event resume]@ [Event randomWalk] @ [State Waiting]
         ,listens\<Zprime> = {turn, stop, resume}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_Avoiding =
  over Movement
  params nxt \<in> "{{}, {turn}, {resume}, {stop}}"
  pre "st= Avoiding \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= Avoiding
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_AvoidingAgain =
  over Movement
  params nxt \<in> "{{}, {resume}, {stop}}"
  pre "st= AvoidingAgain \<and> \<not>(stop \<in> offered) \<and> \<not>(resume \<in> offered) \<and> \<not>((since(evasionStart())<stuckPeriod() \<or> (d1 - d0)>stuckDist())) \<and> \<not>(\<not>since(evasionStart())<stuckPeriod() \<and> \<not>(d1 - d0)>stuckDist())"
  update "[st\<Zprime>= AvoidingAgain
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_Found =
  over Movement
  params nxt \<in> "{{}, {stop}}"
  pre "st= Found \<and> \<not>(stop \<in> offered)"
  update "[st\<Zprime>= Found
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

zoperation Idle_Going =
  over Movement
  params nxt \<in> "{{}, {resume}, {obstacle}, {turn}, {stop}}"
  pre "st= Going \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(obstacle \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= Going
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_TryingAgain =
  over Movement
  params nxt \<in> "{{}, {turn}, {stop}, {obstacle}, {resume}}"
  pre "st= TryingAgain \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(obstacle \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= TryingAgain
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_Waiting =
  over Movement
  params nxt \<in> "{{}, {turn}, {stop}, {resume}}"
  pre "st= Waiting \<and> \<not>(stop \<in> offered) \<and> \<not>(turn \<in> offered) \<and> \<not>(resume \<in> offered)"
  update "[st\<Zprime>= Waiting
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
  operations  InitialToWaiting WaitingToFound WaitingToGoing WaitingToWaiting GoingToFound GoingToGoing GoingToAvoiding GoingToWaiting FoundToFound AvoidingToFound AvoidingToTryingAgain AvoidingToWaiting TryingAgainToFound TryingAgainToTryingAgain TryingAgainToAvoidingAgain TryingAgainToWaiting AvoidingAgainToFound AvoidingAgainToWaiting AvoidingAgainToAvoiding AvoidingAgainToGettingOut GettingOutToFound GettingOutToGoing GettingOutToWaiting Idle_Avoiding Idle_AvoidingAgain Idle_Found Idle_GettingOut Idle_Going Idle_TryingAgain Idle_Waiting


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes Movement_inv"
  by zpog_full

lemma InitialToWaiting_inv [hoare_lemmas]: "InitialToWaiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma WaitingToFound_inv [hoare_lemmas]: "WaitingToFound (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma WaitingToGoing_inv [hoare_lemmas]: "WaitingToGoing (a_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma WaitingToWaiting_inv [hoare_lemmas]: "WaitingToWaiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GoingToFound_inv [hoare_lemmas]: "GoingToFound (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GoingToGoing_inv [hoare_lemmas]: "GoingToGoing (a_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GoingToAvoiding_inv [hoare_lemmas]: "GoingToAvoiding (l_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GoingToWaiting_inv [hoare_lemmas]: "GoingToWaiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma FoundToFound_inv [hoare_lemmas]: "FoundToFound (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AvoidingToFound_inv [hoare_lemmas]: "AvoidingToFound (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AvoidingToTryingAgain_inv [hoare_lemmas]: "AvoidingToTryingAgain (a_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma AvoidingToWaiting_inv [hoare_lemmas]: "AvoidingToWaiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TryingAgainToFound_inv [hoare_lemmas]: "TryingAgainToFound (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma TryingAgainToTryingAgain_inv [hoare_lemmas]: "TryingAgainToTryingAgain (a_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TryingAgainToAvoidingAgain_inv [hoare_lemmas]: "TryingAgainToAvoidingAgain (l_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma TryingAgainToWaiting_inv [hoare_lemmas]: "TryingAgainToWaiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AvoidingAgainToFound_inv [hoare_lemmas]: "AvoidingAgainToFound (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AvoidingAgainToWaiting_inv [hoare_lemmas]: "AvoidingAgainToWaiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AvoidingAgainToAvoiding_inv [hoare_lemmas]: "AvoidingAgainToAvoiding (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma AvoidingAgainToGettingOut_inv [hoare_lemmas]: "AvoidingAgainToGettingOut (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GettingOutToFound_inv [hoare_lemmas]: "GettingOutToFound (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma GettingOutToGoing_inv [hoare_lemmas]: "GettingOutToGoing (a_input, nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma GettingOutToWaiting_inv [hoare_lemmas]: "GettingOutToWaiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: see twin lemma above for rationale *)

lemma Idle_Avoiding_inv [hoare_lemmas]: "Idle_Avoiding (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)

lemma Idle_AvoidingAgain_inv [hoare_lemmas]: "Idle_AvoidingAgain (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)

lemma Idle_Found_inv [hoare_lemmas]: "Idle_Found (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)

lemma Idle_GettingOut_inv [hoare_lemmas]: "Idle_GettingOut (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)

lemma Idle_Going_inv [hoare_lemmas]: "Idle_Going (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)

lemma Idle_TryingAgain_inv [hoare_lemmas]: "Idle_TryingAgain (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)

lemma Idle_Waiting_inv [hoare_lemmas]: "Idle_Waiting (nxt) preserves Movement_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)



subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToWaiting (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "WaitingToFound (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "WaitingToGoing (a_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "WaitingToWaiting (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "GoingToFound (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "GoingToGoing (a_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "GoingToAvoiding (l_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "GoingToWaiting (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "FoundToFound (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "AvoidingToFound (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "AvoidingToTryingAgain (a_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "AvoidingToWaiting (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToFound (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToTryingAgain (a_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToAvoidingAgain (l_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "TryingAgainToWaiting (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToFound (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToWaiting (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToAvoiding (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "AvoidingAgainToGettingOut (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "GettingOutToFound (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "GettingOutToGoing (a_input, nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  
lemma "GettingOutToWaiting (nxt) preserves R1 under Movement_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "Movement_axioms = True"

lemma Movement_deadlock_free: "Movement_axioms  \<Longrightarrow> deadlock_free MovementMachine"
  unfolding MovementMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
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
