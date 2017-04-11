#!/bin/bash
CLASSPATH="build/classes:dist/lib/*"
PROGRAM=$1
shift
java -cp $CLASSPATH $PROGRAM "$@"
