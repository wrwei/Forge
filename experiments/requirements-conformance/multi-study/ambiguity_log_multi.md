# Ambiguity and interpretation log: SRanger and Chemical Detector relations

Each entry records a point where the requirements prose admits more than one
reading, the reading adopted, and the basis. Entries follow the convention of
the LRE log (`ambiguity_log.md`).

## SR-A1 (SRanger): obstacle event vs obstacleDetected guard
SR-Beh2 triggers on the obstacle event; SR-GP1 names obstacleDetected as the
guard on the same transition. SR-DM3 states the sensor layer emits obstacle
exactly when the distance is at or below the threshold, so event and guard are
correlated by the sensor contract. Adopted: the transition requires both; cells
with obstacle offered and obstacleDetected false are outside the occupiable
space and are reported as flag-inconsistent rather than scored.

## SR-A2 (SRanger): staying in a mode
Where no behavioural requirement fires (e.g. Moving with the move output event
offered), the controller stays in its mode. Adopted: staying is admitted
exactly when no transition is enabled; when a transition is enabled, staying is
not admitted (SR-Beh5's autonomous transition is evaluated every step).

## GA-A1 (GasAnalysis): 'concludes its search'
CD-GA-Beh6 ends with 'concludes its search, processing no further readings'
without naming a mode. Adopted: the terminal mode Final of the controller's
mode enumeration. Basis: CD-GA-FR4 describes the same outcome as concluding,
and the published study configuration lists Final among the controller's modes.

## GA-A2 (GasAnalysis): two-valued classification
sts is modelled as the single boolean stsIsGasD (noGas | gasD), as in the
published Tier-B configuration; the Analysis tick operation guarded by
'not noGas and not gasD' is enabled nowhere under this reading.

## MV-A1 (Movement): the Found sink
CD-MV-Beh9 reads 'Once in Found, the movement subsystem stays halted'. The
generated implementation realises this as a silent autonomous transition from
Found into a terminal sink Final (its own documentation notes the sink), and
the extracted model represents that transition faithfully. The two encodings
are observably equivalent: no output is emitted on the hop and nothing leaves
either state. Adopted: the admitted set at Found is {Found, Final} -- the
set-valued convention used throughout where wording admits more than one
reading -- so this is not scored as a disagreement; it is an encoding
convention, not an extraction error. Under the strict single-successor reading
the 20 Found cells (of 140) would read as disagreements (120/140); that reading
is recorded here for completeness and is recoverable from the shipped relation
by dropping the entry's also_admits field.

## MV-A2 (Movement): progress disjunction
CD-MV-Beh17's 'making progress' is the disjunction (elapsed < stuckPeriod) OR
(advanced > stuckDist); CD-MV-Beh18's 'stuck' is the conjunction of the two
negations. Adopted as stated; the two conditions partition every valuation of
the two atoms, so exactly one of Beh17/Beh18 is enabled in each AvoidingAgain
cell.
