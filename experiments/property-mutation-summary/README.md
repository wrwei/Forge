# Mutation testing of the obligations (Section 4.8.1, Table 10)

The second of the paper's three mutation-testing campaigns. The code is left as it is and the
generated obligations are mutated: Dafny contracts, parts of the Isabelle theories, and CSP trace
claims. Each study's mutants, verdicts and logs are in its own folder:

| Folder | Contents |
|---|---|
| [`../property-mutation-sranger/`](../property-mutation-sranger/) | SRanger: Dafny, Isabelle and CSP |
| [`../property-mutation-lre/`](../property-mutation-lre/) | LRE: Dafny, Isabelle and CSP |
| [`../property-mutation-chemical_detector/`](../property-mutation-chemical_detector/) | Chemical Detector: Isabelle |
| [`../property-mutation-steam_boiler/`](../property-mutation-steam_boiler/) | Steam Boiler: Isabelle |
| [`../property-mutation-dafny-others/`](../property-mutation-dafny-others/) | Chemical Detector and Steam Boiler: Dafny |
| [`../property-mutation-csp-others/`](../property-mutation-csp-others/) | Chemical Detector and Steam Boiler: CSP |
| [`../property-mutation-isabelle-all.sh`](../property-mutation-isabelle-all.sh) | runs every study's Isabelle mutants |

`summarise.py` reads the result files of those folders and writes `summary.csv` and
`table_rows.tex`, the rows of Table 10.
