#!/bin/bash
CLASSPATH="build/classes:dist/lib/*"
java -Djava.library.path="lib/libremoteApiJava.so" -cp $CLASSPATH mase.MaseEvolve "$@"

