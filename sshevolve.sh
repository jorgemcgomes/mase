#!/bin/bash
source classpath

IP=$1
shift

ARGS="$@"
OUTFOLDER=$2
echo $OUTFOLDER

echo "Cleaning classes"
ssh $IP rm -rf build lib
ssh $IP mkdir -p build/classes lib

echo "Copying classes"
IFS=':' read -ra ARRAY <<< "$CLASSPATH"
for i in "${ARRAY[@]}"; do
    echo $i
    if [[ -d $i ]]
    then
	scp -q -r $i $IP:build
    else
        scp -q -r $i $IP:lib
    fi
done

echo "Running"
ssh $IP "java -cp build/classes:lib/* mase.MaseEvolve "$ARGS

echo "Copying results"
OUTPARENT="$(dirname "$OUTFOLDER")"
echo $OUTPARENT
scp -p -r $IP:$OUTFOLDER $OUTPARENT

#echo "Cleaning results"
#ssh $IP rm -rf $OUTFOLDER

