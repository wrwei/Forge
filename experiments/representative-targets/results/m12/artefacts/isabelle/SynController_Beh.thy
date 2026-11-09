theory SynController_Beh
imports "Z_Machines.Z_Machine"
begin

subsection \<open> Introduction \<close>

text \<open> This theory file is to model the SynController state machine in Z Machine notations.\<close>

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
   
enumtype St = M1 | M2 | M3 | M4 | M5 | M6 | M7 | M8 | M9 | M10 | M11 | Final | initial 
 


enumtype Evt = endTask | move | advance | retreat | tick 
 


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

consts reading1 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold1 :: "unit \<Rightarrow> real" where "threshold1 u = 0.75"  (* named constant; value from the model *)
consts reading2 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold2 :: "unit \<Rightarrow> real" where "threshold2 u = 1"  (* named constant; value from the model *)
consts reading3 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold3 :: "unit \<Rightarrow> real" where "threshold3 u = 1.25"  (* named constant; value from the model *)
consts reading4 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold4 :: "unit \<Rightarrow> real" where "threshold4 u = 1.5"  (* named constant; value from the model *)
consts reading5 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold5 :: "unit \<Rightarrow> real" where "threshold5 u = 1.75"  (* named constant; value from the model *)
consts reading6 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold6 :: "unit \<Rightarrow> real" where "threshold6 u = 2"  (* named constant; value from the model *)
consts reading7 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold7 :: "unit \<Rightarrow> real" where "threshold7 u = 2.25"  (* named constant; value from the model *)
consts reading8 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold8 :: "unit \<Rightarrow> real" where "threshold8 u = 2.5"  (* named constant; value from the model *)
consts reading9 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold9 :: "unit \<Rightarrow> real" where "threshold9 u = 2.75"  (* named constant; value from the model *)
consts reading10 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold10 :: "unit \<Rightarrow> real" where "threshold10 u = 3"  (* named constant; value from the model *)
consts reading11 :: "unit \<Rightarrow> real"  (* FORK: auto-emitted for CallExp identifier used in guards *)
definition threshold11 :: "unit \<Rightarrow> real" where "threshold11 u = 3.25"  (* named constant; value from the model *)

subsection \<open> State Space \<close>

zstore SynController =
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
                      \<and> (st = M1 \<longrightarrow> listens = {tick, advance, retreat, endTask})
                      \<and> (st = M2 \<longrightarrow> listens = {retreat, tick, advance, endTask})
                      \<and> (st = M3 \<longrightarrow> listens = {advance, retreat, tick, endTask})
                      \<and> (st = M4 \<longrightarrow> listens = {advance, retreat, endTask, tick})
                      \<and> (st = M5 \<longrightarrow> listens = {endTask, advance, retreat, tick})
                      \<and> (st = M6 \<longrightarrow> listens = {endTask, advance, tick, retreat})
                      \<and> (st = M7 \<longrightarrow> listens = {advance, endTask, retreat, tick})
                      \<and> (st = M8 \<longrightarrow> listens = {tick, retreat, advance, endTask})
                      \<and> (st = M9 \<longrightarrow> listens = {retreat, advance, tick, endTask})
                      \<and> (st = M10 \<longrightarrow> listens = {retreat, endTask, advance, tick})
                      \<and> (st = M11 \<longrightarrow> listens = {tick, advance, retreat, endTask})
                      \<and> (st = Final \<longrightarrow> listens = {tick})
                                                                               \<and> (st = M1 \<longrightarrow> (offered = {} \<or> offered = {tick} \<or> offered = {advance} \<or> offered = {retreat} \<or> offered = {endTask}))
                                                                               \<and> (st = M2 \<longrightarrow> (offered = {} \<or> offered = {retreat} \<or> offered = {tick} \<or> offered = {advance} \<or> offered = {endTask}))
                                                                               \<and> (st = M3 \<longrightarrow> (offered = {} \<or> offered = {advance} \<or> offered = {retreat} \<or> offered = {tick} \<or> offered = {endTask}))
                                                                               \<and> (st = M4 \<longrightarrow> (offered = {} \<or> offered = {advance} \<or> offered = {retreat} \<or> offered = {endTask} \<or> offered = {tick}))
                                                                               \<and> (st = M5 \<longrightarrow> (offered = {} \<or> offered = {endTask} \<or> offered = {advance} \<or> offered = {retreat} \<or> offered = {tick}))
                                                                               \<and> (st = M6 \<longrightarrow> (offered = {} \<or> offered = {endTask} \<or> offered = {advance} \<or> offered = {tick} \<or> offered = {retreat}))
                                                                               \<and> (st = M7 \<longrightarrow> (offered = {} \<or> offered = {advance} \<or> offered = {endTask} \<or> offered = {retreat} \<or> offered = {tick}))
                                                                               \<and> (st = M8 \<longrightarrow> (offered = {} \<or> offered = {tick} \<or> offered = {retreat} \<or> offered = {advance} \<or> offered = {endTask}))
                                                                               \<and> (st = M9 \<longrightarrow> (offered = {} \<or> offered = {retreat} \<or> offered = {advance} \<or> offered = {tick} \<or> offered = {endTask}))
                                                                               \<and> (st = M10 \<longrightarrow> (offered = {} \<or> offered = {retreat} \<or> offered = {endTask} \<or> offered = {advance} \<or> offered = {tick}))
                                                                               \<and> (st = M11 \<longrightarrow> (offered = {} \<or> offered = {tick} \<or> offered = {advance} \<or> offered = {retreat} \<or> offered = {endTask}))
                                                                               \<and> (st = Final \<longrightarrow> (offered = {} \<or> offered = {tick}))
                                                                               \<and> (st = initial \<longrightarrow> (offered = {}))"

subsection \<open> Operations \<close>

zoperation InitialToM1 =
  over SynController
  params nxt \<in> "{{}, {tick}, {advance}, {retreat}, {endTask}}" 
  pre "st= initial"
  update "[st\<Zprime>= M1
         ,tr\<Zprime> =tr  @ [State M1]
         ,listens\<Zprime> = {tick, advance, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M1ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M1 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M1ToM2 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {advance}, {endTask}}" 
  pre "st= M1 \<and> reading1()\<le>threshold1() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M2
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M2]
         ,listens\<Zprime> = {retreat, tick, advance, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M1ToM1 =
  over SynController
  params nxt \<in> "{{}, {tick}, {advance}, {retreat}, {endTask}}" 
  pre "st= M1 \<and> \<not>reading1()\<le>threshold1() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M1
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M1]
         ,listens\<Zprime> = {tick, advance, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M1ToM1_1 =
  over SynController
  params nxt \<in> "{{}, {tick}, {advance}, {retreat}, {endTask}}" 
  pre "st= M1 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M1
         ,tr\<Zprime> =tr @ [Event tick] @ [State M1]
         ,listens\<Zprime> = {tick, advance, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M2ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M2 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M2ToM3 =
  over SynController
  params nxt \<in> "{{}, {advance}, {retreat}, {tick}, {endTask}}" 
  pre "st= M2 \<and> reading2()\<le>threshold2() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M3
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M3]
         ,listens\<Zprime> = {advance, retreat, tick, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M2ToM1 =
  over SynController
  params nxt \<in> "{{}, {tick}, {advance}, {retreat}, {endTask}}" 
  pre "st= M2 \<and> \<not>reading2()\<le>threshold2() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M1
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M1]
         ,listens\<Zprime> = {tick, advance, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M2ToM2 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {advance}, {endTask}}" 
  pre "st= M2 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M2
         ,tr\<Zprime> =tr @ [Event tick] @ [State M2]
         ,listens\<Zprime> = {retreat, tick, advance, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M3ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M3 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M3ToM4 =
  over SynController
  params nxt \<in> "{{}, {advance}, {retreat}, {endTask}, {tick}}" 
  pre "st= M3 \<and> reading3()\<le>threshold3() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M4
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M4]
         ,listens\<Zprime> = {advance, retreat, endTask, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M3ToM2 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {advance}, {endTask}}" 
  pre "st= M3 \<and> \<not>reading3()\<le>threshold3() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M2
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M2]
         ,listens\<Zprime> = {retreat, tick, advance, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M3ToM3 =
  over SynController
  params nxt \<in> "{{}, {advance}, {retreat}, {tick}, {endTask}}" 
  pre "st= M3 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M3
         ,tr\<Zprime> =tr @ [Event tick] @ [State M3]
         ,listens\<Zprime> = {advance, retreat, tick, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M4ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M4 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M4ToM5 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {advance}, {retreat}, {tick}}" 
  pre "st= M4 \<and> reading4()\<le>threshold4() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M5
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M5]
         ,listens\<Zprime> = {endTask, advance, retreat, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M4ToM3 =
  over SynController
  params nxt \<in> "{{}, {advance}, {retreat}, {tick}, {endTask}}" 
  pre "st= M4 \<and> \<not>reading4()\<le>threshold4() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M3
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M3]
         ,listens\<Zprime> = {advance, retreat, tick, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M4ToM4 =
  over SynController
  params nxt \<in> "{{}, {advance}, {retreat}, {endTask}, {tick}}" 
  pre "st= M4 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M4
         ,tr\<Zprime> =tr @ [Event tick] @ [State M4]
         ,listens\<Zprime> = {advance, retreat, endTask, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M5ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M5 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M5ToM6 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {advance}, {tick}, {retreat}}" 
  pre "st= M5 \<and> reading5()\<le>threshold5() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M6
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M6]
         ,listens\<Zprime> = {endTask, advance, tick, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M5ToM4 =
  over SynController
  params nxt \<in> "{{}, {advance}, {retreat}, {endTask}, {tick}}" 
  pre "st= M5 \<and> \<not>reading5()\<le>threshold5() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M4
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M4]
         ,listens\<Zprime> = {advance, retreat, endTask, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M5ToM5 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {advance}, {retreat}, {tick}}" 
  pre "st= M5 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M5
         ,tr\<Zprime> =tr @ [Event tick] @ [State M5]
         ,listens\<Zprime> = {endTask, advance, retreat, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M6ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M6 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M6ToM7 =
  over SynController
  params nxt \<in> "{{}, {advance}, {endTask}, {retreat}, {tick}}" 
  pre "st= M6 \<and> reading6()\<le>threshold6() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M7
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M7]
         ,listens\<Zprime> = {advance, endTask, retreat, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M6ToM5 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {advance}, {retreat}, {tick}}" 
  pre "st= M6 \<and> \<not>reading6()\<le>threshold6() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M5
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M5]
         ,listens\<Zprime> = {endTask, advance, retreat, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M6ToM6 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {advance}, {tick}, {retreat}}" 
  pre "st= M6 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M6
         ,tr\<Zprime> =tr @ [Event tick] @ [State M6]
         ,listens\<Zprime> = {endTask, advance, tick, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M7ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M7 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M7ToM8 =
  over SynController
  params nxt \<in> "{{}, {tick}, {retreat}, {advance}, {endTask}}" 
  pre "st= M7 \<and> reading7()\<le>threshold7() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M8
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M8]
         ,listens\<Zprime> = {tick, retreat, advance, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M7ToM6 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {advance}, {tick}, {retreat}}" 
  pre "st= M7 \<and> \<not>reading7()\<le>threshold7() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M6
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M6]
         ,listens\<Zprime> = {endTask, advance, tick, retreat}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M7ToM7 =
  over SynController
  params nxt \<in> "{{}, {advance}, {endTask}, {retreat}, {tick}}" 
  pre "st= M7 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M7
         ,tr\<Zprime> =tr @ [Event tick] @ [State M7]
         ,listens\<Zprime> = {advance, endTask, retreat, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M8ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M8 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M8ToM9 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {advance}, {tick}, {endTask}}" 
  pre "st= M8 \<and> reading8()\<le>threshold8() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M9
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M9]
         ,listens\<Zprime> = {retreat, advance, tick, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M8ToM7 =
  over SynController
  params nxt \<in> "{{}, {advance}, {endTask}, {retreat}, {tick}}" 
  pre "st= M8 \<and> \<not>reading8()\<le>threshold8() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M7
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M7]
         ,listens\<Zprime> = {advance, endTask, retreat, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M8ToM8 =
  over SynController
  params nxt \<in> "{{}, {tick}, {retreat}, {advance}, {endTask}}" 
  pre "st= M8 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M8
         ,tr\<Zprime> =tr @ [Event tick] @ [State M8]
         ,listens\<Zprime> = {tick, retreat, advance, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M9ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M9 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M9ToM10 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {endTask}, {advance}, {tick}}" 
  pre "st= M9 \<and> reading9()\<le>threshold9() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M10
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M10]
         ,listens\<Zprime> = {retreat, endTask, advance, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M9ToM8 =
  over SynController
  params nxt \<in> "{{}, {tick}, {retreat}, {advance}, {endTask}}" 
  pre "st= M9 \<and> \<not>reading9()\<le>threshold9() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M8
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M8]
         ,listens\<Zprime> = {tick, retreat, advance, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M9ToM9 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {advance}, {tick}, {endTask}}" 
  pre "st= M9 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M9
         ,tr\<Zprime> =tr @ [Event tick] @ [State M9]
         ,listens\<Zprime> = {retreat, advance, tick, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M10ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M10 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M10ToM11 =
  over SynController
  params nxt \<in> "{{}, {tick}, {advance}, {retreat}, {endTask}}" 
  pre "st= M10 \<and> reading10()\<le>threshold10() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M11
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M11]
         ,listens\<Zprime> = {tick, advance, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M10ToM9 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {advance}, {tick}, {endTask}}" 
  pre "st= M10 \<and> \<not>reading10()\<le>threshold10() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M9
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M9]
         ,listens\<Zprime> = {retreat, advance, tick, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M10ToM10 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {endTask}, {advance}, {tick}}" 
  pre "st= M10 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M10
         ,tr\<Zprime> =tr @ [Event tick] @ [State M10]
         ,listens\<Zprime> = {retreat, endTask, advance, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M11ToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= M11 \<and> endTask \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event endTask]@ [Event move] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M11ToM1 =
  over SynController
  params nxt \<in> "{{}, {tick}, {advance}, {retreat}, {endTask}}" 
  pre "st= M11 \<and> reading11()\<le>threshold11() \<and> advance \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M1
         ,tr\<Zprime> =tr @ [Event advance]@ [Event move] @ [State M1]
         ,listens\<Zprime> = {tick, advance, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M11ToM10 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {endTask}, {advance}, {tick}}" 
  pre "st= M11 \<and> \<not>reading11()\<le>threshold11() \<and> retreat \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M10
         ,tr\<Zprime> =tr @ [Event retreat]@ [Event move] @ [State M10]
         ,listens\<Zprime> = {retreat, endTask, advance, tick}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation M11ToM11 =
  over SynController
  params nxt \<in> "{{}, {tick}, {advance}, {retreat}, {endTask}}" 
  pre "st= M11 \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= M11
         ,tr\<Zprime> =tr @ [Event tick] @ [State M11]
         ,listens\<Zprime> = {tick, advance, retreat, endTask}
         ,offered\<Zprime> = nxt
         ]"
        
zoperation FinalToFinal =
  over SynController
  params nxt \<in> "{{}, {tick}}" 
  pre "st= Final \<and> tick \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> =tr @ [Event tick] @ [State Final]
         ,listens\<Zprime> = {tick}
         ,offered\<Zprime> = nxt
         ]"
        

  
zoperation Idle_M1 =
  over SynController
  params nxt \<in> "{{}, {tick}, {advance}, {retreat}, {endTask}}"
  pre "st= M1 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading1()\<le>threshold1() \<and> advance \<in> offered) \<and> \<not>(\<not>reading1()\<le>threshold1() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M1
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M2 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {tick}, {advance}, {endTask}}"
  pre "st= M2 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading2()\<le>threshold2() \<and> advance \<in> offered) \<and> \<not>(\<not>reading2()\<le>threshold2() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M2
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M3 =
  over SynController
  params nxt \<in> "{{}, {advance}, {retreat}, {tick}, {endTask}}"
  pre "st= M3 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading3()\<le>threshold3() \<and> advance \<in> offered) \<and> \<not>(\<not>reading3()\<le>threshold3() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M3
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M4 =
  over SynController
  params nxt \<in> "{{}, {advance}, {retreat}, {endTask}, {tick}}"
  pre "st= M4 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading4()\<le>threshold4() \<and> advance \<in> offered) \<and> \<not>(\<not>reading4()\<le>threshold4() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M4
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M5 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {advance}, {retreat}, {tick}}"
  pre "st= M5 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading5()\<le>threshold5() \<and> advance \<in> offered) \<and> \<not>(\<not>reading5()\<le>threshold5() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M5
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M6 =
  over SynController
  params nxt \<in> "{{}, {endTask}, {advance}, {tick}, {retreat}}"
  pre "st= M6 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading6()\<le>threshold6() \<and> advance \<in> offered) \<and> \<not>(\<not>reading6()\<le>threshold6() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M6
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M7 =
  over SynController
  params nxt \<in> "{{}, {advance}, {endTask}, {retreat}, {tick}}"
  pre "st= M7 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading7()\<le>threshold7() \<and> advance \<in> offered) \<and> \<not>(\<not>reading7()\<le>threshold7() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M7
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M8 =
  over SynController
  params nxt \<in> "{{}, {tick}, {retreat}, {advance}, {endTask}}"
  pre "st= M8 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading8()\<le>threshold8() \<and> advance \<in> offered) \<and> \<not>(\<not>reading8()\<le>threshold8() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M8
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M9 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {advance}, {tick}, {endTask}}"
  pre "st= M9 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading9()\<le>threshold9() \<and> advance \<in> offered) \<and> \<not>(\<not>reading9()\<le>threshold9() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M9
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M10 =
  over SynController
  params nxt \<in> "{{}, {retreat}, {endTask}, {advance}, {tick}}"
  pre "st= M10 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading10()\<le>threshold10() \<and> advance \<in> offered) \<and> \<not>(\<not>reading10()\<le>threshold10() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M10
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_M11 =
  over SynController
  params nxt \<in> "{{}, {tick}, {advance}, {retreat}, {endTask}}"
  pre "st= M11 \<and> \<not>(endTask \<in> offered) \<and> \<not>(reading11()\<le>threshold11() \<and> advance \<in> offered) \<and> \<not>(\<not>reading11()\<le>threshold11() \<and> retreat \<in> offered) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= M11
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

zoperation Idle_Final =
  over SynController
  params nxt \<in> "{{}, {tick}}"
  pre "st= Final \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= Final
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

definition Init :: "SynController subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ,offered\<leadsto> {}
  ]"
(* FORK: defaults emitted by template; edit to match intended initial state if needed. *)
  
  
zmachine SynControllerMachine =
  init Init
  invariant SynController_inv
  operations  InitialToM1 M1ToFinal M1ToM2 M1ToM1 M1ToM1_1 M2ToFinal M2ToM3 M2ToM1 M2ToM2 M3ToFinal M3ToM4 M3ToM2 M3ToM3 M4ToFinal M4ToM5 M4ToM3 M4ToM4 M5ToFinal M5ToM6 M5ToM4 M5ToM5 M6ToFinal M6ToM7 M6ToM5 M6ToM6 M7ToFinal M7ToM8 M7ToM6 M7ToM7 M8ToFinal M8ToM9 M8ToM7 M8ToM8 M9ToFinal M9ToM10 M9ToM8 M9ToM9 M10ToFinal M10ToM11 M10ToM9 M10ToM10 M11ToFinal M11ToM1 M11ToM10 M11ToM11 FinalToFinal Idle_M1 Idle_M2 Idle_M3 Idle_M4 Idle_M5 Idle_M6 Idle_M7 Idle_M8 Idle_M9 Idle_M10 Idle_M11 Idle_Final


subsection \<open> Structural Invariants \<close>

lemma Init_inv [hoare_lemmas]: "Init establishes SynController_inv"
  by zpog_full

lemma InitialToM1_inv [hoare_lemmas]: "InitialToM1 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M1ToFinal_inv [hoare_lemmas]: "M1ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M1ToM2_inv [hoare_lemmas]: "M1ToM2 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M1ToM1_inv [hoare_lemmas]: "M1ToM1 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M1ToM1_1_inv [hoare_lemmas]: "M1ToM1_1 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M2ToFinal_inv [hoare_lemmas]: "M2ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M2ToM3_inv [hoare_lemmas]: "M2ToM3 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M2ToM1_inv [hoare_lemmas]: "M2ToM1 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M2ToM2_inv [hoare_lemmas]: "M2ToM2 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M3ToFinal_inv [hoare_lemmas]: "M3ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M3ToM4_inv [hoare_lemmas]: "M3ToM4 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M3ToM2_inv [hoare_lemmas]: "M3ToM2 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M3ToM3_inv [hoare_lemmas]: "M3ToM3 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M4ToFinal_inv [hoare_lemmas]: "M4ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M4ToM5_inv [hoare_lemmas]: "M4ToM5 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M4ToM3_inv [hoare_lemmas]: "M4ToM3 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M4ToM4_inv [hoare_lemmas]: "M4ToM4 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M5ToFinal_inv [hoare_lemmas]: "M5ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M5ToM6_inv [hoare_lemmas]: "M5ToM6 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M5ToM4_inv [hoare_lemmas]: "M5ToM4 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M5ToM5_inv [hoare_lemmas]: "M5ToM5 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M6ToFinal_inv [hoare_lemmas]: "M6ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M6ToM7_inv [hoare_lemmas]: "M6ToM7 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M6ToM5_inv [hoare_lemmas]: "M6ToM5 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M6ToM6_inv [hoare_lemmas]: "M6ToM6 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M7ToFinal_inv [hoare_lemmas]: "M7ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M7ToM8_inv [hoare_lemmas]: "M7ToM8 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M7ToM6_inv [hoare_lemmas]: "M7ToM6 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M7ToM7_inv [hoare_lemmas]: "M7ToM7 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M8ToFinal_inv [hoare_lemmas]: "M8ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M8ToM9_inv [hoare_lemmas]: "M8ToM9 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M8ToM7_inv [hoare_lemmas]: "M8ToM7 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M8ToM8_inv [hoare_lemmas]: "M8ToM8 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M9ToFinal_inv [hoare_lemmas]: "M9ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M9ToM10_inv [hoare_lemmas]: "M9ToM10 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M9ToM8_inv [hoare_lemmas]: "M9ToM8 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M9ToM9_inv [hoare_lemmas]: "M9ToM9 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M10ToFinal_inv [hoare_lemmas]: "M10ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M10ToM11_inv [hoare_lemmas]: "M10ToM11 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M10ToM9_inv [hoare_lemmas]: "M10ToM9 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M10ToM10_inv [hoare_lemmas]: "M10ToM10 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M11ToFinal_inv [hoare_lemmas]: "M11ToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M11ToM1_inv [hoare_lemmas]: "M11ToM1 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M11ToM10_inv [hoare_lemmas]: "M11ToM10 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma M11ToM11_inv [hoare_lemmas]: "M11ToM11 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma FinalToFinal_inv [hoare_lemmas]: "FinalToFinal (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M1_inv [hoare_lemmas]: "Idle_M1 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M2_inv [hoare_lemmas]: "Idle_M2 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M3_inv [hoare_lemmas]: "Idle_M3 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M4_inv [hoare_lemmas]: "Idle_M4 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M5_inv [hoare_lemmas]: "Idle_M5 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M6_inv [hoare_lemmas]: "Idle_M6 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M7_inv [hoare_lemmas]: "Idle_M7 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M8_inv [hoare_lemmas]: "Idle_M8 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M9_inv [hoare_lemmas]: "Idle_M9 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M10_inv [hoare_lemmas]: "Idle_M10 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_M11_inv [hoare_lemmas]: "Idle_M11 (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)

lemma Idle_Final_inv [hoare_lemmas]: "Idle_Final (nxt) preserves SynController_inv"
  by (zpog_full; auto simp add: wf_rcstore_def)  (* FORK: archive used `auto` alone; our trace-update shape `tr @ [Event x] @ [State y]` needs explicit unfolding of wf_rcstore *)


subsection \<open> Safety Requirements \<close>

zexpr R1 is "True"

lemma  "Init establishes R1"
  by zpog_full

lemma "InitialToM1 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M1ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M1ToM2 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M1ToM1 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M1ToM1_1 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M2ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M2ToM3 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M2ToM1 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M2ToM2 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M3ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M3ToM4 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M3ToM2 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M3ToM3 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M4ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M4ToM5 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M4ToM3 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M4ToM4 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M5ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M5ToM6 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M5ToM4 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M5ToM5 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M6ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M6ToM7 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M6ToM5 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M6ToM6 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M7ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M7ToM8 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M7ToM6 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M7ToM7 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M8ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M8ToM9 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M8ToM7 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M8ToM8 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M9ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M9ToM10 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M9ToM8 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M9ToM9 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M10ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M10ToM11 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M10ToM9 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M10ToM10 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M11ToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M11ToM1 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M11ToM10 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "M11ToM11 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "FinalToFinal (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M1 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M2 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M3 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M4 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M5 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M6 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M7 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M8 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M9 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M10 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_M11 (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  
lemma "Idle_Final (nxt) preserves R1 under SynController_inv"
  by (zpog_full; auto)
  

definition [z_defs]: "SynController_axioms = True"

lemma SynController_deadlock_free: "SynController_axioms  \<Longrightarrow> deadlock_free SynControllerMachine"
  unfolding SynControllerMachine_def
  apply deadlock_free
  apply (case_tac st; (simp)?; (elim disjE)?; (simp)?)
  by blast+
end
