#!/bin/bash
THIS_BASE="/home/jorge/Dropbox/mase/"
ARGS="$@"

OUTFOLDER=$2
echo $OUTFOLDER

echo "Cleaning classes"
ssh jorge@10.20.0.243 rm -rf build/classes lib

echo "Copying classes"
scp -q -r $THIS_BASE"build/classes" jorge@10.20.0.243:build
scp -q -r $THIS_BASE"lib" jorge@10.20.0.243:
COMMAND="java -cp build/classes:lib/* mase.MaseEvolve "$ARGS

echo "Running"
ssh jorge@10.20.0.243 $COMMAND

echo "Copying results"
OUTPARENT="$(dirname "$OUTFOLDER")"
echo $OUTPARENT
scp -p -r jorge@10.20.0.243:$OUTFOLDER $OUTPARENT

#echo "Cleaning results"
#ssh jorge@10.20.0.243 rm -rf $OUTFOLDER

