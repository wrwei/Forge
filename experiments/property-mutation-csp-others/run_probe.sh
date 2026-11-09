#!/usr/bin/env bash
cd "$(dirname "$0")"; FDR=/Applications/FDR4.app/Contents/MacOS/refines

( cd ~/Gitee/.fdr-stage-boiler/defs && "$FDR" --format framed_json SteamBoilerController_probe.csp ) > results/SteamBoilerController_probe.json 2> results/SteamBoilerController_probe.err; echo "Boiler probe: $(grep -o '"result": *[01]' results/SteamBoilerController_probe.json | grep -o '[01]' | tr '\n' ' ')"
