#!/bin/bash
THIS_BASE="/home/jorge/Dropbox/mase/"
ARGS="$@"

OUTFOLDER=$2
echo $OUTFOLDER

echo "Cleaning classes"
ssh jorge@10.20.0.249 rm -rf build/classes lib

echo "Copying classes"
scp -q -r $THIS_BASE"build/classes" jorge@10.20.0.249:build
scp -q -r $THIS_BASE"lib" jorge@10.20.0.249:
COMMAND="java -cp build/classes:lib/* mase.MaseEvolve "$ARGS

echo "Running"
ssh jorge@10.20.0.249 $COMMAND

echo "Copying results"
OUTPARENT="$(dirname "$OUTFOLDER")"
echo $OUTPARENT
scp -p -r jorge@10.20.0.249:$OUTFOLDER $OUTPARENT

