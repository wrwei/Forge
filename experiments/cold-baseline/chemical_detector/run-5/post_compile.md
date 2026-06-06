# Compile (gradle build) — FAILED

## Summary
gradle build failed (exit 1)

## Run history
**WARNING — thrashing detected.** 1 issue(s) have recurred 3+ runs in a row despite fixes: `gradle build of java.generated.project failed`. Previous fix strategies are not working — change approach (e.g. re-read the originating requirement, or escalate to the user).

- New this run: 0
- Recurring from previous run: 1
- Resolved since previous run: 0

## Issues
### Issue 1: compile_failure — gradle build of java.generated.project failed [RECURRING x4 — fix strategy failing]

**Raw**
```
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.12/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.

FAILURE: Build failed with an exception.

* What went wrong:
Unable to start the daemon process.
This problem might be caused by incorrect configuration of the daemon.
For example, an unrecognized jvm option is used.For more details on the daemon, please refer to https://docs.gradle.org/8.12/userguide/gradle_daemon.html in the Gradle documentation.
Process command line: C:\Program Files\Java\jdk-21\bin\java.exe --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED --add-opens=java.base/java.nio.charset=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-opens=java.xml/javax.xml.namespace=ALL-UNNAMED -XX:MaxMetaspaceSize=512m -Xmx2g -Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en -Duser.variant -cp E:\Software\Repositories\wrapper\dists\gradle-8.12-bin\cetblhg4pflnnks72fxwobvgv\gradle-8.12\lib\gradle-daemon-main-8.12.jar -javaagent:E:\Software\Repositories\wrapper\dists\gradle-8.12-bin\cetblhg4pflnnks72fxwobvgv\gradle-8.12\lib\agents\gradle-instrumentation-agent-8.12.jar org.gradle.launcher.daemon.bootstrap.GradleDaemon 8.12
Please read the following process output to find out more:
-----------------------
Java HotSpot(TM) 64-Bit Server VM warning: INFO: os::commit_memory(0x0000000080000000, 1071644672, 0) failed; error='The paging file is too small for this operation to complete' (DOS error/errno=1455)
#
# There is insufficient memory for the Java Runtime Environment to continue.
# Native memory allocation (mmap) failed to map 1071644672 bytes for G1 virtual space
# An error report file with more information is saved as:
# E:\Software\Repositories\daemon\8.12\hs_err_pid76108.log


* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.
```

**Fix directive**
gradle build of java.generated.project raised an error. Inspect the raw message above and correlate with the Java code. If the message references a Spoon/EMF construct, the Java source may use a feature banned by java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Read the issue above, follow the fix directive, edit the Java source, and re-run this phase.
