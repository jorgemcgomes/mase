#!/bin/bash
THIS_BASE="/home/jorge/Dropbox/mase/"
ARGS="$@"

OUTFOLDER=$2
echo $OUTFOLDER

echo "Cleaning classes"
ssh jorge@194.117.20.167 rm -rf build/classes lib

echo "Copying classes"
scp -q -r $THIS_BASE"build/classes" jorge@194.117.20.167:build
scp -q -r $THIS_BASE"lib" jorge@194.117.20.167:
COMMAND="java -cp build/classes:lib/* mase.MaseEvolve "$ARGS

echo "Running"
ssh jorge@194.117.20.167 $COMMAND

echo "Copying results"
OUTPARENT="$(dirname "$OUTFOLDER")"
echo $OUTPARENT
scp -p -r jorge@194.117.20.167:$OUTFOLDER $OUTPARENT
