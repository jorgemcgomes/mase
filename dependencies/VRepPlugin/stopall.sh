#!/bin/bash
source zombies.sh

COUNTER=0
echo ${#IPS[@]}
while [  $COUNTER -lt ${#IPS[@]} ]; do
  IP=${IPS[$COUNTER]}
  ./stop.sh $IP
  let COUNTER=COUNTER+1 
done

