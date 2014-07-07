#!/bin/bash
BASE="/home/jorge/Dropbox/mase/"
CLASSPATH=$BASE"build/classes:"$BASE"lib/*"

java -cp $CLASSPATH mase.MaseEvolve "$@"

