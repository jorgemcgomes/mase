#!/bin/bash
source classpath

IP=$1
shift

ARGS="$@"
OUTFOLDER=$2
echo $OUTFOLDER

echo "Cleaning classes"
ssh jorge@10.20.0.243 rm -rf build lib
ssh jorge@10.20.0.243 mkdir -p build/classes lib

echo "Copying classes"
IFS=':' read -ra ARRAY <<< "$CLASSPATH"
for i in "${ARRAY[@]}"; do
    echo $i
    if [[ -d $i ]]
    then
	scp -q -r $i jorge@10.20.0.243:build
    else
        scp -q -r $i jorge@10.20.0.243:lib
    fi
done

echo "Running"
ssh jorge@10.20.0.243 "java -cp build/classes:lib/* mase.MaseEvolve "$ARGS

echo "Copying results"
OUTPARENT="$(dirname "$OUTFOLDER")"
echo $OUTPARENT
scp -p -r jorge@10.20.0.243:$OUTFOLDER $OUTPARENT

#echo "Cleaning results"
#ssh jorge@10.20.0.243 rm -rf $OUTFOLDER

