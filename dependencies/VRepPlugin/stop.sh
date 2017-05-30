#!/bin/bash

if [ "$#" -lt 1 ]; then
	echo 'Usage: stop.sh <worker_address>'
	exit 1
fi

IP=$1
ssh $IP killall -9 vrep Xvfb
pkill -9 -f "start_keep_alive.sh.*$IP"
