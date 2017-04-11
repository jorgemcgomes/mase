#!/bin/bash
CLASSPATH="build/classes:dist/lib/*"

IP=${1-robot@10.40.50.135}
FOLDER=${2-mase}
echo "IP: "$IP
echo "FOLDER: "$FOLDER

echo "Cleaning classes"
ssh $IP rm -rf $FOLDER"/build" $FOLDER"/lib"
ssh $IP mkdir -p $FOLDER"/build/classes" $FOLDER"/lib"

echo "Copying classes"
IFS=':' read -ra ARRAY <<< "$CLASSPATH"
for i in "${ARRAY[@]}"; do
    echo $i
    if [[ -d $i ]]
    then
	scp -q -r $i $IP:$FOLDER"/build"
    else
        scp -q -r $i $IP:$FOLDER"/lib"
    fi
done

scp "conillonevolve.sh" $IP:$FOLDER
ssh $IP mv $FOLDER/conillonevolve.sh $FOLDER/evolve.sh
ssh $IP chmod 777 $FOLDER"/evolve.sh"

scp "conillonrun.sh" $IP:$FOLDER
ssh $IP mv $FOLDER/conillonrun.sh $FOLDER/run.sh
ssh $IP chmod 777 $FOLDER"/run.sh"

