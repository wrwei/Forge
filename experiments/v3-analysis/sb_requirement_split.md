# Steam Boiler: requirements the verified machine implements

Post-hoc, from the committed records; method in `scripts/v3_sb_requirement_split.py`.

| Run | Inside | Partly | Outside | Vocabulary only | Untraced |
|---|---|---|---|---|---|
| 1 | 1 | 28 | 19 | 5 | 0 |
| 2 | 2 | 29 | 20 | 2 | 0 |
| 3 | 8 | 21 | 16 | 8 | 0 |
| 4 | 4 | 26 | 19 | 4 | 0 |
| 5 | 3 | 28 | 16 | 6 | 0 |

## Per requirement

| Requirement | Kind | Run 1 | Run 2 | Run 3 | Run 4 | Run 5 |
|---|---|---|---|---|---|---|
| SB-V1 SystemArchitecture | Non-Functional Requirement | partly | partly | partly | partly | partly |
| SB-V2 OperatingModes | Non-Functional Requirement | partly | inside | partly | partly | partly |
| SB-V3 CycleStructure | Non-Functional Requirement | outside | outside | outside | partly | partly |
| SB-V4 ControlObjective | Functional Requirement | partly | partly | partly | partly | partly |
| SB-V5 Variable_q | Non-Functional Requirement | outside | outside | outside | outside | outside |
| SB-V6 Variable_v | Non-Functional Requirement | outside | outside | outside | outside | outside |
| SB-V7 Variable_p | Derived Requirement | outside | outside | outside | outside | outside |
| SB-V8 Variable_EstimatedLevel | Derived Requirement | outside | outside | outside | outside | outside |
| SB-V9 Variable_UnitStatus | Derived Requirement | outside | outside | outside | outside | outside |
| SB-V10 Variable_StopCount | Derived Requirement | outside | outside | outside | outside | outside |
| SB-DM1 BoilerConstants | Non-Functional Requirement | vocabulary | vocabulary | vocabulary | vocabulary | vocabulary |
| SB-DM2 SteamConstants | Non-Functional Requirement | vocabulary | vocabulary | vocabulary | vocabulary | vocabulary |
| SB-DM3 PumpCharacteristics | Non-Functional Requirement | vocabulary | partly | vocabulary | outside | vocabulary |
| SB-DM4 PumpController | Non-Functional Requirement | vocabulary | outside | vocabulary | vocabulary | vocabulary |
| SB-DM5 LevelDevice | Non-Functional Requirement | outside | outside | vocabulary | outside | outside |
| SB-DM6 SteamDevice | Non-Functional Requirement | outside | outside | vocabulary | outside | outside |
| SB-DM7 EvacuationValve | Non-Functional Requirement | outside | partly | outside | outside | partly |
| SB-DM8 OutputMessages | Non-Functional Requirement | outside | outside | vocabulary | outside | outside |
| SB-DM9 InputMessages | Non-Functional Requirement | outside | outside | outside | outside | vocabulary |
| SB-DM10 ModeDatatype | Non-Functional Requirement | vocabulary | outside | vocabulary | vocabulary | vocabulary |
| SB-Beh1 InitialMode | Functional Requirement | inside | inside | inside | inside | inside |
| SB-Beh2 Init_WaitForPlant | Functional Requirement | partly | partly | partly | partly | partly |
| SB-Beh3 Init_SteamMustBeZero | Functional Requirement | partly | partly | partly | partly | partly |
| SB-Beh4 Init_DrainExcessWater | Functional Requirement | partly | partly | partly | partly | partly |
| SB-Beh5 Init_FillLowWater | Functional Requirement | partly | partly | partly | partly | partly |
| SB-Beh6 Init_LevelDeviceFailure | Functional Requirement | partly | partly | partly | partly | partly |
| SB-Beh7 Init_ReadyHandshake | Functional Requirement | partly | partly | partly | partly | partly |
| SB-Beh8 Init_to_NormalOrDegraded | Functional Requirement | partly | partly | partly | partly | partly |
| SB-Beh9 Init_TransmissionFailure | Functional Requirement | partly | partly | partly | partly | partly |
| SB-Beh10 Normal_LevelRegulation | Functional Requirement | partly | partly | partly | partly | partly |
| SB-Beh11 Normal_to_Rescue | Functional Requirement | partly | partly | partly | partly | partly |
| SB-Beh12 Normal_to_Degraded | Functional Requirement | partly | partly | inside | partly | partly |
| SB-Beh13 Normal_to_Emergency_LevelRisk | Functional Requirement | partly | partly | partly | partly | partly |
| SB-Beh14 Normal_to_Emergency_Transmission | Functional Requirement | partly | partly | inside | partly | partly |
| SB-Beh15 Degraded_Regulation | Functional Requirement | partly | partly | partly | partly | inside |
| SB-Beh16 Degraded_to_Normal | Functional Requirement | partly | partly | inside | partly | inside |
| SB-Beh17 Degraded_to_Rescue | Functional Requirement | partly | partly | inside | inside | partly |
| SB-Beh18 Degraded_to_Emergency | Functional Requirement | partly | partly | inside | partly | partly |
| SB-Beh19 Rescue_LevelEstimation | Functional Requirement | partly | partly | partly | partly | partly |
| SB-Beh20 Rescue_to_DegradedOrNormal | Functional Requirement | partly | partly | inside | inside | partly |
| SB-Beh21 Rescue_to_Emergency | Functional Requirement | partly | partly | partly | partly | partly |
| SB-Beh22 EmergencyStop_Terminal | Functional Requirement | partly | partly | partly | partly | partly |
| SB-Beh23 OperatorStop | Functional Requirement | partly | partly | partly | partly | partly |
| SB-Beh24 ModeBroadcast | Functional Requirement | partly | partly | inside | inside | partly |
| SB-Beh25 PumpStartLatency | Derived Requirement | partly | partly | outside | outside | outside |
| SB-FR1 PumpFailureDetection | Functional Requirement | outside | outside | outside | outside | outside |
| SB-FR2 PumpControlFailureDetection | Functional Requirement | outside | outside | outside | outside | outside |
| SB-FR3 LevelFailureDetection | Functional Requirement | outside | outside | outside | outside | outside |
| SB-FR4 SteamFailureDetection | Functional Requirement | outside | outside | outside | outside | outside |
| SB-FR5 TransmissionFailureDetection | Functional Requirement | outside | outside | partly | partly | partly |
| SB-FR6 FailureAnnouncement | Functional Requirement | outside | outside | outside | outside | outside |
| SB-FR7 RepairHandling | Functional Requirement | outside | outside | outside | outside | outside |
| SB-FR8 FailureConsequencePrecedence | Derived Requirement | partly | partly | partly | partly | partly |
