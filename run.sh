#!/bin/bash
PROGRAM=$1
source classpath
shift
java -cp $CLASSPATH $PROGRAM "$@"
