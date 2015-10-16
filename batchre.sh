#!/bin/bash

./run.sh mase.stat.BatchReevaluate -r 50 -file ~/exps/ecal2/down_tog/fit/config.params -f ~/exps/ecal2/down_tog/fit -p problem.landPlacement=1 -p problem.flyingPlacement=1
./run.sh mase.stat.BatchReevaluate -r 50 -file ~/exps/ecal2/down_sep/fit/config.params -f ~/exps/ecal2/down_sep/fit -p problem.landPlacement=1 -p problem.flyingPlacement=2
./run.sh mase.stat.BatchReevaluate -r 50 -file ~/exps/ecal2/stable_tog/fit/config.params -f ~/exps/ecal2/stable_tog/fit -p problem.landPlacement=1 -p problem.flyingPlacement=1
./run.sh mase.stat.BatchReevaluate -r 50 -file ~/exps/ecal2/stable_sep/fit/config.params -f ~/exps/ecal2/stable_sep/fit -p problem.landPlacement=1 -p problem.flyingPlacement=2


