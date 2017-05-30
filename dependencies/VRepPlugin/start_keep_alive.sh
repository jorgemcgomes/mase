#!/bin/bash

BASE_PORT=19996
REPERTOIRE="repertoire.txt"
PLUGIN="vrep/programming/v_repExtPluginJBot/v_repExtPluginJBot.cpp"
ADDON="vrep/vrepAddOnScript_jbotstartstop.lua"
JAR="vrep/programming/v_repExtPluginJBot/java/jbot.jar"

if [ "$#" -lt 3 ]; then
	echo 'Usage: start.sh <worker_address> <number_cores> <scene_file.ttt> [repertoire_file.txt]'
	exit 1
fi

IP=$1
C=$2
SCENE_FILE=$3

if [ "$#" -gt 3 ]; then
	REPERTOIRE=$4
else
	echo 'No repertoire file provided. Using default '$REPERTOIRE
fi

ssh $IP killall -9 vrep Xvfb  # kill vreps before copying stuff -- safer

rsync -av $SCENE_FILE $IP:vrep/scene.ttt
rsync -av $REPERTOIRE $IP:vrep/repertoire.txt
rsync -av $ADDON $IP:$ADDON
rsync -av $JAR $IP:$JAR
rsync -av $PLUGIN $IP:$PLUGIN
ssh $IP 'cd vrep/programming/v_repExtPluginJBot;make'	

while true; do
    COUNTER_C=0
    while [ $COUNTER_C -lt $C ]; do
      let PORT=BASE_PORT+COUNTER_C
      ssh $IP 'cd vrep; screen -d -m xvfb-run --auto-servernum --server-num=1 ./vrep.sh -h -gREMOTEAPISERVERSERVICE_'$PORT'_FALSE_FALSE scene.ttt' & 
      echo "Started server at "$IP" - "$PORT
      let COUNTER_C=COUNTER_C+1 
    done
    echo 'Waiting 2h until the next restart'
    sleep 2h
    echo 'Restarting'
    ssh $IP killall -9 vrep Xvfb
done




