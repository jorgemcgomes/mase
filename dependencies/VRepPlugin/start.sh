#!/bin/bash

pkill -9 -f "start_keep_alive.sh.*$1"  # kill start scripts with this IP
./start_keep_alive.sh $1 $2 $3 $4 &

