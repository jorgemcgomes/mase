#!/bin/bash

pkill -9 -f "start_keep_alive_local.sh"  # kill start scripts with this IP
./start_keep_alive_local.sh $1 $2 $3 &

