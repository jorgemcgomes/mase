#!/bin/bash

BASE_PORT=19996
REPERTOIRE="repertoire.txt"
PLUGIN="vrep/programming/v_repExtPluginJBot/v_repExtPluginJBot.cpp"
ADDON="vrep/vrepAddOnScript_jbotstartstop.lua"
JAR="vrep/programming/v_repExtPluginJBot/java/jbot.jar"

if [ "$#" -lt 2 ]; then
	echo 'Usage: start_local.sh <number_cores> <scene_file.ttt> [repertoire_file.txt]'
	exit 1
fi

C=$1
SCENE_FILE=$2

if [ "$#" -gt 2 ]; then
	REPERTOIRE=$3
else
	echo 'No repertoire file provided. Using default '$REPERTOIRE
fi

killall -9 vrep Xvfb  # kill vreps before copying stuff -- safer

rsync -av $SCENE_FILE ~/vrep/scene.ttt
rsync -av $REPERTOIRE ~/vrep/repertoire.txt
rsync -av $ADDON ~/$ADDON
rsync -av $JAR ~/$JAR
rsync -av $PLUGIN ~/$PLUGIN
cd ~/vrep/programming/v_repExtPluginJBot;make	

while true; do
    COUNTER_C=0
    while [ $COUNTER_C -lt $C ]; do
      let PORT=BASE_PORT+COUNTER_C
      cd ~/vrep
      screen -d -m ./vrep.sh -h -gREMOTEAPISERVERSERVICE_$PORT'_FALSE_FALSE' scene.ttt & 
      echo "Started local server at "$PORT
      let COUNTER_C=COUNTER_C+1 
    done
    echo 'Waiting 2h until the next restart'
    sleep 2h
    echo 'Restarting'
    killall -9 vrep Xvfb
done




