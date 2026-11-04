import json, sys
B = "src/main/java/steam_boiler/"
F = {
 "ann": B+"annotation/RoboChartType.java", "mode": B+"mode/OperatingMode.java",
 "pp": B+"domain/PumpPosition.java", "fo": B+"domain/FlowObservation.java", "vp": B+"domain/ValvePosition.java",
 "pr": B+"domain/PumpReading.java", "fr": B+"domain/FlowReading.java",
 "in": B+"event/InputEvent.java", "out": B+"event/OutputEvent.java",
 "k": B+"controller/BoilerConstants.java", "c": B+"controller/SteamBoilerController.java",
 "s": B+"sensor/PlantSensor.java", "ur": B+"sensor/UnitRecord.java", "ut": B+"sensor/UnitStatusTable.java",
 "rx": B+"sensor/MessageReceiver.java", "act": B+"actuator/BoilerActuator.java",
 "tx": B+"actuator/Transmitter.java", "po": B+"actuator/PumpOrder.java", "prog": B+"SteamBoilerProgram.java",
}
T = []
def add(req, f, el, ty):
    T.append({"requirement_gid": req, "java_file": F[f], "java_element": el, "element_type": ty})
def many(req, items):
    for f, el, ty in items: add(req, f, el, ty)

step = ("c", "step", "method")
ctrl = ("c", "SteamBoilerController", "class")

many("SB-V1", [ctrl, ("prog","SteamBoilerProgram","class"), ("s","PlantSensor","class"), ("rx","MessageReceiver","class"),
               ("tx","Transmitter","class"), ("act","BoilerActuator","class"), ("ut","UnitStatusTable","class"),
               ("ann","RoboChartType","interface"), ("ann","value","method")])
many("SB-V2", [ctrl, ("mode","OperatingMode","enum"), ("c","currentMode","field"), ("c","currentMode","method"),
               ("prog","currentMode","method"), ("tx","lastMode","method")])
many("SB-V3", [ctrl, step, ("prog","runCycle","method"), ("rx","receive","method"), ("tx","transmit","method"),
               ("in","TransmissionReceived","record"), ("act","startCycle","method"), ("ur","startCycle","method"),
               ("ut","startCycle","method")])
many("SB-V4", [ctrl, step, ("k","LIMIT_M1","field"), ("k","LIMIT_M2","field"), ("k","NORMAL_N1","field"), ("k","NORMAL_N2","field")])
many("SB-V5", [("s","levelReading","method"), ("in","Level","record")])
many("SB-V6", [("s","steamReading","method"), ("in","Steam","record")])
many("SB-V7", [("s","pumpThroughput","method"), ("rx","receive","method")])
many("SB-V8", [("rx","levelLow","field"), ("rx","levelHigh","field"), ("s","levelEstimateLow","method"), ("s","levelEstimateHigh","method")])
many("SB-V9", [("ur","UnitRecord","class"), ("ur","isOutOfOrder","method"), ("ur","failureAnnouncementDue","method"),
               ("ur","recordFailure","method"), ("ur","recordFailureAcknowledgement","method"), ("ur","recordRepair","method"),
               ("ut","UnitStatusTable","class"), ("ut","pump","method"), ("ut","pumpControl","method"),
               ("ut","levelDevice","method"), ("ut","steamDevice","method"), ("ut","anyPumpOutOfOrder","method"),
               ("ut","anyPumpControlOutOfOrder","method"), ("s","levelDeviceOutOfOrder","method"),
               ("s","otherUnitOutOfOrder","method"), ("s","rescueEstimateUnreliable","method")])
many("SB-V10", [("s","successiveStops","method"), ("rx","receive","method")])
many("SB-DM1", [("k","BoilerConstants","class"), ("k","CAPACITY_C","field"), ("k","LIMIT_M1","field"), ("k","LIMIT_M2","field"),
                ("k","NORMAL_N1","field"), ("k","NORMAL_N2","field")])
many("SB-DM2", [("k","STEAM_MAX_W","field"), ("k","STEAM_RISE_U1","field"), ("k","STEAM_FALL_U2","field"),
                ("rx","receive","method"), ("s","levelLimitRisk","method")])
many("SB-DM3", [("k","PUMP_CAPACITY_P","field"), ("k","PUMP_COUNT","field"), ("k","CYCLE_SECONDS","field"),
                ("pp","PumpPosition","enum"), ("pp","PUMP_OFF","field"), ("pp","PUMP_ON","field"),
                ("pr","PumpReading","record"), ("in","PumpState","record")])
many("SB-DM4", [("fo","FlowObservation","enum"), ("fo","NO_FLOW","field"), ("fo","WATER_FLOWING","field"),
                ("fr","FlowReading","record"), ("in","PumpControlState","record")])
many("SB-DM5", [("in","Level","record"), ("s","levelReading","method")])
many("SB-DM6", [("in","Steam","record"), ("s","steamReading","method")])
many("SB-DM7", [("vp","ValvePosition","enum"), ("vp","VALVE_CLOSED","field"), ("vp","VALVE_OPEN","field"),
                ("act","setValve","method"), ("act","requestedValve","method"), ("tx","valvePosition","method")])
outs = ["Mode","ProgramReady","Valve","OpenPump","ClosePump","PumpFailureDetection","PumpControlFailureDetection",
        "LevelFailureDetection","SteamFailureDetection","PumpRepairedAcknowledgement","PumpControlRepairedAcknowledgement",
        "LevelRepairedAcknowledgement","SteamRepairedAcknowledgement"]
many("SB-DM8", [("out","OutputEvent","interface")] + [("out",n,"record") for n in outs] +
               [("act","sendMode","method"), ("act","sendProgramReady","method"), ("act","setValve","method"),
                ("act","startAllPumps","method"), ("act","stopAllPumps","method"), ("act","startFillPump","method"),
                ("act","sendUnitMessages","method"), ("act","announcedMode","method"),
                ("act","modePending","method"), ("act","programReadyPending","method"), ("act","requestedPumps","method"),
                ("act","pumpOrderPending","method"), ("act","unitMessagesPending","method"), ("tx","transmit","method")])
ins = ["StopRequest","SteamBoilerWaiting","PhysicalUnitsReady","PumpState","PumpControlState","Level","Steam",
       "PumpRepaired","PumpControlRepaired","LevelRepaired","SteamRepaired","PumpFailureAcknowledgement",
       "PumpControlFailureAcknowledgement","LevelFailureAcknowledgement","SteamOutcomeFailureAcknowledgement"]
many("SB-DM9", [("in","InputEvent","interface")] + [("in",n,"record") for n in ins] + [("rx","receive","method")])
many("SB-DM10", [("mode","OperatingMode","enum"), ("out","Mode","record")])
many("SB-Beh1", [step, ("c","currentMode","field"), ("mode","INITIALIZATION","field")])
many("SB-Beh2", [step, ("s","steamBoilerWaitingSeen","method"), ("in","SteamBoilerWaiting","record")])
many("SB-Beh3", [step, ("s","steamRateNonZero","method"), ("s","steamReading","method")])
many("SB-Beh4", [step, ("act","setValve","method"), ("act","stopAllPumps","method"), ("s","levelAboveNormal","method")])
many("SB-Beh5", [step, ("act","startFillPump","method"), ("s","levelBelowNormal","method")])
many("SB-Beh6", [step, ("s","levelDeviceOutOfOrder","method")])
many("SB-Beh7", [step, ("c","readyAnnounced","field"), ("act","sendProgramReady","method"), ("s","physicalUnitsReadyReceived","method"),
                 ("tx","programReadySent","method")])
many("SB-Beh8", [step, ("s","physicalUnitsReadyReceived","method"), ("in","PhysicalUnitsReady","record"), ("s","otherUnitOutOfOrder","method")])
many("SB-Beh9", [step, ("s","transmissionFailed","method")])
many("SB-Beh10", [step, ("mode","NORMAL","field"), ("act","startAllPumps","method"), ("act","stopAllPumps","method"), ("s","levelHeadingLow","method"), ("s","levelHeadingHigh","method")])
for r in ["SB-Beh11","SB-Beh12","SB-Beh13","SB-Beh14"]: many(r, [step])
many("SB-Beh11", [("s","levelDeviceOutOfOrder","method")])
many("SB-Beh12", [("s","otherUnitOutOfOrder","method")])
many("SB-Beh13", [("s","levelLimitRisk","method")])
many("SB-Beh14", [("s","transmissionFailed","method")])
many("SB-Beh15", [step, ("mode","DEGRADED","field")])
for r in ["SB-Beh16","SB-Beh17","SB-Beh18"]: many(r, [step])
many("SB-Beh19", [step, ("mode","RESCUE","field"), ("rx","levelLow","field"), ("rx","levelHigh","field"), ("s","levelEstimateLow","method"), ("s","levelEstimateHigh","method")])
for r in ["SB-Beh20","SB-Beh21"]: many(r, [step])
many("SB-Beh21", [("s","rescueEstimateUnreliable","method")])
many("SB-Beh22", [step, ("mode","EMERGENCY_STOP","field"), ("prog","runCycle","method")])
many("SB-Beh23", [step, ("s","successiveStops","method"), ("k","STOP_LIMIT","field"), ("in","StopRequest","record")])
many("SB-Beh24", [step, ("act","sendMode","method"), ("out","Mode","record")])
many("SB-Beh25", [step, ("rx","receive","method"), ("k","CYCLE_SECONDS","field"),
                  ("tx","transmissionsSinceOrder","method"), ("po","PumpOrder","class")])
many("SB-FR1", [("rx","MessageReceiver","class"), ("tx","pumpOrderedOn","method"), ("po","PumpOrder","class"),
                ("s","otherUnitOutOfOrder","method")])
many("SB-FR2", [("rx","MessageReceiver","class"), ("tx","transmissionsSinceOrder","method"), ("s","rescueEstimateUnreliable","method")])
many("SB-FR3", [("rx","MessageReceiver","class"), ("s","levelDeviceOutOfOrder","method")])
many("SB-FR4", [("rx","MessageReceiver","class"), ("s","rescueEstimateUnreliable","method")])
many("SB-FR5", [("rx","receive","method"), ("s","transmissionFailed","method"), ("ut","isPumpNumber","method"), step,
                ("tx","lastMode","method"), ("tx","programReadySent","method")])
many("SB-FR6", [("ur","failureAnnouncementDue","method"), ("ur","recordFailure","method"),
                ("ur","recordFailureAcknowledgement","method"), ("act","sendUnitMessages","method"), ("tx","transmit","method"),
                ("out","PumpFailureDetection","record"), ("out","PumpControlFailureDetection","record"),
                ("out","LevelFailureDetection","record"), ("out","SteamFailureDetection","record")])
many("SB-FR7", [("ur","recordRepair","method"), ("ur","repairAcknowledgementDue","method"), ("act","sendUnitMessages","method"),
                ("out","PumpRepairedAcknowledgement","record"), ("out","PumpControlRepairedAcknowledgement","record"),
                ("out","LevelRepairedAcknowledgement","record"), ("out","SteamRepairedAcknowledgement","record")])
many("SB-FR8", [ctrl, step])
json.dump({"codegen_trace": T}, open(sys.argv[1], "w"), indent=4)
print(len(T), "entries")
