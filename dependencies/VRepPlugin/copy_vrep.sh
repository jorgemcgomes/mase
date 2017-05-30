#!/bin/bash

VREP_FOLDER="vrep"

if [ "$#" -lt 1 ]; then
	echo 'Usage: copy_vrep.sh <worker_address>'
	exit 1
fi

IP=$1
rsync -au --info=progress2 $VREP_FOLDER $IP:
ssh $IP 'cd vrep/programming/v_repExtPluginJBot;make'	
ssh $IP rm -f vrep/remoteApiConnections.txt
ssh $IP rm -f vrep/repertoire.txt
ssh $IP rm -f vrep/scene.ttt
ssh $IP rm -f vrep/debug.txt
