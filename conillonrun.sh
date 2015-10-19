#!/bin/bash
PROGRAM=$1
shift
java -cp build/classes:lib/* $PROGRAM "$@"
