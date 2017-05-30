#!/bin/bash
source zombies.sh

COUNTER=0
echo ${#IPS[@]}
while [  $COUNTER -lt ${#IPS[@]} ]; do
  IP=${IPS[$COUNTER]}
  C=${CORES[$COUNTER]}
  pkill -9 -f "start_keep_alive.sh.*$IP"  # kill start scripts with this IP
  ./start_keep_alive.sh $IP $C "$@" &
  let COUNTER=COUNTER+1 
done
