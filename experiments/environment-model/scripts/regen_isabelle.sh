#!/bin/bash
# regen.sh <study> <xmi-source-dir> : run the current IsabellePhase on a copy of robochart_model.xmi
C=/Users/ranwei/Gitee/formal_method_guided_vibe_coding; W=$(pwd)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
CACHE=~/.gradle/caches/modules-2/files-2.1
CP=$(find $CACHE -name "*.jar" | grep -v -e -sources -e -javadoc | grep -v "epsilon.*2\.5\.0" | tr "\n" ":")
ulimit -n 8192 2>/dev/null
rm -rf $W/gen-b/$1; mkdir -p $W/gen-b/$1 && cp $2/robochart_model.xmi $W/gen-b/$1/
( cd $C/forge.transformations
  perl -e 'alarm 600; exec @ARGV' "$JAVA_HOME/bin/java" -cp "src/main/resources:${CP}build/classes/java/main:build/resources/main" forge.transformations.core.App forge.transformations.m2t.IsabellePhase output=$W/gen-b/$1 > $W/gen-b/$1.log 2>&1 )
grep -iE "exception|error:" $W/gen-b/$1.log | grep -v "tr.condition" | head -3 | cut -c1-220 | sed 's/^/  ERR: /'
ls $W/gen-b/$1/isabelle/*.thy 2>/dev/null | xargs -n1 basename | sed "s/^/  $1: /"
